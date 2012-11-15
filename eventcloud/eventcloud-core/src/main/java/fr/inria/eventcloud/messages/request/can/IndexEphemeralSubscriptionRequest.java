/**
 * Copyright (c) 2011-2012 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.messages.request.can;

import java.util.List;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.utils.SerializedValue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.PublishSubscribeConstants;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.QuadrupleIterator;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.overlay.can.SemanticElement;
import fr.inria.eventcloud.pubsub.PublishSubscribeUtils;
import fr.inria.eventcloud.pubsub.Subscription;
import fr.inria.eventcloud.pubsub.notifications.NotificationId;
import fr.inria.eventcloud.pubsub.notifications.QuadruplesNotification;

/**
 * 
 * 
 * @author lpellegr
 */
public class IndexEphemeralSubscriptionRequest extends
        StatelessQuadruplePatternRequest {

    private static final long serialVersionUID = 130L;

    private final SerializedValue<SubscriptionId> subscriptionId;

    private final SerializedValue<String> subscriberUrl;

    public IndexEphemeralSubscriptionRequest(Node graph,
            SubscriptionId subscriptionId, String subscriberUrl) {
        super(new QuadruplePattern(graph, Node.ANY, Node.ANY, Node.ANY, true),
                null);

        this.subscriptionId = SerializedValue.create(subscriptionId);
        this.subscriberUrl = SerializedValue.create(subscriberUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPeerValidatingKeyConstraints(final CanOverlay<SemanticElement> overlay,
                                               QuadruplePattern quadruplePattern) {
        SemanticCanOverlay semanticOverlay = (SemanticCanOverlay) overlay;

        if (!this.storeEphemeralSubscription(semanticOverlay)) {
            return;
        }

        TransactionalDatasetGraph txnGraph =
                semanticOverlay.getMiscDatastore().begin(AccessMode.READ_ONLY);
        try {
            // we use the meta graph node because the quadruples are stored
            // along with meta information
            QuadrupleIterator it =
                    txnGraph.find(
                            quadruplePattern.createMetaGraphNode(), Node.ANY,
                            Node.ANY, Node.ANY);

            if (it.hasNext()) {
                NotificationId notificationId =
                        new NotificationId(
                                this.subscriptionId.getValue(),
                                super.quadruplePattern.getValue().getGraph());

                Builder<Quadruple> builder = ImmutableList.builder();
                while (it.hasNext()) {
                    Quadruple quadruple = it.next();

                    if (semanticOverlay.markAsSent(notificationId, quadruple)) {
                        builder.add(quadruple);
                    }
                }

                List<Quadruple> quadruples = builder.build();

                if (!quadruples.isEmpty()) {
                    final QuadruplesNotification n =
                            new QuadruplesNotification(
                                    this.subscriptionId.getValue(),
                                    super.quadruplePattern.getValue()
                                            .getGraph(),
                                    PAActiveObject.getUrl(semanticOverlay.getStub()),
                                    quadruples);

                    Subscription.SUBSCRIBE_PROXIES_CACHE.get(
                            this.subscriberUrl.getValue()).receive(n);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            txnGraph.end();
        }
    }

    private final boolean storeEphemeralSubscription(SemanticCanOverlay overlay) {
        TransactionalDatasetGraph txnGraph =
                overlay.getSubscriptionsDatastore().begin(AccessMode.WRITE);

        try {
            QuadrupleIterator it =
                    txnGraph.find(
                            this.quadruplePattern.getValue().getGraph(),
                            Node.ANY,
                            PublishSubscribeConstants.SUBSCRIPTION_SUBSCRIBER_NODE,
                            Node.ANY);

            if (it.hasNext()) {
                return false;
            }

            txnGraph.add(this.createEphemeralSubscriptionQuadruple());
            txnGraph.commit();

            return true;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            txnGraph.end();
        }
    }

    private final Quadruple createEphemeralSubscriptionQuadruple() {
        return new Quadruple(
                this.quadruplePattern.getValue().getGraph(),
                PublishSubscribeUtils.createSubscriptionIdUri(this.subscriptionId.getValue()),
                PublishSubscribeConstants.SUBSCRIPTION_SUBSCRIBER_NODE,
                Node.createURI(this.subscriberUrl.getValue()));
    }

}
