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
package fr.inria.eventcloud.api;

import java.io.InputStream;

import fr.inria.eventcloud.api.listeners.BindingsNotificationListener;
import fr.inria.eventcloud.api.listeners.EventsNotificationListener;

/**
 * Defines the publish/subscribe operations that can be executed on an
 * Event-Cloud.
 * 
 * @author lpellegr
 */
public interface PublishSubscribeApi {

    public enum SerializationFormat {
        TriG, NQuads
    }

    /**
     * Publishes the specified quadruple.
     * 
     * @param quad
     *            the quadruple to publish.
     */
    public void publish(Quadruple quad);

    /**
     * Publishes the specified event.
     * 
     * @param event
     *            the event to publish.
     */
    public void publish(Event event);

    /**
     * Publishes the specified collection of {@link Event}s.
     * 
     * @param events
     *            the events to publish.
     */
    public void publish(Collection<Event> events);

    /**
     * Publishes the quadruples that are read from the specified input stream.
     * The input stream is assumed to comply with the <a
     * href="http://www4.wiwiss.fu-berlin.de/bizer/TriG/">TriG</a> or <a
     * href="http://sw.deri.org/2008/07/n-quads/">N-Quads</a> syntax.
     * 
     * @param in
     *            the input stream from where the quadruples are read.
     * 
     * @param format
     *            the format that is used to read the data from the input
     *            stream.
     */
    public void publish(InputStream in, SerializationFormat format);

    /**
     * Subscribes for notifications of type {@link BindingsNotificationListener}
     * with the specified SPARQL query.
     * 
     * @param sparqlQuery
     *            the SPARQL query that is used to subscribe.
     * @param listener
     *            the listener that defines the action to execute when a
     *            notification is received.
     * 
     * @return the subscription identifier.
     */
    public SubscriptionId subscribe(String sparqlQuery,
                                    BindingsNotificationListener listener);

    /**
     * Subscribes for notifications of type {@link EventsNotificationListener}
     * with the specified SPARQL query.
     * 
     * @param sparqlQuery
     *            the SPARQL query that is used to subscribe.
     * @param listener
     *            the listener that defines the action to execute when a
     *            notification is received.
     * 
     * @return the subscription identifier.
     */
    public SubscriptionId subscribe(String sparqlQuery,
                                    EventsNotificationListener listener);

    /**
     * Unsubscribes by using the specified subscription identifier.
     * 
     * @param id
     *            the subscription identifier.
     */
    public void unsubscribe(SubscriptionId id);

}
