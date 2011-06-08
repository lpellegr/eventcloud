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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.api;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.core.util.converter.MakeDeepCopy;

import com.hp.hpl.jena.graph.Node;

/**
 * Tests cases associated to the {@link Quadruple} class.
 * 
 * @author lpellegr
 */
public class QuadrupleTest {

    private static final String DEFAULT_URI = "http://www.inria.fr";

    @Test(expected = IllegalArgumentException.class)
    public void testQuadrupleInstanciationWithBlankNode() {
        new Quadruple(
                Node.createAnon(), Node.createURI(DEFAULT_URI),
                Node.createURI(DEFAULT_URI), Node.createURI(DEFAULT_URI));
        new Quadruple(
                Node.createURI(DEFAULT_URI), Node.createAnon(),
                Node.createURI(DEFAULT_URI), Node.createURI(DEFAULT_URI));
        new Quadruple(
                Node.createURI(DEFAULT_URI), Node.createURI(DEFAULT_URI),
                Node.createAnon(), Node.createURI(DEFAULT_URI));
        new Quadruple(
                Node.createURI(DEFAULT_URI), Node.createURI(DEFAULT_URI),
                Node.createURI(DEFAULT_URI), Node.createAnon());
    }

    @Test
    public void testSerialization() {
        Quadruple quad =
                new Quadruple(
                        Node.createURI(DEFAULT_URI),
                        Node.createURI(DEFAULT_URI),
                        Node.createURI(DEFAULT_URI),
                        Node.createLiteral("Literal Value"));

        Quadruple newQuad = null;
        try {
            newQuad =
                    (Quadruple) MakeDeepCopy.WithObjectStream.makeDeepCopy(quad);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(quad, newQuad);
    }

}
