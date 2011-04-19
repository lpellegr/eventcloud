package fr.inria.eventcloud.overlay.can;

import static fr.inria.eventcloud.config.EventCloudProperties.DEFAULT_CONTEXT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.api.PAFuture;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;

import fr.inria.eventcloud.api.responses.SparqlConstructResponse;
import fr.inria.eventcloud.api.responses.SparqlDescribeResponse;
import fr.inria.eventcloud.initializers.EventCloudInitializer;
import fr.inria.eventcloud.overlay.SemanticPeer;
import fr.inria.eventcloud.util.RDF2GoBuilder;
import fr.inria.eventcloud.util.SemanticHelper;

/**
 * Tests associated to semantic operations provided by {@link SemanticPeer}.
 * 
 * @author lpellegr
 */
public class SemanticPeerTest {

    private static EventCloudInitializer initializer =
            new EventCloudInitializer();

    private static String[][] statementsToAdd = {
            {"http://Chair", "http://Made-of", "http://Wood"},
            {"http://Table", "http://Made-of", "http://Wood"},
            {"http://Looking-glass", "http://Made-of", "http://Glass"},
            {"http://Lamp", "http://On", "http://Chest"},
            {"http://Carpet", "http://Under", "http://CoffeeTable"},
            {"http://Accordion", "http://IsUsedFor", "http://Music"}};

    @BeforeClass
    public static void setUp() {
        initializer.setUpNetworkOnLocalMachine(10);
    }

    @Test
    public void runTests() {
        this.testSparlqConstructTriplePatternDecomposedWithoutDataAvailable();
        this.testSparqlConstructTriplePatternWithNoDataAvailable();
        this.testAddStatements();
        this.testSparqlAskTriplePattern();
        this.testSparqlConstructTriplePattern1();
        this.testSparqlConstructTriplePattern2();
        this.testSparqlConstructTriplePattern3();
        this.testSparqlConstructTriplePattern4();
        this.testSparqlConstructBasicGraphPattern();

        // TODO: fixes describe queries which are not supported
        // this.testSparqlDescribe();

        this.testSparqlSelectTriplePattern();
        this.testSparqlSelectBasicGraphPattern();
        this.testRemoveStatement();
        this.testRemoveStatements();
    }

    public void testSparlqConstructTriplePatternDecomposedWithoutDataAvailable() {
        StringBuffer query = new StringBuffer();
        query.append("CONSTRUCT { ?s ?p ?o } WHERE { ");
        query.append("?s ?p <");
        query.append(statementsToAdd[0][2]);
        query.append(">. <");
        query.append(statementsToAdd[2][0]);
        query.append("> ?p ?o. }");

        SparqlConstructResponse response =
                initializer.getRandomPeer().executeSparqlConstruct(
                        query.toString());

        Assert.assertEquals(0, SemanticHelper.size(response.getResult()
                .toRDF2Go()));
    }

    public void testSparqlConstructTriplePatternWithNoDataAvailable() {
        String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }";

        SparqlConstructResponse response =
                initializer.getRandomPeer().executeSparqlConstruct(
                        query.toString());

        assertEquals(0, SemanticHelper.size(response.getResult().toRDF2Go()));
    }

    /**
     * /!\ This test adds statements in datastore(s) and is compulsory for the
     * next tests. All tests are executed sequentially and use the same context.
     */
    public void testAddStatements() {
        for (String[] stmt : statementsToAdd) {
            initializer.getRandomPeer().addStatement(
                    DEFAULT_CONTEXT,
                    RDF2GoBuilder.createStatementInternal(
                            stmt[0], stmt[1], stmt[2]));
        }

        String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }";

        SparqlConstructResponse response =
                initializer.getRandomPeer().executeSparqlConstruct(query);

        assertEquals(
                statementsToAdd.length,
                SemanticHelper.size(response.getResult().toRDF2Go()));
    }

    public void testSparqlDescribe() {
        StringBuffer query = new StringBuffer();
        query.append("DESCRIBE ?s WHERE { ?s ?p <");
        query.append(statementsToAdd[0][2]);
        query.append("> }");

        SparqlDescribeResponse response =
                initializer.getRandomPeer().executeSparqlDescribe(
                        query.toString());

        assertEquals(2, SemanticHelper.size(response.getResult().toRDF2Go()));
    }

    public void testSparqlConstructTriplePattern1() {
        StringBuffer query = new StringBuffer();
        query.append("CONSTRUCT { ?s <http://Chair> ?o } ");
        query.append("WHERE { ?s <http://Chair> ?o }");

        SparqlConstructResponse response =
                initializer.getRandomPeer().executeSparqlConstruct(
                        query.toString());

        assertEquals(0, SemanticHelper.size(response.getResult().toRDF2Go()));
    }

    public void testSparqlConstructTriplePattern2() {
        StringBuffer query = new StringBuffer();
        query.append("CONSTRUCT { ?s ?p <http://Wood> } ");
        query.append("WHERE { ?s ?p <http://Wood> }");

        SparqlConstructResponse response =
                initializer.getRandomPeer().executeSparqlConstruct(
                        query.toString());

        assertEquals(2, SemanticHelper.size(response.getResult().toRDF2Go()));
    }

    public void testSparqlConstructTriplePattern3() {
        StringBuffer query = new StringBuffer();
        query.append("CONSTRUCT { <http://Chair> ?p ?o } ");
        query.append("WHERE { <http://Chair> ?p ?o }");

        SparqlConstructResponse response =
                initializer.getRandomPeer().executeSparqlConstruct(
                        query.toString());

        assertEquals(1, SemanticHelper.size(response.getResult().toRDF2Go()));
    }

    public void testSparqlConstructTriplePattern4() {
        StringBuffer query = new StringBuffer();
        query.append("CONSTRUCT { <");
        query.append(statementsToAdd[0][0]);
        query.append("> ?p <");
        query.append(statementsToAdd[2][2]);
        query.append("> } WHERE { ");
        query.append("<");
        query.append(statementsToAdd[0][0]);
        query.append("> ?p <");
        query.append(statementsToAdd[2][2]);
        query.append("> }");

        SparqlConstructResponse response =
                initializer.getRandomPeer().executeSparqlConstruct(
                        query.toString());

        assertEquals(0, SemanticHelper.size(response.getResult().toRDF2Go()));
    }

    public void testSparqlConstructBasicGraphPattern() {
        StringBuffer query = new StringBuffer();
        query.append("CONSTRUCT { ?s ?p ?o } ");
        query.append("WHERE {  ?s ?p <");
        query.append(statementsToAdd[0][2]);
        query.append("> . <");
        query.append(statementsToAdd[2][0]);
        query.append("> ?p ?o. }");

        SparqlConstructResponse response =
                initializer.getRandomPeer().executeSparqlConstruct(
                        query.toString());

        assertEquals(2, SemanticHelper.size(response.getResult().toRDF2Go()));
    }

    public void testSparqlSelectTriplePattern() {
        String query = "SELECT ?p WHERE { ?s ?p ?o }";

        QueryResultTable response =
                initializer.getRandomPeer()
                        .executeSparqlSelect(query)
                        .getResult()
                        .toRDF2Go();
        ClosableIterator<QueryRow> it = response.iterator();

        Set<String> predicates = new HashSet<String>();
        for (int i = 0; i < statementsToAdd.length; i++) {
            predicates.add(statementsToAdd[i][1]);
        }

        int i = 0;
        QueryRow row = null;
        while (it.hasNext()) {
            row = it.next();
            assertTrue(predicates.contains(row.getValue("p").toString()));
            i++;
        }

        assertEquals(statementsToAdd.length, i);
    }

    public void testSparqlSelectBasicGraphPattern() {
        StringBuffer query = new StringBuffer();
        query.append("SELECT ?p WHERE { ");
        query.append("?s <http://Made-of> ?o . ");
        query.append("<http://Chair> ?p ?o. }");

        QueryResultTable response =
                initializer.getRandomPeer().executeSparqlSelect(
                        query.toString()).getResult().toRDF2Go();
        ClosableIterator<QueryRow> it = response.iterator();

        int i = 0;
        QueryRow row = null;
        while (it.hasNext()) {
            row = it.next();
            Assert.assertEquals(statementsToAdd[0][1], row.getValue("p")
                    .toString());
            i++;
        }

        assertEquals(2, i);
    }

    public void testSparqlAskTriplePattern() {
        String query = "ASK { ?s ?p ?o }";

        assertEquals(true, initializer.getRandomPeer()
                .executeSparqlAsk(query)
                .getResult());
    }

    public void testRemoveStatement() {
        PAFuture.waitFor(initializer.getRandomPeer().removeStatement(
                DEFAULT_CONTEXT,
                RDF2GoBuilder.createStatementInternal(
                        "http://Table", "http://Made-of", "http://Wood")));

        String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }";
        SparqlConstructResponse response =
                initializer.getRandomPeer().executeSparqlConstruct(
                        query.toString());

        assertEquals(
                statementsToAdd.length - 1,
                SemanticHelper.size(response.getResult().toRDF2Go()));
    }

    public void testRemoveStatements() {
        PAFuture.waitFor(initializer.getRandomPeer().removeStatements(
                DEFAULT_CONTEXT,
                RDF2GoBuilder.createStatementInternal(null, null, null)));

        String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }";
        SparqlConstructResponse response =
                initializer.getRandomPeer().executeSparqlConstruct(
                        query.toString());

        assertEquals(0, SemanticHelper.size(response.getResult().toRDF2Go()));
    }

    @AfterClass
    public static void tearDown() {
        initializer.tearDownNetwork();
    }

}
