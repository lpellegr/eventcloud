package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.operations.Operation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCANOverlay;

/**
 * This operation is used to performed the join introduce phase: it consists in
 * retrieving the information (i.e. the zone, the neighbors and the data) the
 * peer which join the network have to set.
 * 
 * @author Laurent Pellegrino
 * 
 * @see AbstractCANOverlay#join(Peer)
 * @see AbstractCANOverlay#handleJoinIntroduceMessage(JoinIntroduceOperation)
 */
public class JoinIntroduceOperation implements Operation {

    private static final long serialVersionUID = 1L;
    
    private final UUID peerID;

	private final Peer remotePeer;

	public JoinIntroduceOperation(UUID peerID, Peer remotePeer) {
		super();
		this.peerID = peerID;
		this.remotePeer = remotePeer;
	}

    public UUID getPeerID() {
		return this.peerID;
	}

	public Peer getRemotePeer() {
		return this.remotePeer;
	}

	/**
     * {@inheritDoc}
     */
    public JoinIntroduceResponseOperation handle(StructuredOverlay overlay) {
        return ((AbstractCANOverlay) overlay).handleJoinIntroduceMessage(this);
    }

}
