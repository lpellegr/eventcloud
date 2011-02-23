package fr.inria.eventcloud.query;

import junit.framework.Assert;

import org.junit.Test;
import org.ontoware.rdf2go.model.node.URI;
import org.openrdf.query.MalformedQueryException;

import fr.inria.eventcloud.api.messages.request.SparqlAskQuery;
import fr.inria.eventcloud.api.messages.request.SparqlConstructQuery;
import fr.inria.eventcloud.api.messages.request.SparqlSelectQuery;
import fr.inria.eventcloud.reasoner.ParsedSparqlQuery;
import fr.inria.eventcloud.reasoner.SparqlQueryReasoner;
import fr.inria.eventcloud.util.RDF2GoBuilder;

/**
 * Test {@link SparqlQueryReasoner}.
 * 
 * @author lpellegr
 */
public class SparqlQueryReasonerTest {

    private static final URI DEFAULT_SPACE_URI = RDF2GoBuilder.createURI("http://www.inria.fr");

    @Test
    public void testAskDecomposition() throws MalformedQueryException {
        ParsedSparqlQuery parsedQuery = 
        	SparqlQueryReasoner.getInstance().decompose(
        			new SparqlAskQuery(DEFAULT_SPACE_URI, "ASK { ?s ?p ?o. }"));

        Assert.assertEquals(1, parsedQuery.getSubQueries().size());
        Assert.assertEquals("ASK { ?s ?p ?o. }", parsedQuery.getSubQueries().get(0).toString());
    }

    @Test
    public void testConstructDecomposition() throws MalformedQueryException {
    	ParsedSparqlQuery parsedQuery = 
        	SparqlQueryReasoner.getInstance().decompose(
        			new SparqlConstructQuery(
                            DEFAULT_SPACE_URI,
                            "CONSTRUCT { ?s ?p <http://test> }  WHERE { ?s ?p ?o. FILTER (str(?s) >= \"test\" && ?p < 5). }"));

        Assert.assertEquals(1, parsedQuery.getSubQueries().size());
        Assert.assertEquals("CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }",
                			parsedQuery.getSubQueries().get(0).toString());
    }

    @Test
    public void testSelectDecomposition() throws MalformedQueryException {
    	ParsedSparqlQuery parsedQuery = 
        	SparqlQueryReasoner.getInstance().decompose(
        			new SparqlSelectQuery(
        					DEFAULT_SPACE_URI,
                    		"SELECT ?nick WHERE { ?x <http://mbox> <mailto:bob@work.example> . ?x <http://nick> ?nick }"));

        Assert.assertEquals(2, parsedQuery.getSubQueries().size());
    }

}
