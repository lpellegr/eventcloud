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

import java.net.URI;
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

    private static final long serialVersionUID = 140L;

    // \u00A2 -> ¢, CENT SIGN
    protected static String EMPTY_STRING_ROUTING_CHARACTER = "\u00A2";

    /**
     * Constructs a new semantic element from the specified {@code value} by
     * trying to remove RDF prefixes.
     * 
     * @param value
     *            the value which is analyzed.
     */
    public SemanticElement(Node value) {
        super(removePrefix(value));
    }

    /**
     * Constructs a new semantic element from the specified {@code value}
     * representation.
     * 
     * @param value
     *            the value.
     */
    public SemanticElement(Apfloat value) {
        super(value);
    }

    /**
     * Constructs a new semantic element from the specified {@code value}
     * without trying to remove RDF prefixes. It has to be used with care.
     * 
     * @param value
     *            the value.
     */
    protected SemanticElement(String value) {
        super(value);
    }

    /**
     * Analyze the specified {@link Node} value to remove some prefixes which
     * are redundant in RDF data. This suppression is done to improve the load
     * balancing (i.e. especially to avoid to have several values with popular
     * prefixes that are managed in the same zone).
     * 
     * @param value
     *            the value to parse.
     * 
     * @return a String which has been improved for load balancing.
     */
    public static final String removePrefix(final Node value) {
        if (value.isURI()) {
            // TODO: add support for opaque URI (c.f.
            // http://download.oracle.com/javase/6/docs/api/java/net/URI.html)
            String content = value.getURI();

            int slashIndex = content.lastIndexOf('/');
            int sharpIndex = content.lastIndexOf('#');
            int lastCharIndex = content.length() - 1;

            // if the last character is # or / it can be removed safely
            if (slashIndex == lastCharIndex || sharpIndex == lastCharIndex) {
                content = content.substring(0, lastCharIndex);
                slashIndex = content.lastIndexOf('/');
                sharpIndex = content.lastIndexOf('#');
            }

            URI uri = null;
            try {
                uri = new URI(content);
            } catch (URISyntaxException e) {
                return content;
            }

            int schemeColonSlashSlashLength;

            // if there is no other / or # starting from the authority part of
            // the uri, some pre-defined prefixes can be removed from the
            // authority part (e.g. scheme://www). Otherwise the prefix before
            // the last / or # is removed
            if (uri.getScheme() != null
                    && slashIndex < (schemeColonSlashSlashLength =
                            uri.getScheme().length() + 3) && sharpIndex == -1) {
                // the pre-defined prefix is contained in the authority part
                if (content.startsWith("www.", schemeColonSlashSlashLength)) {
                    return content.substring(schemeColonSlashSlashLength + 4);
                }

                // no pre-defined prefix is contained in the authority part then
                // only the scheme + :// is removed
                return content.substring(uri.getScheme().length() + 3);
            } else if (slashIndex > sharpIndex) {
                // remove the prefix before the last /
                return content.substring(slashIndex + 1);
            } else if (slashIndex < sharpIndex) {
                // remove the prefix before the last #
                return content.substring(sharpIndex + 1);
            } else {
                return content;
            }
        } else if (value.isLiteral()) {
            String literal = value.getLiteralLexicalForm();

            if (literal.isEmpty()) {
                // if the literal value contains an empty String we have to
                // decide which constraint is associated to this particular
                // case where there is no character to compare with
                return EMPTY_STRING_ROUTING_CHARACTER;
            }

            return literal;
        } else if (value.isBlank()) {
            return value.getBlankNodeLabel();
        } else if (value.isVariable()) {
            return value.getName();
        } else {
            throw new IllegalArgumentException("Unknown node type: "
                    + value.getClass());
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
