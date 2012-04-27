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
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;
import org.objectweb.proactive.extensions.p2p.structured.tracker.TrackerAttributeController;
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

    private static final Logger logger =
            LoggerFactory.getLogger(TrackerFactory.class);

    private static Factory factory;

    static {
        CentralPAPropertyRepository.GCM_PROVIDER.setValue(P2PStructuredProperties.GCM_PROVIDER.getValue());
        try {
            factory = FactoryFactory.getFactory();
        } catch (ADLException e) {
            e.printStackTrace();
        }
    }

    private TrackerFactory() {
    }

    /**
     * Creates a new tracker component on the local JVM and associates it to the
     * network name "default".
     * 
     * @return the reference on the {@link Tracker} interface of the new tracker
     *         component created.
     */
    public static Tracker newTracker() {
        return TrackerFactory.newTracker(
                "default", new HashMap<String, Object>());
    }

    /**
     * Creates a new tracker component on the local JVM and associates it to the
     * specified {@code networkName}.
     * 
     * @param networkName
     *            the name of the network the tracker manages.
     * 
     * @return the reference on the {@link Tracker} interface of the new tracker
     *         component created.
     */
    public static Tracker newTracker(String networkName) {
        return TrackerFactory.newTracker(
                networkName, new HashMap<String, Object>());
    }

    /**
     * Creates a new tracker component on the specified {@code node} and
     * associates it to the given {@code networkName}.
     * 
     * @param networkName
     *            the name of the network the tracker manages.
     * @param node
     *            the node to use for deployment.
     * 
     * @return the reference on the {@link Tracker} interface of the new tracker
     *         component created.
     */
    public static Tracker newTracker(String networkName, Node node) {
        Map<String, Object> context = new HashMap<String, Object>();
        if (node != null) {
            List<Node> nodeList = new ArrayList<Node>(1);
            nodeList.add(node);
            context.put(ADLNodeProvider.NODES_ID, nodeList);
        }
        return TrackerFactory.newTracker(networkName, context);
    }

    /**
     * Creates a new tracker component on the specified {@code GCM virtual node}
     * and associates it to the given {@code networkName}.
     * 
     * @param networkName
     *            the name of the network the tracker manages.
     * @param vn
     *            the GCM virtual node to use for deployment.
     * 
     * @return the reference on the {@link Tracker} interface of the new tracker
     *         component created.
     */
    public static Tracker newTracker(String networkName, GCMVirtualNode vn) {
        Map<String, Object> context = new HashMap<String, Object>();
        if (vn != null) {
            context.put(vn.getName(), vn);
        }
        return TrackerFactory.newTracker(networkName, context);
    }

    /**
     * Creates a new tracker component by using the specified
     * {@code GCM virtual node} and associates it to the given
     * {@code networkName}.
     * 
     * @param networkName
     *            the name of the network the tracker manages.
     * @param gcma
     *            the GCM deployment to use for deployment.
     * 
     * @return the reference on the {@link Tracker} interface of the new tracker
     *         component created.
     */
    public static Tracker newTracker(String networkName, GCMApplication gcma) {
        Map<String, Object> context = new HashMap<String, Object>();
        if (gcma != null) {
            context.put("deployment-descriptor", gcma);
        }
        return TrackerFactory.newTracker(networkName, context);
    }

    private static Tracker newTracker(String networkName,
                                      Map<String, Object> context) {
        try {
            Component tracker =
                    (Component) factory.newComponent(
                            P2PStructuredProperties.TRACKER_ADL.getValue(),
                            context);
            Tracker stub =
                    (Tracker) tracker.getFcInterface(P2PStructuredProperties.TRACKER_SERVICES_ITF.getValue());

            ((TrackerAttributeController) GCM.getAttributeController(tracker)).setAttributes(
                    stub, networkName);

            GCM.getGCMLifeCycleController(tracker).startFc();

            logger.info(
                    "Tracker {} associated to network named '{}' has been created",
                    stub.getId(), networkName);

            return stub;
        } catch (ADLException e) {
            e.printStackTrace();
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
        } catch (IllegalLifeCycleException e) {
            e.printStackTrace();
        }

        return null;
    }

}
