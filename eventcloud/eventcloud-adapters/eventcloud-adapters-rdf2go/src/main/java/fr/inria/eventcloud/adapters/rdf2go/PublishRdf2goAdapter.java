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
package fr.inria.eventcloud.adapters.rdf2go;

import java.net.URL;

import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.Node;
import org.ontoware.rdf2go.model.node.Resource;
import org.ontoware.rdf2go.model.node.URI;

import fr.inria.eventcloud.api.PublishApi;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;

/**
 * This class is used as an adapter for any object that implements the
 * {@link PublishApi} interface. It provides methods with types which are
 * compatible with RDF2Go. These methods then delegate the calls to the
 * underlying object by using the {@link PublishApi}.
 * 
 * @author lpellegr
 */
public class PublishRdf2goAdapter extends Rdf2goAdapter<PublishApi> {

    /**
     * Constructs a new RDF2Go adapter for the given delegate.
     * 
     * @param delegate
     *            the object to adapt.
     */
    public PublishRdf2goAdapter(PublishApi delegate) {
        super(delegate);
    }

    public void publish(URI context, Resource subject, URI predicate,
                        Node object) {
        super.delegate.publish(toQuadruple(context, subject, predicate, object));
    }

    public void publish(Statement stmt) {
        this.publish(
                stmt.getContext(), stmt.getSubject(), stmt.getPredicate(),
                stmt.getObject());
    }

    public void publish(URL url, SerializationFormat format) {
        super.delegate.publish(url, format);
    }

}
