package org.objectweb.proactive.extensions.p2p.structured.api;

import java.util.concurrent.Callable;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayType;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;

/**
 * {@link TrackerFactory} provides some static methods in order to ease the
 * creation of tracker active objects.
 * 
 * @author lpellegr
 */
public class TrackerFactory {

	/**
	 * Creates a new tracker active object. The tracker is deployed by default
	 * on the JVM from which this method is called and is associated to the
	 * network named <code>default</code>.
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
	 * Creates a new tracker active object. The tracker is deployed by using the
	 * specified <code>node</code>. Moreover, the tracker is by default
	 * associated to the network named <code>default</code>.
	 * 
	 * @param type
	 *            the type of network the tracker manages.
	 * 
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
	 * Creates a new tracker active object on the local JVM and associates it to
	 * the specified <code>networkName</code>.
	 * 
	 * @param type
	 *            the type of network the tracker manages.
	 * 
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
	 * and associates it to the given <code>networkName</code>.
	 * 
	 * @param type
	 *            the type of network the tracker manages.
	 * 
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
        return PAActiveObject.newActive(Tracker.class, new Object[] { type, networkName }, node);
    }

    /**
     * Creates the specified number of trackers in parallel.
	 *
	 * @param type
	 *            the type of network the tracker manages.
	 *
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
    
}
