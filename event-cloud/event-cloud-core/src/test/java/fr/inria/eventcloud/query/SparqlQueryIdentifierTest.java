//package fr.inria.eventcloud.query;
//
//import junit.framework.Assert;
//
//import org.junit.Test;
//import org.ontoware.rdf2go.model.node.URI;
//import org.ontoware.rdf2go.model.node.impl.URIImpl;
//import org.openrdf.query.MalformedQueryException;
//
//import fr.inria.eventcloud.api.messages.request.SparqlAskQuery;
//import fr.inria.eventcloud.api.messages.request.SparqlConstructQuery;
//import fr.inria.eventcloud.api.messages.request.SparqlSelectQuery;
//import fr.inria.eventcloud.messages.request.can.SparqlAskRequest;
//import fr.inria.eventcloud.reasoner.SparqlQueryIdentifier;
//
///**
// * Test {@link SparqlQueryIdentifier}.
// * 
// * @author lpellegr
// */
//public class SparqlQueryIdentifierTest {
//
//    private static final URI DEFAULT_SPACE_URI = new URIImpl("http://www.inria.fr");
//
////    @Test
////    public void testRangeQueryCreation1() throws MalformedQueryException {
////        RangeQueryMessage msg = 
////        	(RangeQueryMessage) SparqlQueryIdentifier.createQueryMessage(
////        			new SparqlSelectQuery(
////                        DEFAULT_SPACE_URI,
////                        "SELECT ?s ?p ?o WHERE { ?s ?p ?o. FILTER ( str(?o) > \"http://aaa\" && str(?o) < \"http://bbb\" ). }"));
////
////        for (RangeQueryCondition condition : msg.getConditions()) {
////            Assert.assertEquals("o", condition.getVar());
////        }
////    }
//
////    @Test
////    public void testRangeQueryCreation2() throws MalformedQueryException {
////        RangeQueryMessage msg = (RangeQueryMessage) SparqlQueryIdentifier
////                .createQueryMessage(new SparqlSelectQuery(
////                        DEFAULT_SPACE_URI,
////                        "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o . FILTER ( str(?s) > \"0\" && str(?s) < \"U\" && str(?p) > \"U\" && str(?p) < \"{\" && str(?o) > \"0\" && str(?o) < \"U\"). }"));
////
////        Assert.assertTrue(msg instanceof RangeQueryMessage);
////    }
//
//    @Test
//    public void testTriplePatternQueryCreation1() throws MalformedQueryException {
//        Assert
//                .assertTrue(SparqlQueryIdentifier.createQueryMessage(new SparqlSelectQuery(DEFAULT_SPACE_URI,
//                        "SELECT ?s ?p ?o WHERE { <http://toto> <http://aa> <http://aa>. }")) instanceof SparqlAskRequest);
//    }
//
//    @Test
//    public void testTriplePatternQueryCreation2() throws MalformedQueryException {
//        Assert
//                .assertTrue(SparqlQueryIdentifier.createQueryMessage(new SparqlSelectQuery(DEFAULT_SPACE_URI,
//                        "SELECT ?s ?p ?o WHERE { ?s <http://aa> <http://aa>. }")) instanceof SparqlAskRequest);
//    }
//
//    @Test
//    public void testTriplePatternQueryCreation3() throws MalformedQueryException {
//        Assert
//                .assertTrue(SparqlQueryIdentifier.createQueryMessage(new SparqlSelectQuery(DEFAULT_SPACE_URI,
//                        "SELECT ?s ?p ?o WHERE { <http://toto> ?p <http://aa>. }")) instanceof SparqlAskRequest);
//    }
//
//    @Test
//    public void testTriplePatternQueryCreation4() throws MalformedQueryException {
//        Assert
//                .assertTrue(SparqlQueryIdentifier.createQueryMessage(new SparqlSelectQuery(DEFAULT_SPACE_URI,
//                        "SELECT ?s ?p ?o WHERE { <http://toto> <http://aa> ?o. }")) instanceof SparqlAskRequest);
//    }
//
//    @Test
//    public void testTriplePatternQueryCreation5() throws MalformedQueryException {
//        Assert
//                .assertTrue(SparqlQueryIdentifier.createQueryMessage(new SparqlSelectQuery(DEFAULT_SPACE_URI,
//                        "SELECT ?s ?p ?o WHERE { ?s ?p <http://aa>. }")) instanceof SparqlAskRequest);
//    }
//
//    @Test
//    public void testTriplePatternQueryCreation6() throws MalformedQueryException {
//        Assert.assertTrue(SparqlQueryIdentifier.createQueryMessage(new SparqlSelectQuery(DEFAULT_SPACE_URI,
//                "SELECT ?s ?p ?o WHERE { ?s ?p ?o. }")) instanceof SparqlAskRequest);
//    }
//
//    @Test
//    public void testTriplePatternQueryCreation7() throws MalformedQueryException {
//        Assert
//                .assertTrue(SparqlQueryIdentifier.createQueryMessage(new SparqlSelectQuery(DEFAULT_SPACE_URI,
//                        "SELECT ?s ?p ?o WHERE { <http://toto> ?p ?o. }")) instanceof SparqlAskRequest);
//    }
//
//    @Test
//    public void testTriplePatternQueryCreation8() throws MalformedQueryException {
//        Assert
//                .assertTrue(SparqlQueryIdentifier.createQueryMessage(new SparqlConstructQuery(DEFAULT_SPACE_URI,
//                        "CONSTRUCT { ?s ?p ?o } WHERE { <http://toto> <http://aa> <http://aa>. }")) instanceof SparqlAskRequest);
//    }
//
//    @Test
//    public void testTriplePatternQueryCreation9() throws MalformedQueryException {
//        Assert
//                .assertTrue(SparqlQueryIdentifier.createQueryMessage(new SparqlConstructQuery(DEFAULT_SPACE_URI,
//                        "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o. }")) instanceof SparqlAskRequest);
//    }
//
//    @Test
//    public void testTriplePatternQueryCreation10() throws MalformedQueryException {
//        Assert.assertTrue(SparqlQueryIdentifier.createQueryMessage(new SparqlAskQuery(DEFAULT_SPACE_URI,
//                "ASK { ?s ?p ?o. }")) instanceof SparqlAskRequest);
//    }
//
//    @Test
//    public void testTriplePatternQueryCreation11() throws MalformedQueryException {
//        Assert
//                .assertTrue(SparqlQueryIdentifier.createQueryMessage(new SparqlAskQuery(DEFAULT_SPACE_URI,
//                        "ASK { GRAPH<http://hello-world> { ?s ?p ?o }. }")) instanceof SparqlAskRequest);
//    }
//
//    @Test(expected = MalformedQueryException.class)
//    public void testTriplePatternQueryCreationFailureWithAsk() throws MalformedQueryException {
//        Assert.assertTrue(SparqlQueryIdentifier.createQueryMessage(new SparqlAskQuery(DEFAULT_SPACE_URI,
//                "ASK { ?s ?p . }")) instanceof SparqlAskRequest);
//    }
//
//    @Test(expected = MalformedQueryException.class)
//    public void testTriplePatternQueryCreationFailureWithConstruct() throws MalformedQueryException {
//        Assert
//                .assertTrue(SparqlQueryIdentifier.createQueryMessage(new SparqlConstructQuery(DEFAULT_SPACE_URI,
//                        "CONSTRUCT { ?s ?p ?o } WHERE { <http://toto> <http://aa> http://c }")) instanceof SparqlAskRequest);
//    }
//
//    @Test(expected = MalformedQueryException.class)
//    public void testTriplePatternQueryCreationFailureWithSelect() throws MalformedQueryException {
//        Assert.assertTrue(SparqlQueryIdentifier.createQueryMessage(new SparqlSelectQuery(DEFAULT_SPACE_URI,
//                "SELECT { ?s ?p ?o } WHERE { ?s ?p ?o. }")) instanceof SparqlAskRequest);
//    }
//
//}
