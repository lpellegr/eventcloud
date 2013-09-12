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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.objectweb.proactive.extensions.p2p.structured.utils.LoggerUtils;

import com.google.common.base.Stopwatch;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.generators.NodeGenerator;
import fr.inria.eventcloud.api.generators.QuadrupleGenerator;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.QuadrupleIterator;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastoreBuilder;

/**
 * A simple benchmark that compares the time take to find the quadruples that
 * match a predefined set of graph values by using three different solutions: a
 * quad pattern for each predefined graph, a select query using the IN keyword
 * and a select query that uses the VALUES keyword.
 * 
 * @author lpellegr
 */
public class SelectOrQuadPatternsBenchmark {

    private static final File REPOSITORY_PATH = new File(
            System.getProperty("user.home") + File.separator
                    + SelectOrQuadPatternsBenchmark.class.getSimpleName());

    private final TransactionalTdbDatastore datastore;

    public SelectOrQuadPatternsBenchmark() {
        TransactionalTdbDatastoreBuilder builder =
                new TransactionalTdbDatastoreBuilder(REPOSITORY_PATH);
        builder.deleteFilesAfterClose(true);

        this.datastore = builder.build();
    }

    public void execute() {
        List<Node> graphs = this.addData();

        for (int i = 0; i < 10; i++) {
            this.executeQuadPatterns(graphs);
        }

        for (int i = 0; i < 10; i++) {
            this.executeSparql("IN Block", createSparqlQueryIN(graphs));
        }

        for (int i = 0; i < 10; i++) {
            this.executeSparql("VALUES Block", createSparqlQueryVALUES(graphs));
        }
    }

    private List<Node> addData() {
        System.out.println("ADD STARTED");

        TransactionalDatasetGraph txnGraph =
                this.datastore.begin(AccessMode.WRITE);

        List<Node> graphs = new ArrayList<Node>();
        for (int i = 0; i < 1000; i++) {
            graphs.add(NodeGenerator.randomUri(20));
        }

        try {
            for (int i = 0; i < 10000; i++) {
                txnGraph.add(QuadrupleGenerator.random());
                if (i % 100 == 0) {
                    System.out.println("     " + i);
                }
            }

            for (int i = 0; i < graphs.size(); i++) {
                for (int j = 0; j < 10; j++) {
                    txnGraph.add(QuadrupleGenerator.random(graphs.get(i)));
                }
            }

            txnGraph.commit();
        } finally {
            txnGraph.end();
        }

        System.out.println("ADD TERMINATED");

        return graphs;
    }

    private List<Quadruple> executeQuadPatterns(List<Node> graphs) {
        TransactionalDatasetGraph txnGraph =
                this.datastore.begin(AccessMode.READ_ONLY);

        List<Quadruple> result = new ArrayList<Quadruple>();

        Stopwatch stopwatch = Stopwatch.createUnstarted();
        stopwatch.start();

        try {
            for (Node g : graphs) {
                QuadrupleIterator it = txnGraph.find(g, null, null, null);
                while (it.hasNext()) {
                    result.add(it.next());
                }
            }
        } finally {
            txnGraph.end();
        }

        stopwatch.stop();
        System.out.println("EXECUTE QUAD PATTERNS TERMINATED "
                + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms and "
                + result.size() + " quads");

        return result;
    }

    private List<Quadruple> executeSparql(String name, String sparqlQuery) {
        TransactionalDatasetGraph txnGraph =
                this.datastore.begin(AccessMode.READ_ONLY);

        List<Quadruple> result = new ArrayList<Quadruple>();

        // System.out.println("SPARQL QUERY=\n" + sparqlQuery);
        // System.out.println("EXECUTE SPARQL STARTED");

        Stopwatch stopwatch = Stopwatch.createUnstarted();
        stopwatch.start();

        QueryExecution qexec =
                QueryExecutionFactory.create(
                        QueryFactory.create(sparqlQuery),
                        txnGraph.getUnderlyingDataset());

        try {
            ResultSet resultSet = qexec.execSelect();

            while (resultSet.hasNext()) {
                QuerySolution solution = resultSet.next();

                result.add(new Quadruple(
                        solution.get("g").asNode(), solution.get("s").asNode(),
                        solution.get("p").asNode(), solution.get("o").asNode()));
            }
        } finally {
            txnGraph.end();
        }

        stopwatch.stop();
        System.out.println("EXECUTE SPARQL " + name + " TERMINATED "
                + stopwatch.elapsed(TimeUnit.MILLISECONDS) + "ms and "
                + result.size() + " quads");

        return result;
    }

    private static String createSparqlQueryIN(List<Node> graphs) {
        StringBuilder result = new StringBuilder();
        result.append("SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { ?s ?p ?o } FILTER (?g IN(");
        for (int i = 0; i < graphs.size(); i++) {
            result.append("<");
            result.append(graphs.get(i));
            result.append(">");
            if (i < graphs.size() - 1) {
                result.append(", ");
            }
        }
        result.append(")) }");

        return result.toString();
    }

    private static String createSparqlQueryVALUES(List<Node> graphs) {
        StringBuilder result = new StringBuilder();
        result.append("SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { ?s ?p ?o } VALUES ?g { ");
        for (int i = 0; i < graphs.size(); i++) {
            result.append("<");
            result.append(graphs.get(i));
            result.append(">");
            if (i < graphs.size() - 1) {
                result.append("\n ");
            }
        }
        result.append(" }}");

        return result.toString();
    }

    public static void main(String[] args) {
        LoggerUtils.disableLoggers();

        if (REPOSITORY_PATH.exists()) {
            FileUtils.deleteQuietly(REPOSITORY_PATH);
        }

        REPOSITORY_PATH.mkdirs();

        new SelectOrQuadPatternsBenchmark().execute();

        FileUtils.deleteQuietly(REPOSITORY_PATH);
    }

}
