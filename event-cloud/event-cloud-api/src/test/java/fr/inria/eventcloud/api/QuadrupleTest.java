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
package fr.inria.eventcloud.api;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.utils.converters.MakeDeepCopy;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.generators.NodeGenerator;
import fr.inria.eventcloud.api.generators.QuadrupleGenerator;

/**
 * Tests cases associated to the {@link Quadruple} class.
 * 
 * @author lpellegr
 */
public class QuadrupleTest {

    @Test(expected = IllegalArgumentException.class)
    public void testQuadrupleInstanciationWithBlankNode() {
        new Quadruple(
                Node.createAnon(), NodeGenerator.createUri(),
                NodeGenerator.createUri(), NodeGenerator.createUri());
        new Quadruple(
                NodeGenerator.createUri(), Node.createAnon(),
                NodeGenerator.createUri(), NodeGenerator.createUri());
        new Quadruple(
                NodeGenerator.createUri(), NodeGenerator.createUri(),
                Node.createAnon(), NodeGenerator.createUri());
        new Quadruple(
                NodeGenerator.createUri(), NodeGenerator.createUri(),
                NodeGenerator.createUri(), Node.createAnon());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testQuadrupleInstanciationWithVars() {
        new Quadruple(
                Node.ANY, NodeGenerator.createUri(), NodeGenerator.createUri(),
                NodeGenerator.createUri());
        new Quadruple(
                NodeGenerator.createUri(), Node.ANY, NodeGenerator.createUri(),
                NodeGenerator.createUri());
        new Quadruple(
                NodeGenerator.createUri(), NodeGenerator.createUri(), Node.ANY,
                NodeGenerator.createUri());
        new Quadruple(
                NodeGenerator.createUri(), NodeGenerator.createUri(),
                NodeGenerator.createUri(), Node.ANY);

        new Quadruple(
                Node.createVariable("test"), NodeGenerator.createUri(),
                NodeGenerator.createUri(), NodeGenerator.createUri());
        new Quadruple(
                NodeGenerator.createUri(), Node.createVariable("test"),
                NodeGenerator.createUri(), NodeGenerator.createUri());
        new Quadruple(
                NodeGenerator.createUri(), NodeGenerator.createUri(),
                Node.createVariable("test"), NodeGenerator.createUri());
        new Quadruple(
                NodeGenerator.createUri(), NodeGenerator.createUri(),
                NodeGenerator.createUri(), Node.createVariable("test"));
    }

    @Test
    public void testQuadrupleValuesAfterTimestampMethodCall() {
        Quadruple quad = QuadrupleGenerator.createWithLiteral();
        Node graph = quad.getGraph();

        quad.timestamp();
        Assert.assertEquals(
                "The graph value is not the same after timestamping the quadruple",
                graph, quad.getGraph());
    }

    @Test
    public void testSerialization() {
        Quadruple quad =
                new Quadruple(
                        NodeGenerator.createUri(), NodeGenerator.createUri(),
                        NodeGenerator.createUri(),
                        Node.createLiteral("Literal Value"));

        Quadruple newQuad = null;
        try {
            newQuad = (Quadruple) MakeDeepCopy.makeDeepCopy(quad);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(quad.hashCode(), newQuad.hashCode());
        Assert.assertEquals(
                "Quadruples not equals after serialization", quad, newQuad);
    }

    @Test
    public void testSerializationWithTimestampedQuadruple() {
        Quadruple quad = QuadrupleGenerator.createWithLiteral();
        quad.timestamp();

        Quadruple newQuad = null;
        try {
            newQuad = (Quadruple) MakeDeepCopy.makeDeepCopy(quad);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(quad, newQuad);
    }

    @Test(expected = IllegalStateException.class)
    public void testTimestamp() {
        Quadruple quad = QuadrupleGenerator.createWithLiteral();
        // first call allowed
        quad.timestamp();

        Assert.assertTrue(quad.isTimestamped());
        Assert.assertNotNull(quad.getTimestampedGraph());

        // second call not allowed
        quad.timestamp();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTimestampLong() {
        Quadruple quad = QuadrupleGenerator.createWithLiteral();

        long dateTime = System.currentTimeMillis();

        quad.timestamp(dateTime);

        Assert.assertTrue(quad.isTimestamped());
        Assert.assertNotNull(quad.getTimestampedGraph());
        Assert.assertEquals(dateTime, quad.getPublicationDateTime());

        quad.timestamp(-1);
    }

}
