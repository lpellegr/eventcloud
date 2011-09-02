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
package fr.inria.eventcloud.api;

import fr.inria.eventcloud.api.listeners.BindingNotificationListener;
import fr.inria.eventcloud.api.listeners.EventNotificationListener;

/**
 * Defines the publish/subscribe operations that can be executed on an
 * Event Cloud.
 * 
 * @author lpellegr
 */
public interface SubscribeApi {

    /**
     * Subscribes for notifications of type {@link BindingNotificationListener}
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
                                    BindingNotificationListener listener);

    /**
     * Subscribes for notifications of type {@link EventNotificationListener}
     * with the specified SPARQL query. To have the possibility to retrieve an
     * {@link Event}, the method reconstructs internally the Event from the
     * event identifier which matches the subscription. <strong>This operation
     * must be used carefully</strong>.
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
                                    EventNotificationListener listener);

    /**
     * Unsubscribes by using the specified subscription identifier.
     * 
     * @param id
     *            the subscription identifier.
     */
    public void unsubscribe(SubscriptionId id);

}
