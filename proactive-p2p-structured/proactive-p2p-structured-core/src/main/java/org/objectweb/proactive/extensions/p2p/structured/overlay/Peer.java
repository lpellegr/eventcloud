package org.objectweb.proactive.extensions.p2p.structured.overlay;

import java.io.Serializable;
import java.util.UUID;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.p2p.structured.api.PeerFactory;
import org.objectweb.proactive.extensions.p2p.structured.api.messages.Reply;
import org.objectweb.proactive.extensions.p2p.structured.api.messages.Request;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkNotJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.StructuredP2PException;
import org.objectweb.proactive.extensions.p2p.structured.messages.RequestReplyMessage;
import org.objectweb.proactive.extensions.p2p.structured.messages.reply.AbstractReply;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.AbstractRequest;
import org.objectweb.proactive.extensions.p2p.structured.operations.Operation;
import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;

/**
 * A Peer contains all operations which are common to peer-to-peer protocols.
 * These operations are the join and leave but also the send operation in order
 * to send messages. A Peer is composed of a {@link StructuredOverlay} which
 * allows to have several implementations of common operations for each protocol
 * to implement.
 * 
 * Warning, this class must not be instantiate directly. In order to create a
 * new active peer you must use the {@link PeerFactory}.
 * 
 * @author lpellegr
 */
public class Peer implements InitActive, EndActive, RunActive, Serializable {

    private static final long serialVersionUID = 1L;
    
    private Peer stub;
    
    protected StructuredOverlay overlay;

    private boolean activated = false;

    /**
     * The no-argument constructor as commanded by ProActive.
     */
    public Peer() {

    }

    /**
     * Constructs a new peer using the specified overlay. Warning, you must use
     * {@link PeerFactory} to instantiate a new active peer.
     * 
     * @param overlay
     *            the overlay to set to the new peer.
     */
    public Peer(StructuredOverlay overlay) {
        this.overlay = overlay;
        this.overlay.setLocalPeer(this);
    }

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
    public boolean create() throws StructuredP2PException {
    	if (!this.activated) {
    		if (this.overlay.create()) {
        		this.setActivated(true);
        		return true;
        	}
    		return false;
    	} else {
    		throw new StructuredP2PException("Peer has already joined or created an existing network.");
    	}
    }

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
    public boolean join(Peer landmarkPeer) throws NetworkAlreadyJoinedException {
        if (this.isActivated()) {
            throw new NetworkAlreadyJoinedException();
        }

        if (this.overlay.join(landmarkPeer)) {
            this.setActivated(true);
            return true;
        }

        return false;
    }

    /**
     * Forces the current peer to leave the network it has joined.
     * 
     * @return Returns <code>true</code> if the operation has succeeded,
     *         <code>false</code> otherwise.
     * @throws NetworkNotJoinedException
     *             if the current peer try leave without having joined a
     *             network.
     */
    public boolean leave() throws NetworkNotJoinedException {
        if (!this.isActivated()) {
            throw new NetworkNotJoinedException();
        }

        if (this.overlay.leave()) {
            this.setActivated(false);
            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void initActivity(Body body) {
        this.stub = (Peer) PAActiveObject.getStubOnThis();

        PAActiveObject.setImmediateService("receiveOperationIS");
        
    	// these methods does not change the state of the peer
    	PAActiveObject.setImmediateService("equals");
    	PAActiveObject.setImmediateService("getId");
    	PAActiveObject.setImmediateService("hashCode");
    	PAActiveObject.setImmediateService("toString");
    	PAActiveObject.setImmediateService("getType");
  
        this.overlay.initActivity(body);
        
        // receiveOperation cannot be handled as immediate service
        PAActiveObject.removeImmediateService("receiveOperation");
    }

    /**
     * {@inheritDoc}
     */
    public void endActivity(Body body) {
		if (this.activated) {
			try {
				this.leave();
			} catch (NetworkNotJoinedException e) {
				e.printStackTrace();
			}
		}
    }
    
    /**
     * Returns debug information as String.
     * 
     * @return debug information as String.
     */
    public String dump() {
        return this.overlay.dump();
    }

    /**
     * Returns the {@link Body} of the current active object.
     * 
     * @return the {@link Body} of the current active object.
     */
    public Body getBody() {
        return PAActiveObject.getBodyOnThis();
    }

    /**
     * Returns the unique identifier associated to this peer.
     *  
     * @return the unique identifier associated to this peer.
     */
    public UUID getId() {
        return this.overlay.getId();
    }

    /**
     * Returns the {@link StructuredOverlay} which is used by the peer.
     * 
     * @return the {@link StructuredOverlay} which is used by the peer.
     */
    public StructuredOverlay getStructuredOverlay() {
        return this.overlay;
    }

    /**
     * Returns the stub associated to the current peer.
     * 
     * @return the stub associated to the current peer.
     */
    public Peer getStub() {
        return this.stub;
    }

    /**
     * Returns the type of overlay for the current peer.
     * 
     * @return the type of overlay for the current peer.
     */
    public OverlayType getType() {
        return this.overlay.getType();
    }

    /**
     * Indicates whether the join operation has already be performed or not.
     * 
     * @return <code>true</code> if the peer is activated, <code>false</code>
     *         otherwise.
     */
    public boolean isActivated() {
        return this.activated;
    }

    public void setActivated(boolean value) {
        this.activated = value;
    }

    /**
     * {@inheritDoc}
     */
    public void runActivity(Body body) {
        this.overlay.runActivity(body);
    }

    /**
     * Sends a {@link Request} on the network from the current peer.
     * 
     * @param query
     *            the query to send.
     * @return the response in agreement with the type of query sent.
     * 
     * @exception DispatchException
     *                if a problem occurs while the query is dispatched.
     */
    public Reply send(Request query) throws DispatchException {
        return this.overlay.getQueryManager().dispatch(query);
    }

    /**
     * Sends a {@link RequestReplyMessage}.
     * 
     * @param msg
     *            the message to send.
     * @return the response in agreement with the type of query sent.
     */
    public AbstractReply<?> send(AbstractRequest<?> msg) {
        return this.overlay.getQueryManager().process(msg);
    }

    public void route(RequestReplyMessage<?> msg) {
        this.overlay.getQueryManager().route(msg);
    }

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
    public ResponseOperation receiveOperationIS(Operation op) {
        return op.handle(this.overlay);
    }

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
    public ResponseOperation receiveOperation(Operation op) {
        return op.handle(this.overlay);
    }
    
    /**
     * Sets the overlay.
     * 
     * @param structuredOverlay
     *            the new overlay to set.
     */
    public void setStructuredOverlay(StructuredOverlay structuredOverlay) {
        this.overlay = structuredOverlay;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
    	return this.getId().hashCode();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
    	return obj instanceof Peer
    			&& this.getId().equals(((Peer) obj).getId());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.overlay.toString();
    }

}
