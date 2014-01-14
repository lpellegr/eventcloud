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
package org.objectweb.proactive.extensions.p2p.structured.factories;

import java.util.HashMap;
import java.util.Map;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.p2p.structured.deployment.DeploymentConfiguration;
import org.objectweb.proactive.extensions.p2p.structured.deployment.NodeProvider;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;
import org.objectweb.proactive.extensions.p2p.structured.tracker.TrackerAttributeController;
import org.objectweb.proactive.extensions.p2p.structured.tracker.TrackerImpl;
import org.objectweb.proactive.extensions.p2p.structured.utils.ComponentUtils;
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
                new HashMap<String, Object>(), null, "default");
    }

    /**
     * Creates a new tracker component deployed on the local JVM, by using the
     * specified deployment configuration and associates it to the network name
     * "default".
     * 
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * 
     * @return the reference on the {@link Tracker} interface of the new tracker
     *         component created.
     */
    public static Tracker newTracker(DeploymentConfiguration deploymentConfiguration) {
        return TrackerFactory.createTracker(
                new HashMap<String, Object>(), deploymentConfiguration,
                "default");
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
                new HashMap<String, Object>(), null, networkName);
    }

    /**
     * Creates a new tracker component deployed on the local JVM, by using the
     * specified deployment configuration and associates it to the specified
     * {@code networkName}.
     * 
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * @param networkName
     *            the name of the network the tracker manages.
     * 
     * @return the reference on the {@link Tracker} interface of the new tracker
     *         component created.
     */
    public static Tracker newTracker(DeploymentConfiguration deploymentConfiguration,
                                     String networkName) {
        return TrackerFactory.createTracker(
                new HashMap<String, Object>(), deploymentConfiguration,
                networkName);
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
        return TrackerFactory.newTracker(node, null, "default");
    }

    /**
     * Creates a new tracker component deployed on the specified {@code node},
     * by using the specified deployment configuration and associates it to the
     * network name "default".
     * 
     * @param node
     *            the node to be used for deployment.
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * 
     * @return the reference on the {@link Tracker} interface of the new tracker
     *         component created.
     */
    public static Tracker newTracker(Node node,
                                     DeploymentConfiguration deploymentConfiguration) {
        return TrackerFactory.newTracker(
                node, deploymentConfiguration, "default");
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
        return TrackerFactory.newTracker(vn, null, "default");
    }

    /**
     * Creates a new tracker component deployed on the specified
     * {@code GCM virtual node}, by using the specified deployment configuration
     * and associates it to the network name "default".
     * 
     * @param vn
     *            the GCM virtual node to be used for deployment.
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * 
     * @return the reference on the {@link Tracker} interface of the new tracker
     *         component created.
     */
    public static Tracker newTracker(GCMVirtualNode vn,
                                     DeploymentConfiguration deploymentConfiguration) {
        return TrackerFactory.newTracker(vn, deploymentConfiguration, "default");
    }

    /**
     * Creates a new tracker component deployed on a node provided by the
     * specified {@code node provider} and associates it to the network name
     * "default".
     * 
     * @param nodeProvider
     *            the node provider to be used for deployment.
     * 
     * @return the reference on the {@link Tracker} interface of the new tracker
     *         component created.
     */
    public static Tracker newTracker(NodeProvider nodeProvider) {
        return TrackerFactory.newTracker(nodeProvider, null, "default");
    }

    /**
     * Creates a new tracker component deployed on a node provided by the
     * specified {@code node provider}, by using the specified deployment
     * configuration and associates it to the network name "default".
     * 
     * @param nodeProvider
     *            the node provider to be used for deployment.
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * 
     * @return the reference on the {@link Tracker} interface of the new tracker
     *         component created.
     */
    public static Tracker newTracker(NodeProvider nodeProvider,
                                     DeploymentConfiguration deploymentConfiguration) {
        return TrackerFactory.newTracker(
                nodeProvider, deploymentConfiguration, "default");
    }

    /**
     * Creates a new tracker component deployed on the specified {@code node}
     * and associates it to the specified {@code networkName}.
     * 
     * @param node
     *            the node to be used for deployment.
     * @param networkName
     *            the name of the network the tracker manages.
     * 
     * @return the reference on the {@link Tracker} interface of the new tracker
     *         component created.
     */
    public static Tracker newTracker(Node node, String networkName) {
        return TrackerFactory.newTracker(node, null, networkName);
    }

    /**
     * Creates a new tracker component deployed on the specified {@code node},
     * by using the specified deployment configuration and associates it to the
     * specified {@code networkName}.
     * 
     * @param node
     *            the node to be used for deployment.
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * @param networkName
     *            the name of the network the tracker manages.
     * 
     * @return the reference on the {@link Tracker} interface of the new tracker
     *         component created.
     */
    public static Tracker newTracker(Node node,
                                     DeploymentConfiguration deploymentConfiguration,
                                     String networkName) {
        return TrackerFactory.createTracker(
                ComponentUtils.createContext(node), deploymentConfiguration,
                networkName);
    }

    /**
     * Creates a new tracker component deployed on the specified
     * {@code GCM virtual node} and associates it to the specified
     * {@code networkName}.
     * 
     * @param vn
     *            the GCM virtual node to be used for deployment.
     * @param networkName
     *            the name of the network the tracker manages.
     * 
     * @return the reference on the {@link Tracker} interface of the new tracker
     *         component created.
     */
    public static Tracker newTracker(GCMVirtualNode vn, String networkName) {
        return TrackerFactory.newTracker(vn, null, networkName);
    }

    /**
     * Creates a new tracker component deployed on the specified
     * {@code GCM virtual node}, by using the specified deployment configuration
     * and associates it to the specified {@code networkName}.
     * 
     * @param vn
     *            the GCM virtual node to be used for deployment.
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * @param networkName
     *            the name of the network the tracker manages.
     * 
     * @return the reference on the {@link Tracker} interface of the new tracker
     *         component created.
     */
    public static Tracker newTracker(GCMVirtualNode vn,
                                     DeploymentConfiguration deploymentConfiguration,
                                     String networkName) {
        return TrackerFactory.createTracker(
                ComponentUtils.createContext(vn), deploymentConfiguration,
                networkName);
    }

    /**
     * Creates a new tracker component deployed on a node provided by the
     * specified {@code node provider} and associates it to the specified
     * {@code networkName}.
     * 
     * @param nodeProvider
     *            the node provider to be used for deployment.
     * @param networkName
     *            the name of the network the tracker manages.
     * 
     * @return the reference on the {@link Tracker} interface of the new tracker
     *         component created.
     */
    public static Tracker newTracker(NodeProvider nodeProvider,
                                     String networkName) {
        return TrackerFactory.newTracker(nodeProvider, null, networkName);
    }

    /**
     * Creates a new tracker component deployed on a node provided by the
     * specified {@code node provider}, by using the specified deployment
     * configuration and associates it to the specified {@code networkName}.
     * 
     * @param nodeProvider
     *            the node provider to be used for deployment.
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * @param networkName
     *            the name of the network the tracker manages.
     * 
     * @return the reference on the {@link Tracker} interface of the new tracker
     *         component created.
     */
    public static Tracker newTracker(NodeProvider nodeProvider,
                                     DeploymentConfiguration deploymentConfiguration,
                                     String networkName) {
        return TrackerFactory.createTracker(
                AbstractFactory.getContextFromNodeProvider(
                        nodeProvider, TrackerImpl.TRACKER_VN),
                deploymentConfiguration, networkName);
    }

    private static Tracker createTracker(Map<String, Object> context,
                                         DeploymentConfiguration deploymentConfiguration,
                                         String networkName) {
        try {
            Tracker tracker =
                    ComponentUtils.createComponentAndGetInterface(
                            TrackerImpl.TRACKER_ADL, context,
                            TrackerImpl.TRACKER_SERVICES_ITF, Tracker.class,
                            true);

            TrackerAttributeController trackerAttributeController =
                    (TrackerAttributeController) GCM.getAttributeController(((Interface) tracker).getFcItfOwner());
            if (deploymentConfiguration != null) {
                ((TrackerAttributeController) GCM.getAttributeController(((Interface) tracker).getFcItfOwner())).setDeploymentConfiguration(deploymentConfiguration);
            }
            trackerAttributeController.initAttributes(tracker, networkName);

            log.info(
                    "Tracker {} associated to network named '{}' created",
                    tracker.getId(), networkName);

            return tracker;
        } catch (NoSuchInterfaceException e) {
            throw new IllegalStateException(e);
        }
    }

}
