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
package fr.inria.eventcloud.api.generators;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;

/**
 * Utility class that defines some convenient methods to create arbitrary
 * {@link Node}s.
 * 
 * @author lpellegr
 */
public class NodeGenerator extends Generator {

    public static final String DEFAULT_URI_SCHEME_NAME = "http://";

    private NodeGenerator() {

    }

    /**
     * Creates a random Node whose length is the number of characters specified.
     * The node can be either an URI or a literal.
     * 
     * @return the randomly generated node.
     */
    public static Node random() {
        return random(DEFAULT_LENGTH);
    }

    /**
     * Creates a random Node whose length is the number of characters specified.
     * The node can be either an URI or a literal. If the node which is
     * generated is an URI the final length is equals to
     * {@code length + DEFAULT_URI_SCHEME_NAME.size()}.
     * 
     * @param prefix
     *            a prefix to append before the randomly generated part of
     *            {@code length} characters.
     * @param length
     *            the length of random string to create.
     * 
     * @return the randomly generated node.
     */
    public static Node random(String prefix, int length) {
        if (RANDOM.nextInt(2) == 0) {
            return randomUri(prefix, length);
        } else {
            return randomLiteral(prefix, length);
        }
    }

    /**
     * Creates a random Node whose length is the number of characters specified.
     * The node can be either an URI or a literal. If the node which is
     * generated is an URI the final length is equals to
     * {@code length + "http://".size()}.
     * 
     * @param length
     *            the length of random string to create.
     * 
     * @return the randomly generated node.
     */
    public static Node random(int length) {
        if (RANDOM.nextInt(2) == 0) {
            return randomUri(length);
        } else {
            return randomLiteral(length);
        }
    }

    /**
     * Creates a random URI node by using the specified {@code prefix}.
     * 
     * @param prefix
     *            the prefix added to the generated URI.
     * 
     * @return the randomly generated node.
     */
    public static Node randomUri(String prefix) {
        return NodeFactory.createURI(UriGenerator.randomPrefixed(
                DEFAULT_LENGTH, prefix));
    }

    /**
     * Creates a random URI node by using the specified {@code prefix}.
     * 
     * @param prefix
     *            the prefix added to the generated URI.
     * @param length
     *            the length of random string to create.
     * 
     * @return the randomly generated node.
     */
    public static Node randomUri(String prefix, int length) {
        return NodeFactory.createURI(UriGenerator.randomPrefixed(length, prefix));
    }

    /**
     * Creates a random URI node by using the {@code http} scheme name.
     * 
     * @return the randomly generated node.
     */
    public static Node randomUri() {
        return NodeFactory.createURI(UriGenerator.random(
                DEFAULT_LENGTH, DEFAULT_URI_SCHEME_NAME));
    }

    /**
     * Creates a random URI node whose the final length is equals to
     * {@code length + "http://".size()}.
     * 
     * @param length
     *            the length of random string to create.
     * 
     * @return the randomly generated node.
     */
    public static Node randomUri(int length) {
        return NodeFactory.createURI(UriGenerator.random(
                length, DEFAULT_URI_SCHEME_NAME));
    }

    /**
     * Creates a random literal node.
     * 
     * @return the randomly generated node.
     */
    public static Node randomLiteral() {
        return randomLiteral(DEFAULT_LENGTH);
    }

    /**
     * Creates a random literal node whose length is the number of characters
     * specified.
     * 
     * @param prefix
     *            a prefix to append before the randomly generated part of
     *            {@code length} characters.
     * @param length
     *            the length of random string to create.
     * 
     * @return the randomly generated node.
     */
    public static Node randomLiteral(String prefix, int length) {
        return NodeFactory.createLiteral(prefix
                + StringGenerator.randomPrintableAscii(length));
    }

    /**
     * Creates a random literal node whose length is the number of characters
     * specified.
     * 
     * @param length
     *            the length of random string to create.
     * 
     * @return the randomly generated node.
     */
    public static Node randomLiteral(int length) {
        return NodeFactory.createLiteral(StringGenerator.randomPrintableAscii(length));
    }

}
