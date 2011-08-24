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
package fr.inria.eventcloud.adapters.rdf2go;

import org.ontoware.rdf2go.RDF2Go;
import org.ontoware.rdf2go.model.Model;
import org.ontoware.rdf2go.model.node.PlainLiteral;
import org.ontoware.rdf2go.model.node.URI;
import org.ontoware.rdf2go.model.node.impl.URIImpl;

/**
 * @author lpellegr
 */
public class Rdf2goAdapterTest<T extends Rdf2goAdapter<?>> {

    protected static final Model model;

    protected static final URI uri;

    protected static final PlainLiteral literal;

    protected T adapter;

    static {
        String defaultURL = "http://www.inria.fr";
        model = RDF2Go.getModelFactory().createModel(new URIImpl(defaultURL));
        uri = model.createURI("http://www.inria.fr");
        literal = model.createPlainLiteral("My Literal Value");
    }

}
