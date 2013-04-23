/**
 * Copyright (c) 2011-2013 INRIA.
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

import java.io.Serializable;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.multiactivity.MultiActiveService;

import fr.inria.eventcloud.api.SubscriptionId;

/**
 * A NotificationListener offers the possibility to define an action to execute
 * depending of the notification type which is received.
 * 
 * @author lpellegr
 */
@DefineGroups({@Group(name = "parallel", selfCompatible = true)})
public abstract class NotificationListener<T> implements Serializable,
        RunActive {

    private static final long serialVersionUID = 150L;

    /**
     * Handles a notification that has been received.
     * 
     * @param id
     *            the subscription identifier that identifies which subscription
     *            is matched by the solution which is received.
     * @param solution
     *            a solution that matches the subscription.
     */
    @MemberOf("parallel")
    public abstract void onNotification(SubscriptionId id, T solution);

    /**
     * Returns the notification type.
     * 
     * @return the notification type.
     */
    @MemberOf("parallel")
    public abstract NotificationListenerType getType();

    /**
     * Returns the URL of the subscriber. This is not {@code null} for proxies
     * exposed as Webservices but {@code null} for others.
     * 
     * @return the URL of the subscriber. This is not {@code null} for proxies
     *         exposed as Webservices but {@code null} for others.
     */
    @MemberOf("parallel")
    public abstract String getSubscriberUrl();

    /**
     * {@inheritDoc}
     */
    @Override
    public void runActivity(Body body) {
        new MultiActiveService(body).multiActiveServing();
    }

}
