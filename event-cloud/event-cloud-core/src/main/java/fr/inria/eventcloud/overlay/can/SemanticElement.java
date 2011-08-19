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
package fr.inria.eventcloud.overlay.can;

import java.net.URISyntaxException;

import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;

import com.hp.hpl.jena.graph.Node;

/**
 * Represents a semantic coordinate element. This kind of element extends
 * {@link StringElement} and removes some prefix which are specific to semantic
 * data in order to improve the load balancing.
 * 
 * @author lpellegr
 */
public class SemanticElement extends StringElement {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new coordinate element with the specified {@code value}.
     * 
     * @param value
     *            the value that will be parsed.
     */
    public SemanticElement(String value) {
        super(parseElement(value));
    }

    public SemanticElement(Node value) {
        super(parseElement(value.toString()));
    }

    /**
     * Parses the String value from a {@link Node} in order to remove the
     * prefixes and some characters specific to the RDF syntax used. This
     * suppression is done to improve the load balancing (i.e. especially to
     * avoid to have several values with popular prefixes that are managed in
     * the same zone).
     * 
     * @param value
     *            the value to parse.
     * 
     * @return a String that has been improved for load balancing.
     */
    public static String parseElement(String value) {
        try {
            java.net.URI uri = new java.net.URI(value);

            int slashIndex = value.lastIndexOf('/');
            int sharpIndex = value.lastIndexOf('#');

            // # or / is the last character
            if (slashIndex == value.length() - 1
                    || sharpIndex == value.length() - 1) {
                value = value.substring(0, value.length() - 1);
                slashIndex = value.lastIndexOf('/');
                sharpIndex = value.lastIndexOf('#');
            }

            if (slashIndex < 7 && sharpIndex == -1) {
                if (uri.getScheme().equals("http")
                        && value.startsWith("http://www.")) {
                    return value.substring(11);
                }
                return value.substring(uri.getScheme().length() + 3);
            } else if (slashIndex > sharpIndex) {
                return value.substring(slashIndex + 1, value.length());
            } else if (slashIndex < sharpIndex) {
                return value.substring(sharpIndex + 1, value.length());
            }

            return "";
        } catch (URISyntaxException e) {
            if (value.startsWith("_:")) {
                return value.substring(2);
            } else if (value.startsWith("\"")) {
                return value.substring(1, value.length() - 1);
            } else {
                return value;
            }
        }
    }

}
