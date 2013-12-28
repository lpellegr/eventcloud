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

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.points.Point;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_ANY;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;

/**
 * Represents a semantic coordinate element. This kind of element extends
 * {@link StringCoordinate} and removes some prefix which are specific to
 * semantic data in order to improve the load balancing.
 * 
 * @author lpellegr
 */
public final class SemanticPointFactory {

    private SemanticPointFactory() {

    }

    /**
     * Creates a {@link Point} containing {@link SemanticCoordinate}s from the
     * specified quadruple pattern.
     * 
     * @param quadruplePattern
     *            the quadruple pattern instance to use in order to create the
     *            point.
     * 
     * @return the point which has been created.
     */
    public static Point<SemanticCoordinate> newSemanticCoordinate(QuadruplePattern quadruplePattern) {
        return newSemanticPoint(
                quadruplePattern.getGraph(), quadruplePattern.getSubject(),
                quadruplePattern.getPredicate(), quadruplePattern.getObject());
    }

    /**
     * Creates a {@link Point} containing {@link SemanticCoordinate}s from the
     * specified quadruple.
     * 
     * @param quad
     *            the quadruple instance to use in order to create the point.
     * 
     * @return the point which has been created.
     */
    public static Point<SemanticCoordinate> newSemanticCoordinate(Quadruple quad) {
        return newSemanticPoint(
                quad.getGraph(), quad.getSubject(), quad.getPredicate(),
                quad.getObject());
    }

    /**
     * Creates a {@link Point} containing {@link SemanticCoordinate}s from the
     * specified quadruple components.
     * 
     * @param graph
     *            the graph value.
     * @param subject
     *            the subject value.
     * @param predicate
     *            the predicate value.
     * @param object
     *            the object value.
     * 
     * @return the coordinate which has been created.
     */
    public static Point<SemanticCoordinate> newSemanticPoint(Node graph,
                                                             Node subject,
                                                             Node predicate,
                                                             Node object) {
        return new Point<SemanticCoordinate>(
                createSemanticCoordinateWithVars(graph),
                createSemanticCoordinateWithVars(subject),
                createSemanticCoordinateWithVars(predicate),
                createSemanticCoordinateWithVars(object));
    }

    private static SemanticCoordinate createSemanticCoordinateWithVars(Node n) {
        if (!isVariable(n)) {
            return new SemanticCoordinate(n);
        }

        return null;
    }

    private static boolean isVariable(Node n) {
        return n == null || n.isVariable() || n instanceof Node_ANY;
    }

    public static String toNTripleSyntax(String tripleElt) {
        if (tripleElt.length() > 0 && tripleElt.charAt(0) != '?'
                && tripleElt.charAt(0) != '"') {
            return '<' + tripleElt + '>';
        } else {
            return tripleElt;
        }
    }

    protected static Point<SemanticCoordinate> newSemanticPoint(String value) {
        SemanticCoordinate[] elts =
                new SemanticCoordinate[P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue()];

        for (int i = 0; i < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); i++) {
            elts[i] = new SemanticCoordinate(value);
        }

        return new Point<SemanticCoordinate>(elts);
    }

}
