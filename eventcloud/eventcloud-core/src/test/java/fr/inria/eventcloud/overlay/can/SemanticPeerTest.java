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
package fr.inria.eventcloud.overlay.can;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.exceptions.MalformedSparqlQueryException;
import fr.inria.eventcloud.api.generators.NodeGenerator;
import fr.inria.eventcloud.api.generators.QuadrupleGenerator;
import fr.inria.eventcloud.api.responses.SparqlAskResponse;
import fr.inria.eventcloud.deployment.JunitByClassEventCloudDeployer;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.overlay.SemanticPeer;

/**
 * Tests associated to semantic operations provided by {@link SemanticPeer}.
 * 
 * @author lpellegr
 */
public class SemanticPeerTest extends JunitByClassEventCloudDeployer {

    private static final Logger log =
            LoggerFactory.getLogger(SemanticPeerTest.class);

    public SemanticPeerTest() {
        super(1, 10);
    }

    @Test
    public void testAddQuadruple() {
        Set<Quadruple> quadruples = new HashSet<Quadruple>();

        Quadruple quadruple;
        for (int i = 0; i < 100; i++) {
            quadruple = QuadrupleGenerator.random();
            quadruples.add(quadruple);
            super.getPutGetProxy().add(quadruple);
        }

        List<Quadruple> quadruplesFound =
                super.getPutGetProxy().find(QuadruplePattern.ANY);

        for (Quadruple quad : quadruplesFound) {
            Assert.assertTrue(quadruples.contains(quad));
        }

        Assert.assertEquals(quadruples.size(), quadruplesFound.size());
    }

    @Test
    public void testAddCollectionQuadruples() {
        Set<Quadruple> quadruples = new HashSet<Quadruple>();

        for (int i = 0; i < 100; i++) {
            quadruples.add(QuadrupleGenerator.random());
        }

        super.getPutGetProxy().add(quadruples);

        List<Quadruple> quadruplesFound =
                super.getPutGetProxy().find(QuadruplePattern.ANY);

        for (Quadruple quad : quadruplesFound) {
            Assert.assertTrue(quadruples.contains(quad));
        }

        Assert.assertEquals(quadruples.size(), quadruplesFound.size());
    }

    @Test
    public void testContainsQuadruple() {
        Quadruple quadToCheck = QuadrupleGenerator.random();
        Assert.assertFalse(super.getPutGetProxy().contains(quadToCheck));

        super.getPutGetProxy().add(quadToCheck);
        Assert.assertTrue(super.getPutGetProxy().contains(quadToCheck));
    }

    @Test
    public void testCountQuadruplePattern() {
        Assert.assertEquals(0, super.getPutGetProxy().count(
                QuadruplePattern.ANY));

        for (int i = 0; i < 10; i++) {
            super.getPutGetProxy().add(QuadrupleGenerator.random());
        }

        Assert.assertEquals(10, super.getPutGetProxy().count(
                QuadruplePattern.ANY));

        Node graph = NodeFactory.createURI("http://example.org/graph");
        for (int i = 0; i < 5; i++) {
            super.getPutGetProxy().add(QuadrupleGenerator.random(graph));
        }

        Assert.assertEquals(5, super.getPutGetProxy().count(
                new QuadruplePattern(graph, Node.ANY, Node.ANY, Node.ANY)));

        Assert.assertEquals(15, super.getPutGetProxy().count(
                QuadruplePattern.ANY));
    }

    @Test
    public void testDeleteQuadruple() {
        Quadruple quad = QuadrupleGenerator.random();
        super.getPutGetProxy().add(quad);
        Assert.assertTrue(super.getPutGetProxy().contains(quad));

        super.getPutGetProxy().delete(quad);
        Assert.assertFalse(super.getPutGetProxy().contains(quad));
    }

    @Test
    public void testDeleteCollectionQuadruples() {
        Set<Quadruple> quadruples = new HashSet<Quadruple>();

        for (int i = 0; i < 100; i++) {
            quadruples.add(QuadrupleGenerator.random());
        }

        super.getPutGetProxy().delete(new ArrayList<Quadruple>(quadruples));

        Assert.assertEquals(0, super.getPutGetProxy()
                .find(QuadruplePattern.ANY)
                .size());
    }

    @Test
    public void testDeleteQuadruples() {
        Set<Quadruple> quadruples = new HashSet<Quadruple>();

        Quadruple quadruple;
        Node graphValue = NodeGenerator.randomUri();
        for (int i = 0; i < 100; i++) {
            if (i < 20) {
                // some nodes with the same graph value
                quadruple = QuadrupleGenerator.random(graphValue);
            } else {
                quadruple = QuadrupleGenerator.random();
            }
            quadruples.add(quadruple);
            super.getPutGetProxy().add(quadruple);
        }

        List<Quadruple> quadruplesRemoved =
                super.getPutGetProxy().delete(
                        new QuadruplePattern(
                                graphValue, Node.ANY, Node.ANY, Node.ANY));

        quadruplesRemoved = PAFuture.getFutureValue(quadruplesRemoved);

        List<Quadruple> quadruplesFound =
                super.getPutGetProxy().find(QuadruplePattern.ANY);

        Assert.assertEquals(80, quadruplesFound.size());

        for (Quadruple quad : quadruplesFound) {
            Assert.assertTrue(quadruples.contains(quad));
        }

        SetView<Quadruple> expectedQuadruplesRemoved =
                Sets.difference(quadruples, new HashSet<Quadruple>(
                        quadruplesFound));

        Assert.assertEquals(20, expectedQuadruplesRemoved.size());
        Assert.assertEquals(
                expectedQuadruplesRemoved.size(), quadruplesRemoved.size());

        for (Quadruple q : quadruplesRemoved) {
            Assert.assertTrue(expectedQuadruplesRemoved.contains(q));
        }
    }

    @Test
    public void testExecuteSparqlAsk() throws MalformedSparqlQueryException {
        Assert.assertFalse(super.getPutGetProxy().executeSparqlAsk(
                "ASK { GRAPH ?g { ?s ?p ?o } }").getResult());

        Quadruple quad =
                QuadrupleGenerator.random(NodeFactory.createURI("http://sparql.org"));
        super.getPutGetProxy().add(quad);

        Assert.assertTrue(super.getPutGetProxy().executeSparqlAsk(
                "ASK { GRAPH ?g { ?s ?p ?o } }").getResult());

        Assert.assertTrue(super.getPutGetProxy().executeSparqlAsk(
                "ASK { GRAPH ?g { <" + quad.getSubject().toString()
                        + "> ?p ?o } }").getResult());

        Assert.assertFalse(super.getPutGetProxy().executeSparqlAsk(
                "ASK { GRAPH <http://sparql.com> { ?s ?p ?o } }").getResult());
    }

    @Test
    public void testExecuteSparqlConstruct()
            throws MalformedSparqlQueryException {
        Set<Quadruple> quadruples = new HashSet<Quadruple>();

        Quadruple quadruple;
        for (int i = 0; i < 100; i++) {
            quadruple = QuadrupleGenerator.random();
            quadruples.add(quadruple);
            super.getPutGetProxy().add(quadruple);
        }

        Assert.assertEquals(
                100,
                super.getPutGetProxy()
                        .executeSparqlConstruct(
                                "CONSTRUCT { ?s ?p ?o } WHERE { GRAPH ?g { ?s ?p ?o } }")
                        .getResult()
                        .size());
    }

    @Test
    public void testExecuteSparqlSelect1() throws MalformedSparqlQueryException {
        Set<Quadruple> quadruples = new HashSet<Quadruple>();

        Quadruple quadruple;
        for (int i = 0; i < 100; i++) {
            quadruple = QuadrupleGenerator.random();
            quadruples.add(quadruple);
            super.getPutGetProxy().add(quadruple);
        }

        ResultSet resultSet =
                super.getPutGetProxy()
                        .executeSparqlSelect(
                                "SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { ?s ?p ?o } }")
                        .getResult();

        Binding binding = null;
        Quadruple quad = null;

        Var vars[] = new Var[resultSet.getResultVars().size()];
        for (int i = 0; i < resultSet.getResultVars().size(); i++) {
            vars[i] = Var.alloc(resultSet.getResultVars().get(i));
        }

        int count = 0;
        while (resultSet.hasNext()) {
            binding = resultSet.nextBinding();
            quad =
                    new Quadruple(
                            binding.get(vars[0]), binding.get(vars[1]),
                            binding.get(vars[2]), binding.get(vars[3]));
            Assert.assertTrue(quadruples.contains(quad));
            count++;
        }

        Assert.assertEquals(100, count);
    }

    @Test
    public void testExecuteSparqlSelect2() throws MalformedSparqlQueryException {
        for (int i = 0; i < 100; i++) {
            super.getPutGetProxy().add(QuadrupleGenerator.random());
        }

        ResultSet resultSet =
                super.getPutGetProxy()
                        .executeSparqlSelect(
                                "SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { ?s ?p ?o } } LIMIT 10")
                        .getResult();

        assertEquals(resultSet, 10);
    }

    @Test
    public void testExecuteSparqlSelect3()
            throws MalformedSparqlQueryException, ProActiveException,
            EventCloudIdNotManaged {
        for (int i = 0; i < 10; i++) {
            super.getPublishProxy().publish(QuadrupleGenerator.random());
        }

        ResultSet resultSet =
                super.getPutGetProxy()
                        .executeSparqlSelect(
                                "PREFIX eventcloud: <http://eventcloud.inria.fr/function#> SELECT ?g { GRAPH ?g { ?s ?p ?o } }")
                        .getResult();

        while (resultSet.hasNext()) {
            QuerySolution binding = resultSet.next();
            Assert.assertTrue(binding.get("g").asNode().getURI().contains(
                    Quadruple.PUBLICATION_TIME_SEPARATOR));
        }

        resultSet =
                super.getPutGetProxy()
                        .executeSparqlSelect(
                                "PREFIX eventcloud: <http://eventcloud.inria.fr/function#> SELECT ?shortGraph { GRAPH ?g { ?s ?p ?o . BIND(eventcloud:removeMetadata(?g) AS ?shortGraph) } }")
                        .getResult();

        while (resultSet.hasNext()) {
            QuerySolution binding = resultSet.next();
            Assert.assertFalse(binding.get("shortGraph")
                    .asNode()
                    .getURI()
                    .contains(Quadruple.PUBLICATION_TIME_SEPARATOR));
        }
    }

    @Test
    public void testExecuteSparqlSelect4() throws MalformedSparqlQueryException {
        for (int i = 0; i < 5; i++) {
            super.getPutGetProxy().add(QuadrupleGenerator.random());
        }

        Quadruple quadruple = QuadrupleGenerator.random();
        for (int i = 0; i < 5; i++) {
            super.getPutGetProxy().add(quadruple);
        }

        ResultSet resultSet =
                super.getPutGetProxy()
                        .executeSparqlSelect(
                                "SELECT DISTINCT ?g ?s ?p ?o WHERE { GRAPH ?g { ?s ?p ?o } } LIMIT 10")
                        .getResult();

        assertEquals(resultSet, 6);
    }

    @Test
    public void testExecuteSparqlSelect5() throws MalformedSparqlQueryException {
        for (int i = 0; i < 5; i++) {
            super.getPutGetProxy().add(QuadrupleGenerator.randomWithLiteral());
        }

        Quadruple quadruple = QuadrupleGenerator.randomWithLiteral();
        for (int i = 0; i < 5; i++) {
            super.getPutGetProxy().add(quadruple);
        }

        ResultSet resultSet =
                super.getPutGetProxy()
                        .executeSparqlSelect(
                                "SELECT DISTINCT ?g ?s ?p ?o WHERE { GRAPH ?g { ?s ?p ?o } } ORDER BY DESC(?o) LIMIT 10")
                        .getResult();

        assertEquals(resultSet, 6);

        int i = 0;
        String lastLiteralValue = null;

        while (resultSet.hasNext()) {
            QuerySolution binding = resultSet.next();
            String currentLiteralValue =
                    binding.get("o").asNode().getLiteralLexicalForm();

            if (i > 0) {
                Assert.assertTrue(lastLiteralValue.compareTo(currentLiteralValue) >= 0);
            }

            lastLiteralValue = currentLiteralValue;
            i++;
        }
    }

    @Test
    public void testExecuteSparqlWithEmptyNetwork()
            throws MalformedSparqlQueryException {
        Assert.assertFalse(super.getPutGetProxy().executeSparqlAsk(
                "ASK { GRAPH ?g { ?s ?p ?o } }").getResult());

        Assert.assertEquals(
                0,
                super.getPutGetProxy()
                        .executeSparqlConstruct(
                                "CONSTRUCT { ?s ?p ?o } WHERE { GRAPH ?g { ?s ?p ?o } }")
                        .getResult()
                        .size());

        Assert.assertFalse(super.getPutGetProxy()
                .executeSparqlSelect(
                        "SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { ?s ?p ?o } }")
                .getResult()
                .hasNext());
    }

    @Test
    public void testExecuteSparqlWithConjunctionsAndOneJoinVariable()
            throws MalformedSparqlQueryException {
        Node commonURI = NodeGenerator.randomUri();
        Node graphValue = NodeGenerator.randomUri();

        // two quadruples share the same value respectively for the object
        // and subject component and they have the same graphValue
        List<Quadruple> quadruples =
                ImmutableList.of(
                        new Quadruple(
                                graphValue, NodeGenerator.randomUri(),
                                NodeGenerator.randomUri(), commonURI),
                        new Quadruple(
                                graphValue, commonURI,
                                NodeGenerator.randomUri(),
                                NodeGenerator.random()),
                        QuadrupleGenerator.random(),
                        QuadrupleGenerator.random(),
                        QuadrupleGenerator.random());

        super.getPutGetProxy().add(quadruples);

        // test with ask query form
        Assert.assertTrue(super.getPutGetProxy().executeSparqlAsk(
                "ASK { GRAPH ?a { ?b ?c <" + commonURI.toString() + "> . <"
                        + commonURI.toString() + "> ?e ?f } }").getResult());

        // test with construct query form
        Assert.assertEquals(2, super.getPutGetProxy().executeSparqlConstruct(
                "CONSTRUCT { ?a ?b ?c . ?a ?d ?e } WHERE { GRAPH ?a { ?b ?c <"
                        + commonURI.toString() + "> . <" + commonURI.toString()
                        + "> ?d ?e } }").getResult().size());

        // test with select query form
        Assert.assertTrue(super.getPutGetProxy()
                .executeSparqlSelect(
                        "SELECT ?a WHERE { GRAPH ?a { ?b ?c <"
                                + commonURI.toString() + "> . <"
                                + commonURI.toString() + "> ?d ?e } }")
                .getResult()
                .hasNext());
    }

    @Test
    public void testMeasurementsReturnedBySparqlQuery()
            throws MalformedSparqlQueryException {
        SparqlAskResponse response =
                super.getPutGetProxy().executeSparqlAsk(
                        "ASK { GRAPH ?g { ?s ?p ?o } }");

        log.debug(
                "Measurements returned for a SPARQL ASK query with no data: latency={}, queryDatastoreTime={}, nbInboundHop={}, nbOutboundHop={}",
                new Object[] {
                        response.getLatency(),
                        response.getQueryDatastoreTime(),
                        response.getInboundHopCount(),
                        response.getOutboundHopCount()});

        Assert.assertTrue(
                "Latency is not greater than 0", response.getLatency() > 0);
        Assert.assertTrue(
                "The time to query the datastore is not greater than 0",
                response.getQueryDatastoreTime() > 0);
        Assert.assertEquals(
                "The number of inbound hop count is not equals to the number of outbound hop count",
                response.getInboundHopCount(), response.getOutboundHopCount());
    }

    private static void assertEquals(ResultSet resultSet, int expectedSize) {
        Assert.assertEquals(expectedSize, size(resultSet));
    }

    private static int size(ResultSet resultSet) {
        int count = 0;
        while (resultSet.hasNext()) {
            resultSet.next();
            count++;
        }

        return count;
    }

}
