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

import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSCRIPTION_ID_PROPERTY;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSCRIPTION_INDEXED_WITH_PROPERTY;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSCRIPTION_NS_NODE;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSUBSCRIPTION_GRAPH_VALUE_PROPERTY;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSUBSCRIPTION_ID_PROPERTY;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSUBSCRIPTION_OBJECT_VALUE_PROPERTY;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSUBSCRIPTION_PREDICATE_VALUE_PROPERTY;
import static fr.inria.eventcloud.api.PublishSubscribeConstants.SUBSUBSCRIPTION_SUBJECT_VALUE_PROPERTY;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.utils.HomogenousPair;
import org.openjena.riot.out.NodeFmtLib;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import fr.inria.eventcloud.api.PublishSubscribeConstants;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.datastore.SynchronizedJenaDatasetGraph;
import fr.inria.eventcloud.operations.can.RetrieveSubSolutionOperation;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.overlay.SemanticPeer;
import fr.inria.eventcloud.pubsub.Notification;
import fr.inria.eventcloud.pubsub.NotificationId;
import fr.inria.eventcloud.pubsub.PublishSubscribeUtils;
import fr.inria.eventcloud.pubsub.Subscription;
import fr.inria.eventcloud.pubsub.SubscriptionRewriter;

/**
 * Publishes a quadruple into the network. The publish operation consists in
 * storing the quadruple which is published on the peer managing the constraints
 * constituted by the quadruple. After that, an algorithm is triggered to detect
 * whether some subscriptions are matched or not.
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
        quad = quad.toTimestampedQuadruple();

        SynchronizedJenaDatasetGraph datastore =
                ((SynchronizedJenaDatasetGraph) overlay.getDatastore());
        // the quad is stored by using its timestamped graph value
        datastore.add(quad);

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
        List<HomogenousPair<Node>> matchingIds =
                new ArrayList<HomogenousPair<Node>>();

        while (result.hasNext()) {
            QuerySolution solution = result.nextSolution();
            // the first component is composed of the subscription id whereas
            // the second contains the sub subscription id
            matchingIds.add(new HomogenousPair<Node>(solution.get(
                    "subscriptionId").asNode(), solution.get(
                    "subSubscriptionId").asNode()));
        }

        for (HomogenousPair<Node> pair : matchingIds) {
            log.debug(
                    "The peer {} has a sub subscription {} that matches the quadruple {} ",
                    new Object[] {
                            overlay, pair.getSecond().getLiteralLexicalForm(),
                            quad});

            Node subscriptionIdURL =
                    PublishSubscribeUtils.createSubscriptionIdUrl(pair.getFirst()
                            .getLiteralLexicalForm());

            // the identifier of the sub subscription that is matched is
            // available from the result of the query which has been executed
            SubscriptionId subscriptionId =
                    SubscriptionId.parseFrom(pair.getFirst()
                            .getLiteralLexicalForm());

            Subscription subscription =
                    ((SemanticCanOverlay) overlay).findSubscription(subscriptionId);

            // a subscription with only one sub subscription (that matches the
            // quadruple which has been inserted) has been detected
            if (subscription.getSubSubscriptions().length == 1) {
                log.debug(
                        "{} matches a subscription which cannot be rewritten, a notification will be delivered",
                        quad);

                NotificationId notificationId =
                        new NotificationId(
                                subscription.getOriginalId(),
                                System.currentTimeMillis());

                // sends part of the solution to the subscriber
                // TODO: this operation can be done in parallel with the
                // RetrieveSubSolutionOperation
                subscription.getSourceStub()
                        .receive(
                                new Notification(
                                        notificationId,
                                        PAActiveObject.getUrl(overlay.getStub()),
                                        PublishSubscribeUtils.filter(
                                                quad,
                                                subscription.getResultVars(),
                                                subscription.getSubSubscriptions()[0].getAtomicQuery())));

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
            } else {
                // stores a quadruple that contains the information about the
                // subscription that is matched and the quadruple that matches
                // the
                // subscription. This is useful to create the notification
                // later.
                // The matching quadruple is not sent directly to the next peers
                // because the quadruple value will be stored in memory on
                // several
                // peer. Moreover, there is no limit about the size of a
                // quadruple.
                Quadruple metaQuad =
                        PublishSubscribeUtils.createMetaQuadruple(
                                quad, subscriptionIdURL, pair.getSecond());
                datastore.add(metaQuad);

                // a subscription with more that one sub subscription (that
                // matches the quadruple which has been inserted) has been
                // detected. Then we find the subscription object associated to
                // the subscriptionId that is matched and we rewrite the
                // subscription according to the quadruple that matches the
                // first sub-subscription.
                Subscription rewrittenSubscription =
                        SubscriptionRewriter.rewrite(subscription, quad);
                // the hash value associated to the quadruple that matches the
                // subscription and the stub url for the current peer is stored
                // into the message which is sent to the peers for indexing the
                // rewritten subscription. These information are useful to have
                // the possibility to come back later for retrieving the sub
                // solution.
                rewrittenSubscription.addStub(new Subscription.Stub(
                        PAActiveObject.getUrl(overlay.getStub()),
                        quad.hashValue()));

                log.debug(
                        "Subscription matching {} has been rewritten to {} and is ready to be indexed",
                        quad, rewrittenSubscription);

                try {
                    overlay.getStub()
                            .send(
                                    new IndexSubscriptionRequest(
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
        query.append("            (regex(?subSubscriptionGraph, \"^");
        query.append(quad.getGraph().getURI());
        query.append("\") || sameTerm(?subSubscriptionGraph, ");
        query.append(NodeFmtLib.serialize(quad.getGraph()));
        query.append(") || datatype(?subSubscriptionGraph) = <");
        query.append(PublishSubscribeConstants.SUBSCRIPTION_VARIABLE_VALUE);
        query.append(">)\n");
        query.append("             && (sameTerm(?subSubscriptionSubject, ");
        query.append(NodeFmtLib.serialize(quad.getSubject()));
        query.append(") || datatype(?subSubscriptionSubject) = <");
        query.append(PublishSubscribeConstants.SUBSCRIPTION_VARIABLE_VALUE);
        query.append(">)\n");
        query.append("             && (sameTerm(?subSubscriptionPredicate, ");
        query.append(NodeFmtLib.serialize(quad.getPredicate()));
        query.append(") || datatype(?subSubscriptionPredicate) = <");
        query.append(PublishSubscribeConstants.SUBSCRIPTION_VARIABLE_VALUE);
        query.append(">)\n");
        query.append("             && (sameTerm(?subSubscriptionObject, ");
        query.append(NodeFmtLib.serialize(quad.getObject()));
        query.append(") || datatype(?subSubscriptionObject) = <");
        query.append(PublishSubscribeConstants.SUBSCRIPTION_VARIABLE_VALUE);
        query.append(">)\n");
        query.append("        )\n");
        query.append("     }\n");
        query.append("}");

        return query.toString();
    }

}
