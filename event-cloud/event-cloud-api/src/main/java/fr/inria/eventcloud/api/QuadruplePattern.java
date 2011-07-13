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

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;

/**
 * A QuadruplePattern is {@link Quadruple} where each component value may be
 * either a regular value (the same as a Quadruple) or a variable. A variable is
 * constructed by using {@link Node#createVariable(String)}, {@link Node#ANY} or
 * a {@code null} value.
 * <p>
 * For example, a quadruple {@code Q=(Node.ANY, v1, v2, v3)} means that we want
 * to retrieve all the quadruples that have a graph value which is set to any
 * value and a subject, predicate and object value respectively set to
 * {@code v1}, {@code v2} and {@code v3}.
 * 
 * @author lpellegr
 */
public final class QuadruplePattern extends Quadruple {

    private static final long serialVersionUID = 1L;

    /**
     * QuadruplePattern that may be used to retrieve all the quadruples.
     */
    public static final QuadruplePattern ANY = new QuadruplePattern(
            Node.ANY, Node.ANY, Node.ANY, Node.ANY);

    public QuadruplePattern(Node g, Node s, Node p, Node o) {
        super(replaceNullWithVarNode(g), replaceNullWithVarNode(s),
                replaceNullWithVarNode(p), replaceNullWithVarNode(o), false);
    }

    private final static Node replaceNullWithVarNode(Node node) {
        if (node == null || node instanceof Node_Variable) {
            return Node.ANY;
        }

        return node;
    }

}