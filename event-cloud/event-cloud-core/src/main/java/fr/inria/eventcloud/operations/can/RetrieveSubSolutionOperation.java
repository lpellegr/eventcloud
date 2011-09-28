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
import fr.inria.eventcloud.api.listeners.NotificationListenerType;
import fr.inria.eventcloud.datastore.SynchronizedJenaDatasetGraph;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.pubsub.Notification;
import fr.inria.eventcloud.pubsub.NotificationId;
import fr.inria.eventcloud.pubsub.PublishSubscribeUtils;
import fr.inria.eventcloud.pubsub.Subscription;
import fr.inria.eventcloud.pubsub.Subsubscription;
import fr.inria.eventcloud.utils.LongLong;

/**
 * The class RetrieveSubSolutionOperation is used to retrieve the sub-solutions
 * associated to a {@link Notification}.
 * 
 * @author lpellegr
 */
public class RetrieveSubSolutionOperation implements AsynchronousOperation {

    private static final long serialVersionUID = 1L;

    private final NotificationId notificationId;

    private final LongLong hash;

    public RetrieveSubSolutionOperation(NotificationId id, LongLong hash) {
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
        // TODO: try to understand why we got an exception when the meta quad
        // is removed at this step
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

        Subscription sub =
                ((SemanticCanOverlay) overlay).findSubscription(subSubscription.getParentId());

        Binding binding = null;
        // for a signal it is not necessary to retrieve the binding value
        if (sub.getType() != NotificationListenerType.SIGNAL) {
            binding =
                    PublishSubscribeUtils.filter(
                            extractedMetaInfo.getFirst(), sub.getResultVars(),
                            subSubscription.getAtomicQuery());
        }

        // TODO: replace PAActiveObject.getUrl(overlay.getStub()) by the
        // component URL? (same in PublishQuadrupleRequest and
        // IndexSubscriptionRequest)
        sub.getSubscriberProxy().receive(
                new Notification(
                        this.notificationId,
                        PAActiveObject.getUrl(overlay.getStub()), binding));
    }

}
