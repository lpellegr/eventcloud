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

import static fr.inria.eventcloud.pubsub.PublishSubscribeConstants.PUBLICATION_INSERTION_DATETIME_NODE;
import static fr.inria.eventcloud.pubsub.PublishSubscribeConstants.QUADRUPLE_MATCHES_SUBSCRIPTION_NODE;
import static fr.inria.eventcloud.pubsub.PublishSubscribeConstants.QUADRUPLE_NS;
import static fr.inria.eventcloud.pubsub.PublishSubscribeConstants.SUBSCRIPTION_NS;
import static fr.inria.eventcloud.pubsub.PublishSubscribeConstants.SUBSCRIPTION_NS_NODE;
import static fr.inria.eventcloud.pubsub.PublishSubscribeConstants.SUBSCRIPTION_SUBSCRIBER_NODE;

import java.io.IOException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.p2p.structured.operations.AsynchronousOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.datastore.JenaDatastore;
import fr.inria.eventcloud.proxies.PublishSubscribeProxy;
import fr.inria.eventcloud.pubsub.Notification;
import fr.inria.eventcloud.pubsub.NotificationId;
import fr.inria.eventcloud.pubsub.Subsubscription;

/**
 * The Class RetrieveSubSolutionOperation.
 * 
 * @author lpellegr
 */
public class RetrieveSubSolutionOperation implements AsynchronousOperation {

    private static final long serialVersionUID = 1L;

    private final NotificationId id;

    private final long hash;

    public RetrieveSubSolutionOperation(NotificationId id, long hash) {
        super();
        this.id = id;
        this.hash = hash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(StructuredOverlay overlay) {
        // TODO retrieve from the local datastore the quadruple that matches the
        // subscription id.
        // then we have to find the subscriber associated to the subscription to
        // send the sub-solution
        JenaDatastore datastore = (JenaDatastore) overlay.getDatastore();

        Node quadrupleHashId = Node.createURI(QUADRUPLE_NS + this.hash);

        Collection<Quadruple> result =
                datastore.find(new QuadruplePattern(
                        quadrupleHashId, Node.ANY, Node.ANY, Node.ANY));

        if (result.size() > 0) {
            // identifiers is an URI
            Node subscriptionMatched = null;
            // identifier is a literal
            Node subSubscriptionMatched = null;
            Node[] quadReconstructed = new Node[4];

            for (Quadruple quad : result) {
                if (quad.getPredicate().equals(
                        QUADRUPLE_MATCHES_SUBSCRIPTION_NODE)) {
                    subscriptionMatched = quad.getSubject();
                    subSubscriptionMatched = quad.getObject();
                } else if (quad.getPredicate().equals(
                        PUBLICATION_INSERTION_DATETIME_NODE)) {
                    quadReconstructed[0] = quad.getSubject();
                } else {
                    quadReconstructed[1] = quad.getSubject();
                    quadReconstructed[2] = quad.getPredicate();
                    quadReconstructed[3] = quad.getObject();
                }
            }

            // removes the quadruple indicating which quad matches which sub
            // subscription

            // TODO make this part working!!!
            // System.err.println("CONTAINS3? "
            // + datastore.contains(new Quadruple(
            // quadrupleHashId, subscriptionMatched,
            // QUADRUPLE_MATCHES_SUBSCRIPTION_NODE,
            // subSubscriptionMatched)));
            //
            // datastore.delete(new Quadruple(
            // quadrupleHashId, subscriptionMatched,
            // QUADRUPLE_MATCHES_SUBSCRIPTION_NODE, subSubscriptionMatched));

            // retrieves the subSubscription that was matched by the quadruple
            // (graph, subject, predicate, object) in order to extract the
            // bindings from the quadruple
            String subscriptionMatchedSplits[] =
                    subscriptionMatched.getURI().split("/");
            Subsubscription subSubscription =
                    Subsubscription.parseFrom(
                            datastore,
                            subscriptionMatchedSplits[subscriptionMatchedSplits.length - 1],
                            subSubscriptionMatched.getLiteralLexicalForm());

            // extracts bindings
            Binding binding = BindingFactory.create();
            Node[] ssaa = subSubscription.getAtomicQuery().toArray();
            for (int i = 0; i < ssaa.length; i++) {
                if (ssaa[i].isVariable()) {
                    binding.add(
                            Var.alloc(ssaa[i].getName()), quadReconstructed[i]);
                }
            }

            // retrieves the subscriber url by using the subscription id from
            // the notification id, which is the original subscription id
            String subscriberUrl =
                    datastore.find(
                            new QuadruplePattern(
                                    SUBSCRIPTION_NS_NODE,
                                    Node.createURI(SUBSCRIPTION_NS
                                            + this.id.getSubscriptionId()),
                                    SUBSCRIPTION_SUBSCRIBER_NODE, Node.ANY))
                            .iterator()
                            .next()
                            .getObject()
                            .getLiteralLexicalForm();

            try {
                PublishSubscribeProxy proxy =
                        (PublishSubscribeProxy) PAActiveObject.lookupActive(
                                PublishSubscribeProxy.class, subscriberUrl);

                proxy.receive(new Notification(
                        this.id, PAActiveObject.getUrl(overlay.getStub()),
                        binding));
            } catch (ActiveObjectCreationException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public NotificationId getId() {
        return this.id;
    }

    public long getRetrieveTimestamp() {
        return this.hash;
    }

}
