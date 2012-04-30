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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.component.adl.FactoryFactory;
import org.objectweb.proactive.core.component.adl.nodes.ADLNodeProvider;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.factories.AbstractFactory;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.configuration.EventCloudProperties;

/**
 * This class is used to create and to deploy an {@link EventCloudsRegistry} as
 * an active object.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public class EventCloudsRegistryFactory extends AbstractFactory {

    private static final Logger log;

    protected static Factory factory;

    static {
        log = LoggerFactory.getLogger(EventCloudsRegistryFactory.class);

        CentralPAPropertyRepository.GCM_PROVIDER.setValue(P2PStructuredProperties.GCM_PROVIDER.getValue());
        try {
            factory = FactoryFactory.getFactory();
        } catch (ADLException e) {
            e.printStackTrace();
        }
    }

    private EventCloudsRegistryFactory() {
    }

    public static EventCloudsRegistry newEventCloudsRegistry() {
        return EventCloudsRegistryFactory.createEventCloudsRegistry(new HashMap<String, Object>());
    }

    public static EventCloudsRegistry newEventCloudsRegistry(Node node) {
        Map<String, Object> context = new HashMap<String, Object>();
        if (node != null) {
            List<Node> nodeList = new ArrayList<Node>(1);
            nodeList.add(node);
            context.put(ADLNodeProvider.NODES_ID, nodeList);
        }
        return EventCloudsRegistryFactory.createEventCloudsRegistry(context);
    }

    public static EventCloudsRegistry newEventCloudsRegistry(GCMVirtualNode vn) {
        Map<String, Object> context = new HashMap<String, Object>();
        if (vn != null) {
            context.put(vn.getName(), vn);
        }
        return EventCloudsRegistryFactory.createEventCloudsRegistry(context);
    }

    public static EventCloudsRegistry newEventCloudsRegistry(GCMApplication gcma) {
        Map<String, Object> context = new HashMap<String, Object>();
        if (gcma != null) {
            context.put("deployment-descriptor", gcma);
        }
        return EventCloudsRegistryFactory.createEventCloudsRegistry(context);
    }

    private static EventCloudsRegistry createEventCloudsRegistry(Map<String, Object> context) {
        try {
            Component registry =
                    (Component) factory.newComponent(
                            EventCloudProperties.EVENTCLOUDS_REGISTRY_ADL.getValue(),
                            context);

            EventCloudsRegistry stub =
                    (EventCloudsRegistry) registry.getFcInterface(EventCloudProperties.EVENTCLOUDS_REGISTRY_SERVICES_ITF.getValue());

            GCM.getGCMLifeCycleController(registry).startFc();

            log.info("EventCloudsRegistry created");

            return stub;
        } catch (ADLException e) {
            throw new IllegalStateException(e);
        } catch (NoSuchInterfaceException e) {
            throw new IllegalStateException(e);
        } catch (IllegalLifeCycleException e) {
            throw new IllegalStateException(e);
        }
    }

}
