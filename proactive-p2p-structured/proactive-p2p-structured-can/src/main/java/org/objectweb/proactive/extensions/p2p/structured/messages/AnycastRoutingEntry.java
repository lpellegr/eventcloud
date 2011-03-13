package org.objectweb.proactive.extensions.p2p.structured.messages;

import java.io.Serializable;
import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

/**
 * Contains information about {@link Peer}s met while routing an {@link AnycastRequest}. 
 * 
 * @author lpellegr
 */
public class AnycastRoutingEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID peerId;

    private Peer peerStub;

    public UUID getPeerId() {
        return this.peerId;
    }

    public AnycastRoutingEntry(UUID peerID, Peer peerStub) {
        super();
        this.peerId = peerID;
        this.peerStub = peerStub;
    }

    public Peer getPeerStub() {
        return this.peerStub;
    }

}
