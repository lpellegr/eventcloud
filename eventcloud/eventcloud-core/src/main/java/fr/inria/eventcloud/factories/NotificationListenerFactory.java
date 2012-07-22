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
package fr.inria.eventcloud.factories;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.listeners.NotificationListener;

/**
 * This class is used to create a {@link NotificationListener} as an active
 * object.
 * 
 * @author bsauvan
 */
public class NotificationListenerFactory {

    private static final Logger log =
            LoggerFactory.getLogger(NotificationListenerFactory.class);

    private NotificationListenerFactory() {
    }

    /**
     * Creates a new notification listener active object deployed on the local
     * JVM.
     * 
     * @param notificationListenerClass
     *            the class of the notification listener to instantiate as an
     *            active object.
     * @param constructorParameters
     *            the parameters of the constructor.
     * 
     * @return the reference on new notification listener active object created.
     */
    public static <T extends NotificationListener<?>> T newNotificationListener(Class<T> notificationListenerClass,
                                                                                Object[] constructorParameters) {
        try {
            T notificationListener =
                    PAActiveObject.newActive(
                            notificationListenerClass, constructorParameters);

            log.info("Notification listener "
                    + notificationListenerClass.getName() + " created");

            return notificationListener;
        } catch (ActiveObjectCreationException e) {
            throw new IllegalStateException(e);
        } catch (NodeException e) {
            throw new IllegalStateException(e);
        }
    }

}
