package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import java.util.LinkedList;
import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborTable;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.SplitEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.Zone;

/**
 * Response associated to {@link JoinIntroduceOperation}. This response contains 
 * some information that have to be affected to the peer which join the network.
 * 
 * @author lpellegr
 */
public class JoinIntroduceResponseOperation implements ResponseOperation {

	private static final long serialVersionUID = 1L;

	private final UUID peerId;
	
	private final Zone zone;
	
	private final LinkedList<SplitEntry> splitHistory;
	
	private final NeighborTable neighbors;
	
	private final Object data;

	public JoinIntroduceResponseOperation(UUID peerId, 
			Zone zone, LinkedList<SplitEntry> splitHistory,
			NeighborTable neighbors, Object data) {
		this.peerId = peerId;
		this.zone = zone;
		this.splitHistory = splitHistory;
		this.neighbors = neighbors;
		this.data = data;
	}

	public UUID getPeerId() {
		return this.peerId;
	}

	public Zone getZone() {
		return this.zone;
	}
	
	public LinkedList<SplitEntry> getSplitHistory() {
		return this.splitHistory;
	}

	public NeighborTable getNeighbors() {
		return this.neighbors;
	}

	public Object getData() {
		return this.data;
	}

}
