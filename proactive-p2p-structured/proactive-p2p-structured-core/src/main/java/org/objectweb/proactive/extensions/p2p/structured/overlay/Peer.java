package org.objectweb.proactive.extensions.p2p.structured.overlay;

import java.io.Serializable;
import java.util.UUID;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkNotJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.StructuredP2PException;
import org.objectweb.proactive.extensions.p2p.structured.messages.RequestResponseMessage;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.Request;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.Response;
import org.objectweb.proactive.extensions.p2p.structured.operations.Operation;
import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;

/**
 * A Peer contains all operations which are common to peer-to-peer protocols.
 * These operations are the join and leave but also the send operation in order
 * to send messages. A Peer is composed of a {@link StructuredOverlay} which
 * allows to have several implementations of common operations for each protocol
 * to implement.
 * 
 * @author lpellegr
 */
public interface Peer extends Serializable {

    /**
     * Returns the unique identifier associated to this peer.
     *  
     * @return the unique identifier associated to this peer.
     */
    public UUID getId();

    /**
     * Returns the {@link Body} of the current active object.
     * 
     * @return the {@link Body} of the current active object.
     */
    public Body getBody();

    /**
     * Returns the stub associated to the current peer.
     * 
     * @return the stub associated to the current peer.
     */
    public Peer getStub();

    /**
     * Sets the stub associated to the current peer.
     * Useful only for component peer to initiate its stub.
     */
    public void setStub();

    /**
     * Returns the type of overlay for the current peer.
     * 
     * @return the type of overlay for the current peer.
     */
    public OverlayType getType();

    /**
     * Returns the overlay associated to the current peer.
     * 
     * @return the overlay associated to the current peer.
     */
    public StructuredOverlay getOverlay();

    /**
     * Sets the overlay associated to the current peer.
     *
     * @param structuredOverlay
     *            the overlay associated to the current peer.
     */
    public void setOverlay(StructuredOverlay structuredOverlay);

    /**
     * Indicates whether the join operation has already be performed or not.
     * 
     * @return <code>true</code> if the peer is activated, <code>false</code>
     *         otherwise.
     */
    public boolean isActivated();

    /**
     * This method is used to initialize the state of the peer in 
     * the special case where it is the first peer on the network.
     * 
     * @return a boolean indicating if the operation has succeeded or not.
     * 
     * @throws StructuredP2PException
     *             if the peer has already joined or created an existing
     *             network.
     */
    public boolean create() throws StructuredP2PException;

    /**
     * Forces the current peer to join an existing network by using the
     * specified peer reference. When a peer has joined a network it has to
     * leave the network before to join a new one otherwise a
     * {@link NetworkAlreadyJoinedException} will be thrown.
     * 
     * @param landmarkPeer
     *            the peer used as entry point.
     * @return Returns <code>true</code> if the operation has succeeded,
     *         <code>false</code> otherwise.
     * @throws NetworkAlreadyJoinedException
     *             if the current peer has already joined a network.
     */
    public boolean join(Peer landmarkPeer) throws NetworkAlreadyJoinedException;

    /**
     * Forces the current peer to leave the network it has joined.
     * 
     * @return Returns <code>true</code> if the operation has succeeded,
     *         <code>false</code> otherwise.
     * @throws NetworkNotJoinedException
     *             if the current peer try leave without having joined a
     *             network.
     */
    public boolean leave() throws NetworkNotJoinedException;

    /**
     * Sends a request over the overlay by using message passing.
     * 
     * @param request
     *            the request to handle.
     * 
     * @return the response in agreement with the type of message sent.
     * @throws DispatchException 
     */
    public Response<?> send(Request<?> request) throws DispatchException;

    public void route(RequestResponseMessage<?> msg);

    /**
     * The current remote object receives the specified operation 
     * <code>op</code> by handling it as immediate service.
     * <br>
     * <b>It completely by-passes the message queue model that 
     * comes with the active objects, thus breaks the theoretical 
     * model and can introduce race conditions.</b>
     * 
     * @param op
     *            the operation to handle.
     * 
     * @return a response in agreement with the type of operation handled.
     */
    public ResponseOperation receiveOperationIS(Operation op);

    /**
     * The current remote object receives the specified 
     * <code>op</code> using the default ProActive message 
     * queue model.
     * 
     * @param op
     *            the operation to handle.
     * 
     * @return a response in agreement with the type of operation handled.
     */
    public ResponseOperation receiveOperation(Operation op);
    
    /**
     * Returns debug information as String.
     * 
     * @return debug information as String.
     */
    public String dump();

    public String toString();

}
