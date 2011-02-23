package fr.inria.eventcloud.api;

import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.ontoware.rdf2go.model.node.URI;

import at.sti2.semanticspaces.api.exceptions.SemanticSpaceException;
import fr.inria.eventcloud.kernel.SemanticSpaceOverlayKernel;
import fr.inria.eventcloud.overlay.SemanticPeer;

/**
 * Defines operations that must be performed by administrators only. Indeed, all
 * these operations alter the Semantic Spaces structure and can also have an
 * impact on the scalability.
 * 
 * All methods returns a boolean in order to force synchronization when they are
 * called from a ProActive active object.
 * 
 * @author lpellegr
 */
public interface SemanticSpaceManagement {

    /**
     * Creates and adds a new peer of type Chord on the Chord network.
     */
    public abstract void newPeerOnChord();

    /**
     * Creates and adds the specified number of peers of type Chord on the Chord
     * network.
     * 
     * @param nb
     *            the number of {@link Peer} of type Chord to add.
     */
    public abstract void newPeersOnChordInParallel(int nb);

	/**
	 * Creates a new space by adding a {@link SemanticPeer} of type CAN on the
	 * Chord node which manages the hashed value associated to the specified
	 * <code>spaceURI</code>.
	 * 
	 * @param spaceURI
	 *            the spaceURI used to identify the new space to create.
	 * 
	 * @throws SemanticSpaceException
	 *             if the Chord network doesn't exist.
	 */
    public abstract void createSpace(URI spaceURI) throws SemanticSpaceException;

	/**
	 * Creates a new space by adding a {@link SemanticPeer} of type CAN on the
	 * Chord node which manages the hashed value associated to the specified
	 * <code>spaceURI</code>.
	 * 
	 * @param spaceURI
	 *            the spaceURI used to identify the new space to create.
	 * 
	 * @param kernel
	 *            the {@link SemanticSpaceOverlayKernel} reference to use.
	 * 
	 * @throws SemanticSpaceException
	 *             if the Chord network doesn't exist.
	 */
    public abstract void createSpace(URI spaceURI, SemanticSpaceOverlayKernel kernel) throws SemanticSpaceException;
    
    /**
     * Creates a new peer of type CAN (use the kernel reference from where the
     * operation is handled) and forces it to join the specified spaceURI.
     * 
     * @param spaceURI
     *            identify the space on which the new peer join.
     *            
     * @throws SemanticSpaceException
     *             if the Chord network doesn't exist.
     */
    public abstract void newPeerOnCAN(URI spaceURI) throws SemanticSpaceException;

    /**
     * Creates <code>nb</code> {@link Peer}s of type CAN in parallel and add
     * them on the specified space. If the space does not exist, it is created.
     * 
     * @param spaceURI
     *            identify the space on which the new peers join.
     * @param nb
     *            the number of peers to add.
     * @throws SemanticSpaceException
     *             if the Chord network doesn't exist.
     */
    public abstract void newPeersOnCANInParallel(URI spaceURI, int nb) throws SemanticSpaceException;
    
    /**
     * Creates a new CAN peer and forces it to join the specified spaceURI.
     * 
     * @param spaceURI
     *            identify the space on which the new peer join.
     * 
     * @param kernel
     *            the {@link SemanticSpaceOverlayKernel} reference to use.
     *            
     * @throws SemanticSpaceException
     *             if the Chord network doesn't exist.
     */
    public abstract void newPeerOnCAN(URI spaceURI, SemanticSpaceOverlayKernel kernel) throws SemanticSpaceException;

    /**
     * Randomly selects a peer from the Chord network and force it to leave the
     * network.
     * 
     * @throws SemanticSpaceException
     *             if the Chord network doesn't exist.
     */
    public abstract void removePeerOnChord() throws SemanticSpaceException;

    /**
     * Randomly selects a peer of type CAN from the specified
     * <code>spaceURI</code> and force it to leave the network.
     * 
     * @param spaceURI
     *            identify the space on which we force a peer to leave.
     * @throws SemanticSpaceException
     *             if the Chord network doesn't exist.
     */
    public abstract void removePeerOnCAN(URI spaceURI) throws SemanticSpaceException;

	/**
	 * Returns a boolean indicating if the specified space exists (i.e there is
	 * at least one {@link SemanticPeer} of type CAN associated to hashed value
	 * of the specified spaceURI) or not.
	 * 
	 * @param spaceURI
	 *            the space to check.
	 * @return <code>true</code> if the space exists, <code>false</code>
	 *         otherwise.
	 * @throws SemanticSpaceException
	 *             if the Chord network doesn't exist.
	 */
    public abstract boolean spaceExists(URI spaceURI) throws SemanticSpaceException;

}
