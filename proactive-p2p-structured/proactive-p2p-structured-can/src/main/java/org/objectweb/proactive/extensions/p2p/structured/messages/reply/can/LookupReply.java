package org.objectweb.proactive.extensions.p2p.structured.messages.reply.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.LookupRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

/**
 * Response associated to {@link LookupRequest}.
 * 
 * @author Laurent Pellegrino
 */
public class LookupReply extends ForwardReply {

    private static final long serialVersionUID = 1L;
    
    private Peer peerFound;

    public LookupReply(LookupRequest query, Peer peerFound) {
        super(query);
        this.peerFound = peerFound;
    }

    public Peer getPeerFound() {
        return this.peerFound;
    }
    
}
