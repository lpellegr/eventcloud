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
package fr.inria.eventcloud.operations.can;

import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.jena.riot.tokens.TokenizerFactory;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.p2p.structured.operations.RunnableOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.hash.HashCode;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.Var;

import fr.inria.eventcloud.api.PublishSubscribeConstants;
import fr.inria.eventcloud.api.listeners.BindingNotificationListener;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.pubsub.PublishSubscribeUtils;
import fr.inria.eventcloud.pubsub.PublishSubscribeUtils.BindingMap;
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
public class RetrieveSubSolutionOperation extends RunnableOperation {

    private static final long serialVersionUID = 150L;

    private static final Logger log =
            LoggerFactory.getLogger(RetrieveSubSolutionOperation.class);

    private final NotificationId notificationId;

    private final Set<HashCode> hashes;

    public RetrieveSubSolutionOperation(NotificationId id, Set<HashCode> hashes) {
        this.notificationId = id;
        this.hashes = hashes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(StructuredOverlay overlay) {
        // when this operation is handled we can assume that a binding
        // notification listener is used

        SemanticCanOverlay semanticOverlay = ((SemanticCanOverlay) overlay);

        TransactionalTdbDatastore datastore =
                semanticOverlay.getSubscriptionsDatastore();

        TransactionalDatasetGraph txnGraph =
                datastore.begin(AccessMode.READ_ONLY);

        StringBuilder query = this.createQueryRetrievingIntermediateResults();
        String subscriberURL = null;

        BindingMap bmap = new BindingMap();
        try {
            QueryExecution qExec =
                    QueryExecutionFactory.create(
                            query.toString(), txnGraph.getUnderlyingDataset());
            ResultSet rs = qExec.execSelect();
            try {
                int i = 0;

                while (rs.hasNext()) {
                    QuerySolution solution = rs.nextSolution();
                    if (i == 0) {
                        subscriberURL =
                                solution.get("subscriberURL").asNode().getURI();
                    }

                    String intermediateResults =
                            solution.getLiteral("ir").getLexicalForm();

                    String[] bindings = intermediateResults.split(",");
                    for (String binding : bindings) {
                        String[] pair = binding.split("=");

                        bmap.add(
                                Var.alloc(pair[0]),
                                TokenizerFactory.makeTokenizerString(pair[1])
                                        .next()
                                        .asNode());
                    }

                    i++;
                }
            } finally {
                qExec.close();
            }

        } catch (Exception e) {
            e.printStackTrace();
            txnGraph.abort();
        } finally {
            txnGraph.end();
        }

        try {
            Subscription.getSubscriberProxy(subscriberURL).receiveSbce1Or2(
                    new BindingNotification(
                            this.notificationId, null,
                            PAActiveObject.getUrl(semanticOverlay.getStub()),
                            bmap));
        } catch (ExecutionException e) {
            log.error("No SubscribeProxy found under the given URL: "
                    + subscriberURL, e);

            // TODO: this could be due to a subscriber which has left
            // without unsubscribing. In that case we can remove the
            // subscription information associated to this subscriber
            // and also send a message
        }
    }

    private StringBuilder createQueryRetrievingIntermediateResults() {
        StringBuilder query = new StringBuilder();
        query.append("SELECT ?ir ?subscriberURL WHERE { GRAPH ?g { ?subscriberURL <");
        query.append(PublishSubscribeConstants.INTERMEDIATE_RESULTS_NODE);
        query.append("> ?ir } VALUES ?g { ");

        for (HashCode hc : this.hashes) {
            query.append('<');
            query.append(PublishSubscribeUtils.createQuadrupleHashUri(hc)
                    .getURI());
            query.append("> ");
        }

        query.append(" } }");

        return query;
    }

}
