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

import com.hp.hpl.jena.sparql.engine.binding.Binding;

/**
 * When this type of notification listener is used, a subscriber will receive
 * bindings (a binding contains the name of the variables that has been matched
 * and their associated values) as soon as an event or part of a compound event
 * is matched to ensure that all the values matching a subscription are sent
 * back to the subscriber. However, it is worth to note that a compound event
 * that contains two quadruples matching independently the same subscription may
 * trigger two notifications for the same subscriber and compound event.
 * Bindings are not necessarily collected per compound event before to be
 * delivered to the subscriber because on the subscriber side we have no idea
 * about the number of quadruples contained by a compound event that may match a
 * same subscription independently. The only solution would be to send back a
 * notification each time a quadruple is published whether it matches a
 * subscription or not. This is not scalable and it explains our choice. In
 * spite of this limitation, this behavior may be useful to receive the values
 * matching a subscription as soon as they arrive in a streaming manner when the
 * number of matching chunks do not have to be known in advance.
 * 
 * @author lpellegr
 */
public abstract class BindingNotificationListener extends
        NotificationListener<Binding> {

    private static final long serialVersionUID = 140L;

    /**
     * {@inheritDoc}
     */
    @Override
    public NotificationListenerType getType() {
        return NotificationListenerType.BINDING;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSubscriberUrl() {
        return null;
    }

}
