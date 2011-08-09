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
package fr.inria.eventcloud.proxies;

import java.io.Serializable;

import fr.inria.eventcloud.api.SubscribeApi;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.properties.AlterableElaProperty;
import fr.inria.eventcloud.pubsub.Notification;
import fr.inria.eventcloud.pubsub.Subscription;

/**
 * A SubscribeProxy is a proxy that implements the {@link SubscribeApi}. It has
 * to be used by a user who wants to execute subscribe asynchronous operations
 * on an Event Cloud.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public interface SubscribeProxy extends Proxy, SubscribeApi, Serializable {

    /**
     * The init method is a convenient method for components which is used to
     * initialize the {@link EventCloudCache}. Once this method is called and
     * the value is set, the next calls perform no action.
     * 
     * @param proxy
     *            the event cloud proxy instance to set to the publish subscribe
     *            proxy.
     * 
     * @param properties
     *            a set of {@link AlterableElaProperty} properties to use for
     *            initializing the {@link SubscribeProxy}.
     */
    public void init(EventCloudCache proxy, AlterableElaProperty[] properties);

    /**
     * Searches the {@link Subscription} associated to the specified
     * {@link SubscriptionId}.
     * 
     * @param id
     *            the identifier associated to the {@link Subscription} to
     *            lookup.
     * 
     * @return the subscription associated to the {@code id} if the {@code id}
     *         is contained into the list of the subscription identifiers that
     *         have been register from this proxy, {@code false} otherwise.
     */
    public Subscription find(SubscriptionId id);

    /**
     * Used internally to send back a notification.
     * 
     * @param notification
     *            the notification that is received.
     */
    public void receive(Notification notification);

}
