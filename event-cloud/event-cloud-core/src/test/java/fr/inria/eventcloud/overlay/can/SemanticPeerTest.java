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
package fr.inria.eventcloud.overlay.can;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.generators.NodeGenerator;
import fr.inria.eventcloud.api.generators.QuadrupleGenerator;
import fr.inria.eventcloud.api.responses.SparqlAskResponse;
import fr.inria.eventcloud.deployment.JunitByClassEventCloudDeployer;
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
        super(10);
    }

    @Test
    public void testAddQuadruple() {
        Set<Quadruple> quadruples = new HashSet<Quadruple>();

        Quadruple quadruple;
        for (int i = 0; i < 100; i++) {
            quadruple = QuadrupleGenerator.create();
            quadruples.add(quadruple);
            super.getRandomSemanticPeer().add(quadruple);
        }

        Collection<Quadruple> quadruplesFound =
                super.getRandomSemanticPeer().find(QuadruplePattern.ANY);

        for (Quadruple quad : quadruplesFound) {
            Assert.assertTrue(quadruples.contains(quad));
        }

        Assert.assertEquals(quadruples.size(), quadruplesFound.size());
    }

    @Test
    public void testAddCollectionQuadruples() {
        Set<Quadruple> quadruples = new HashSet<Quadruple>();

        for (int i = 0; i < 100; i++) {
            quadruples.add(QuadrupleGenerator.create());
        }

        super.getRandomSemanticPeer()
                .add(new Collection<Quadruple>(quadruples));

        Collection<Quadruple> quadruplesFound =
                super.getRandomSemanticPeer().find(QuadruplePattern.ANY);

        for (Quadruple quad : quadruplesFound) {
            Assert.assertTrue(quadruples.contains(quad));
        }

        Assert.assertEquals(quadruples.size(), quadruplesFound.size());
    }

    @Test
    public void testContainsQuadruple() {
        Quadruple quadToCheck = QuadrupleGenerator.create();
        Assert.assertFalse(super.getRandomSemanticPeer().contains(quadToCheck));

        super.getRandomSemanticPeer().add(quadToCheck);
        Assert.assertTrue(super.getRandomSemanticPeer().contains(quadToCheck));
    }

    @Test
    public void testDeleteQuadruple() {
        Quadruple quad = QuadrupleGenerator.create();
        super.getRandomSemanticPeer().add(quad);
        Assert.assertTrue(super.getRandomSemanticPeer().contains(quad));

        super.getRandomSemanticPeer().delete(quad);
        Assert.assertFalse(super.getRandomSemanticPeer().contains(quad));
    }

    @Test
    public void testDeleteCollectionQuadruples() {
        Set<Quadruple> quadruples = new HashSet<Quadruple>();

        for (int i = 0; i < 100; i++) {
            quadruples.add(QuadrupleGenerator.create());
        }

        super.getRandomSemanticPeer().delete(
                new Collection<Quadruple>(quadruples));

        Assert.assertEquals(0, super.getRandomSemanticPeer().find(
                QuadruplePattern.ANY).size());
    }

    @Test
    public void testDeleteQuadruples() {
        Set<Quadruple> quadruples = new HashSet<Quadruple>();

        Quadruple quadruple;
        Node graphValue = NodeGenerator.createNode();
        for (int i = 0; i < 100; i++) {
            if (i < 20) {
                // some nodes with the same graph value
                quadruple = QuadrupleGenerator.create(graphValue);
            } else {
                quadruple = QuadrupleGenerator.create();
            }
            quadruples.add(quadruple);
            super.getRandomSemanticPeer().add(quadruple);
        }

        super.getRandomSemanticPeer().delete(
                new QuadruplePattern(graphValue, Node.ANY, Node.ANY, Node.ANY));

        Collection<Quadruple> quadruplesFound =
                super.getRandomSemanticPeer().find(QuadruplePattern.ANY);

        Assert.assertEquals(80, quadruplesFound.size());

        for (Quadruple quad : quadruplesFound) {
            Assert.assertTrue(quadruples.contains(quad));
        }
    }

    @Test
    public void testExecuteSparqlAsk() {
        Assert.assertFalse(super.getRandomSemanticPeer().executeSparqlAsk(
                "ASK { GRAPH ?g { ?s ?p ?o } }").getResult());

        Quadruple quad =
                QuadrupleGenerator.create(Node.createURI("http://sparql.org"));
        super.getRandomSemanticPeer().add(quad);

        Assert.assertTrue(super.getRandomSemanticPeer().executeSparqlAsk(
                "ASK { GRAPH ?g { ?s ?p ?o } }").getResult());

        Assert.assertTrue(super.getRandomSemanticPeer().executeSparqlAsk(
                "ASK { GRAPH ?g { <" + quad.getSubject().toString()
                        + "> ?p ?o } }").getResult());

        Assert.assertFalse(super.getRandomSemanticPeer().executeSparqlAsk(
                "ASK { GRAPH <http://sparql.com> { ?s ?p ?o } }").getResult());
    }

    @Test
    public void testExecuteSparqlConstruct() {
        Set<Quadruple> quadruples = new HashSet<Quadruple>();

        Quadruple quadruple;
        for (int i = 0; i < 100; i++) {
            quadruple = QuadrupleGenerator.create();
            quadruples.add(quadruple);
            super.getRandomSemanticPeer().add(quadruple);
        }

        Assert.assertEquals(
                100,
                super.getRandomSemanticPeer()
                        .executeSparqlConstruct(
                                "CONSTRUCT { ?s ?p ?o } WHERE { GRAPH ?g { ?s ?p ?o } }")
                        .getResult()
                        .size());
    }

    @Test
    public void testExecuteSparqlSelect() {
        Set<Quadruple> quadruples = new HashSet<Quadruple>();

        Quadruple quadruple;
        for (int i = 0; i < 100; i++) {
            quadruple = QuadrupleGenerator.create();
            quadruples.add(quadruple);
            super.getRandomSemanticPeer().add(quadruple);
        }

        ResultSet resultSet =
                super.getRandomSemanticPeer()
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
    public void testExecuteSparqlWithEmptyNetwork() {
        Assert.assertFalse(super.getRandomSemanticPeer().executeSparqlAsk(
                "ASK { GRAPH ?g { ?s ?p ?o } }").getResult());

        Assert.assertEquals(
                0,
                super.getRandomSemanticPeer()
                        .executeSparqlConstruct(
                                "CONSTRUCT { ?s ?p ?o } WHERE { GRAPH ?g { ?s ?p ?o } }")
                        .getResult()
                        .size());

        Assert.assertFalse(super.getRandomSemanticPeer()
                .executeSparqlSelect(
                        "SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { ?s ?p ?o } }")
                .getResult()
                .hasNext());
    }

    @Test
    public void testExecuteSparqlWithConjunctionsAndOneJoinVariable() {
        Node commonURI = NodeGenerator.createUri();
        Node graphValue = NodeGenerator.createUri();

        // two quadruples share the same value respectively for the object and
        // subject component and they have the same graphValue
        Quadruple[] quads =
                {
                        new Quadruple(
                                graphValue, NodeGenerator.createUri(),
                                NodeGenerator.createUri(), commonURI),
                        new Quadruple(
                                graphValue, commonURI,
                                NodeGenerator.createUri(),
                                NodeGenerator.createUri()),
                        QuadrupleGenerator.create(),
                        QuadrupleGenerator.create(),
                        QuadrupleGenerator.create()};

        for (Quadruple quad : quads) {
            super.getRandomSemanticPeer().add(quad);
        }

        // test with ask query form
        Assert.assertTrue(super.getRandomSemanticPeer().executeSparqlAsk(
                "ASK { GRAPH ?a { ?b ?c <" + commonURI.toString() + "> . <"
                        + commonURI.toString() + "> ?e ?f } }").getResult());

        // test with construct query form
        Assert.assertEquals(2, super.getRandomSemanticPeer()
                .executeSparqlConstruct(
                        "CONSTRUCT { ?a ?b ?c . ?a ?d ?e } WHERE { GRAPH ?a { ?b ?c <"
                                + commonURI.toString() + "> . <"
                                + commonURI.toString() + "> ?d ?e } }")
                .getResult()
                .size());

        // test with select query form
        Assert.assertTrue(super.getRandomSemanticPeer()
                .executeSparqlSelect(
                        "SELECT ?a WHERE { GRAPH ?a { ?b ?c <"
                                + commonURI.toString() + "> . <"
                                + commonURI.toString() + "> ?d ?e } }")
                .getResult()
                .hasNext());
    }

    @Test
    public void testMeasurementsReturnedBySparqlQuery() {
        SparqlAskResponse response =
                super.getRandomSemanticPeer().executeSparqlAsk(
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
        // TODO: use System.nanoTime?
        // Assert.assertTrue(
        // "The time to query the datastore is not greater than 0",
        // response.getQueryDatastoreTime() > 0);
        Assert.assertEquals(
                "The number of inbound hop count is not equals to the number of outbound hop count",
                response.getInboundHopCount(), response.getOutboundHopCount());
    }

}
