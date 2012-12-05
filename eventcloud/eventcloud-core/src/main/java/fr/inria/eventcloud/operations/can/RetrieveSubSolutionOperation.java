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
package fr.inria.eventcloud.operations.can;

import java.util.concurrent.ExecutionException;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.p2p.structured.operations.RunnableOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.hash.HashCode;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.BindingNotificationListener;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.QuadrupleIterator;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.pubsub.PublishSubscribeUtils;
import fr.inria.eventcloud.pubsub.Subscription;
import fr.inria.eventcloud.pubsub.notifications.BindingNotification;
import fr.inria.eventcloud.pubsub.notifications.NotificationId;

/**
 * This class is used to retrieve the remaining sub solutions associated to a
 * solution matching a subscription register by a subscriber along with a
 * {@link BindingNotificationListener}.
 * 
 * @author lpellegr
 */
public class RetrieveSubSolutionOperation implements RunnableOperation {

    private static final long serialVersionUID = 130L;

    private static final Logger log =
            LoggerFactory.getLogger(RetrieveSubSolutionOperation.class);

    private final NotificationId notificationId;

    private final HashCode hash;

    public RetrieveSubSolutionOperation(NotificationId id, HashCode hash) {
        this.notificationId = id;
        this.hash = hash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(StructuredOverlay overlay) {
        // when this operation is handled we can suppose that a binding
        // notification listener is used

        SemanticCanOverlay semanticOverlay = ((SemanticCanOverlay) overlay);

        TransactionalTdbDatastore datastore =
                semanticOverlay.getSubscriptionsDatastore();

        TransactionalDatasetGraph txnGraph =
                datastore.begin(AccessMode.READ_ONLY);

        Quadruple metaQuad = null;
        try {
            // finds the matching quadruple meta information
            QuadrupleIterator result =
                    txnGraph.find(
                            PublishSubscribeUtils.createQuadrupleHashUri(this.hash),
                            Node.ANY, Node.ANY, Node.ANY);

            if (!result.hasNext()) {
                log.error(
                        "Peer {} is expected to have a matching quadruple meta information for hash {}",
                        overlay, this.hash);
            }

            metaQuad = result.next();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            txnGraph.end();
        }

        if (metaQuad != null) {
            Pair<Quadruple, SubscriptionId> extractedMetaInfo =
                    PublishSubscribeUtils.extractMetaInformation(metaQuad);

            // extracts only the variables that are declared as result variables
            // in the original subscription
            Subscription subscription =
                    ((SemanticCanOverlay) overlay).findSubscription(PublishSubscribeUtils.extractSubscriptionId(metaQuad.getSubject()));

            // an unsubscribe request has been sent after that a notification
            // has been triggered because a publication matches the current
            // subscription. However, the unsubscribe request has been handled
            // before we send back to the subscriber all the intermediate
            // binding values to the subscriber
            if (subscription == null) {
                return;
            }

            Binding binding =
                    PublishSubscribeUtils.filter(
                            extractedMetaInfo.getFirst(),
                            subscription.getResultVars(),
                            subscription.getSubSubscriptions()[0].getAtomicQuery());

            try {
                subscription.getSubscriberProxy()
                        .receiveSbce1Or2(
                                new BindingNotification(
                                        this.notificationId,
                                        extractedMetaInfo.getSecond(),
                                        PAActiveObject.getUrl(semanticOverlay.getStub()),
                                        binding));
            } catch (ExecutionException e) {
                log.error("No SubscribeProxy found under the given URL: "
                        + subscription.getSubscriberUrl(), e);

                // TODO: this could be due to a subscriber which has left
                // without unsubscribing. In that case we can remove the
                // subscription information associated to this subscriber
                // and also send a message
            }

            txnGraph = datastore.begin(AccessMode.WRITE);
            try {
                txnGraph.delete(metaQuad);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                txnGraph.end();
            }
        }
    }

}
