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
package fr.inria.eventcloud.factories;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.p2p.structured.CommonAttributeController;
import org.objectweb.proactive.extensions.p2p.structured.deployment.DeploymentConfiguration;
import org.objectweb.proactive.extensions.p2p.structured.deployment.NodeProvider;
import org.objectweb.proactive.extensions.p2p.structured.factories.AbstractFactory;
import org.objectweb.proactive.extensions.p2p.structured.utils.ComponentUtils;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.EventCloudsRegistryImpl;

/**
 * This class is used to create and deploy an {@link EventCloudsRegistry} as a
 * component.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public class EventCloudsRegistryFactory extends AbstractFactory {

    private static final Logger log =
            LoggerFactory.getLogger(EventCloudsRegistryFactory.class);

    private EventCloudsRegistryFactory() {
    }

    /**
     * Creates a new EventClouds registry component deployed on the local JVM.
     * 
     * @return the reference on the {@link EventCloudsRegistry} interface of the
     *         new EventClouds registry component created.
     */
    public static EventCloudsRegistry newEventCloudsRegistry() {
        return EventCloudsRegistryFactory.createEventCloudsRegistry(
                new HashMap<String, Object>(), null);
    }

    /**
     * Creates a new EventClouds registry component deployed on the local JVM
     * and by using the specified deployment configuration.
     * 
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * 
     * @return the reference on the {@link EventCloudsRegistry} interface of the
     *         new EventClouds registry component created.
     */
    public static EventCloudsRegistry newEventCloudsRegistry(DeploymentConfiguration deploymentConfiguration) {
        return EventCloudsRegistryFactory.createEventCloudsRegistry(
                new HashMap<String, Object>(), deploymentConfiguration);
    }

    /**
     * Creates a new EventClouds registry component deployed on the specified
     * {@code node}.
     * 
     * @param node
     *            the node to be used for deployment.
     * 
     * @return the reference on the {@link EventCloudsRegistry} interface of the
     *         new EventClouds registry component created.
     */
    public static EventCloudsRegistry newEventCloudsRegistry(Node node) {
        return EventCloudsRegistryFactory.newEventCloudsRegistry(node, null);
    }

    /**
     * Creates a new EventClouds registry component deployed on the specified
     * {@code node} and by using the specified deployment configuration.
     * 
     * @param node
     *            the node to be used for deployment.
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * 
     * @return the reference on the {@link EventCloudsRegistry} interface of the
     *         new EventClouds registry component created.
     */
    public static EventCloudsRegistry newEventCloudsRegistry(Node node,
                                                             DeploymentConfiguration deploymentConfiguration) {
        return EventCloudsRegistryFactory.createEventCloudsRegistry(
                ComponentUtils.createContext(node), deploymentConfiguration);
    }

    /**
     * Creates a new EventClouds registry component deployed on the specified
     * {@code GCM virtual node}.
     * 
     * @param vn
     *            the GCM virtual node to be used for deployment.
     * 
     * @return the reference on the {@link EventCloudsRegistry} interface of the
     *         new EventClouds registry component created.
     */
    public static EventCloudsRegistry newEventCloudsRegistry(GCMVirtualNode vn) {
        return EventCloudsRegistryFactory.newEventCloudsRegistry(vn, null);
    }

    /**
     * Creates a new EventClouds registry component deployed on the specified
     * {@code GCM virtual node} and by using the specified deployment
     * configuration.
     * 
     * @param vn
     *            the GCM virtual node to be used for deployment.
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * 
     * @return the reference on the {@link EventCloudsRegistry} interface of the
     *         new EventClouds registry component created.
     */
    public static EventCloudsRegistry newEventCloudsRegistry(GCMVirtualNode vn,
                                                             DeploymentConfiguration deploymentConfiguration) {
        return EventCloudsRegistryFactory.createEventCloudsRegistry(
                ComponentUtils.createContext(vn), deploymentConfiguration);
    }

    /**
     * Creates a new EventClouds registry component deployed on a node provided
     * by the specified {@code node provider}.
     * 
     * @param nodeProvider
     *            the node provider to be used for deployment.
     * 
     * @return the reference on the {@link EventCloudsRegistry} interface of the
     *         new EventClouds registry component created.
     */
    public static EventCloudsRegistry newEventCloudsRegistry(NodeProvider nodeProvider) {
        return EventCloudsRegistryFactory.newEventCloudsRegistry(
                nodeProvider, null);
    }

    /**
     * Creates a new EventClouds registry component deployed on a node provided
     * by the specified {@code node provider} and by using the specified
     * deployment configuration.
     * 
     * @param nodeProvider
     *            the node provider to be used for deployment.
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * 
     * @return the reference on the {@link EventCloudsRegistry} interface of the
     *         new EventClouds registry component created.
     */
    public static EventCloudsRegistry newEventCloudsRegistry(NodeProvider nodeProvider,
                                                             DeploymentConfiguration deploymentConfiguration) {
        return EventCloudsRegistryFactory.createEventCloudsRegistry(
                AbstractFactory.getContextFromNodeProvider(
                        nodeProvider, EventCloudsRegistryImpl.REGISTRY_VN),
                deploymentConfiguration);
    }

    private static EventCloudsRegistry createEventCloudsRegistry(Map<String, Object> context,
                                                                 DeploymentConfiguration deploymentConfiguration) {
        try {
            EventCloudsRegistry registry =
                    ComponentUtils.createComponentAndGetInterface(
                            EventCloudsRegistryImpl.EVENTCLOUDS_REGISTRY_ADL,
                            context,
                            EventCloudsRegistryImpl.EVENTCLOUDS_REGISTRY_SERVICES_ITF,
                            EventCloudsRegistry.class, true);

            if (deploymentConfiguration != null) {
                ((CommonAttributeController) GCM.getAttributeController(((Interface) registry).getFcItfOwner())).setDeploymentConfiguration(deploymentConfiguration);
            }

            log.info("EventCloudsRegistry created");

            return registry;
        } catch (NoSuchInterfaceException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Lookups an EventClouds registry component on the specified
     * {@code componentUri}.
     * 
     * @param componentUri
     *            the URL of the EventClouds registry component.
     * 
     * @return the reference on the {@link EventCloudsRegistry} interface of the
     *         EventClouds registry component.
     * 
     * @throws IOException
     *             if an error occurs during the construction of the stub.
     */
    public static EventCloudsRegistry lookupEventCloudsRegistry(String componentUri)
            throws IOException {
        return ComponentUtils.lookupFcInterface(
                componentUri,
                EventCloudsRegistryImpl.EVENTCLOUDS_REGISTRY_SERVICES_ITF,
                EventCloudsRegistry.class);
    }

}
