/**
 * Copyright (c) 2011-2014 INRIA.
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
package fr.inria.eventcloud.delayers.actions;

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
import fr.inria.eventcloud.delayers.Delayer;
import fr.inria.eventcloud.delayers.buffers.Buffer;
import fr.inria.eventcloud.delayers.buffers.SubscriptionBuffer;
import fr.inria.eventcloud.exceptions.DecompositionException;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.pubsub.PublishSubscribeUtils;
import fr.inria.eventcloud.pubsub.Subscription;
import fr.inria.eventcloud.pubsub.Subsubscription;

/**
 * Action used by a delayer to find events matching subscriptions that have been
 * flushed. For each event found, a notification is triggered.
 * 
 * @author lpellegr
 * 
 * @see Delayer
 * @see SubscriptionBuffer
 */
public final class SubscriptionAction extends Action<Subscription> {

    private static final Logger log =
            LoggerFactory.getLogger(SubscriptionAction.class);

    public SubscriptionAction(SemanticCanOverlay overlay, int threadPoolSize) {
        super(overlay, threadPoolSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void perform(Buffer<Subscription> buffer) {
        SubscriptionBuffer buf = (SubscriptionBuffer) buffer;

        for (Subscription s : buf) {
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
