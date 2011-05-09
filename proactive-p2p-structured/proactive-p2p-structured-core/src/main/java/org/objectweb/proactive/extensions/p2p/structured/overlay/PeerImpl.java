package org.objectweb.proactive.extensions.p2p.structured.overlay;

import java.io.Serializable;
import java.util.UUID;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PeerImpl is a concrete implementation of {@link Peer}. It is composed of a
 * {@link StructuredOverlay} which allows to have several implementations of
 * common operations for each peer-to-peer protocol to implement. This class
 * acts as a Facade in order to simplify interface (easier to use, understand
 * and test).
 * <p>
 * Warning, this class must not be instantiate directly. In order to create a
 * new active peer you have to use the {@link PeerFactory}.
 * 
 * @author lpellegr
 */
public class PeerImpl implements Peer, InitActive, EndActive, Serializable {

    private static final long serialVersionUID = 1L;

    protected static Logger logger = LoggerFactory.getLogger(PeerImpl.class);

    protected transient StructuredOverlay overlay;

    /**
     * The no-argument constructor as commanded by ProActive.
     */
    public PeerImpl() {
    }

    /**
     * Constructs a new peer using the specified overlay. Warning, you must use
     * {@link PeerFactory} to instantiate a new active peer.
     * 
     * @param overlay
     *            the overlay to set to the new peer.
     */
    public PeerImpl(StructuredOverlay overlay) {
        this();
        this.overlay = overlay;
    }

    /**
     * {@inheritDoc}
     */
    public void init(Peer stub, StructuredOverlay overlay) {
        if (this.overlay == null) {
            this.overlay = overlay;
            this.overlay.stub = stub;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void initActivity(Body body) {
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

        // tests if overlay is null for component instantiation use-case
        if (this.overlay != null) {
            this.overlay.stub = (Peer) PAActiveObject.getStubOnThis();
            this.overlay.initActivity(body);
        }

        // receive cannot be handled as immediate service
        PAActiveObject.removeImmediateService("receive");
    }

    /**
     * {@inheritDoc}
     */
    public void endActivity(Body body) {
        if (this.overlay.activated.get()) {
            try {
                this.leave();
            } catch (NetworkNotJoinedException e) {
                e.printStackTrace();
            }
        }

        this.overlay.endActivity(body);
    }

    /**
     * {@inheritDoc}
     */
    public UUID getId() {
        return this.overlay.id;
    }

    /**
     * {@inheritDoc}
     */
    public OverlayType getType() {
        return this.overlay.getType();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActivated() {
        return this.overlay.activated.get();
    }

    /**
     * {@inheritDoc}
     */
    public boolean create() throws NetworkAlreadyJoinedException {
        if (this.overlay.activated.compareAndSet(false, true)) {
            return this.overlay.create();
        } else {
            throw new NetworkAlreadyJoinedException();
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean join(Peer landmarkPeer) throws NetworkAlreadyJoinedException {
        if (this.overlay.activated.compareAndSet(false, true)) {
            return this.overlay.join(landmarkPeer);
        } else {
            throw new NetworkAlreadyJoinedException();
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean leave() throws NetworkNotJoinedException {
        if (this.overlay.activated.compareAndSet(true, false)) {
            return this.overlay.leave();
        } else {
            throw new NetworkNotJoinedException();
        }
    }

    /**
     * {@inheritDoc}
     */
    public ResponseOperation receive(SynchronousOperation operation) {
        return operation.handle(this.overlay);
    }

    /**
     * {@inheritDoc}
     */
    public void receive(AsynchronousOperation operation) {
        operation.handle(this.overlay);
    }

    /**
     * {@inheritDoc}
     */
    public ResponseOperation receiveImmediateService(SynchronousOperation operation) {
        return operation.handle(this.overlay);
    }

    /**
     * {@inheritDoc}
     */
    public void receiveImmediateService(AsynchronousOperation operation) {
        operation.handle(this.overlay);
    }

    /**
     * {@inheritDoc}
     */
    public void route(RequestResponseMessage<?> msg) {
        msg.route(this.overlay);
    }

    /**
     * {@inheritDoc}
     */
    public Response<?> send(Request<?> request) throws DispatchException {
        return this.overlay.messagingManager.dispatch(request);
    }

    /**
     * {@inheritDoc}
     */
    public String dump() {
        return this.overlay.dump();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof PeerImpl
                && this.getId().equals(((PeerImpl) obj).getId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.overlay.id.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.overlay.toString();
    }

}
