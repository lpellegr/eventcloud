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
 * represented by using {@link Node#ANY} or {@code null} as value.
 * <p>
 * For example, a quadruple {@code Q=(Node.ANY, v1, v2, v3)} means that we want
 * to retrieve all the quadruples that have a graph value which is set to any
 * value and a subject, predicate and object value respectively set to
 * {@code v1}, {@code v2} and {@code v3}.
 * 
 * @author lpellegr
 */
public class QuadruplePattern extends Quadruple {

    private static final long serialVersionUID = 1L;

    /**
     * QuadruplePattern that may be used to retrieve all the quadruples.
     */
    public static final QuadruplePattern ANY = new QuadruplePattern(
            Node.ANY, Node.ANY, Node.ANY, Node.ANY);

    public QuadruplePattern(Node g, Node s, Node p, Node o) {
        super(g, s, p, o, false, true);

        if (g instanceof Node_Variable || s instanceof Node_Variable
                || p instanceof Node_Variable || o instanceof Node_Variable) {
            throw new IllegalArgumentException(
                    "Node_Var is not allowed inside a quadruple pattern, only Node.ANY and null can be used");
        }
    }

    /**
     * Removes the timetamp value associated to the specified quadruple pattern
     * that contains an URI graph value which is timestamped.
     * 
     * @param quadruplePattern
     *            the quadruple pattern to parse.
     * 
     * @return a new quadruple pattern with no timestamp value.
     */
    public static QuadruplePattern removeTimestampFromGraphValue(QuadruplePattern quadruplePattern) {
        if (!quadruplePattern.getGraph().isURI()) {
            return quadruplePattern;
        }

        int timestampSeparatorIndex =
                quadruplePattern.getGraph().getURI().indexOf(
                        Quadruple.META_INFORMATION_SEPARATOR);

        if (timestampSeparatorIndex != -1) {
            return new QuadruplePattern(
                    Node.createURI(quadruplePattern.getGraph()
                            .getURI()
                            .substring(0, timestampSeparatorIndex)),
                    quadruplePattern.getSubject(),
                    quadruplePattern.getPredicate(),
                    quadruplePattern.getObject());
        }

        return quadruplePattern;
    }

}
