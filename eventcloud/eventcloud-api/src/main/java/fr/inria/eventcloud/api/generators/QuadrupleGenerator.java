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
package fr.inria.eventcloud.api.generators;

import static com.hp.hpl.jena.graph.Node.createLiteral;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.generators.QuadrupleGeneratorBuilder.ObjectType;

/**
 * QuadrupleGenerator offers several convenient methods to create quadruples
 * randomly by using several criteria (e.g. with or without a literal value,
 * with the specified length for each component, ...).
 * 
 * @author lpellegr
 */
public final class QuadrupleGenerator extends Generator {

    private Node graph = null;

    private String prefix = null;

    private ObjectType objectType = ObjectType.URI;

    private int[] nodeSizes;

    protected QuadrupleGenerator(Node graph, String prefix, int graphSize,
            int subjectSize, int predicateSize, int objectSize,
            ObjectType objectType) {
        this.graph = graph;
        this.prefix = prefix;
        this.objectType = objectType;
        this.nodeSizes =
                new int[] {graphSize, subjectSize, predicateSize, objectSize};
    }

    public Quadruple generate() {
        return generate(
                this.graph, this.prefix, this.nodeSizes, this.objectType);
    }

    private static Quadruple generate(Node graph, String prefix,
                                      int[] nodeSizes, ObjectType objectType) {
        Node[] nodes = new Node[4];

        for (int i = 0; i < nodes.length; i++) {
            if (i == 0 && graph != null) {
                nodes[0] = graph;
            } else {
                nodes[i] = generate(prefix, objectType, nodeSizes[i]);
            }
        }

        return new Quadruple(
                nodes[0], nodes[1], nodes[2], nodes[3], false, true);
    }

    private static Node generate(String prefix, ObjectType objectType,
                                 int length) {
        switch (objectType) {
            case LITERAL:
                if (prefix != null) {
                    return NodeGenerator.randomLiteral(prefix, length);
                }

                return NodeGenerator.randomLiteral(length);
            case LITERAL_OR_URI:
                if (prefix != null) {
                    return NodeGenerator.random(prefix, length);
                }
                return NodeGenerator.random(length);
            case URI:
                if (prefix != null) {
                    return NodeGenerator.randomUri(prefix, length);
                }
                return NodeGenerator.randomUri(length);
            default:
                throw new IllegalArgumentException("Unknown object type: "
                        + objectType);
        }
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
                NodeGenerator.random(DEFAULT_LENGTH), false, true);
    }

    /**
     * Creates a quadruple whose each RDF term is a randomly generated URI or
     * literal.
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
                NodeGenerator.randomUri(length), NodeGenerator.random(length),
                false, true);
    }

    /**
     * Creates a quadruple whose each RDF tern is a randomly generated URI.
     * 
     * @param length
     *            number of characters which are randomly generated for each
     *            node which is created.
     * 
     * @return a randomly generated quadruple.
     */
    public static Quadruple randomWithoutLiteral(int length) {
        return new Quadruple(
                NodeGenerator.randomUri(length),
                NodeGenerator.randomUri(length),
                NodeGenerator.randomUri(length),
                NodeGenerator.randomUri(length), false, true);
    }

    /**
     * Creates a quadruple whose RDF term is a randomly generated URI or
     * literal.
     * 
     * @param prefix
     *            the prefix to append to each RDF term which is randomly
     *            generated.
     * @param length
     *            number of characters which are randomly generated for each
     *            node which is created.
     * 
     * @return a randomly generated quadruple.
     */
    public static Quadruple random(String prefix, int length) {
        return new Quadruple(
                NodeGenerator.randomUri(prefix, length),
                NodeGenerator.randomUri(prefix, length),
                NodeGenerator.randomUri(prefix, length), NodeGenerator.random(
                        prefix, length), false, true);
    }

    /**
     * Creates a quadruple whose RDF term is a randomly generated URI.
     * 
     * @param prefix
     *            the prefix to append to each RDF term which is randomly
     *            generated.
     * @param length
     *            number of characters which are randomly generated for each
     *            node which is created.
     * 
     * @return a randomly generated quadruple.
     */
    public static Quadruple randomWithoutLiteral(String prefix, int length) {
        return new Quadruple(
                NodeGenerator.randomUri(prefix, length),
                NodeGenerator.randomUri(prefix, length),
                NodeGenerator.randomUri(prefix, length),
                NodeGenerator.randomUri(prefix, length), false, true);
    }

    /**
     * Creates a quadruple by using the specified {@code graph} value and whose
     * the other RDF terms are a randomly generated URI or literal.
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
                NodeGenerator.randomUri(length), NodeGenerator.random(length),
                false, true);
    }

    /**
     * Creates a quadruple by using the specified {@code graph} value and whose
     * the other RDF terms are a randomly generated URI.
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
    public static Quadruple randomWithoutLiteral(Node graph, int length) {
        return new Quadruple(
                graph, NodeGenerator.randomUri(length),
                NodeGenerator.randomUri(length),
                NodeGenerator.randomUri(length), false, true);
    }

    /**
     * Creates a quadruple by using the specified {@code graph} value and whose
     * the other components are randomly generated. The object component can be
     * either an URI or a literal.
     * 
     * @param graph
     *            the graph value to use.
     * @param prefix
     *            the prefix to append to each RDF term which is randomly
     *            generated.
     * @param length
     *            number of characters which are randomly generated for each
     *            node which is created.
     * 
     * @return a randomly generated quadruple.
     */
    public static Quadruple random(Node graph, String prefix, int length) {
        return new Quadruple(
                graph, NodeGenerator.randomUri(prefix, length),
                NodeGenerator.randomUri(prefix, length), NodeGenerator.random(
                        prefix, length), false, true);
    }

    /**
     * Creates a quadruple by using the specified {@code graph} value and whose
     * the other components are randomly generated. The object component can be
     * either an URI or a literal.
     * 
     * @param graph
     *            the graph value to use.
     * @param prefix
     *            the prefix to append to each RDF term which is randomly
     *            generated.
     * @param length
     *            number of characters which are randomly generated for each
     *            node which is created.
     * 
     * @return a randomly generated quadruple.
     */
    public static Quadruple randomWithoutLiteral(Node graph, String prefix,
                                                 int length) {
        return new Quadruple(
                graph, NodeGenerator.randomUri(prefix, length),
                NodeGenerator.randomUri(prefix, length),
                NodeGenerator.randomUri(prefix, length), false, true);
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
                NodeGenerator.randomLiteral(DEFAULT_LENGTH), false, true);
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
                NodeGenerator.randomUri(DEFAULT_LENGTH),
                createLiteral(literal), false, true);
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
                NodeGenerator.randomUri(DEFAULT_LENGTH), false, true);
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
