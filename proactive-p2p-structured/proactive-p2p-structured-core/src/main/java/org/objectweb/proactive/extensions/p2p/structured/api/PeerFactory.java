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
 * @author lpellegr
 */
public class PeerFactory {

	/**
	 * Creates a new peer active object on the local JVM by using the specified
	 * overlay abstraction.
	 * 
	 * @param overlay
	 *            the overlay to set.
	 *
	 * @return the new active object created.
	 */
    public static Peer newActivePeer(StructuredOverlay overlay) {
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
	 *
	 * @return the new active object created.
	 */
    public static Peer newActivePeer(StructuredOverlay overlay, Node node) {
        try {
			return (Peer) PAActiveObject.turnActive(
							new Peer(overlay), node);
		} catch (ActiveObjectCreationException e) {
			e.printStackTrace();
		} catch (NodeException e) {
			e.printStackTrace();
		}
		
		return null;
    }

}
