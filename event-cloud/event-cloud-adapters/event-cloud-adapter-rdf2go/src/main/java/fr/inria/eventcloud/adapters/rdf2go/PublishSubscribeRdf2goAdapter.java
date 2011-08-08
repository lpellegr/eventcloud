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

import java.io.InputStream;

import org.ontoware.rdf2go.model.Statement;
import org.ontoware.rdf2go.model.node.Node;
import org.ontoware.rdf2go.model.node.Resource;
import org.ontoware.rdf2go.model.node.URI;

import fr.inria.eventcloud.api.PublishSubscribeApi;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.BindingNotificationListener;
import fr.inria.eventcloud.api.listeners.EventNotificationListener;

/**
 * This class is used as an adapter for any object that implements the
 * {@link PublishSubscribeApi} interface. It provides methods with types which
 * are compatible with RDF2Go. These methods then delegate the calls to the
 * underlying object by using the {@link PublishSubscribeApi}.
 * 
 * @author lpellegr
 */
public final class PublishSubscribeRdf2goAdapter extends
        Rdf2goAdapter<PublishSubscribeApi> {

    /**
     * Constructs a new RDF2Go adapter for the given object.
     * 
     * @param obj
     *            the object to adapt.
     */
    public PublishSubscribeRdf2goAdapter(PublishSubscribeApi obj) {
        super(obj);
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

    public void publish(InputStream in, SerializationFormat format) {
        super.delegate.publish(in, format);
    }

    public SubscriptionId subscribe(String sparqlQuery,
                                    BindingNotificationListener listener) {
        return super.delegate.subscribe(sparqlQuery, listener);
    }

    public SubscriptionId subscribe(String sparqlQuery,
                                    EventNotificationListener listener) {
        return super.delegate.subscribe(sparqlQuery, listener);
    }

    public void unsubscribe(SubscriptionId id) {
        super.delegate.unsubscribe(id);
    }

}
