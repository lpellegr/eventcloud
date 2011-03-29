package fr.inria.eventcloud.api;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.UUID;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;

import fr.inria.eventcloud.overlay.SemanticPeer;
import fr.inria.eventcloud.overlay.can.SemanticCanOverlay;
import fr.inria.eventcloud.tracker.SemanticCanTracker;

/**
 * SemanticFactory can be used to create new instances of Semantic objects like
 * for example {@link SemanticCanOverlay} or {@link SemanticPeer}.
 * 
 * @author lpellegr
 */
public class SemanticFactory {

    /**
     * Creates a new {@link SemanticCanTracker} active object.
     * 
     * @param associatedNetworkName
     *            the name of the network to which the tracker is associated.
     * @return the new Tracker object created.
     */
    public static SemanticCanTracker newActiveSemanticCanTracker(String associatedNetworkName) {
        return SemanticFactory.newActiveSemanticCanTracker(
					associatedNetworkName, UUID.randomUUID().toString());
    }

    /**
     * Creates a new {@link SemanticCanTracker} active object.
     * 
     * @param trackerName
     *            the name of the tracker.
     * @param associatedNetworkName
     *            the name of the network to which the tracker is associated.
     * @return the new Tracker object created.
     */
    public static SemanticCanTracker newActiveSemanticCanTracker(
    					String associatedNetworkName, String trackerName) {
		return SemanticFactory.newActiveSemanticCanTracker(
						associatedNetworkName, trackerName, null);
    }

    /**
     * Creates a new {@link SemanticCanTracker} active object.
     * 
     * @param associatedNetworkName
     *            the name of the network to which the tracker is associated.
     * @param trackerName
     *            the name of the tracker.
     * @param node
     *            the node to use for deployment.
     * @return the new Tracker object created.
     */
    public static SemanticCanTracker newActiveSemanticCanTracker(
    					String associatedNetworkName, String trackerName, Node node) {
        Object[] constructorParameters;
        if (trackerName == null) {
            constructorParameters = new Object[] { associatedNetworkName };
        } else {
            constructorParameters = new Object[] { associatedNetworkName, trackerName };
        }

        try {
			return PAActiveObject.newActive(SemanticCanTracker.class, constructorParameters, node);
		} catch (ActiveObjectCreationException e) {
			e.printStackTrace();
		} catch (NodeException e) {
			e.printStackTrace();
		}
		
		return null;
    }

    /**
     * Creates a new {@link SemanticPeer} active object by using the specified
     * overlay abstraction.
     * 
     * @param trackers
     *            the {@link Tracker}s which serve as entry point.
     * @return the new Peer object created.
     */
    public static SemanticPeer newActiveSemanticCanPeer(SemanticCanTracker... trackers) {
        return SemanticFactory.newActiveSemanticCanPeer(null, trackers);
    }

    /**
     * Creates a new {@link SemanticPeer} active object by using the specified
     * overlay abstraction and the given node which indicates where to deploy
     * the object.
     * 
     * @param node
     *            the node used by the peer.
     * @param trackers
     *            the {@link Tracker}s which serves as entry point.
     * @return the new {@link SemanticPeer} stub object created.
     */
    public static SemanticPeer newActiveSemanticCanPeer(Node node, SemanticCanTracker... trackers) {
    	try {
			return PAActiveObject.newActive(SemanticPeer.class, new Object[] { new SemanticCanOverlay(), trackers }, node);
		} catch (ActiveObjectCreationException e) {
			e.printStackTrace();
		} catch (NodeException e) {
			e.printStackTrace();
		}
		
		return null;
    }

	/**
	 * Creates {@link SemanticPeer}s of type CAN in parallel by using one
	 * {@link Node} by peer object.
	 * 
	 * @param number
	 *            the number of {@link SemanticPeer} of type CAN to create in
	 *            parallel.
	 * @param trackers
	 *            the trackers which serve as entry point.
	 * @return the {@link SemanticPeer} created.
	 */
    public static SemanticPeer[] newActiveSemanticCanPeersInParallel(
    									int number, final SemanticCanTracker... trackers) {
        return newActiveSemanticCanPeersInParallel(new Node[number], trackers);
    }

    /**
     * Creates {@link SemanticPeer}s of type CAN in parallel by using one
     * {@link Node} by peer object.
     * 
     * @param nodes
     *            the nodes to use for the deployment of active objects.
     * @param trackers
     *            the {@link Tracker}s which serve as entry point.
     * @return the {@link SemanticPeer}s created.
     */
    public static SemanticPeer[] newActiveSemanticCanPeersInParallel(Node[] nodes, SemanticCanTracker... trackers) {
    	Object[][] parameters = new Object[checkNotNull(nodes).length][2];
    	for (int i=0; i<parameters.length; i++) {
    		parameters[i][0] = new SemanticCanOverlay();
    		parameters[i][1] = trackers;
    	}
    	
    	try {
			return (SemanticPeer[]) PAActiveObject.newActiveInParallel(
						SemanticPeer.class.getCanonicalName(), parameters, nodes);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
    }

}
