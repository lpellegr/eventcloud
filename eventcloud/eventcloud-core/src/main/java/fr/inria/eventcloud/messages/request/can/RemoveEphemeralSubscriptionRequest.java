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

import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.utils.SerializedValue;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.PublishSubscribeConstants;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.overlay.can.SemanticElement;
import fr.inria.eventcloud.pubsub.PublishSubscribeUtils;
import fr.inria.eventcloud.pubsub.notifications.NotificationId;

/**
 * Request used to remove an ephemeral subscription from peers once it is
 * matched.
 * 
 * @author lpellegr
 */
public class RemoveEphemeralSubscriptionRequest extends
        StatelessQuadruplePatternRequest {

    private static final long serialVersionUID = 140L;

    private final SerializedValue<SubscriptionId> subscriptionId;

    public RemoveEphemeralSubscriptionRequest(Node graph,
            SubscriptionId subscriptionId) {
        super(new QuadruplePattern(graph, Node.ANY, Node.ANY, Node.ANY), null);

        this.subscriptionId = SerializedValue.create(subscriptionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPeerValidatingKeyConstraints(final CanOverlay<SemanticElement> overlay,
                                               QuadruplePattern quadruplePattern) {
        SemanticCanOverlay semanticOverlay = (SemanticCanOverlay) overlay;

        TransactionalDatasetGraph txnGraph =
                semanticOverlay.getSubscriptionsDatastore().begin(
                        AccessMode.WRITE);
        try {
            txnGraph.delete(
                    super.quadruplePattern.getValue().getGraph(),
                    PublishSubscribeUtils.createSubscriptionIdUri(this.subscriptionId.getValue()),
                    PublishSubscribeConstants.EPHEMERAL_SUBSCRIPTION_SUBSCRIBER_NODE,
                    Node.ANY);
            txnGraph.delete(
                    super.quadruplePattern.getValue().getGraph(),
                    PublishSubscribeUtils.createSubscriptionIdUri(this.subscriptionId.getValue()),
                    PublishSubscribeConstants.EPHEMERAL_SUBSCRIPTION_INDEXATION_DATETIME_NODE,
                    Node.ANY);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            txnGraph.end();
        }

        if (EventCloudProperties.PREVENT_CHUNK_DUPLICATES.getValue()) {
            semanticOverlay.dropAsSent(new NotificationId(
                    this.subscriptionId.getValue(),
                    super.quadruplePattern.getValue().getGraph()));
        }
    }

}
