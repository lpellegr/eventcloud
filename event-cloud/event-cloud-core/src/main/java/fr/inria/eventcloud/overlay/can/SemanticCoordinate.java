/**
 * Copyright (c) 2011-2012 INRIA.
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

import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_ANY;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;

/**
 * Represents a semantic coordinate element. This kind of element extends
 * {@link StringElement} and removes some prefix which are specific to semantic
 * data in order to improve the load balancing.
 * 
 * @author lpellegr
 */
public class SemanticCoordinate extends StringCoordinate {

    private static final long serialVersionUID = 1L;

    public SemanticCoordinate(SemanticElement... elts) {
        super(elts);
    }

    /**
     * Creates a {@link SemanticCoordinate} from the specified quadruple
     * pattern.
     * 
     * @param quadruplePattern
     *            the quadruple pattern instance to use in order to create the
     *            coordinate.
     * 
     * @return the coordinate which has been created.
     */
    public static SemanticCoordinate create(QuadruplePattern quadruplePattern) {
        return create(
                quadruplePattern.getGraph(), quadruplePattern.getSubject(),
                quadruplePattern.getPredicate(), quadruplePattern.getObject());
    }

    /**
     * Creates a {@link SemanticCoordinate} from the specified quadruple.
     * 
     * @param quad
     *            the quad instance to use in order to create the coordinate.
     * 
     * @return the coordinate which has been created.
     */
    public static SemanticCoordinate create(Quadruple quad) {
        return create(
                quad.getGraph(), quad.getSubject(), quad.getPredicate(),
                quad.getObject());
    }

    /**
     * Creates a {@link StringCoordinate} from the specified quadruple
     * components.
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
    public static SemanticCoordinate create(Node graph, Node subject,
                                            Node predicate, Node object) {
        // if the literal value contains an empty String we have to decide which
        // constraint is associated to this particular case where there is no
        // character to compare.
        if (object.isLiteral() && object.getLiteralLexicalForm().isEmpty()) {
            object =
                    Node.createLiteral(SemanticElement.EMPTY_STRING_ROUTING_CHARACTER);
        }

        return new SemanticCoordinate(
                createSemanticElementWithVars(graph),
                createSemanticElementWithVars(subject),
                createSemanticElementWithVars(predicate),
                createSemanticElementWithVars(object));
    }

    private static SemanticElement createSemanticElementWithVars(Node n) {
        if (!isVariable(n)) {
            return new SemanticElement(n);
        }

        return null;
    }

    private static boolean isVariable(Node n) {
        return n == null || n.isVariable() || n instanceof Node_ANY;
    }

    public static String toNTripleSyntax(String tripleElt) {
        if (tripleElt.length() > 0 && tripleElt.charAt(0) != '?'
                && tripleElt.charAt(0) != '"') {
            StringBuilder triple = new StringBuilder("<");
            triple.append(tripleElt);
            triple.append('>');
            return triple.toString();
        } else {
            return tripleElt;
        }
    }

}
