/**
 * Copyright (c) 2011-2014 INRIA.
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
package fr.inria.eventcloud.pubsub.notifications;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.SubscriptionId;

/**
 * Defines a notification that is used to reconstruct a compound event that has
 * matched a subscription with polling.
 * 
 * @author lpellegr
 */
public class PollingSignalNotification extends Notification<Object> {

    private static final long serialVersionUID = 160L;

    public PollingSignalNotification(SubscriptionId subscriptionId,
            Node eventId, String source) {
        super(subscriptionId, eventId, source, null);
    }

}
