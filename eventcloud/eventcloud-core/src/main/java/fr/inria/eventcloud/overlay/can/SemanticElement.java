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

import java.net.URISyntaxException;

import org.apfloat.Apfloat;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;

import com.hp.hpl.jena.graph.Node;

/**
 * Represents a semantic coordinate element. This kind of element parses input
 * value and removes some prefix which are specific to semantic data in order to
 * improve the load balancing.
 * 
 * @author lpellegr
 */
public class SemanticElement extends StringElement {

    private static final long serialVersionUID = 1L;

    // \u00A2 -> ¢, CENT SIGN
    public static String EMPTY_STRING_ROUTING_CHARACTER = "\u00A2";

    /**
     * Constructs a new semantic coordinate element from the specified
     * {@code value}.
     * 
     * @param value
     *            the value which is analyzed.
     */
    public SemanticElement(Node value) {
        this(value.toString());
    }

    /**
     * Constructs a new semantic coordinate element from the specified
     * {@code value}.
     * 
     * @param value
     *            the value which is analyzed.
     */
    public SemanticElement(String value) {
        super(removePrefix(value));
    }

    private SemanticElement(String value, Void dummy) {
        super(value);
    }

    private SemanticElement(Apfloat apfloat) {
        super(apfloat);
    }

    /**
     * Constructs a new semantic coordinate element from the specified
     * {@code value}. Contrary to the constructor of this class, the value is
     * not analyzed to remove a potential prefix. It has to be used with care.
     * 
     * @param value
     *            the value to use.
     * 
     * @return a new semantic element.
     */
    public static SemanticElement newRawSemanticElement(String value) {
        return new SemanticElement(value);
    }

    /**
     * Analyzes the String value from a {@link Node} in order to remove the
     * prefixes and some characters specific to RDF data. This suppression is
     * done to improve the load balancing (i.e. especially to avoid to have
     * several values with popular prefixes that are managed in the same zone).
     * 
     * @param value
     *            the value to parse.
     * 
     * @return a String that has been improved for load balancing.
     */
    public static String removePrefix(String value) {
        // TODO: add support for opaque URI (c.f.
        // http://download.oracle.com/javase/6/docs/api/java/net/URI.html)

        try {
            java.net.URI uri = new java.net.URI(value);

            int slashIndex = value.lastIndexOf('/');
            int sharpIndex = value.lastIndexOf('#');

            // if the last character is # or / it can be safely removed
            if (slashIndex == value.length() - 1
                    || sharpIndex == value.length() - 1) {
                value = value.substring(0, value.length() - 1);
                slashIndex = value.lastIndexOf('/');
                sharpIndex = value.lastIndexOf('#');
            }

            // if there is no other / or # starting from the authority part of
            // the uri some pre-defined prefix can be removed in the authority
            // part (e.g. scheme://www) otherwise the prefix before the last /
            // or # is removed
            if (uri.getScheme() != null
                    && slashIndex < uri.getScheme().length() + 3 // +3 for ://
                    && sharpIndex == -1) {
                int wwwDotIndex = -1;

                // the pre-defined prefix is contained in the authority part
                if ((wwwDotIndex = value.indexOf("www.")) != -1) {
                    return value.substring(wwwDotIndex + 4, value.length());
                }

                // no pre-defined prefix is contained in the authority part then
                // only the scheme + :// is removed
                return value.substring(uri.getScheme().length() + 3);
            } else if (slashIndex > sharpIndex) {
                // remove the prefix before the last /
                return value.substring(slashIndex + 1, value.length());
            } else if (slashIndex < sharpIndex) {
                // remove the prefix before the last #
                return value.substring(sharpIndex + 1, value.length());
            } else {
                return value;
            }
        } catch (URISyntaxException e) {
            // blank node
            if (value.startsWith("_:")) {
                return value.substring(2);
            } else if (value.length() > 0 && value.charAt(0) == '"') { // literal
                return value.substring(1, value.length() - 1);
            } else {
                return value;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected StringElement newStringElement(Apfloat apfloat) {
        return new SemanticElement(apfloat);
    }

}
