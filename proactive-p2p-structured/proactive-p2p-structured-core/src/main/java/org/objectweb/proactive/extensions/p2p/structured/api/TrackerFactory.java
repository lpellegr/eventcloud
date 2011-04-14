package org.objectweb.proactive.extensions.p2p.structured.api;

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
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayType;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;
import org.objectweb.proactive.extensions.p2p.structured.tracker.TrackerImpl;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

/**
 * {@link TrackerFactory} provides some static methods in order to ease the
 * creation of tracker active objects.
 * 
 * @author lpellegr
 */
public class TrackerFactory {
    private static Factory factory;

    static {
        CentralPAPropertyRepository.GCM_PROVIDER.setValue(P2PStructuredProperties.GCM_PROVIDER.getValue());
        try {
            factory = FactoryFactory.getFactory();
        } catch (ADLException e) {
            e.printStackTrace();
        }
    }

	/**
	 * Creates a new tracker active object on the local JVM and associates it to
	 * the network named <code>default</code>.
	 * 
	 * @param type
	 *            the type of network the tracker manages.
	 * 
	 * @return the new tracker active object created.
	 * 
	 * @throws ActiveObjectCreationException
	 *             if a problem occurs during the creation of the active object.
	 * @throws NodeException
	 *             if the node on which the tracker must be deployed has some
	 *             problems.
	 */
    public static Tracker newActiveTracker(OverlayType type) 
            throws ActiveObjectCreationException, NodeException {
        return TrackerFactory.newActiveTracker(type, "default");
    }

    /**
     * Creates a new tracker active object on the local JVM and associates it to
     * the specified <code>networkName</code>.
     * 
     * @param type
     *            the type of network the tracker manages.
     * @param networkName
     *            the name of the network the tracker manages.
     * 
     * @return the new tracker active object created.
     * 
     * @throws ActiveObjectCreationException
     *             if a problem occurs during the creation of the active object.
     */
    public static Tracker newActiveTracker(OverlayType type, String networkName)
            throws ActiveObjectCreationException, NodeException {
        return TrackerFactory.newActiveTracker(type, networkName, null);
    }

	/**
	 * Creates a new tracker active object on the specified <code>node</code>
     * and associates it to the network named <code>default</code>.
	 * 
	 * @param type
	 *            the type of network the tracker manages.
	 * @param node
	 *            the node to use for deployment.
	 * 
	 * @return the new tracker active object created.
	 * 
	 * @throws ActiveObjectCreationException
	 *             if a problem occurs during the creation of the active object.
	 * @throws NodeException
	 *             if the node on which the tracker must be deployed has some
	 *             problems.
	 */
    public static Tracker newActiveTracker(OverlayType type, Node node)
            throws ActiveObjectCreationException, NodeException {
        return TrackerFactory.newActiveTracker(type, "default", node);
    }

	/**
	 * Creates a new tracker active object on the specified <code>node</code>
	 * and associates it to the given <code>networkName</code>.
	 * 
	 * @param type
	 *            the type of network the tracker manages.
	 * @param networkName
	 *            the name of the network the tracker manages.
	 * @param node
	 *            the node to use for deployment.
	 * 
	 * @return the new tracker active object created.
	 * 
	 * @throws ActiveObjectCreationException
	 *             if a problem occurs during the creation of the active object.
	 * @throws NodeException
	 *             if the node on which the tracker must be deployed has some
	 *             problems.
	 */
    public static Tracker newActiveTracker(OverlayType type, String networkName, Node node) 
    							throws ActiveObjectCreationException, NodeException {
        return PAActiveObject.newActive(TrackerImpl.class, new Object[] { type, networkName }, node);
    }

    /**
     * Creates the specified number of trackers in parallel.
	 *
	 * @param type
	 *            the type of network the tracker manages.
     * @param number
     *            the number of trackers to create.
     *            
     * @return the trackers created.
     */
    public static Tracker[] newActiveTrackerInParallel(final OverlayType type, int number) {
        return Executor.execute(Tracker.class, new Callable<Tracker>() {
            public Tracker call() throws Exception {
                return TrackerFactory.newActiveTracker(type);
            }
        }, number);
    }

    /**
     * Creates a new tracker component on the local JVM and associates it to the
     * network named <code>default</code>.
     * 
     * @param type
     *            the type of network the tracker manages.
     * 
     * @return the reference on the {@link Tracker} interface of the new tracker
     *         component created.
     */
    public static Tracker newComponentTracker(OverlayType type) {
        return TrackerFactory.newComponentTracker(type, "default", new HashMap<String, Object>());
    }

    /**
     * Creates a new tracker component on the local JVM and associates it to the
     * specified <code>networkName</code>.
     * 
     * @param type
     *            the type of network the tracker manages.
     * @param networkName
     *            the name of the network the tracker manages.
     * 
     * @return the reference on the {@link Tracker} interface of the new tracker
     *         component created.
     */
    public static Tracker newComponentTracker(OverlayType type, String networkName) {
        return TrackerFactory.newComponentTracker(type, networkName, new HashMap<String, Object>());
    }

    /**
     * Creates a new tracker component on the specified <code>node</code> and
     * associates it to the network named <code>default</code>.
     * 
     * @param type
     *            the type of network the tracker manages.
     * @param node
     *            the node to use for deployment.
     * 
     * @return the reference on the {@link Tracker} interface of the new tracker
     *         component created.
     */
    public static Tracker newComponentTracker(OverlayType type, Node node) {
        return TrackerFactory.newComponentTracker(type, "default", node);
    }

    /**
     * Creates a new tracker component on the specified <code>node</code> and
     * associates it to the given <code>networkName</code>.
     * 
     * @param type
     *            the type of network the tracker manages.
     * @param networkName
     *            the name of the network the tracker manages.
     * @param node
     *            the node to use for deployment.
     * 
     * @return the reference on the {@link Tracker} interface of the new tracker
     *         component created.
     */
    public static Tracker newComponentTracker(OverlayType type, String networkName, Node node) {
        Map<String, Object> context = new HashMap<String, Object>();
        if (node != null) {
            List<Node> nodeList = new ArrayList<Node>(1);
            nodeList.add(node);
            context.put(ADLNodeProvider.NODES_ID, nodeList);
        }
        return TrackerFactory.newComponentTracker(type, networkName, context);
    }

    /**
     * Creates a new tracker component on the specified
     * <code>GCM virtual node</code> and associates it to the network named
     * <code>default</code>.
     * 
     * @param type
     *            the type of network the tracker manages.
     * @param vn
     *            the GCM virtual node to use for deployment.
     *
     * @return the reference on the {@link Tracker} interface of the new tracker
     *         component created.
     */
    public static Tracker newComponentTracker(OverlayType type, GCMVirtualNode vn) {
        return TrackerFactory.newComponentTracker(type, "default", vn);
    }

    /**
     * Creates a new tracker component on the specified
     * <code>GCM virtual node</code> and associates it to the given
     * <code>networkName</code>.
     * 
     * @param type
     *            the type of network the tracker manages.
     * @param networkName
     *            the name of the network the tracker manages.
     * @param vn
     *            the GCM virtual node to use for deployment.
     *
     * @return the reference on the {@link Tracker} interface of the new tracker
     *         component created.
     */
    public static Tracker newComponentTracker(OverlayType type, String networkName, GCMVirtualNode vn) {
        Map<String, Object> context = new HashMap<String, Object>();
        if (vn != null) {
            context.put(vn.getName(), vn);
        }
        return TrackerFactory.newComponentTracker(type, networkName, context);
    }

    /**
     * Creates a new tracker component by using the specified
     * <code>GCM deployment</code> and associates it to the network named
     * <code>default</code>.
     * 
     * @param type
     *            the type of network the tracker manages.
     * @param gcma
     *            the GCM deployment to use for deployment.
     *
     * @return the reference on the {@link Tracker} interface of the new tracker
     *         component created.
     */
    public static Tracker newComponentTracker(OverlayType type, GCMApplication gcma) {
        return TrackerFactory.newComponentTracker(type, "default", gcma);
    }

    /**
     * Creates a new tracker component by using the specified
     * <code>GCM deployment</code> and associates it to the given
     * <code>networkName</code>.
     * 
     * @param type
     *            the type of network the tracker manages.
     * @param networkName
     *            the name of the network the tracker manages.
     * @param gcma
     *            the GCM deployment to use for deployment.
     *
     * @return the reference on the {@link Tracker} interface of the new tracker
     *         component created.
     */
    public static Tracker newComponentTracker(OverlayType type, String networkName, GCMApplication gcma) {
        Map<String, Object> context = new HashMap<String, Object>();
        if (gcma != null) {
            context.put("deployment-descriptor", gcma);
        }
        return TrackerFactory.newComponentTracker(type, networkName, context);
    }

    private static Tracker newComponentTracker(OverlayType type, String networkName, Map<String, Object> context) {
        try {
            Component tracker = (Component) factory.newComponent(P2PStructuredProperties.TRACKER_ADL.getValue(), context);
            Tracker stub = (Tracker) tracker.getFcInterface(P2PStructuredProperties.TRACKER_SERVICES_ITF.getValue());
            stub.setAssociatedNetworkName(networkName);
            stub.setStub();
            stub.setType(type);
            GCM.getGCMLifeCycleController(tracker).startFc();

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
