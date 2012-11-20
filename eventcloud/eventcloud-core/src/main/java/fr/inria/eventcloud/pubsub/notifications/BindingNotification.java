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
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.wrappers.BindingWrapper;

/**
 * Defines a notification that embeds a {@link Binding} as value.
 * 
 * @author lpellegr
 */
public class BindingNotification extends Notification<BindingWrapper> {

    private static final long serialVersionUID = 130L;

    public BindingNotification(SubscriptionId subscriptionId, Node eventId,
            String source, BindingWrapper binding) {
        super(subscriptionId, eventId, source, binding);
    }

    public BindingNotification(NotificationId id,
            SubscriptionId subscriptionId, String source, BindingWrapper binding) {
        super(id, subscriptionId, source, binding);
    }

}
