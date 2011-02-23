package fr.inria.eventcloud.datastore;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.ontoware.aifbcommons.collection.ClosableIterable;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.impl.StatementImpl;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.impl.URIImpl;

import at.sti2.semanticspaces.api.exceptions.SemanticSpaceException;
import fr.inria.eventcloud.util.RDF2GoBuilder;
import fr.inria.eventcloud.util.SemanticHelper;

/**
 * Provides tests for some operations from {@link SemanticDataStoreOperations}
 * on a local {@link SemanticDataStore} which is RDF2Go compatible.
 * 
 * @author lpellegr
 */
public class OwlimDatastoreTest {

    private static SemanticDatastore datastore;

    private static final ExecutorService executor = 
        Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4);

    private static final URI spaceURI = RDF2GoBuilder.createURI("http://www.inria.fr");

    private static final int CONCURRENT_RANDOM_OPERATIONS = 500;
    
    private static final int CONCURRENT_ADD_OPERATIONS = 500;

    private static String[][] statementsForSingleThread = {
            { "http://chair", "http://made-of", "http://wood" },
            { "http://table", "http://made-of", "http://wood" },
            { "http://looking-glass", "http://made-of", "http://glass" } };
    
    @BeforeClass
    public static void setUp() {
        datastore = new OwlimDatastore(true);
        datastore.open();
    }
    
    @Test
    public void testAddStatementsMultiThread() {
        final CountDownLatch doneSignal = new CountDownLatch(CONCURRENT_ADD_OPERATIONS);
        for (int i = 0; i < CONCURRENT_ADD_OPERATIONS; i++) {
            executor.execute(new Runnable() {
                public void run() {
                    try {
                        datastore.addStatement(
                                spaceURI, 
                                SemanticHelper.generateRandomStatement());
                    } finally {
                        doneSignal.countDown();
                    }
                }
            });
        }

        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertEquals(CONCURRENT_ADD_OPERATIONS, sparqlConstruct(spaceURI,
                "CONSTRUCT { ?s ?p ?o } WHERE { GRAPH<" + spaceURI + "> { ?s ?p ?o } . }").size());
    }

    @Test
    public void testRandomConcurrentAccess() {
        final CountDownLatch doneSignal = new CountDownLatch(CONCURRENT_RANDOM_OPERATIONS);
        for (int i = 0; i < CONCURRENT_RANDOM_OPERATIONS; i++) {
            executor.execute(new Runnable() {
                public void run() {
                    try {
                        if (ProActiveRandom.nextFloat() > 0.5) {
                            datastore.addStatement(
                                    spaceURI, 
                                    SemanticHelper.generateRandomStatement());
                        } else {
                            datastore.sparqlConstruct(spaceURI, "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }");
                        }
                    } finally {
                        doneSignal.countDown();
                    }
                }
            });
        }
        
        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }
    
    @Test
    public void testRemoveAllStatements() {
        datastore.removeAll(spaceURI);

        Assert.assertEquals(0, 
                sparqlConstruct(spaceURI,
                "CONSTRUCT { ?s ?p ?o } WHERE { GRAPH<" + spaceURI + "> { ?s ?p ?o } . }").size());
    }
    
    @Test
    public void testSequentialOperations() {
        this.testAddStatementsSingleThread();
        this.testSparqlConstructQueries();
        this.testAskQueryWithData();
        this.testRemoveStatement();
        this.testAskQueryWithoutData();
        this.testSparqlConstructWithFilter();
        this.testSparqlSelect();
    }
    
    public void testAddStatementsSingleThread() {
        for (int i = 0; i < statementsForSingleThread.length; i++) {
            try {
                datastore.addStatement(spaceURI, RDF2GoBuilder.toStatement(
                        statementsForSingleThread[i][0], statementsForSingleThread[i][1],
                        statementsForSingleThread[i][2]));
            } catch (SemanticSpaceException e) {
                e.printStackTrace();
            }
        }
        Assert.assertEquals(statementsForSingleThread.length, sparqlConstruct(spaceURI,
                "CONSTRUCT { ?s ?p ?o } WHERE { GRAPH<" + spaceURI + "> { ?s ?p ?o } . }").size());
    }
    
    public void testSparqlConstructQueries() {
        Assert.assertEquals(3, sparqlConstruct(
                spaceURI,
                "CONSTRUCT { ?s <" + statementsForSingleThread[0][1] + "> ?o } WHERE { GRAPH<"
                        + spaceURI + "> { ?s <" + statementsForSingleThread[0][1] + "> ?o } . }")
                .size());

        Assert.assertEquals(2, sparqlConstruct(
                spaceURI,
                "CONSTRUCT { ?s ?p <" + statementsForSingleThread[0][2] + "> } WHERE { GRAPH<"
                        + spaceURI + "> { ?s ?p <" + statementsForSingleThread[0][2] + "> } . }")
                .size());

        Assert.assertEquals(1, sparqlConstruct(
                spaceURI,
                "CONSTRUCT { <" + statementsForSingleThread[2][0] + "> ?p ?o } WHERE { GRAPH<"
                        + spaceURI + "> { <" + statementsForSingleThread[2][0] + "> ?p ?o } . }")
                .size());
    }

    public void testAskQueryWithData() {
        Assert.assertEquals(true, datastore.sparqlAsk(spaceURI, "ASK { GRAPH<" + spaceURI
                + "> { ?s ?p ?o } . }"));
    }

    public void testRemoveStatement() {
        datastore.removeStatement(spaceURI, 
                new StatementImpl(spaceURI, 
                        new URIImpl(statementsForSingleThread[0][0]),
                        new URIImpl(statementsForSingleThread[0][1]), 
                        new URIImpl(statementsForSingleThread[0][2])));

        Assert.assertEquals(statementsForSingleThread.length - 1, 
                sparqlConstruct(spaceURI,
                        "CONSTRUCT { ?s ?p ?o } WHERE { GRAPH<" + spaceURI + "> { ?s ?p ?o } . }").size());
    }

    public void testAskQueryWithoutData() {
        datastore.removeAll(spaceURI);
        
        Assert.assertEquals(false, datastore.sparqlAsk(spaceURI, "ASK { GRAPH<" + spaceURI
                + "> { ?s ?p ?o} . }"));
    }

    public void testSparqlConstructWithFilter() {
        statementsForSingleThread[0][0] = "http://f";
        statementsForSingleThread[0][1] = "http://f";
        statementsForSingleThread[0][2] = "http://f";

        statementsForSingleThread[1][0] = "http://d";
        statementsForSingleThread[1][1] = "http://b";
        statementsForSingleThread[1][2] = "http://e";

        statementsForSingleThread[2][0] = "http://z";
        statementsForSingleThread[2][1] = "http://z";
        statementsForSingleThread[2][2] = "http://z";

        for (int i = 0; i < statementsForSingleThread.length; i++) {
            datastore.addStatement(spaceURI, new StatementImpl(spaceURI, new URIImpl(
                    statementsForSingleThread[i][0]), new URIImpl(
                    statementsForSingleThread[i][1]), new URIImpl(
                    statementsForSingleThread[i][2])));
        }

        Assert
                .assertEquals(
                        1,
                        sparqlConstruct(
                                spaceURI,
                                "CONSTRUCT { ?s ?p ?o }  WHERE { GRAPH<"
                                        + spaceURI
                                        + "> { ?s ?p ?o . FILTER ( str(?s) >= \"http://d\" && str(?s) < \"http://z\" && str(?p) >= \"http://b\" && str(?p) < \"http://y\" && str(?o) >= \"http://f\" && str(?o) < \"http://z\").}}")
                                .size());
    }

    public void testSparqlSelect() {
        QueryResultTable qtr = null;
        qtr = datastore.sparqlSelect(spaceURI, "SELECT ?s WHERE { GRAPH<" + spaceURI
                + "> { ?s ?p ?o } . }");

        /*
         * Warning : There is no guarantee that statements are 
         *           retrieved in the order they are added.
         */
        Set<String> subjects = new HashSet<String>();
        for (String[] stmt: statementsForSingleThread) {
            subjects.add(stmt[0]);
        }
        
        ClosableIterator<QueryRow> it = qtr.iterator();
        int i = 0;
        while (it.hasNext()) {
            String subject = it.next().getValue("s").toString();
            Assert.assertTrue(subjects.contains(subject));
            subjects.remove(subject);
            i++;
        }

        Assert.assertEquals(statementsForSingleThread.length, i);
    }

    @AfterClass
    public static void tearDown() {
        datastore.close();
        executor.shutdown();
    }

    public static Set<Statement> sparqlConstruct(URI space, String query) {
        ClosableIterable<Statement> res = null;
        res = datastore.sparqlConstruct(space, query);
        Set<Statement> set = new HashSet<Statement>();

        while (res.iterator().hasNext()) {
            set.add(res.iterator().next());
        }

        return set;
    }

}
