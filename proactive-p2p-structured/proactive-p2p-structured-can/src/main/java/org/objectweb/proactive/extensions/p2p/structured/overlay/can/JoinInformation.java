package org.objectweb.proactive.extensions.p2p.structured.overlay.can;

/**
 * The JoinInformation class is used to store temporary data that are compute on the landmark peer which is joined.
 * These information are computed during the join introduce phase and will be used during the join welcome phase.
 * 
 * @author Laurent Pellegrino
 */
public class JoinInformation {

	private final int dimension;
	
	private final int direction;
	
	private final Zone zone;
	
	private final NeighborEntry entry;

	public JoinInformation(int dimension, int direction, Zone zone,
			NeighborEntry entry) {
		super();
		this.dimension = dimension;
		this.direction = direction;
		this.zone = zone;
		this.entry = entry;
	}
	
	public int getDimension() {
		return this.dimension;
	}

	public int getDirection() {
		return this.direction;
	}

	public Zone getZone() {
		return this.zone;
	}

	public NeighborEntry getEntry() {
		return this.entry;
	}

}
