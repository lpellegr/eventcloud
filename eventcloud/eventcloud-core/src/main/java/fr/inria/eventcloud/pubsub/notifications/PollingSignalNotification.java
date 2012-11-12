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
package fr.inria.eventcloud.pubsub.notifications;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.SubscriptionId;

/**
 * Defines a notification that embeds the id of the compound event which is
 * matching as value.
 * 
 * @author lpellegr
 */
public class PollingSignalNotification extends Notification<String> {

    private static final long serialVersionUID = 130L;

    public PollingSignalNotification(SubscriptionId subscriptionId,
            Node eventId, String source, Node eventIdWithMetaInformation) {
        super(subscriptionId, eventId, source,
                eventIdWithMetaInformation.getURI());
    }

}
