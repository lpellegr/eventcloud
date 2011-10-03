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
package fr.inria.eventcloud.api.listeners;

import java.io.Serializable;

import fr.inria.eventcloud.api.SubscriptionId;

/**
 * A NotificationListener offers the possibility to define an action to execute
 * depending of the notification type which is received.
 * 
 * @author lpellegr
 */
public interface NotificationListener<T> extends Serializable {

    /**
     * Handles a notification that has been received.
     * 
     * @param id
     *            the subscription identifier that identified which subscription
     *            is matched by the solution which is received.
     * @param solution
     *            a solution that matches the subscription.
     */
    public void onNotification(SubscriptionId id, T solution);

    /**
     * Returns the notification type.
     * 
     * @return the notification type.
     */
    public NotificationListenerType getType();

}
