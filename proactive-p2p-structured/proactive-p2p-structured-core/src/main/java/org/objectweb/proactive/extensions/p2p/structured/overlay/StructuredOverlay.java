package org.objectweb.proactive.extensions.p2p.structured.overlay;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

import org.objectweb.proactive.Body;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.StructuredP2PException;
import org.objectweb.proactive.extensions.p2p.structured.messages.RequestResponseMessage;
import org.objectweb.proactive.extensions.p2p.structured.messages.ResponseEntry;

/**
 * The StructuredOverlay class contains the logic associated to methods exposed
 * by the {@link Peer} class. Each structured p2p protocol is assumed to be
 * provide a concrete implementation of this class.
 * 
 * @author lpellegr
 */
public abstract class StructuredOverlay implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UUID identifier;

    private RequestResponseManager messagingManager;

    private Peer localPeer;

    protected StructuredOverlay() {
    	this.identifier = UUID.randomUUID();
    }

    protected StructuredOverlay(RequestResponseManager queryManager) {
    	this();
    	this.messagingManager = queryManager;
        this.messagingManager.setOverlay(this);
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

    public void initActivity(Body body) {
        // to be overridden
    }

    public void endActivity(Body body) {
        // to be overridden
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

    public RequestResponseManager getRequestResponseManager() {
        return this.messagingManager;
    }

    public ResponseEntry getResponseEntry(UUID responseId) {
    	return this.messagingManager.getResponsesReceived().get(responseId);
    }
    
    public Map<UUID, ResponseEntry> getResponseEntries() {
        return this.messagingManager.getResponsesReceived();
    }

    public void setLocalPeer(Peer localPeer) {
        this.localPeer = localPeer;
    }

    public void route(RequestResponseMessage<?> msg) {
    	msg.route(this);
    }

    public abstract String dump();
    
}
