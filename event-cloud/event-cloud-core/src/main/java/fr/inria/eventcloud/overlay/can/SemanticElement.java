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
package fr.inria.eventcloud.overlay.can;

import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;
import org.ontoware.rdf2go.model.node.Node;
import org.ontoware.rdf2go.model.node.Resource;

import fr.inria.eventcloud.util.SemanticHelper;

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
        super(SemanticHelper.parseTripleElement(value));
    }

    public SemanticElement(Resource value) {
        super(SemanticHelper.parseTripleElement(value.toString()));
    }

    public SemanticElement(Node value) {
        super(SemanticHelper.parseTripleElement(value.toString()));
    }

}
