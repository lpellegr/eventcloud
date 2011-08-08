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

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.PublishSubscribeApi;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.BindingNotificationListener;
import fr.inria.eventcloud.api.listeners.EventNotificationListener;

/**
 * This class is assumed to play the role of a mock PublishSubscribeProxy in
 * order to test if the translation between RDF2Go and Jena objects work.
 * 
 * @author lpellegr
 */
public class MockPublishSubscribeProxy implements PublishSubscribeApi {

    public MockPublishSubscribeProxy() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(Quadruple quad) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(Event event) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(Collection<Event> events) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void publish(InputStream in, SerializationFormat format) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SubscriptionId subscribe(String sparqlQuery,
                                    BindingNotificationListener listener) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SubscriptionId subscribe(String sparqlQuery,
                                    EventNotificationListener listener) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unsubscribe(SubscriptionId id) {
    }

}
