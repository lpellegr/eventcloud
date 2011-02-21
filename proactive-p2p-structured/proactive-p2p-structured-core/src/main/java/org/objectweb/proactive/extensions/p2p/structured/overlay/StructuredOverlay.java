package org.objectweb.proactive.extensions.p2p.structured.overlay;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.Service;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.StructuredP2PException;
import org.objectweb.proactive.extensions.p2p.structured.messages.PendingReplyEntry;
import org.objectweb.proactive.extensions.p2p.structured.messages.RequestReplyMessage;

/**
 * The StructuredOverlay class contains the logic associated to methods exposed
 * by the {@link Peer} class. Each structured p2p protocol is assumed to be
 * provide a concrete implementation of this class.
 * 
 * @author Laurent Pellegrino
 */
public abstract class StructuredOverlay implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID identifier;

    private QueryManager queryManager;

    private Peer localPeer;

    protected StructuredOverlay() {
    	this.identifier = UUID.randomUUID();
    }

    /**
     * Constructor.
     * 
     * @param peer
     *            the peer which is associated to the overlay.
     * @param queryManager
     *            the query manager to associate to the overlay.
     */
    protected StructuredOverlay(Peer peer, QueryManager queryManager) {
        this(queryManager);
        this.localPeer = peer;
    }

    protected StructuredOverlay(QueryManager queryManager) {
    	this();
    	this.queryManager = queryManager;
        this.queryManager.setOverlay(this);
    }

    public abstract boolean create();

	/**
	 * Forces the current peer to join an existing network by using the
	 * specified landmark peer.
	 * 
	 * @param landmarkPeer
	 *            the peer (entry point) which is used in order to join the
	 *            network.
	 * @return <code>true</code> if join operation has succeeded,
	 *         <code>false</code> otherwise.
	 * @throws StructuredP2PException
	 */
    public abstract boolean join(Peer landmarkPeer);

    public abstract boolean leave();

    public abstract String toString();

    public abstract void initActivity(Body body);

    public void runActivity(Body body) {
        Service service = new Service(body);
        while (body.isActive()) {
            service.serveOldest();
        }
    }

    public abstract OverlayType getType();

    public UUID getId() {
        return this.identifier;
    }

    /**
     * Returns the current peer that use this overlay.
     * 
     * @return the current peer that use this overlay.
     */
    public Peer getLocalPeer() {
        return this.localPeer;
    }

    /**
     * Returns the stub associated to the local peer.
     * 
     * @return the stub associated to the local peer.
     */
    public Peer getRemotePeer() {
        return this.localPeer.getStub();
    }

    public QueryManager getQueryManager() {
        return this.queryManager;
    }

    public Map<UUID, PendingReplyEntry> getRepliesReceived() {
        return this.queryManager.getResponsesReceived();
    }

    public void setLocalPeer(Peer localPeer) {
        this.localPeer = localPeer;
    }

    public void route(RequestReplyMessage<?> msg) {
        this.queryManager.route(msg);
    }

    public abstract String dump();
    
}
