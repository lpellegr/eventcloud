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

import fr.inria.eventcloud.api.wrappers.BindingWrapper;

/**
 * This kind of notification listener will only receive a binding (i.e. the
 * variables and their associated values) that matches the subscription and
 * which is wrapped into a {@link BindingWrapper}. This class is useful when the
 * solution (ie. the Binding) needs to be serialized in order to be delivered to
 * the listener (for instance if the listener is an Active Object).
 * 
 * @author bsauvan
 */
public abstract class BindingWrapperNotificationListener extends
        NotificationListener<BindingWrapper> {

    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    @Override
    public NotificationListenerType getType() {
        return NotificationListenerType.BINDINGS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSubscriberUrl() {
        return null;
    }

}
