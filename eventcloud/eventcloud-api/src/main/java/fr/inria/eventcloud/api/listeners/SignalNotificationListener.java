/**
 * Copyright (c) 2011-2012 INRIA.
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

import fr.inria.eventcloud.api.SubscriptionId;

/**
 * This notification listener is used to report to the subscriber that an event
 * (quadruple) matching its subscription has been detected. However, the event
 * value is not conveyed to the subscriber.
 * 
 * @author lpellegr
 */
public abstract class SignalNotificationListener extends
        NotificationListener<Object> {

    private static final long serialVersionUID = 130L;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNotification(SubscriptionId id, Object solution) {
        // the solution is supposed to be null and can be discarded because only
        // the signal is interesting for end users
        this.onNotification(id);
    }

    public abstract void onNotification(SubscriptionId id);

    /**
     * {@inheritDoc}
     */
    @Override
    public NotificationListenerType getType() {
        return NotificationListenerType.SIGNAL;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSubscriberUrl() {
        return null;
    }

}
