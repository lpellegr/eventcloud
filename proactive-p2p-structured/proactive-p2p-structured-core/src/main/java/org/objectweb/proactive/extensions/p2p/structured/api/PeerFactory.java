package org.objectweb.proactive.extensions.p2p.structured.api;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;

/**
 * Provides some static methods in order to ease the creation of peer active
 * objects.
 * 
 * @author Laurent Pellegrino
 */
public class PeerFactory {

	/**
	 * Creates a new peer active object on the local JVM by using the specified
	 * overlay abstraction.
	 * 
	 * @param overlay
	 *            the overlay to set.
	 
	 * @return the new active object created.
	 * 
	 * @throws ActiveObjectCreationException
	 *             if a problem occurs during the creation of the active object.
	 * @throws NodeException
	 *             if a problem occurs during the deployment.
	 */
    public static Peer newActivePeer(StructuredOverlay overlay)
            throws ActiveObjectCreationException, NodeException {
        return PeerFactory.newActivePeer(overlay, null);
    }

	/**
	 * Creates a new peer active object on the specified <code>node</code> by
	 * using the given overlay abstraction.
	 * 
	 * @param overlay
	 *            the overlay to set.
	 * @param node
	 *            the node used by the peer.
	 
	 * @return the new active object created.
	 * 
	 * @throws ActiveObjectCreationException
	 *             if a problem occurs during the creation of the active object.
	 * @throws NodeException
	 *             if a problem occurs during the deployment.
	 */
    public static Peer newActivePeer(StructuredOverlay overlay, Node node)
            throws ActiveObjectCreationException, NodeException {
        return (Peer) PAActiveObject.turnActive(
        				new Peer(overlay), node);
    }

}
