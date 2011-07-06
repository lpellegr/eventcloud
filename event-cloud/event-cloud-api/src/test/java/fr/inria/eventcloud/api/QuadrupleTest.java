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
import org.objectweb.proactive.extensions.p2p.structured.utils.converter.MakeDeepCopy;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.generators.NodeGenerator;

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

        Assert.assertEquals(quad, newQuad);
    }

}
