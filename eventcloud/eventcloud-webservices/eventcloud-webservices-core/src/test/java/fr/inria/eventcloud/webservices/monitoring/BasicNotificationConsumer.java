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
package fr.inria.eventcloud.webservices.monitoring;

import java.util.ArrayList;
import java.util.List;

import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.bw_2.NotificationConsumer;

/**
 * Provides a basic implementation of {@link NotificationConsumer} by storing
 * all incoming notifications into an in-memory list. This list can be retrieved
 * at any time for any purpose.
 * 
 * @author lpellegr
 */
public class BasicNotificationConsumer implements NotificationConsumer {

    public final List<Notify> notificationsReceived;

    /**
     * Creates a {@link BasicNotificationConsumer}.
     */
    public BasicNotificationConsumer() {
        this.notificationsReceived = new ArrayList<Notify>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notify(Notify notify) {
        synchronized (this.notificationsReceived) {
            this.notificationsReceived.add(notify);
            this.notificationsReceived.notifyAll();
        }
    }

}
