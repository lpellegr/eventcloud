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
package fr.inria.eventcloud.api.generators;

import static com.hp.hpl.jena.graph.Node.createLiteral;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Quadruple;

/**
 * QuadrupleGenerator offers several convenient methods to create quadruples
 * randomly by using several criteria (e.g. with or without a literal value,
 * with the specified length for each component, ...).
 * 
 * @author lpellegr
 */
public final class QuadrupleGenerator extends Generator {

    private QuadrupleGenerator() {

    }

    /**
     * Creates a quadruple whose each component is randomly generated. The
     * object component can be either an URI or a literal.
     * 
     * @return a randomly generated quadruple.
     */
    public static Quadruple random() {
        return random(DEFAULT_LENGTH);
    }

    /**
     * Creates a quadruple by using the specified {@code graph} value and whose
     * the other components are randomly generated. The object component can be
     * either an URI or a literal.
     * 
     * @param graph
     *            the graph value to use.
     * 
     * @return a randomly generated quadruple.
     */
    public static Quadruple random(Node graph) {
        return new Quadruple(
                graph, NodeGenerator.randomUri(DEFAULT_LENGTH),
                NodeGenerator.randomUri(DEFAULT_LENGTH),
                NodeGenerator.random(DEFAULT_LENGTH));
    }

    /**
     * Creates a quadruple whose each component is randomly generated. The
     * object component can be either an URI or a literal.
     * 
     * @param length
     *            number of characters which are randomly generated for each
     *            node which is created.
     * 
     * @return a randomly generated quadruple.
     */
    public static Quadruple random(int length) {
        return new Quadruple(
                NodeGenerator.randomUri(length),
                NodeGenerator.randomUri(length),
                NodeGenerator.randomUri(length), NodeGenerator.random(length));
    }

    /**
     * Creates a quadruple by using the specified {@code graph} value and whose
     * the other components are randomly generated. The object component can be
     * either an URI or a literal.
     * 
     * @param graph
     *            the graph value to use.
     * 
     * @param length
     *            number of characters which are randomly generated for each
     *            node which is created.
     * 
     * @return a randomly generated quadruple.
     */
    public static Quadruple random(Node graph, int length) {
        return new Quadruple(
                graph, NodeGenerator.randomUri(length),
                NodeGenerator.randomUri(length), NodeGenerator.random(length));
    }

    /**
     * Creates a quadruple with random node values and whose the object value is
     * a literal.
     * 
     * @return a randomly generated quadruple.
     */
    public static Quadruple randomWithLiteral() {
        return new Quadruple(
                NodeGenerator.randomUri(DEFAULT_LENGTH),
                NodeGenerator.randomUri(DEFAULT_LENGTH),
                NodeGenerator.randomUri(DEFAULT_LENGTH),
                NodeGenerator.randomLiteral(DEFAULT_LENGTH));
    }

    /**
     * Creates a quadruple with random node values and whose the object value is
     * a literal with the specified {@code literal} value.
     * 
     * @return a randomly generated quadruple.
     */
    public static Quadruple randomWithLiteral(String literal) {
        return new Quadruple(
                NodeGenerator.randomUri(DEFAULT_LENGTH),
                NodeGenerator.randomUri(DEFAULT_LENGTH),
                NodeGenerator.randomUri(DEFAULT_LENGTH), createLiteral(literal));
    }

    /**
     * Creates a quadruple with random node values and whose the object value is
     * not a literal but an URI.
     * 
     * @return a randomly generated quadruple.
     */
    public static Quadruple randomWithoutLiteral() {
        return new Quadruple(
                NodeGenerator.randomUri(DEFAULT_LENGTH),
                NodeGenerator.randomUri(DEFAULT_LENGTH),
                NodeGenerator.randomUri(DEFAULT_LENGTH),
                NodeGenerator.randomUri(DEFAULT_LENGTH));
    }

    public static void main(String[] args) {
        System.out.println("random");
        System.out.println(random());
        System.out.println("randomNode");
        System.out.println(random(NodeGenerator.randomUri()));
        System.out.println("randomInt");
        System.out.println(random(5));
        System.out.println("randomNodeInt");
        System.out.println(random(NodeGenerator.randomUri(), 5));
        System.out.println("randomWithLiteral");
        System.out.println(randomWithLiteral());
        System.out.println("randomWithLiteralString");
        System.out.println(randomWithLiteral("test"));
        System.out.println("randomWithoutLiteral");
        System.out.println(randomWithoutLiteral());
    }

}
