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

import fr.inria.eventcloud.api.listeners.NotificationListener;

/**
 * Defines the subscribe operations that can be executed on an Event Cloud.
 * 
 * @author lpellegr
 */
public interface SubscribeApi {

    /**
     * Subscribes to interest with the specified SPARQL query and the given
     * {@link NotificationListener}.
     * 
     * @param subscription
     *            the subscription object.
     * 
     * @param listener
     *            the listener that defines the type of notifications and the
     *            action to execute when a notification is received.
     */
    public <T> void subscribe(Subscription subscription,
                              NotificationListener<T> listener);

    /**
     * Unsubscribes by using the specified subscription identifier.
     * 
     * @param id
     *            the subscription identifier.
     */
    public void unsubscribe(SubscriptionId id);

}
