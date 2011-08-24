/**
 * Copyright (c) 2011 INRIA.
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

import java.io.IOException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.p2p.structured.operations.AsynchronousOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.utils.Pair;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.datastore.SynchronizedJenaDatasetGraph;
import fr.inria.eventcloud.overlay.SparqlRequestResponseManager;
import fr.inria.eventcloud.proxies.SubscribeProxy;
import fr.inria.eventcloud.pubsub.Notification;
import fr.inria.eventcloud.pubsub.NotificationId;
import fr.inria.eventcloud.pubsub.PublishSubscribeConstants;
import fr.inria.eventcloud.pubsub.PublishSubscribeUtils;
import fr.inria.eventcloud.pubsub.Subsubscription;

/**
 * The class RetrieveSubSolutionOperation is used to retrieve the sub-solutions
 * associated to a {@link Notification}.
 * 
 * @author lpellegr
 */
public class RetrieveSubSolutionOperation implements AsynchronousOperation {

    private static final long serialVersionUID = 1L;

    private final NotificationId notificationId;

    private final long hash;

    public RetrieveSubSolutionOperation(NotificationId id, long hash) {
        super();
        this.notificationId = id;
        this.hash = hash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(StructuredOverlay overlay) {
        SynchronizedJenaDatasetGraph datastore =
                (SynchronizedJenaDatasetGraph) overlay.getDatastore();

        // finds the matching quadruple meta information
        Collection<Quadruple> result =
                datastore.find(new QuadruplePattern(
                        PublishSubscribeUtils.createQuadrupleHashUrl(this.hash),
                        Node.ANY, Node.ANY, Node.ANY));

        if (result.size() != 1) {
            throw new IllegalStateException(
                    "The peer "
                            + overlay
                            + " is expected to have a matching quadruple meta information for hash "
                            + this.hash);
        }

        Quadruple metaQuad = result.iterator().next();
        // TODO: find why we got an exception when the meta quad is removed at
        // this step
        // datastore.delete(metaQuad);

        Pair<Quadruple, SubscriptionId> extractedMetaInfo =
                PublishSubscribeUtils.extractMetaInformation(metaQuad);

        Subsubscription subSubscription =
                Subsubscription.parseFrom(
                        datastore,
                        PublishSubscribeUtils.extractSubscriptionId(metaQuad.getSubject()),
                        extractedMetaInfo.getSecond());

        // extracts only the variabless that are declared as result variables in
        // the original subscription
        Binding binding =
                PublishSubscribeUtils.filter(
                        extractedMetaInfo.getFirst(),
                        ((SparqlRequestResponseManager) overlay.getRequestResponseManager()).find(
                                subSubscription.getParentId())
                                .getResultVars(),
                        subSubscription.getAtomicQuery());

        // retrieves the subscriber url by using the subscription id from
        // the notification id, which is the original subscription id
        String subscriberUrl =
                datastore.find(
                        new QuadruplePattern(
                                PublishSubscribeConstants.SUBSCRIPTION_NS_NODE,
                                PublishSubscribeUtils.createSubscriptionIdUrl(this.notificationId.getSubscriptionId()),
                                PublishSubscribeConstants.SUBSCRIPTION_SUBSCRIBER_NODE,
                                Node.ANY))
                        .iterator()
                        .next()
                        .getObject()
                        .getLiteralLexicalForm();

        try {
            SubscribeProxy proxy =
                    (SubscribeProxy) PAActiveObject.lookupActive(
                            SubscribeProxy.class, subscriberUrl);

            proxy.receive(new Notification(
                    this.notificationId,
                    PAActiveObject.getUrl(overlay.getStub()), binding));
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
