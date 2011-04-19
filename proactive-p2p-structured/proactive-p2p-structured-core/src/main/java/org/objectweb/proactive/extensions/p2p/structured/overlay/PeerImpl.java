package org.objectweb.proactive.extensions.p2p.structured.overlay;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.EndActive;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.p2p.structured.api.PeerFactory;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkNotJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.StructuredP2PException;
import org.objectweb.proactive.extensions.p2p.structured.messages.RequestResponseMessage;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.Request;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.Response;
import org.objectweb.proactive.extensions.p2p.structured.operations.AsynchronousOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.SynchronousOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * PeerImpl is a concrete implementation of {@link Peer}.It is composed of a
 * {@link StructuredOverlay} which allows to have several implementations of
 * common operations for each peer-to-peer protocol to implement.
 * <p>
 * Warning, this class must not be instantiate directly. In order to create a
 * new active peer you must use the {@link PeerFactory}.
 * 
 * @author lpellegr
 */
public class PeerImpl implements Peer, InitActive, EndActive, Serializable {

    private static final long serialVersionUID = 1L;

    protected static Logger logger = LoggerFactory.getLogger(PeerImpl.class);

    protected Peer stub;

    protected StructuredOverlay overlay;

    private AtomicBoolean activated;

    /**
     * The no-argument constructor as commanded by ProActive.
     */
    public PeerImpl() {
        this.activated = new AtomicBoolean();
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
        this.overlay.setLocalPeer(this);
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

        if (this.overlay != null) {
            this.overlay.initActivity(body);
        }

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
     * {@inheritDoc}
     */
    public UUID getId() {
        return this.overlay.getId();
    }

    /**
     * {@inheritDoc}
     */
    public Body getBody() {
        return PAActiveObject.getBodyOnThis();
    }

    /**
     * {@inheritDoc}
     */
    public Peer getStub() {
        return this.stub;
    }

    /**
     * {@inheritDoc}
     */
    public void setStub() {
        // Nothing to do. This method is only useful for component peer.
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
    public StructuredOverlay getOverlay() {
        return this.overlay;
    }

    /**
     * {@inheritDoc}
     */
    public void setOverlay(StructuredOverlay structuredOverlay) {
        this.overlay = structuredOverlay;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isActivated() {
        return this.activated.get();
    }

    /**
     * {@inheritDoc}
     */
    public boolean create() throws StructuredP2PException {
        if (this.activated.compareAndSet(false, true)) {
            return this.overlay.create();
        } else {
            throw new NetworkAlreadyJoinedException();
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean join(Peer landmarkPeer) throws NetworkAlreadyJoinedException {
        if (this.activated.compareAndSet(false, true)) {
            return this.overlay.join(landmarkPeer);
        } else {
            throw new NetworkAlreadyJoinedException();
        }
    }

    /**
     * {@inheritDoc}
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
        this.overlay.route(msg);
    }

    /**
     * {@inheritDoc}
     */
    public Response<?> send(Request<?> request) throws DispatchException {
        return this.overlay.getRequestResponseManager().dispatch(request);
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
        return this.getId().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.overlay.toString();
    }

}
