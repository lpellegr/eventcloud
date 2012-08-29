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
package org.objectweb.proactive.extensions.p2p.structured.factories;

import java.util.HashMap;
import java.util.Map;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;
import org.objectweb.proactive.extensions.p2p.structured.tracker.TrackerAttributeController;
import org.objectweb.proactive.extensions.p2p.structured.tracker.TrackerImpl;
import org.objectweb.proactive.extensions.p2p.structured.utils.ComponentUtils;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link TrackerFactory} provides some static methods in order to ease the
 * creation of tracker components.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public class TrackerFactory extends AbstractFactory {

    private static final Logger log =
            LoggerFactory.getLogger(TrackerFactory.class);

    private TrackerFactory() {
    }

    /**
     * Creates a new tracker component deployed on the local JVM and associates
     * it to the network name "default".
     * 
     * @return the reference on the {@link Tracker} interface of the new tracker
     *         component created.
     */
    public static Tracker newTracker() {
        return TrackerFactory.createTracker(
                "default", new HashMap<String, Object>());
    }

    /**
     * Creates a new tracker component deployed on the local JVM and associates
     * it to the specified {@code networkName}.
     * 
     * @param networkName
     *            the name of the network the tracker manages.
     * 
     * @return the reference on the {@link Tracker} interface of the new tracker
     *         component created.
     */
    public static Tracker newTracker(String networkName) {
        return TrackerFactory.createTracker(
                networkName, new HashMap<String, Object>());
    }

    /**
     * Creates a new tracker component deployed on the specified {@code node}
     * and associates it to the network name "default".
     * 
     * @param node
     *            the node to be used for deployment.
     * 
     * @return the reference on the {@link Tracker} interface of the new tracker
     *         component created.
     */
    public static Tracker newTracker(Node node) {
        return TrackerFactory.newTracker("default", node);
    }

    /**
     * Creates a new tracker component deployed on the specified
     * {@code GCM virtual node} and associates it to the network name "default".
     * 
     * @param vn
     *            the GCM virtual node to be used for deployment.
     * 
     * @return the reference on the {@link Tracker} interface of the new tracker
     *         component created.
     */
    public static Tracker newTracker(GCMVirtualNode vn) {
        return TrackerFactory.newTracker("default", vn);
    }

    /**
     * Creates a new tracker component deployed on a {@code node} provided by
     * the specified GCM application and associates it to the network name
     * "default".
     * 
     * @param gcma
     *            the GCM application to be used for deployment.
     * 
     * @return the reference on the {@link Tracker} interface of the new tracker
     *         component created.
     */
    public static Tracker newTracker(GCMApplication gcma) {
        return TrackerFactory.newTracker("default", gcma);
    }

    /**
     * Creates a new tracker component deployed on the specified {@code node}
     * and associates it to the given {@code networkName}.
     * 
     * @param networkName
     *            the name of the network the tracker manages.
     * @param node
     *            the node to be used for deployment.
     * 
     * @return the reference on the {@link Tracker} interface of the new tracker
     *         component created.
     */
    public static Tracker newTracker(String networkName, Node node) {
        return TrackerFactory.createTracker(
                networkName, ComponentUtils.createContext(node));
    }

    /**
     * Creates a new tracker component deployed on the specified
     * {@code GCM virtual node} and associates it to the given
     * {@code networkName}.
     * 
     * @param networkName
     *            the name of the network the tracker manages.
     * @param vn
     *            the GCM virtual node to be used for deployment.
     * 
     * @return the reference on the {@link Tracker} interface of the new tracker
     *         component created.
     */
    public static Tracker newTracker(String networkName, GCMVirtualNode vn) {
        return TrackerFactory.createTracker(
                networkName, ComponentUtils.createContext(vn));
    }

    /**
     * Creates a new tracker component deployed on a {@code node} provided by
     * the specified GCM application and associates it to the given
     * {@code networkName}.
     * 
     * @param networkName
     *            the name of the network the tracker manages.
     * @param gcma
     *            the GCM application to be used for deployment.
     * 
     * @return the reference on the {@link Tracker} interface of the new tracker
     *         component created.
     */
    public static Tracker newTracker(String networkName, GCMApplication gcma) {
        return TrackerFactory.createTracker(
                networkName, ComponentUtils.createContext(gcma));
    }

    private static Tracker createTracker(String networkName,
                                         Map<String, Object> context) {
        try {
            Tracker tracker =
                    ComponentUtils.createComponentAndGetInterface(
                            TrackerImpl.TRACKER_ADL, context,
                            TrackerImpl.TRACKER_SERVICES_ITF, Tracker.class,
                            true);

            ((TrackerAttributeController) GCM.getAttributeController(((Interface) tracker).getFcItfOwner())).setAttributes(
                    tracker, networkName);

            log.info(
                    "Tracker {} associated to network named '{}' created",
                    tracker.getId(), networkName);

            return tracker;
        } catch (NoSuchInterfaceException e) {
            throw new IllegalStateException(e);
        }
    }

}
