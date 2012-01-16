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

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.utils.HomogenousPair;
import org.openjena.riot.out.NodeFormatterNT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import fr.inria.eventcloud.api.PublishSubscribeConstants;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.pubsub.PublishSubscribeUtils;
import fr.inria.eventcloud.pubsub.Subscription;

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

    private static final NodeFormatterNT nodeFormatter = new NodeFormatterNT();

    public PublishQuadrupleRequest(Quadruple quad) {
        super(quad);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestinationReached(final StructuredOverlay overlay,
                                     final Quadruple quadrupleMatching) {
        SemanticCanOverlay semanticOverlay = (SemanticCanOverlay) overlay;
        final TransactionalTdbDatastore datastore =
                ((TransactionalTdbDatastore) overlay.getDatastore());

        // Future<?> future =
        // semanticOverlay.datastoreThreadPool.submit((new Runnable() {
        // @Override
        // public void run() {
        TransactionalDatasetGraph txnGraph = datastore.begin(AccessMode.WRITE);
        // the quadruple is stored by using its timestamped
        // graph value
        txnGraph.add(
                quadrupleMatching.createMetaGraphNode(),
                quadrupleMatching.getSubject(),
                quadrupleMatching.getPredicate(), quadrupleMatching.getObject());
        txnGraph.commit();
        txnGraph.close();
        // }
        // }));
        //
        // try {
        // future.get();
        // } catch (InterruptedException e1) {
        // e1.printStackTrace();
        // } catch (ExecutionException e1) {
        // e1.printStackTrace();
        // }

        log.debug(
                "SPARQL query used to retrieve the sub subscriptions matching {}:\n{}",
                quadrupleMatching,
                createQueryRetrievingSubscriptionsMatching(quadrupleMatching));

        // we have to store the identifiers found into a new list because
        // we cannot iterate on a Jena iterator and perform operations on
        // the datastore at the same time
        List<HomogenousPair<Node>> matchingIds =
                new ArrayList<HomogenousPair<Node>>();

        // finds the sub subscriptions which are stored locally and that are
        // matching the quadruple which have been just inserted into the
        // local datastore
        txnGraph = datastore.begin(AccessMode.READ_ONLY);
        QueryExecution queryExecution =
                QueryExecutionFactory.create(
                        createQueryRetrievingSubscriptionsMatching(quadrupleMatching),
                        txnGraph.toDataset());

        ResultSet result = queryExecution.execSelect();
        try {
            while (result.hasNext()) {
                QuerySolution solution = result.nextSolution();

                // the first component is composed of the subscription id
                // whereas the second contains the sub-subscription id
                matchingIds.add(new HomogenousPair<Node>(solution.get(
                        "subscriptionId").asNode(), solution.get(
                        "subSubscriptionId").asNode()));
            }
        } finally {
            queryExecution.close();
            txnGraph.close();
        }

        if (matchingIds.isEmpty()) {
            log.debug(
                    "No subscription matching {} has been found on {}",
                    quadrupleMatching, overlay);
        }

        for (HomogenousPair<Node> pair : matchingIds) {
            log.debug(
                    "Peer {} has a sub-subscription {} that matches the quadruple {} ",
                    new Object[] {
                            overlay, pair.getSecond().getLiteralLexicalForm(),
                            quadrupleMatching});

            // the identifier of the sub subscription that is matched is
            // available from the result of the query which has been executed
            SubscriptionId subscriptionId =
                    SubscriptionId.parseFrom(pair.getFirst()
                            .getLiteralLexicalForm());

            Subscription subscription =
                    semanticOverlay.findSubscription(subscriptionId);

            // a subscription with only one sub subscription (that matches the
            // quadruple which has been inserted) has been detected
            PublishSubscribeUtils.rewriteSubscriptionOrNotifySender(
                    semanticOverlay, subscription, quadrupleMatching);
        }
    }

    private static String createQueryRetrievingSubscriptionsMatching(Quadruple quad) {
        StringWriter query = new StringWriter();
        query.append("SELECT ?subscriptionId ?subSubscriptionId WHERE {\n    GRAPH <");
        query.append(SUBSCRIPTION_NS_NODE.getURI());
        query.append("> {\n        ?subSubscriptionSource <");
        query.append(SUBSUBSCRIPTION_GRAPH_VALUE_PROPERTY);
        query.append("> ?subSubscriptionGraph .\n        ?subSubscriptionSource <");
        query.append(SUBSUBSCRIPTION_SUBJECT_VALUE_PROPERTY);
        query.append("> ?subSubscriptionSubject .\n        ?subSubscriptionSource <");
        query.append(SUBSUBSCRIPTION_PREDICATE_VALUE_PROPERTY);
        query.append("> ?subSubscriptionPredicate .\n        ?subSubscriptionSource <");
        query.append(SUBSUBSCRIPTION_OBJECT_VALUE_PROPERTY);
        query.append("> ?subSubscriptionObject .\n        ?subSubscriptionSource <");
        query.append(SUBSUBSCRIPTION_ID_PROPERTY);
        query.append("> ?subSubscriptionId .\n        ?subscriptionSource <");
        query.append(SUBSCRIPTION_INDEXED_WITH_PROPERTY);
        query.append("> ?subSubscriptionId .\n        ?subscriptionSource <");
        query.append(SUBSCRIPTION_ID_PROPERTY);
        query.append("> ?subscriptionId .\n        FILTER (\n            (STRSTARTS(str(?subSubscriptionGraph), \"");
        query.append(quad.getGraph().getURI());
        query.append("\") || sameTerm(?subSubscriptionGraph, ");

        nodeFormatter.formatURI(query, quad.getGraph().getURI());

        query.append(") || datatype(?subSubscriptionGraph) = <");
        query.append(PublishSubscribeConstants.SUBSCRIPTION_VARIABLE_VALUE);
        query.append(">)\n             && (sameTerm(?subSubscriptionSubject, ");

        nodeFormatter.formatURI(query, quad.getSubject());

        query.append(") || datatype(?subSubscriptionSubject) = <");
        query.append(PublishSubscribeConstants.SUBSCRIPTION_VARIABLE_VALUE);
        query.append(">)\n             && (sameTerm(?subSubscriptionPredicate, ");

        nodeFormatter.formatURI(query, quad.getPredicate());

        query.append(") || datatype(?subSubscriptionPredicate) = <");
        query.append(PublishSubscribeConstants.SUBSCRIPTION_VARIABLE_VALUE);
        query.append(">)\n             && (sameTerm(?subSubscriptionObject, ");

        nodeFormatter.format(query, quad.getObject());

        query.append(") || datatype(?subSubscriptionObject) = <");
        query.append(PublishSubscribeConstants.SUBSCRIPTION_VARIABLE_VALUE);
        query.append(">)\n        )\n     }\n}");

        return query.toString();
    }

}
