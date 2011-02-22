package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import java.util.LinkedList;

import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborTable;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.SplitEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.Zone;

/**
 * Response associated to {@link JoinIntroduceOperation}. This response contains 
 * some information that have to be affected to the peer which join the network.
 * 
 * @author Laurent Pellegrino
 */
public class JoinIntroduceResponseOperation implements ResponseOperation {

	private static final long serialVersionUID = 1L;

	private final Zone zone;
	
	private final LinkedList<SplitEntry> splitHistory;
	
	private final NeighborTable neighbors;
	
	private final Object data;

	public JoinIntroduceResponseOperation(
			Zone zone, LinkedList<SplitEntry> splitHistory,
			NeighborTable neighbors, Object data) {
		super();
		this.zone = zone;
		this.splitHistory = splitHistory;
		this.neighbors = neighbors;
		this.data = data;
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
