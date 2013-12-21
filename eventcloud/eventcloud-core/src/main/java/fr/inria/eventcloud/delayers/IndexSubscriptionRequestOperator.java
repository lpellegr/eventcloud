/**
 * Copyright (c) 2011-2013 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.delayers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.QuadrupleIterator;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.exceptions.DecompositionException;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.pubsub.PublishSubscribeUtils;
import fr.inria.eventcloud.pubsub.Subscription;
import fr.inria.eventcloud.pubsub.Subsubscription;

/**
 * Delayer used to buffer write operations due to subscriptions that are indexed
 * with SBCE1, SBCE2 and SBCE3.
 * 
 * @author lpellegr
 */
public class IndexSubscriptionRequestOperator extends
        BufferOperator<CustomBuffer> {

    private static final Logger log =
            LoggerFactory.getLogger(IndexSubscriptionRequestOperator.class);

    public IndexSubscriptionRequestOperator(SemanticCanOverlay overlay) {
        super(overlay);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void _flushBuffer(CustomBuffer buffer) {
        TransactionalDatasetGraph txnGraph =
                this.overlay.getSubscriptionsDatastore()
                        .begin(AccessMode.WRITE);

        try {
            for (Subscription s : buffer.getSubscriptions()) {
                this.overlay.getSubscriptionsCache().put(s.getId(), s);
                txnGraph.add(s.toQuadruples());
            }

            txnGraph.commit();
        } catch (Exception e) {
            e.printStackTrace();
            txnGraph.abort();
        } finally {
            txnGraph.end();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void _triggerAction(CustomBuffer buffer) {
        for (Subscription s : buffer.getSubscriptions()) {
            this.fireQuadrupleMatching(s);
        }
    }

    private void fireQuadrupleMatching(Subscription s) {
        Subsubscription firstSubsubscription;

        try {
            firstSubsubscription = s.getSubSubscriptions()[0];
        } catch (DecompositionException e) {
            throw new IllegalStateException(e);
        }

        // stores the quadruples into a list in order to avoid a concurrent
        // exception if a add operation (or more generally a write operation) is
        // done on the datastore while we are iterating on the ResultSet.
        // Indeed, the result set does not contain the solutions but knows how
        // to retrieve a solution each time a call to next is performed.
        Builder<Quadruple> matchingQuadruples = ImmutableList.builder();

        QuadruplePattern qp =
                firstSubsubscription.getAtomicQuery().getQuadruplePattern();

        TransactionalDatasetGraph txnGraph =
                this.overlay.getMiscDatastore().begin(AccessMode.READ_ONLY);

        try {
            QuadrupleIterator it =
                    txnGraph.find(
                            Node.ANY, qp.getSubject(), qp.getPredicate(),
                            qp.getObject());
            while (it.hasNext()) {
                Quadruple q = it.next();

                if (!PublishSubscribeUtils.isMetaQuadruple(q)
                        && (qp.getGraph() == Node.ANY || q.getGraph()
                                .getURI()
                                .startsWith(qp.getGraph().getURI()))) {
                    if (firstSubsubscription.getAtomicQuery()
                            .isFilterEvaluationRequired()) {
                        if (PublishSubscribeUtils.matches(
                                q, firstSubsubscription.getAtomicQuery()) != null) {
                            matchingQuadruples.add(q);
                        }
                    } else {
                        matchingQuadruples.add(q);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            txnGraph.abort();
        } finally {
            txnGraph.end();
        }

        for (Quadruple quadrupleMatching : matchingQuadruples.build()) {
            // TODO each quadrupleMatching could be handled in parallel to
            // another by using the thread pool from the super class but doing
            // so requires to check that no new concurrent issue occurs
            boolean mustIgnoreQuadrupleMatching =
                    quadrupleMatching.getPublicationTime() < s.getIndexationTime();

            if (log.isDebugEnabled()) {
                log.debug(
                        "Timestamp comparison, subscriptionTimestamp={}, quadrupleTimestamp={}, quadrupleId={}, quadruple must be ignored? {}",
                        new Object[] {
                                s.getIndexationTime(),
                                quadrupleMatching.getPublicationTime(),
                                quadrupleMatching.getGraph(),
                                mustIgnoreQuadrupleMatching});
            }

            // if q sent before s but s indexed before q then q must not be
            // notified
            if (mustIgnoreQuadrupleMatching) {
                continue;
            }

            if (log.isDebugEnabled() && s.getParentId() == null) {
                log.debug(
                        "Ordering issue detected for eventId {} with subscription {} on {}",
                        quadrupleMatching.getGraph(), s.getId(),
                        super.overlay.getId());
            }

            PublishSubscribeUtils.rewriteSubscriptionOrNotifySender(
                    super.overlay, s, quadrupleMatching);
        }
    }

}
