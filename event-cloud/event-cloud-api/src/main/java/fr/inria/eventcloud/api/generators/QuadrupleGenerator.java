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
import static fr.inria.eventcloud.api.generators.NodeGenerator.createLiteral;
import static fr.inria.eventcloud.api.generators.NodeGenerator.createNode;
import static fr.inria.eventcloud.api.generators.NodeGenerator.createUri;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Quadruple;

/**
 * QuadrupleGenerator offers several convenient methods to create a quadruple
 * with several criteria (e.g. with or without a literal value, with the
 * specified length for each component, etc).
 * 
 * @author lpellegr
 */
public final class QuadrupleGenerator extends Generator {

    private static final int DEFAULT_LENGTH = 10;

    private QuadrupleGenerator() {

    }

    public static Quadruple create() {
        return create(DEFAULT_LENGTH, DEFAULT_LENGTH);
    }

    public static Quadruple create(Node graphValue) {
        return create(graphValue, DEFAULT_LENGTH, DEFAULT_LENGTH);
    }

    public static Quadruple create(int exactLength) {
        return new Quadruple(
                createUri(exactLength), createUri(exactLength),
                createUri(exactLength), createNode(exactLength));
    }

    public static Quadruple create(Node graphValue, int exactLength) {
        return new Quadruple(
                graphValue, createUri(exactLength), createUri(exactLength),
                createNode(exactLength));
    }

    public static Quadruple create(int minLength, int maxLength) {
        return new Quadruple(
                createUri(minLength, maxLength),
                createUri(minLength, maxLength),
                createUri(minLength, maxLength), createNode(
                        minLength, maxLength));
    }

    public static Quadruple create(Node graphValue, int minLength, int maxLength) {
        return new Quadruple(
                graphValue, createUri(minLength, maxLength), createUri(
                        minLength, maxLength), createNode(minLength, maxLength));
    }

    public static Quadruple createWithLiteral() {
        return new Quadruple(
                createUri(DEFAULT_LENGTH, DEFAULT_LENGTH), createUri(
                        DEFAULT_LENGTH, DEFAULT_LENGTH), createUri(
                        DEFAULT_LENGTH, DEFAULT_LENGTH), createLiteral(
                        DEFAULT_LENGTH, DEFAULT_LENGTH));
    }

    public static Quadruple createWithLiteral(String literalValue) {
        return new Quadruple(
                createUri(DEFAULT_LENGTH, DEFAULT_LENGTH), createUri(
                        DEFAULT_LENGTH, DEFAULT_LENGTH), createUri(
                        DEFAULT_LENGTH, DEFAULT_LENGTH),
                createLiteral(literalValue));
    }

    public static Quadruple createWithoutLiteral() {
        return new Quadruple(
                createUri(DEFAULT_LENGTH, DEFAULT_LENGTH), createUri(
                        DEFAULT_LENGTH, DEFAULT_LENGTH), createUri(
                        DEFAULT_LENGTH, DEFAULT_LENGTH), createUri(
                        DEFAULT_LENGTH, DEFAULT_LENGTH));
    }

}
