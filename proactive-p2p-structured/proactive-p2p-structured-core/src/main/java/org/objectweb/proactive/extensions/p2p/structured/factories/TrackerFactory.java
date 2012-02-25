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
import java.util.concurrent.Callable;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.component.adl.FactoryFactory;
import org.objectweb.proactive.core.component.adl.nodes.ADLNodeProvider;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;
import org.objectweb.proactive.extensions.p2p.structured.tracker.TrackerImpl;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link TrackerFactory} provides some static methods in order to ease the
 * creation of tracker active objects.
 * 
 * @author lpellegr
 */
public class TrackerFactory {

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
     * Creates a new active tracker on the local JVM and associates it to the
     * network named "default".
     * 
     * @return the new active tracker created.
     */
    public static Tracker newActiveTracker() {
        return TrackerFactory.newActiveTracker("default", null);
    }

    /**
     * Creates a new active tracker on the local JVM and associates it to the
     * specified {@code networkName}.
     * 
     * @param networkName
     *            the network name managed by the tracker.
     * 
     * @return the new active tracker created.
     */
    public static Tracker newActiveTracker(String networkName) {
        return TrackerFactory.newActiveTracker(networkName, null);
    }

    /**
     * Creates a new tracker active object on the specified {@code node} and
     * associates it to the given {@code networkName}.
     * 
     * @param networkName
     *            the network name managed by the tracker.
     * @param node
     *            the node to use for deployment.
     * 
     * @return the new tracker active object created.
     */
    public static Tracker newActiveTracker(String networkName, Node node) {
        try {
            Tracker tracker =
                    PAActiveObject.newActive(
                            TrackerImpl.class, new Object[] {networkName}, node);

            logger.info(
                    "Tracker {} associated to network named '{}' has been created",
                    tracker.getId(), networkName);

            return tracker;
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Creates the specified {@code number} of active trackers in parallel. Each
     * tracker manages the same network, namely {@code networkName}.
     * 
     * @param number
     *            the number of trackers to create.
     * 
     * @param networkName
     *            the name of the network each tracker belongs to.
     * 
     * @return the trackers created.
     */
    public static Tracker[] newActiveTrackerInParallel(int number,
                                                       final String networkName) {
        return Executor.execute(Tracker.class, new Callable<Tracker>() {
            public Tracker call() throws Exception {
                return TrackerFactory.newActiveTracker(networkName);
            }
        }, number);
    }

    /**
     * Creates a new tracker component on the local JVM and associates it to the
     * network name "default".
     * 
     * @return the reference on the {@link Tracker} interface of the new tracker
     *         component created.
     */
    public static Tracker newComponentTracker() {
        return TrackerFactory.newComponentTracker(
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
    public static Tracker newComponentTracker(String networkName) {
        return TrackerFactory.newComponentTracker(
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
    public static Tracker newComponentTracker(String networkName, Node node) {
        Map<String, Object> context = new HashMap<String, Object>();
        if (node != null) {
            List<Node> nodeList = new ArrayList<Node>(1);
            nodeList.add(node);
            context.put(ADLNodeProvider.NODES_ID, nodeList);
        }
        return TrackerFactory.newComponentTracker(networkName, context);
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
    public static Tracker newComponentTracker(String networkName,
                                              GCMVirtualNode vn) {
        Map<String, Object> context = new HashMap<String, Object>();
        if (vn != null) {
            context.put(vn.getName(), vn);
        }
        return TrackerFactory.newComponentTracker(networkName, context);
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
    public static Tracker newComponentTracker(String networkName,
                                              GCMApplication gcma) {
        Map<String, Object> context = new HashMap<String, Object>();
        if (gcma != null) {
            context.put("deployment-descriptor", gcma);
        }
        return TrackerFactory.newComponentTracker(networkName, context);
    }

    private static Tracker newComponentTracker(String networkName,
                                               Map<String, Object> context) {
        try {
            Component tracker =
                    (Component) factory.newComponent(
                            P2PStructuredProperties.TRACKER_ADL.getValue(),
                            context);
            Tracker stub =
                    (Tracker) tracker.getFcInterface(P2PStructuredProperties.TRACKER_SERVICES_ITF.getValue());
            stub.init(stub, networkName);
            GCM.getGCMLifeCycleController(tracker).startFc();

            logger.info(
                    "ComponentTracker {} associated to network named '{}' has been created",
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
