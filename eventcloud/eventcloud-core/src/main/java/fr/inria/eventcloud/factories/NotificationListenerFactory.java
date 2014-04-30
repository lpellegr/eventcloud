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
package fr.inria.eventcloud.factories;

import java.io.IOException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.p2p.structured.deployment.NodeProvider;
import org.objectweb.proactive.extensions.p2p.structured.factories.AbstractFactory;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.listeners.NotificationListener;

/**
 * This class is used to create a {@link NotificationListener} as an active
 * object.
 * 
 * @author bsauvan
 */
public class NotificationListenerFactory extends AbstractFactory {

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
     * @return the reference on new the notification listener active object
     *         created.
     */
    public static <T extends NotificationListener<?>> T newNotificationListener(Class<T> notificationListenerClass,
                                                                                Object[] constructorParameters) {
        return NotificationListenerFactory.createNotificationListener(
                notificationListenerClass, constructorParameters, null);
    }

    /**
     * Creates a new notification listener active object deployed on the
     * specified {@code node}.
     * 
     * @param notificationListenerClass
     *            the class of the notification listener to instantiate as an
     *            active object.
     * @param constructorParameters
     *            the parameters of the constructor.
     * @param node
     *            the node to be used for deployment.
     * 
     * @return the reference on new the notification listener active object
     *         created.
     */
    public static <T extends NotificationListener<?>> T newNotificationListener(Class<T> notificationListenerClass,
                                                                                Object[] constructorParameters,
                                                                                Node node) {
        return NotificationListenerFactory.createNotificationListener(
                notificationListenerClass, constructorParameters, node);
    }

    /**
     * Creates a new notification listener active object deployed on the
     * specified {@code GCM virtual node}.
     * 
     * @param notificationListenerClass
     *            the class of the notification listener to instantiate as an
     *            active object.
     * @param constructorParameters
     *            the parameters of the constructor.
     * @param vn
     *            the GCM virtual node to be used for deployment.
     * 
     * @return the reference on new the notification listener active object
     *         created.
     */
    public static <T extends NotificationListener<?>> T newNotificationListener(Class<T> notificationListenerClass,
                                                                                Object[] constructorParameters,
                                                                                GCMVirtualNode vn) {
        return NotificationListenerFactory.createNotificationListener(
                notificationListenerClass, constructorParameters, vn.getANode());
    }

    /**
     * Creates a new notification listener active object deployed on a node
     * provided by the specified {@code node provider}.
     * 
     * @param notificationListenerClass
     *            the class of the notification listener to instantiate as an
     *            active object.
     * @param constructorParameters
     *            the parameters of the constructor.
     * @param nodeProvider
     *            the node provider to be used for deployment.
     * 
     * @return the reference on new the notification listener active object
     *         created.
     */
    public static <T extends NotificationListener<?>> T newNotificationListener(Class<T> notificationListenerClass,
                                                                                Object[] constructorParameters,
                                                                                NodeProvider nodeProvider) {
        return NotificationListenerFactory.createNotificationListener(
                notificationListenerClass, constructorParameters,
                nodeProvider.getANode());
    }

    private static <T extends NotificationListener<?>> T createNotificationListener(Class<T> notificationListenerClass,
                                                                                    Object[] constructorParameters,
                                                                                    Node node) {
        try {
            T notificationListener =
                    PAActiveObject.newActive(
                            notificationListenerClass, constructorParameters,
                            node);

            log.info("Notification listener "
                    + notificationListenerClass.getName() + " created");

            return notificationListener;
        } catch (ActiveObjectCreationException e) {
            throw new IllegalStateException(e);
        } catch (NodeException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Lookups a notification listener active object on the specified
     * {@code aoUri}.
     * 
     * @param notificationListenerClass
     *            the class of the notification listener active object.
     * @param aoUri
     *            the URL of the notification listener active object.
     * 
     * @return the reference on the notification listener active object.
     * 
     * @throws IOException
     *             if an error occurs during the construction of the stub.
     */
    public static <T extends NotificationListener<?>> T lookupNotificationListener(Class<T> notificationListenerClass,
                                                                                   String aoUri)
            throws IOException {
        try {
            return PAActiveObject.lookupActive(notificationListenerClass, aoUri);
        } catch (ActiveObjectCreationException e) {
            throw new IOException(e);
        }
    }

}
