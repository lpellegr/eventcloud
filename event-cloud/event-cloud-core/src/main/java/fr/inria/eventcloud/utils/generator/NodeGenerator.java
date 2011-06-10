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
package fr.inria.eventcloud.utils.generator;

import org.objectweb.proactive.core.util.ProActiveRandom;

import com.hp.hpl.jena.graph.Node;

/**
 * Utility class that defines some convenient methods to create {@link Node}s.
 * 
 * @author lpellegr
 */
public class NodeGenerator {

    public static final char[][] DEFAULT_BOUNDS = new char[][] {
            {'0', '9'}, {'A', 'Z'}, {'a', 'z'}};

    public static final String DEFAULT_URL_PREFIX = "http://";

    public static final String DEFAULT_URN_PREFIX = "urn:inria:";

    private static final int DEFAULT_MIN_LENGTH = 10;

    private static final int DEFAULT_MAX_LENGTH = 20;

    public static Node createNode() {
        return createNode(DEFAULT_MIN_LENGTH, DEFAULT_MAX_LENGTH);
    }

    public static Node createNode(int exactLength) {
        if (ProActiveRandom.nextInt(2) == 0) {
            return createUri(exactLength);
        } else {
            return createLiteral(exactLength);
        }
    }

    public static Node createNode(int minLength, int maxLength) {
        if (ProActiveRandom.nextInt(2) == 0) {
            return createUri(minLength, maxLength);
        } else {
            return createLiteral(minLength, maxLength);
        }
    }

    public static Node createUri() {
        return createUri(DEFAULT_MIN_LENGTH, DEFAULT_MAX_LENGTH);
    }

    public static Node createUri(int exactLength) {
        return Node.createURI(StringGenerator.create(
                DEFAULT_URL_PREFIX, exactLength, DEFAULT_BOUNDS));
    }

    public static Node createUri(int minLength, int maxLength) {
        return Node.createURI(StringGenerator.create(
                DEFAULT_URL_PREFIX, minLength, maxLength, DEFAULT_BOUNDS));
    }

    public static Node createLiteral() {
        return createLiteral(DEFAULT_MIN_LENGTH, DEFAULT_MAX_LENGTH);
    }

    public static Node createLiteral(int exactLength) {
        return Node.createLiteral(StringGenerator.create(
                exactLength, DEFAULT_BOUNDS));
    }

    public static Node createLiteral(int minLength, int maxLength) {
        return Node.createLiteral(StringGenerator.create(
                minLength, maxLength, DEFAULT_BOUNDS));
    }

}
