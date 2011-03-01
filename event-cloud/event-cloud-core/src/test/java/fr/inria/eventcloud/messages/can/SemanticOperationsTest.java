package fr.inria.eventcloud.messages.can;

import java.util.HashSet;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ontoware.aifbcommons.collection.ClosableIterator;
import org.ontoware.rdf2go.model.QueryResultTable;
import org.ontoware.rdf2go.model.QueryRow;
import org.ontoware.rdf2go.model.Statement;

import fr.inria.eventcloud.initializers.SpaceNetworkInitializer;
import fr.inria.eventcloud.util.RDF2GoBuilder;
import fr.inria.eventcloud.util.SemanticHelper;

/**
 * Provides tests for operations distributed over a 3-dimensional CAN network.
 * 
 * Only one method is marked to be tested because methods must be called in the
 * order they are written.
 * 
 * @author lpellegr
 * 
 * @See SemanticDataStoreOperations
 */
public class SemanticOperationsTest {

    private static SpaceNetworkInitializer spaceNetworkInitializer = 
    	new SpaceNetworkInitializer(
            RDF2GoBuilder.createURI("http://www.inria.fr"));

    private static String[][] statementsToAdd = {
            { "http://Chair", "http://Made-of", "http://Wood" },
            { "http://Table", "http://Made-of", "http://Wood" },
            { "http://Looking-glass", "http://Made-of", "http://Glass" },
            { "http://Lamp", "http://On", "http://Chest" },
            { "http://Carpet", "http://Under", "http://CoffeeTable" },
            { "http://Accordion", "http://IsUsedFor", "http://Music" } 
            };

    @BeforeClass
    public static void setUp() {
        spaceNetworkInitializer.setUpNetworkOnLocalMachine(20);
    }

    @Test
    public void testSemanticOperations() {
        this.testSparqlConstructTriplePatternWithNoDataAvailable();
        this.testSparlqConstructTriplePatternDecomposedWithoutDataAvailable();
        this.testAddStatements();
        this.testSparqlDescribe();
        this.testSparqlConstructTriplePattern1();
        this.testSparqlConstructTriplePattern2();
        this.testSparqlConstructTriplePattern3();
        this.testSparqlConstructTriplePattern4();
        this.testSparqlConstructTriplePatternDecomposed();
        this.testSparqlSelectTriplePattern1();
        this.testSparqlSelectTriplePattern2();
        this.testSparqlAskTriplePattern();
        this.testDeleteOperation();
        this.testRemoveStatements();
    }
    
    public static void main(String[] args) {
       SemanticOperationsTest res = new SemanticOperationsTest();
       setUp();
       res.testRandomAddStatement();
    }
    
    public void testRandomAddStatement() {
        for (int i=0; i<10000; i++) {
            spaceNetworkInitializer.addStatement(SemanticHelper.generateRandomStatement());
            System.out.println("Add number" + i);
        }
        System.out.println("end.");
    }
    
    public void testSparlqConstructTriplePatternDecomposedWithoutDataAvailable() {
        StringBuffer query = new StringBuffer();
        query.append("CONSTRUCT { ?s ?p ?o } WHERE { GRAPH<");
        query.append(spaceNetworkInitializer.getSpaceURI().toString());
        query.append("> { ?s ?p <");
        query.append(statementsToAdd[0][2]);
        query.append(">. <");
        query.append(statementsToAdd[2][0]);
        query.append("> ?p ?o. }. }");

        Set<Statement> response = spaceNetworkInitializer.sparqlConstruct(query.toString());

        Assert.assertEquals(0, response.size());
    }

    public void testSparqlConstructTriplePatternWithNoDataAvailable() {
        StringBuffer query = new StringBuffer();
        query.append("CONSTRUCT { ?s ?p ?o } WHERE { GRAPH<");
        query.append(spaceNetworkInitializer.getSpaceURI().toString());
        query.append("> { ?s ?p ?o }. }");

        Set<Statement> response = spaceNetworkInitializer.sparqlConstruct(query.toString());
        Assert.assertEquals(0, response.size());
    }

    /**
     * /!\ This test adds statements in datastore(s) and is compulsory for the
     * next tests. All tests are executed sequentially and use the same context.
     */
    public void testAddStatements() {
        StringBuffer query = new StringBuffer();
        for (String[] stmt : statementsToAdd) {
            spaceNetworkInitializer.addStatement(stmt[0], stmt[1], stmt[2]);
        }

        query.append("CONSTRUCT { ?s ?p ?o } WHERE { GRAPH<");
        query.append(spaceNetworkInitializer.getSpaceURI());
        query.append("> { ?s ?p ?o }. }");

        Set<Statement> response = spaceNetworkInitializer.sparqlConstruct(query.toString());
        Assert.assertEquals(statementsToAdd.length, response.size());
    }

    public void testSparqlDescribe() {
    	StringBuffer query = new StringBuffer();
        query.append("DESCRIBE ?s WHERE { ?s ?p <");
        query.append(statementsToAdd[0][2]);
        query.append("> }");

        Set<Statement> response = spaceNetworkInitializer.sparqlDescribe(query.toString());
        Assert.assertEquals(2, response.size());
    }
    
    public void testSparqlConstructTriplePattern1() {
        StringBuffer query = new StringBuffer();
        query.append("CONSTRUCT { ?s <http://Chair> ?o } WHERE { GRAPH<");
        query.append(spaceNetworkInitializer.getSpaceURI().toString());
        query.append("> { ?s <http://Chair> ?o }. }");

        Set<Statement> response = spaceNetworkInitializer.sparqlConstruct(query.toString());
        Assert.assertEquals(0, response.size());
    }

    public void testSparqlConstructTriplePattern2() {
        StringBuffer query = new StringBuffer();
        query.append("CONSTRUCT { ?s ?p <http://Wood> } WHERE { GRAPH<");
        query.append(spaceNetworkInitializer.getSpaceURI().toString());
        query.append("> { ?s ?p <http://Wood> }. }");

        Set<Statement> response = spaceNetworkInitializer.sparqlConstruct(query.toString());
        Assert.assertEquals(2, response.size());
    }

    public void testSparqlConstructTriplePattern3() {
        StringBuffer query = new StringBuffer();
        query.append("CONSTRUCT { <http://Chair> ?p ?o } WHERE { GRAPH<");
        query.append(spaceNetworkInitializer.getSpaceURI().toString());
        query.append("> { <http://Chair> ?p ?o }. }");

        Set<Statement> response = spaceNetworkInitializer.sparqlConstruct(query.toString());
        Assert.assertEquals(1, response.size());
    }

    public void testSparqlConstructTriplePattern4() {
        StringBuffer query = new StringBuffer();
        query.append("CONSTRUCT { <");
        query.append(statementsToAdd[0][0]);
        query.append("> ?p <");
        query.append(statementsToAdd[2][2]);
        query.append("> } WHERE { GRAPH<");
        query.append(spaceNetworkInitializer.getSpaceURI().toString());
        query.append("> { <");
        query.append(statementsToAdd[0][0]);
        query.append("> ?p <");
        query.append(statementsToAdd[2][2]);
        query.append("> }. }");

        Set<Statement> response = spaceNetworkInitializer.sparqlConstruct(query.toString());

        Assert.assertEquals(0, response.size());
    }

    public void testSparqlConstructTriplePatternDecomposed() {
        StringBuffer query = new StringBuffer();
        query.append("CONSTRUCT { ?s ?p ?o } WHERE { GRAPH<");
        query.append(spaceNetworkInitializer.getSpaceURI().toString());
        query.append("> { ?s ?p <");
        query.append(statementsToAdd[0][2]);
        query.append(">. <");
        query.append(statementsToAdd[2][0]);
        query.append("> ?p ?o. }. }");

        Set<Statement> response = spaceNetworkInitializer.sparqlConstruct(query.toString());

        Assert.assertEquals(2, response.size());
    }

    public void testSparqlSelectTriplePattern1() {
        StringBuffer query = new StringBuffer();
        query.append("SELECT ?p WHERE { GRAPH<");
        query.append(spaceNetworkInitializer.getSpaceURI().toString());
        query.append("> { ?s ?p ?o }. }");

        QueryResultTable response = spaceNetworkInitializer.sparqlSelect(query.toString());
        ClosableIterator<QueryRow> it = response.iterator();

        Set<String> predicates = new HashSet<String>();
        for (int i = 0; i < statementsToAdd.length; i++) {
            predicates.add(statementsToAdd[i][1]);
        }

        int i = 0;
        QueryRow row = null;
        while (it.hasNext()) {
            row = it.next();
            Assert.assertTrue(predicates.contains(row.getValue("p").toString()));
            i++;
        }
        Assert.assertEquals(statementsToAdd.length, i);
    }

    public void testSparqlSelectTriplePattern2() {
        StringBuffer query = new StringBuffer();
        query.append("SELECT ?p WHERE { GRAPH<");
        query.append(spaceNetworkInitializer.getSpaceURI().toString());
        query.append("> { ?s <http://Made-of> ?o. <http://Chair> ?p ?o. }. }");

        QueryResultTable response = spaceNetworkInitializer.sparqlSelect(query.toString());
        ClosableIterator<QueryRow> it = response.iterator();

        int i = 0;
        QueryRow row = null;
        while (it.hasNext()) {
            row = it.next();
            Assert.assertEquals(statementsToAdd[0][1], row.getValue("p").toString());
            i++;
        }
        Assert.assertEquals(2, i);
    }

    public void testSparqlAskTriplePattern() {
        StringBuffer query = new StringBuffer();
        query.append("ASK { GRAPH<");
        query.append(spaceNetworkInitializer.getSpaceURI().toString());
        query.append("> { ?s ?p ?o }. }");

        boolean response = spaceNetworkInitializer.sparqlAsk(query.toString());

        Assert.assertEquals(true, response);
    }

    public void testDeleteOperation() {
        spaceNetworkInitializer.removeStatement("http://Table", "http://Made-of", "http://Wood");

        StringBuffer query = new StringBuffer();
        query.append("CONSTRUCT { ?s ?p ?o } WHERE { GRAPH<");
        query.append(spaceNetworkInitializer.getSpaceURI().toString());
        query.append("> { ?s ?p ?o }. }");

        Set<Statement> response = spaceNetworkInitializer.sparqlConstruct(query.toString());
        Assert.assertEquals(statementsToAdd.length - 1, response.size());
    }

    public void testRemoveStatements() {
        spaceNetworkInitializer.removeStatements(null, null, null);
        StringBuffer query = new StringBuffer();
        query.append("CONSTRUCT { ?s ?p ?o } WHERE { GRAPH<");
        query.append(spaceNetworkInitializer.getSpaceURI().toString());
        query.append("> { ?s ?p ?o }. }");
        
        Set<Statement> response = spaceNetworkInitializer.sparqlConstruct(query.toString());
        Assert.assertEquals(0, response.size());
    }

    @AfterClass
    public static void tearDown() {
        spaceNetworkInitializer.tearDownNetwork();
    }

}
