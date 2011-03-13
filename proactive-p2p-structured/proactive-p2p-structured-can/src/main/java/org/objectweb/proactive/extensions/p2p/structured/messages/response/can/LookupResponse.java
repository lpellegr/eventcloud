package org.objectweb.proactive.extensions.p2p.structured.messages.response.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.LookupRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

/**
 * Response associated to {@link LookupRequest}.
 * 
 * @author lpellegr
 */
public class LookupResponse extends ForwardResponse {

    private static final long serialVersionUID = 1L;
    
    private Peer peerFound;

    public LookupResponse(LookupRequest query, Peer peerFound) {
        super(query);
        this.peerFound = peerFound;
    }

    public Peer getPeerFound() {
        return this.peerFound;
    }
    
}
