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
package fr.inria.eventcloud.pubsub;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.CompoundEventNotificationListener;

public class CustomCompoundEventNotificationListenerActiveObject extends
        CompoundEventNotificationListener {

    private static final long serialVersionUID = 1L;

    private static final Logger log =
            LoggerFactory.getLogger(CustomCompoundEventNotificationListenerActiveObject.class);

    private List<CompoundEvent> events = new ArrayList<CompoundEvent>();

    public CustomCompoundEventNotificationListenerActiveObject() {
    }

    public List<CompoundEvent> getEvents() {
        return this.events;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onNotification(SubscriptionId id, CompoundEvent solution) {
        synchronized (this.events) {
            this.events.add(solution);
        }
        log.info("New event received:\n" + solution);
    }

}
