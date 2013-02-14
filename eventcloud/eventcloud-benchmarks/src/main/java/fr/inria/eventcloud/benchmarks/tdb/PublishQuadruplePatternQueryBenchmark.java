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
package fr.inria.eventcloud.benchmarks.tdb;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.objectweb.proactive.extensions.p2p.structured.utils.LoggerUtils;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.NotificationListenerType;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastoreBuilder;
import fr.inria.eventcloud.pubsub.Subscription;

/**
 * 
 * @author lpellegr
 */
public class PublishQuadruplePatternQueryBenchmark {

    private static final File REPOSITORY_PATH =
            new File(
                    System.getProperty("user.home")
                            + File.separator
                            + PublishQuadruplePatternQueryBenchmark.class.getSimpleName());

    private final TransactionalTdbDatastore datastore;

    public PublishQuadruplePatternQueryBenchmark() {
        TransactionalTdbDatastoreBuilder builder =
                new TransactionalTdbDatastoreBuilder(REPOSITORY_PATH);
        builder.deleteFilesAfterClose(true);

        this.datastore = builder.build();
    }

    public void execute() {
        this.storeSubscription();

        int count = 1000;
        long sum = 0;
        for (int i = 0; i < 1000; i++) {
            sum += this.executeSparqlQuery1();
        }

        System.out.println("TOTAL=" + sum / count);
    }

    private void storeSubscription() {
        // System.out.println("Store subscription BEGIN");

        TransactionalDatasetGraph txnGraph =
                this.datastore.begin(AccessMode.WRITE);

        try {
            SubscriptionId id = new SubscriptionId();
            Subscription subscription =
                    new Subscription(
                            id, null, id, System.currentTimeMillis(),
                            System.currentTimeMillis(),
                            "SELECT ?g WHERE { GRAPH ?g { ?s ?p ?o }}",
                            "subscriberURI", "destinationURI",
                            NotificationListenerType.BINDING);
            txnGraph.add(subscription.toQuadruples());
            txnGraph.commit();
        } finally {
            txnGraph.end();
        }

        // System.out.println("Store subscription END");
    }

    private long executeSparqlQuery1() {

        // simulate values for the quadruple which is published
        String g = "g";
        String s = "s";
        String p = "p";
        String o = "o";

        String sparqlQuery =
                "SELECT  ?a\n"
                        + "WHERE\n"
                        + "  { GRAPH ?g\n"
                        + "      { ?d <urn:ec:ss:g> ?e .\n"
                        + "        ?d <urn:ec:ss:s> ?f .\n"
                        + "        ?d <urn:ec:ss:p> ?h .\n"
                        + "        ?d <urn:ec:ss:o> ?i .\n"
                        + "        ?d <urn:ec:ss:id> ?c .\n"
                        + "        ?b <urn:ec:s:iref> ?c .\n"
                        + "        ?b <urn:ec:s:id> ?a\n"
                        + "        FILTER ( ( strstarts(str(?e), <"
                        + g
                        + ">) || ( ( datatype(?e) = <urn:ec:var> ) || ( ?e = <"
                        + g
                        + "> ) ) ) && ( ( sameTerm(?f, <"
                        + s
                        + ">) || ( datatype(?f) = <urn:ec:var> ) ) && ( ( sameTerm(?h, <"
                        + p
                        + ">) || ( datatype(?h) = <urn:ec:var> ) ) && ( sameTerm(?i, <"
                        + o + ">) || ( datatype(?i) = <urn:ec:var> ) ) ) ) )\n"
                        + "      }\n" + "  }";

        long start = System.currentTimeMillis();

        TransactionalDatasetGraph txnGraph =
                this.datastore.begin(AccessMode.READ_ONLY);

        QueryExecution qexec =
                QueryExecutionFactory.create(
                        QueryFactory.create(sparqlQuery),
                        txnGraph.getUnderlyingDataset());

        try {
            ResultSet resultSet = qexec.execSelect();

            int i = 1;
            while (resultSet.hasNext()) {
                QuerySolution solution = resultSet.next();

                System.out.println("SOLUTION " + i);
                System.out.println("id=" + solution.get("a"));
                i++;
            }
        } finally {
            txnGraph.end();
        }

        long diff = (System.currentTimeMillis() - start);
        System.out.println("Execute SPARQL query 1 END=" + diff);
        return diff;
    }

    public static void main(String[] args) {
        LoggerUtils.disableLoggers();

        if (REPOSITORY_PATH.exists()) {
            FileUtils.deleteQuietly(REPOSITORY_PATH);
        }

        REPOSITORY_PATH.mkdirs();

        new PublishQuadruplePatternQueryBenchmark().execute();

        FileUtils.deleteQuietly(REPOSITORY_PATH);
    }

}
