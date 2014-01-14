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
package fr.inria.eventcloud.api;

import fr.inria.eventcloud.api.listeners.BindingNotificationListener;
import fr.inria.eventcloud.api.listeners.CompoundEventNotificationListener;
import fr.inria.eventcloud.api.listeners.SignalNotificationListener;

/**
 * Defines the subscribe operations that can be executed on an EventCloud.
 * 
 * @author lpellegr
 */
public interface SubscribeApi {

    /**
     * Subscribes to interest with the specified SPARQL query and the given
     * {@link BindingNotificationListener}.
     * 
     * @param subscription
     *            the subscription object.
     * 
     * @param listener
     *            the listener that defines the type of notifications and the
     *            action to execute when a notification is received.
     */
    void subscribe(Subscription subscription,
                   BindingNotificationListener listener);

    /**
     * Subscribes to interest with the specified SPARQL query and the given
     * {@link CompoundEventNotificationListener}.
     * 
     * @param subscription
     *            the subscription object.
     * 
     * @param listener
     *            the listener that defines the type of notifications and the
     *            action to execute when a notification is received.
     */
    void subscribe(Subscription subscription,
                   CompoundEventNotificationListener listener);

    /**
     * Subscribes to interest with the specified SPARQL query and the given
     * {@link SignalNotificationListener}.
     * 
     * @param subscription
     *            the subscription object.
     * 
     * @param listener
     *            the listener that defines the type of notifications and the
     *            action to execute when a notification is received.
     */
    void subscribe(Subscription subscription,
                   SignalNotificationListener listener);

    /**
     * Unsubscribes by using the specified subscription identifier.
     * 
     * @param id
     *            the subscription identifier.
     */
    void unsubscribe(SubscriptionId id);

}
