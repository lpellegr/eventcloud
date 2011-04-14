package org.objectweb.proactive.extensions.p2p.structured.overlay;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.p2p.structured.api.PeerFactory;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkNotJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.messages.RequestResponseMessage;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.Request;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.Response;
import org.objectweb.proactive.extensions.p2p.structured.operations.AsynchronousOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.SynchronousOperation;

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

    private AtomicBoolean activated;

    /**
     * The no-argument constructor as commanded by ProActive.
     */
    public Peer() {
    	this.activated = new AtomicBoolean();
    }

    /**
     * Constructs a new peer using the specified overlay. Warning, you must use
     * {@link PeerFactory} to instantiate a new active peer.
     * 
     * @param overlay
     *            the overlay to set to the new peer.
     */
    public Peer(StructuredOverlay overlay) {
    	this();
        this.overlay = overlay;
        this.overlay.setLocalPeer(this);
    }

	/**
	 * This method is used to initialize the state of the peer in 
	 * the special case where it is the first peer on the network.
	 * 
	 * @return a boolean indicating if the operation has succeeded or not.
	 * 
	 * @throws NetworkAlreadyJoinedException
	 *             if the peer has already joined or created an existing
	 *             network.
	 */
    public boolean create() throws NetworkAlreadyJoinedException {
		if (this.activated.compareAndSet(false, true)) {
			return this.overlay.create();
		} else {
			throw new NetworkAlreadyJoinedException();
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
	 * @return Returns {@code true} if the operation has succeeded,
	 *         {@code false} otherwise (e.g. if a concurrent join or leave
	 *         operation is detected).
	 * @throws NetworkAlreadyJoinedException
	 *             if the current peer has already joined a network.
	 */
    public boolean join(Peer landmarkPeer) throws NetworkAlreadyJoinedException {
		if (this.activated.compareAndSet(false, true)) {
			return this.overlay.join(landmarkPeer);
		} else {
			throw new NetworkAlreadyJoinedException();
		}
    }

	/**
	 * Forces the current peer to leave the network it has joined.
	 * 
	 * @return Returns {@code true} if the operation has succeeded,
	 *         {@code false} otherwise.
	 * @throws NetworkNotJoinedException
	 *             if the current peer try leave without having joined a
	 *             network.
	 */
    public boolean leave() throws NetworkNotJoinedException {
		if (this.activated.compareAndSet(true, false)) {
			return this.overlay.leave();
		} else {
			throw new NetworkNotJoinedException();
		}     
    }

    /**
     * {@inheritDoc}
     */
    public void initActivity(Body body) {
        this.stub = (Peer) PAActiveObject.getStubOnThis();

        body.setImmediateService("receiveImmediateService", false);
        
    	// these methods do not change the state of the peer
    	body.setImmediateService("equals", false);
    	body.setImmediateService("getId", false);
    	body.setImmediateService("hashCode", false);
    	body.setImmediateService("toString", false);
    	body.setImmediateService("getType", false);
  
    	// puts the following methods as immediate service in 
    	// order to have the possibility to handle concurrent
    	// queries
    	PAActiveObject.setImmediateService("send");
        PAActiveObject.setImmediateService("route");
    	
        this.overlay.initActivity(body);
        
        // receive cannot be handled as immediate service
        PAActiveObject.removeImmediateService("receive");
    }

    /**
     * {@inheritDoc}
     */
    public void endActivity(Body body) {
		if (this.activated.get()) {
			try {
				this.leave();
			} catch (NetworkNotJoinedException e) {
				e.printStackTrace();
			}
		}
		
		this.overlay.endActivity(body);
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
    public StructuredOverlay getOverlay() {
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
	 * @return {@code true} if the peer is activated, {@code false} otherwise.
	 */
    public boolean isActivated() {
        return this.activated.get();
    }

    /**
     * {@inheritDoc}
     */
    public void runActivity(Body body) {
        this.overlay.runActivity(body);
    }

	/**
	 * Sends a request over the overlay by using message passing.
	 * 
	 * @param request
	 *            the request to handle.
	 * 
	 * @return the response in agreement with the type of message sent.
	 * @throws DispatchException 
	 */
    public Response<?> send(Request<?> request) throws DispatchException {
        return this.overlay.getRequestResponseManager().dispatch(request);
    }

    public void route(RequestResponseMessage<?> msg) {
        this.overlay.route(msg);
    }

    /**
     * Receives and handles the specified {@code operation} synchronously.
     *
     * @param operation the operation to handle.
     * 
     * @return a response according to the operation type handled.
     */
    public ResponseOperation receive(SynchronousOperation operation) {
        return operation.handle(this.overlay);
    }
    
    /**
     * Receives and handles the specified {@code operation} asynchronously.
     *
     * @param operation the operation to handle.
     */
    public void receive(AsynchronousOperation operation) {
        operation.handle(this.overlay);
    }
    
    /**
     * Receives in immediate service and handles the specified 
     * {@code operation} synchronously.
     * <p>
     * To receive the operation in immediate service completely 
     * by-passes the message queue model that comes with the active 
     * objects, thus breaks the theoretical model and may introduce 
     * race conditions.
     * 
     * @param operation the operation to handle.
     * 
     * @return a response according to the operation type handled.
     */
    public ResponseOperation receiveImmediateService(SynchronousOperation operation) {
        return operation.handle(this.overlay);
    }
    
    /**
     * Receives in immediate service and handles the specified 
     * {@code operation} synchronously.
     * <p>
     * To receive the operation in immediate service completely 
     * by-passes the message queue model that comes with the active 
     * objects, thus breaks the theoretical model and may introduce 
     * race conditions.
     * 
     * @param operation the operation to handle.
     */
    public void receiveImmediateService(AsynchronousOperation operation) {
        operation.handle(this.overlay);
    }
    
    /**
     * Sets the {@link StructuredOverlay} associated to this peer.
     * 
     * @param structuredOverlay
     *            the new overlay to set.
     */
    public void setOverlay(StructuredOverlay structuredOverlay) {
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
