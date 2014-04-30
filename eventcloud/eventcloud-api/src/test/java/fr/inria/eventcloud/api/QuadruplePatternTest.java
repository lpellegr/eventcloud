/**
 * Copyright (c) 2011-2014 INRIA.
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
package fr.inria.eventcloud.api;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.utils.converters.MakeDeepCopy;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;

/**
 * Tests cases associated to the {@link Quadruple} class.
 * 
 * @author lpellegr
 */
public class QuadruplePatternTest {

    private static final String DEFAULT_URI = "http://www.inria.fr";

    @Test
    public void testNullValues() {
        new QuadruplePattern(null, null, null, null);
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        QuadruplePattern quadPattern =
                new QuadruplePattern(
                        Node.ANY, Node.ANY, NodeFactory.createURI(DEFAULT_URI),
                        NodeFactory.createLiteral("Literal Value"));

        QuadruplePattern quadPatternDeepCopy =
                (QuadruplePattern) MakeDeepCopy.makeDeepCopy(quadPattern);

        Assert.assertEquals(quadPattern, quadPatternDeepCopy);
    }

}
