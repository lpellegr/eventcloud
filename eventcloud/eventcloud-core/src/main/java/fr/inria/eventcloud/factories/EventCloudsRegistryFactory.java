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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.p2p.structured.utils.ComponentUtils;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
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
public class EventCloudsRegistryFactory {

    private static final Logger log =
            LoggerFactory.getLogger(EventCloudsRegistryFactory.class);

    private EventCloudsRegistryFactory() {
    }

    /**
     * Lookups a ProActive stub representation for the specified
     * {@code componentUri}.
     * 
     * @param componentUri
     *            the URL of the component.
     * 
     * @return a ProActive stub representation of an EventCloudsRegistry.
     * 
     * @throws IOException
     *             if an error occurs during the construction of the stub.
     */
    public static EventCloudsRegistry from(String componentUri)
            throws IOException {
        return ComponentUtils.lookupFcInterface(
                componentUri,
                EventCloudsRegistryImpl.EVENTCLOUDS_REGISTRY_SERVICES_ITF,
                EventCloudsRegistry.class);
    }

    /**
     * Creates a new EventClouds registry component deployed on the local JVM.
     * 
     * @return the reference on the {@link EventCloudsRegistry} interface of the
     *         new EventClouds registry component created.
     */
    public static EventCloudsRegistry newEventCloudsRegistry() {
        return EventCloudsRegistryFactory.createEventCloudsRegistry(new HashMap<String, Object>());
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
        return EventCloudsRegistryFactory.createEventCloudsRegistry(ComponentUtils.createContext(node));
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
        return EventCloudsRegistryFactory.createEventCloudsRegistry(ComponentUtils.createContext(vn));
    }

    /**
     * Creates a new EventClouds registry component deployed on a {@code node}
     * provided by the specified GCM application.
     * 
     * @param gcma
     *            the GCM application to be used for deployment.
     * 
     * @return the reference on the {@link EventCloudsRegistry} interface of the
     *         new EventClouds registry component created.
     */
    public static EventCloudsRegistry newEventCloudsRegistry(GCMApplication gcma) {
        return EventCloudsRegistryFactory.createEventCloudsRegistry(ComponentUtils.createContext(gcma));
    }

    private static EventCloudsRegistry createEventCloudsRegistry(Map<String, Object> context) {
        EventCloudsRegistry registry =
                ComponentUtils.createComponentAndGetInterface(
                        EventCloudsRegistryImpl.EVENTCLOUDS_REGISTRY_ADL,
                        context,
                        EventCloudsRegistryImpl.EVENTCLOUDS_REGISTRY_SERVICES_ITF,
                        EventCloudsRegistry.class, true);

        log.info("EventCloudsRegistry created");

        return registry;
    }

}
