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

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.utils.SerializedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.listeners.NotificationListenerType;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.operations.can.RetrieveSubSolutionOperation;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.overlay.SemanticPeer;
import fr.inria.eventcloud.pubsub.Notification;
import fr.inria.eventcloud.pubsub.NotificationId;
import fr.inria.eventcloud.pubsub.PublishSubscribeUtils;
import fr.inria.eventcloud.pubsub.Subscription;
import fr.inria.eventcloud.pubsub.SubscriptionRewriter;
import fr.inria.eventcloud.pubsub.Subsubscription;

/**
 * Request used to index a subscription that have been rewritten after the
 * publication of a quadruple. While the rewritten subscription is indexed, it
 * is possible to have received some quadruples that match the rewritten
 * subscription. That's why an algorithm similar to the one from
 * {@link PublishQuadrupleRequest} is used to rewrite the rewritten subscription
 * for the quadruples that match the rewritten subscription.
 * 
 * @see PublishQuadrupleRequest
 * 
 * @author lpellegr
 */
public class IndexSubscriptionRequest extends StatelessQuadruplePatternRequest {

    private static final long serialVersionUID = 1L;

    private static final Logger log =
            LoggerFactory.getLogger(IndexSubscriptionRequest.class);

    protected SerializedValue<Subscription> subscription;

    /**
     * Constructs an IndexRewrittenSubscriptionRequest from the specified
     * rewritten {@code subscription}.
     * 
     * @param subscription
     *            the rewritten subscription to index.
     */
    public IndexSubscriptionRequest(Subscription subscription) {
        super(subscription.getSubSubscriptions()[0].getAtomicQuery()
                .getQuadruplePattern());

        this.subscription = new SerializedValue<Subscription>(subscription);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPeerValidatingKeyConstraints(CanOverlay overlay,
                                               QuadruplePattern quadruplePattern) {
        // writes the subscription into the cache and the local datastore
        Subscription subscription = this.subscription.getValue();

        log.debug("Indexing subscription {} on peer {}", subscription, overlay);

        ((SemanticCanOverlay) overlay).storeSubscription(subscription);

        TransactionalTdbDatastore datastore =
                (TransactionalTdbDatastore) overlay.getDatastore();
        Subsubscription firstSubsubscription =
                subscription.getSubSubscriptions()[0];

        // stores the quadruples into a list in order to avoid a concurrent
        // exception if a add operation (or more generally a write operation) is
        // done on the datastore while we are iterating on the ResultSet.
        // Indeed, the result set does not contain the solutions but knows how
        // to retrieve a solution each time a call to next is performed.
        List<Quadruple> quadruplesMatching = new ArrayList<Quadruple>();

        String queryRetrievingQuadruplesMatching =
                createQueryRetrievingQuadruplesMatching(firstSubsubscription.getAtomicQuery()
                        .getQuadruplePattern());

        log.debug(
                "Executed the following query:\n {}",
                queryRetrievingQuadruplesMatching);

        TransactionalDatasetGraph txnGraph =
                datastore.begin(AccessMode.READ_ONLY);
        QueryExecution queryExecution =
                QueryExecutionFactory.create(
                        queryRetrievingQuadruplesMatching, txnGraph.toDataset());

        ResultSet queryResult = queryExecution.execSelect();
        while (queryResult.hasNext()) {
            QuerySolution solution = queryResult.next();
            quadruplesMatching.add(new Quadruple(
                    solution.get("g").asNode(), solution.get("s").asNode(),
                    solution.get("p").asNode(), solution.get("o").asNode()));
        }
        queryExecution.close();
        txnGraph.close();

        for (Quadruple quadMatching : quadruplesMatching) {
            if (log.isDebugEnabled() && quadMatching.getPublicationTime() != -1) {
                log.debug(
                        "Comparing the timestamps between the quadruple {} and the subscription matching the quadruple {}",
                        quadMatching, subscription);
            }

            // TODO: skips the quadruples which have been published before the
            // subscription in the SPARQL query
            if (quadMatching.getPublicationTime() < subscription.getIndexationTime()) {
                continue;
            }

            if (subscription.getSubSubscriptions().length == 1) {
                NotificationId notificationId =
                        new NotificationId(
                                subscription.getOriginalId(), System.nanoTime());

                Binding binding = null;
                // for a signal it is not necessary to retrieve the binding
                // value
                if (subscription.getType() != NotificationListenerType.SIGNAL) {
                    binding =
                            PublishSubscribeUtils.filter(
                                    quadMatching, subscription.getResultVars(),
                                    firstSubsubscription.getAtomicQuery());
                }

                // sends part of the solution to the subscriber
                // TODO: this operation can be done in parallel with the send
                // RetrieveSubSolutionOperation
                try {
                    subscription.getSubscriberProxy().receive(
                            new Notification(
                                    notificationId,
                                    PAActiveObject.getUrl(overlay.getStub()),
                                    binding));

                    // broadcasts a message to all the stubs contained by the
                    // subscription to say to these peers to send their
                    // sub-solutions to the subscriber
                    // TODO: send message in parallel
                    for (Subscription.Stub stub : subscription.getStubs()) {
                        try {
                            PAActiveObject.lookupActive(
                                    SemanticPeer.class, stub.peerUrl)
                                    .receive(
                                            new RetrieveSubSolutionOperation(
                                                    notificationId,
                                                    stub.quadrupleHash));
                        } catch (ActiveObjectCreationException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    log.error("No SubscribeProxy found under the given URL: "
                            + subscription.getSubscriberUrl(), e);

                    // TODO: this could be due to a subscriber which has left
                    // without unsubscribing. In that case we can remove the
                    // subscription information associated to this subscriber
                    // and also send a message
                }
            } else {
                Quadruple metaQuad =
                        PublishSubscribeUtils.createMetaQuadruple(
                                quadMatching,
                                PublishSubscribeUtils.createSubscriptionIdUrl(subscription.getParentId()),
                                Node.createLiteral(subscription.getId()
                                        .toString(), XSDDatatype.XSDlong));

                txnGraph = datastore.begin(AccessMode.WRITE);
                txnGraph.add(metaQuad);
                txnGraph.commit();
                txnGraph.close();

                Subscription rewrittenSubscription =
                        SubscriptionRewriter.rewrite(subscription, quadMatching);

                // stores the url of the stub of the current peer in order to
                // have the possibility to retrieve the quadruple later
                rewrittenSubscription.addStub(new Subscription.Stub(
                        PAActiveObject.getUrl(overlay.getStub()),
                        quadMatching.hashValue()));

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

    /**
     * Creates a SPARQL SELECT query that retrieves all the quadruples matches
     * the specified {@link QuadruplePattern}. It is important to note here that
     * the query which is used, filter the quadruples according to the quadruple
     * elements which are fixed. In the case of the graph value {@code g}
     * associated to the given {@link QuadruplePattern}, the query filters the
     * quadruples by verifying if their graph value starts with {@code g} only.
     * Indeed, when a quadruple is inserted into the datastore, it is done by
     * concatenating the meta information associated to the quadruple into the
     * graph value.
     * 
     * @param qp
     *            the {@link QuadruplePattern} to match.
     * 
     * @return a SPARQL SELECT query.
     */
    private static String createQueryRetrievingQuadruplesMatching(QuadruplePattern qp) {
        StringWriter query = new StringWriter();
        query.append("SELECT ?g ?s ?p ?o WHERE {\n    GRAPH ?g {\n        ?s ?p ?o . \n    }\n");

        if (qp.getGraph().isURI()) {
            query.append("    FILTER (STRSTARTS(str(?g), \"");
            query.append(qp.getGraph().getURI());
            query.append("\"))\n");
        }

        query.append("}\n");

        return query.toString();
    }

}
