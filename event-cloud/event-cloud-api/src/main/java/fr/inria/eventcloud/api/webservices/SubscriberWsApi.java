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
package fr.inria.eventcloud.api.webservices;

import fr.inria.eventcloud.api.SubscriptionId;

/**
 * Defines the notification operation that is exposed as a web service by the
 * subscriber of a subscription and is called by the subscribe proxy component
 * when an event that matches the subscription is received.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public interface SubscriberWsApi {

    /**
     * Notifies that an event, represented by the specified WS-Notification
     * notification XML payload, matching a subscription has been received.
     * 
     * @param id
     *            the subscription identifier.
     * @param xmlPayload
     *            the WS-Notification notification XML payload representing the
     *            event.
     */
    public void notifyEvent(SubscriptionId id, String xmlPayload);

}
