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
package fr.inria.eventcloud.api;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.graph.Node;

/**
 * Test cases for class {@link Skolemizator}.
 * 
 * @author lpellegr
 */
public class SkolemizatorTest {

    @Test
    public void testSkolemization() {
        Node graph = Node.createURI("http://example.org/graph");

        Node b1 = Node.createAnon();
        Node b2 = Node.createAnon();

        Quadruple q1 =
                new Quadruple(
                        graph, b1,
                        Node.createURI("http://example.org/properties/p1"),
                        Node.createLiteral("Literal Value"), false, false);
        Quadruple q2 =
                new Quadruple(
                        graph, b1,
                        Node.createURI("http://example.org/properties/p1"), b2,
                        false, false);

        List<Quadruple> skolemizedQuadruples =
                Skolemizator.skolemize(Arrays.asList(q1, q2));

        Assert.assertFalse(containsBlankNodes(skolemizedQuadruples));
    }

    private static <T extends Quadruple> boolean containsBlankNodes(List<T> quadruples) {
        for (Quadruple q : quadruples) {
            if (q.getSubject().isBlank() || q.getObject().isBlank()) {
                return true;
            }
        }

        return false;
    }

}
