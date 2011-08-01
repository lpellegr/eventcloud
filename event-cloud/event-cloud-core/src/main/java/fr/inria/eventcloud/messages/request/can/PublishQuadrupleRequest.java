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
package fr.inria.eventcloud.messages.request.can;

import static fr.inria.eventcloud.pubsub.PublishSubscribeUtils.PUBLICATION_INSERTION_DATETIME_NODE;
import static fr.inria.eventcloud.pubsub.PublishSubscribeUtils.QUADRUPLE_MATCHES_SUBSCRIPTION_NODE;
import static fr.inria.eventcloud.pubsub.PublishSubscribeUtils.QUADRUPLE_NS;
import static fr.inria.eventcloud.pubsub.PublishSubscribeUtils.SUBSCRIPTION_ID_PROPERTY;
import static fr.inria.eventcloud.pubsub.PublishSubscribeUtils.SUBSCRIPTION_INDEXED_WITH_PROPERTY;
import static fr.inria.eventcloud.pubsub.PublishSubscribeUtils.SUBSCRIPTION_NS;
import static fr.inria.eventcloud.pubsub.PublishSubscribeUtils.SUBSCRIPTION_NS_NODE;
import static fr.inria.eventcloud.pubsub.PublishSubscribeUtils.SUBSUBSCRIPTION_GRAPH_VALUE_PROPERTY;
import static fr.inria.eventcloud.pubsub.PublishSubscribeUtils.SUBSUBSCRIPTION_ID_PROPERTY;
import static fr.inria.eventcloud.pubsub.PublishSubscribeUtils.SUBSUBSCRIPTION_OBJECT_VALUE_PROPERTY;
import static fr.inria.eventcloud.pubsub.PublishSubscribeUtils.SUBSUBSCRIPTION_PREDICATE_VALUE_PROPERTY;
import static fr.inria.eventcloud.pubsub.PublishSubscribeUtils.SUBSUBSCRIPTION_SUBJECT_VALUE_PROPERTY;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.utils.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.util.FmtUtils;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.datastore.JenaDatastore;
import fr.inria.eventcloud.operations.can.RetrieveSubSolutionOperation;
import fr.inria.eventcloud.overlay.SemanticPeer;
import fr.inria.eventcloud.overlay.SparqlRequestResponseManager;
import fr.inria.eventcloud.pubsub.Notification;
import fr.inria.eventcloud.pubsub.NotificationId;
import fr.inria.eventcloud.pubsub.PublishSubscribeUtils;
import fr.inria.eventcloud.pubsub.Subscription;
import fr.inria.eventcloud.pubsub.SubscriptionRewriter;
import fr.inria.eventcloud.utils.MurmurHash;

/**
 * Publishes a quadruple into the network.
 * 
 * @author lpellegr
 */
public class PublishQuadrupleRequest extends QuadrupleRequest {

    private static final long serialVersionUID = 1L;

    private static final Logger log =
            LoggerFactory.getLogger(PublishQuadrupleRequest.class);

    public PublishQuadrupleRequest(Quadruple quad) {
        super(quad);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestinationReached(StructuredOverlay overlay, Quadruple quad) {
        System.err.println("PublishQuadrupleRequest.onDestinationReached() "
                + PAActiveObject.getUrl(overlay.getStub()));

        long quadId = MurmurHash.hash64(quad.toString());
        Node quadIdNode = Node.createURI(QUADRUPLE_NS + quadId);
        JenaDatastore datastore = ((JenaDatastore) overlay.getDatastore());

        // stores the quadruple into the local datastore.
        // this quadruple is decomposed into 2 quadruples to indicate when it
        // has been inserted but also to know the quadruple has been inserted
        // from the publish/subscribe api.
        datastore.add(new Quadruple(
                quadIdNode, quad.getSubject(), quad.getPredicate(),
                quad.getObject()));
        datastore.add(new Quadruple(
                quadIdNode,
                quad.getGraph(),
                PUBLICATION_INSERTION_DATETIME_NODE,
                Node.createLiteral(
                        DatatypeConverter.printDateTime(Calendar.getInstance()),
                        null, XSDDatatype.XSDdateTime)));

        log.debug(
                "SPARQL query used to retrieve the sub subscriptions matching {}:\n{}",
                quad, createQueryRetrievingSubscriptionsMatching(quad));

        // finds the sub subscription which are stored locally and that are
        // matching the quadruple that have been just inserted into the local
        // datastore
        ResultSet result =
                datastore.executeSparqlSelect(createQueryRetrievingSubscriptionsMatching(quad));

        // we have to store the identifiers found into a new list because
        // we cannot iterate on a Jena iterator and perform operations on the
        // datastore at the same time
        List<Pair<Node>> matchingIds = new ArrayList<Pair<Node>>();
        while (result.hasNext()) {
            QuerySolution solution = result.nextSolution();
            // the first component is composed of the subscription id whereas
            // the second contains the sub subscription id
            matchingIds.add(new Pair<Node>(solution.get("subscriptionId")
                    .asNode(), solution.get("subSubscriptionId").asNode()));
        }

        log.debug(
                "{} subscription(s) matches the quadruple which has been inserted",
                matchingIds.size());

        for (Pair<Node> pair : matchingIds) {
            log.debug(
                    "The peer {} has a sub subscription {} that matches the quadruple which has been received",
                    overlay, pair.getSecond().getLiteralLexicalForm());

            // stores a quadruple that contains the subscription and the sub
            // subscription id. It will be used later to ease the retrieval of
            // the subscriber URI
            Node subscriptionIdURL =
                    Node.createURI(SUBSCRIPTION_NS
                            + pair.getFirst().getLiteralLexicalForm());
            datastore.add(new Quadruple(
                    quadIdNode, subscriptionIdURL,
                    QUADRUPLE_MATCHES_SUBSCRIPTION_NODE, pair.getSecond()));

            // the identifier of the sub subscription that is matched is
            // available from the result of the query which has been executed
            SubscriptionId subscriptionId =
                    new SubscriptionId(((Number) pair.getFirst()
                            .getLiteralValue()).longValue());

            Subscription subscription =
                    ((SparqlRequestResponseManager) overlay.getRequestResponseManager()).find(subscriptionId);

            if (subscription.getSubSubscriptions().length == 1) {
                NotificationId notificationId =
                        new NotificationId(
                                subscription.getOriginalId(), System.nanoTime());

                // TODO: check if it is ok?
                datastore.delete(new Quadruple(
                        quadIdNode, subscriptionIdURL,
                        QUADRUPLE_MATCHES_SUBSCRIPTION_NODE, pair.getSecond()));

                // broadcast a message to all the stubs contained by the
                // subscription to say to these peers to send their
                // sub-solutions to the subscriber
                // TODO: send messages in parallel
                for (Subscription.Stub stub : subscription.getStubs()) {
                    try {
                        PAActiveObject.lookupActive(
                                SemanticPeer.class, stub.peerUrl).receive(
                                new RetrieveSubSolutionOperation(
                                        notificationId, stub.quadrupleHash));
                    } catch (ActiveObjectCreationException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // sends part of the solution to the subscriber
                // TODO: this operation can be done in parallel with the
                // RetrieveSubSolutionOperation
                subscription.getSourceStub()
                        .receive(
                                new Notification(
                                        notificationId,
                                        PAActiveObject.getUrl(overlay.getStub()),
                                        PublishSubscribeUtils.extractBinding(
                                                subscription.getSubSubscriptions()[0].getAtomicQuery(),
                                                quad)));
            } else {
                // then we find the subscription object associated to the
                // subscriptionId that is matched and we rewrite the
                // subscription according to the quadruple that matches the
                // first sub-subscription
                Subscription rewrittenSubscription =
                        SubscriptionRewriter.rewrite(subscription, quad);
                // stores the url of the stub of the current peer in order to
                // have the possibility to retrieve the quadruple later
                rewrittenSubscription.addStub(new Subscription.Stub(
                        PAActiveObject.getUrl(overlay.getStub()), quadId));

                try {
                    overlay.getStub().send(
                            new IndexRewrittenSubscriptionRequest(
                                    rewrittenSubscription));
                } catch (DispatchException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static final String createQueryRetrievingSubscriptionsMatching(Quadruple quad) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT ?subscriptionId ?subSubscriptionId WHERE {\n");
        query.append("    GRAPH <");
        query.append(SUBSCRIPTION_NS_NODE);
        query.append("> {\n");
        query.append("        ?subSubscriptionSource <");
        query.append(SUBSUBSCRIPTION_GRAPH_VALUE_PROPERTY);
        query.append("> ?subSubscriptionGraph .\n");
        query.append("        ?subSubscriptionSource <");
        query.append(SUBSUBSCRIPTION_SUBJECT_VALUE_PROPERTY);
        query.append("> ?subSubscriptionSubject .\n");
        query.append("        ?subSubscriptionSource <");
        query.append(SUBSUBSCRIPTION_PREDICATE_VALUE_PROPERTY);
        query.append("> ?subSubscriptionPredicate .\n");
        query.append("        ?subSubscriptionSource <");
        query.append(SUBSUBSCRIPTION_OBJECT_VALUE_PROPERTY);
        query.append("> ?subSubscriptionObject .\n");
        query.append("        ?subSubscriptionSource <");
        query.append(SUBSUBSCRIPTION_ID_PROPERTY);
        query.append("> ?subSubscriptionId .\n");
        query.append("        ?subscriptionSource <");
        query.append(SUBSCRIPTION_INDEXED_WITH_PROPERTY);
        query.append("> ?subSubscriptionId .\n");
        query.append("        ?subscriptionSource <");
        query.append(SUBSCRIPTION_ID_PROPERTY);
        query.append("> ?subscriptionId .\n");
        query.append("        FILTER (\n");
        query.append("            (sameTerm(?subSubscriptionGraph, ");
        query.append(FmtUtils.stringForNode(quad.getGraph()));
        query.append(") || datatype(?subSubscriptionGraph) = <");
        query.append(PublishSubscribeUtils.SUBSCRIPTION_VARIABLE_VALUE);
        query.append(">)\n");
        query.append("             && (sameTerm(?subSubscriptionSubject, ");
        query.append(FmtUtils.stringForNode(quad.getSubject()));
        query.append(") || datatype(?subSubscriptionSubject) = <");
        query.append(PublishSubscribeUtils.SUBSCRIPTION_VARIABLE_VALUE);
        query.append(">)\n");
        query.append("             && (sameTerm(?subSubscriptionPredicate, ");
        query.append(FmtUtils.stringForNode(quad.getPredicate()));
        query.append(") || datatype(?subSubscriptionPredicate) = <");
        query.append(PublishSubscribeUtils.SUBSCRIPTION_VARIABLE_VALUE);
        query.append(">)\n");
        query.append("             && (sameTerm(?subSubscriptionObject, ");
        query.append(FmtUtils.stringForNode(quad.getObject()));
        query.append(") || datatype(?subSubscriptionObject) = <");
        query.append(PublishSubscribeUtils.SUBSCRIPTION_VARIABLE_VALUE);
        query.append(">)\n");
        query.append("        )\n");
        query.append("     }\n");
        query.append("}");

        return query.toString();
    }

}
