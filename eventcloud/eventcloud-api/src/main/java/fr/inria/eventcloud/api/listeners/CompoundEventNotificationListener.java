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
package fr.inria.eventcloud.api.listeners;

import fr.inria.eventcloud.api.CompoundEvent;

/**
 * When a subscription is registered along with this type of notification
 * listener, the subscriber will receive notifications as {@link CompoundEvent}
 * s. The matching of the compound event is performed in a distributed manner on
 * the brokering network. When a compound event is detected as matching a
 * subscription, the component in charge of sending a notification to the
 * subscriber may not have all the quadruples that composes the compound event
 * due to some network and threads issues. In that case the compound event is
 * reconstructed little by little. The reconstruct operation depends on the
 * version of the publish/subscribe algorithm used (SBCE1, SBCE2 or SBCE3).
 * 
 * @author lpellegr
 */
public abstract class CompoundEventNotificationListener extends
        NotificationListener<CompoundEvent> {

    private static final long serialVersionUID = 160L;

    /**
     * {@inheritDoc}
     */
    @Override
    public NotificationListenerType getType() {
        return NotificationListenerType.COMPOUND_EVENT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSubscriberUrl() {
        return null;
    }

}
