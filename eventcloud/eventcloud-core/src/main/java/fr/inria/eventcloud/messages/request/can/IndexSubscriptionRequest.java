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
package fr.inria.eventcloud.messages.request.can;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.utils.SerializedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.QuadrupleIterator;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.overlay.can.SemanticElement;
import fr.inria.eventcloud.pubsub.PublishSubscribeUtils;
import fr.inria.eventcloud.pubsub.Subscription;
import fr.inria.eventcloud.pubsub.Subsubscription;

/**
 * Request used to index a subscription or a rewritten subscription after the
 * publication of a quadruple. While the rewritten subscription is indexed, it
 * is possible to have received some quadruples that match the rewritten
 * subscription. That's why an algorithm similar to the one from
 * {@link PublishQuadrupleRequest} is applied to rewrite the subscription for
 * each quadruple that matches it. This type of request is used for SBCE1, SBCE2
 * and SBCE3.
 * 
 * @see PublishQuadrupleRequest
 * 
 * @author lpellegr
 */
public class IndexSubscriptionRequest extends StatelessQuadruplePatternRequest {

    private static final long serialVersionUID = 140L;

    private static final Logger log =
            LoggerFactory.getLogger(IndexSubscriptionRequest.class);

    protected SerializedValue<Subscription> subscription;

    /**
     * Constructs an IndexRewrittenSubscriptionRequest from the specified
     * rewritten {@code subscription}.
     * 
     * @param subscription
     *            the rewritten subscription to index.
     */
    public IndexSubscriptionRequest(Subscription subscription) {
        super(subscription.getSubSubscriptions()[0].getAtomicQuery()
                .getQuadruplePattern(), null);

        this.subscription = SerializedValue.create(subscription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPeerValidatingKeyConstraints(final CanOverlay<SemanticElement> overlay,
                                               QuadruplePattern quadruplePattern) {
        SemanticCanOverlay semanticOverlay = (SemanticCanOverlay) overlay;
        Subscription subscription = this.subscription.getValue();

        if (P2PStructuredProperties.ENABLE_BENCHMARKS_INFORMATION.getValue()) {
            log.info("It took "
                    + (System.currentTimeMillis() - subscription.getCreationTime())
                    + "ms to receive subscription : "
                    + subscription.getSparqlQuery());
        }

        log.debug("Indexing subscription {} on peer {}", subscription, overlay);

        // writes the subscription into the cache and the local datastore
        semanticOverlay.storeSubscription(subscription);

        Subsubscription firstSubsubscription =
                subscription.getSubSubscriptions()[0];

        // stores the quadruples into a list in order to avoid a concurrent
        // exception if a add operation (or more generally a write operation) is
        // done on the datastore while we are iterating on the ResultSet.
        // Indeed, the result set does not contain the solutions but knows how
        // to retrieve a solution each time a call to next is performed.
        List<Quadruple> quadruplesMatching = new ArrayList<Quadruple>();

        QuadruplePattern qp =
                firstSubsubscription.getAtomicQuery().getQuadruplePattern();

        TransactionalDatasetGraph txnGraph =
                ((SemanticCanOverlay) overlay).getMiscDatastore().begin(
                        AccessMode.READ_ONLY);

        try {
            QuadrupleIterator it =
                    txnGraph.find(
                            Node.ANY, qp.getSubject(), qp.getPredicate(),
                            qp.getObject());
            while (it.hasNext()) {
                Quadruple q = it.next();

                if (qp.getGraph() == Node.ANY
                        || q.getGraph().getURI().startsWith(
                                qp.getGraph().getURI())) {
                    quadruplesMatching.add(q);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            txnGraph.end();
        }

        for (Quadruple quadrupleMatching : quadruplesMatching) {
            boolean mustIgnoreQuadrupleMatching =
                    quadrupleMatching.getPublicationTime() < subscription.getIndexationTime();

            if (log.isDebugEnabled()) {
                log.debug(
                        "Timestamp comparison, subscriptionTimestamp={}, quadrupleTimestamp={}, quadrupleId={}, quadruple must be ignored? {}",
                        new Object[] {
                                subscription.getIndexationTime(),
                                quadrupleMatching.getPublicationTime(),
                                quadrupleMatching.getGraph(),
                                mustIgnoreQuadrupleMatching});
            }

            // if q sent before s but s indexed before q then q must not be
            // notified
            if (mustIgnoreQuadrupleMatching) {
                continue;
            }

            log.trace(
                    "Ordering issue detected for eventId {} with subscription {}",
                    quadrupleMatching.getGraph(), subscription.getId());

            PublishSubscribeUtils.rewriteSubscriptionOrNotifySender(
                    semanticOverlay, subscription, quadrupleMatching);
        }
    }

}
