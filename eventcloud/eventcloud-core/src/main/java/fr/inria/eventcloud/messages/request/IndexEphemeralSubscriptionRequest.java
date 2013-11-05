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
package fr.inria.eventcloud.messages.request;

import java.util.List;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.utils.SerializedValue;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;

import fr.inria.eventcloud.api.PublishSubscribeConstants;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.QuadrupleIterator;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.overlay.can.SemanticCoordinate;
import fr.inria.eventcloud.pubsub.PublishSubscribeUtils;
import fr.inria.eventcloud.pubsub.Subscription;
import fr.inria.eventcloud.pubsub.notifications.QuadruplesNotification;

/**
 * A subscription that is used to reconstruct asynchronously a compound event
 * for some of its quadruples that have matched a subscription. This type of
 * subscription is used when SBCE2 is enabled.
 * 
 * @author lpellegr
 */
public class IndexEphemeralSubscriptionRequest extends
        StatelessQuadruplePatternRequest {

    private static final long serialVersionUID = 160L;

    private final SerializedValue<SubscriptionId> subscriptionId;

    private final SerializedValue<String> subscriberUrl;

    private final SerializedValue<String> metaGraph;

    public IndexEphemeralSubscriptionRequest(Node graph,
            SubscriptionId subscriptionId, String subscriberUrl) {
        super(new QuadruplePattern(
                Quadruple.removeMetaInformation(graph), Node.ANY, Node.ANY,
                Node.ANY, true), null);

        this.metaGraph = SerializedValue.create(graph.getURI());
        this.subscriptionId = SerializedValue.create(subscriptionId);
        this.subscriberUrl = SerializedValue.create(subscriberUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPeerValidatingKeyConstraints(final CanOverlay<SemanticCoordinate> overlay,
                                               QuadruplePattern quadruplePattern) {
        SemanticCanOverlay semanticOverlay = (SemanticCanOverlay) overlay;

        // skips this IndexEphemeralSubscription if one has already been handled
        // for the graph value
        if (!this.storeEphemeralSubscription(semanticOverlay)) {
            return;
        }

        Node metaGraphNode = NodeFactory.createURI(this.metaGraph.getValue());

        TransactionalDatasetGraph txnGraph =
                semanticOverlay.getMiscDatastore().begin(AccessMode.READ_ONLY);
        try {
            // we use the meta graph node because the quadruples are stored
            // along with meta information
            QuadrupleIterator it =
                    txnGraph.find(metaGraphNode, Node.ANY, Node.ANY, Node.ANY);

            if (it.hasNext()) {
                Builder<Quadruple> builder = ImmutableList.builder();
                while (it.hasNext()) {
                    Quadruple quadruple = it.next();

                    builder.add(quadruple);
                }

                List<Quadruple> quadruples = builder.build();

                if (!quadruples.isEmpty()) {
                    final QuadruplesNotification n =
                            new QuadruplesNotification(
                                    this.subscriptionId.getValue(),
                                    metaGraphNode,
                                    PAActiveObject.getUrl(semanticOverlay.getStub()),
                                    quadruples);

                    Subscription.SUBSCRIBE_PROXIES_CACHE.get(
                            this.subscriberUrl.getValue()).receiveSbce2(n);
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
            // checks whether there is an ES for the same graph value that is
            // already indexed
            QuadrupleIterator it =
                    txnGraph.find(
                            NodeFactory.createURI(this.metaGraph.getValue()),
                            Node.ANY,
                            PublishSubscribeConstants.EPHEMERAL_SUBSCRIPTION_SUBSCRIBER_NODE,
                            Node.ANY);

            if (it.hasNext()) {
                return false;
            }

            for (Quadruple q : this.createEphemeralSubscriptionQuadruples()) {
                txnGraph.add(q);
            }

            txnGraph.commit();

            return true;
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            txnGraph.end();
        }
    }

    private final List<Quadruple> createEphemeralSubscriptionQuadruples() {
        Node graph = NodeFactory.createURI(this.metaGraph.getValue());
        Node sId =
                PublishSubscribeUtils.createSubscriptionIdUri(this.subscriptionId.getValue());

        return ImmutableList.of(
                new Quadruple(
                        graph,
                        sId,
                        PublishSubscribeConstants.EPHEMERAL_SUBSCRIPTION_SUBSCRIBER_NODE,
                        NodeFactory.createURI(this.subscriberUrl.getValue()),
                        false, false),
                new Quadruple(
                        graph,
                        sId,
                        PublishSubscribeConstants.EPHEMERAL_SUBSCRIPTION_INDEXATION_DATETIME_NODE,
                        NodeFactory.createLiteral(
                                Long.toString(System.currentTimeMillis()),
                                XSDDatatype.XSDlong), false, false));
    }

}
