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

import fr.inria.eventcloud.api.CompoundEvent;

/**
 * This kind of notification listener will receive the notification in the form
 * of a {@link CompoundEvent}. To have the possibility to retrieve a
 * {@link CompoundEvent}, the method reconstructs internally the
 * {@link CompoundEvent} from the event identifier which matches the
 * subscription. <strong>This listener must be used carefully</strong>.
 * 
 * @author lpellegr
 */
public abstract class CompoundEventNotificationListener implements
        NotificationListener<CompoundEvent> {

    private static final long serialVersionUID = 1L;

    /**
     * {@inheritDoc}
     */
    @Override
    public NotificationListenerType getType() {
        return NotificationListenerType.COMPOUND_EVENT;
    }

}
