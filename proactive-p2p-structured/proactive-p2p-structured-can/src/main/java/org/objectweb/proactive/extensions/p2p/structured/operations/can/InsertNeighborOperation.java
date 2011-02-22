package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import org.objectweb.proactive.extensions.p2p.structured.operations.BooleanResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.Operation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCANOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborEntry;

/**
 * This operation is used to insert a {@link NeighborEntry} at the specified 
 * <code>dimension</code> and <code>direction</code> of the peer receiving 
 * the message.
 * 
 * @author Laurent Pellegrino
 */
public class InsertNeighborOperation implements Operation {

    private static final long serialVersionUID = 1L;

    /**
     * The neighbor peer to remove.
     */
    private final NeighborEntry entry;

    /**
     * The dimension of the neighbor to remove.
     */
    private final int dimension;

    /**
     * The direction of the neighbor to remove.
     */
    private final int direction;

    /**
     * Constructor.
     * 
     * @param neighborEntry
     * 			  the neighbor entry to insert.
     * @param dimension
     *            the dimension of the neighbor to remove.
     * @param direction
     *            the direction of the neighbor to remove
     */
    public InsertNeighborOperation(NeighborEntry neighborEntry, int dimension, int direction) {
    	this.entry = neighborEntry;
        this.dimension = dimension;
        this.direction = direction;
    }

    public NeighborEntry getEntry() {
		return this.entry;
	}

	/**
     * Returns the dimension of the neighbor to remove.
     * 
     * @return the dimension of the neighbor to remove.
     */
    public int getDimension() {
        return this.dimension;
    }

    /**
     * Returns the direction of the neighbor to remove.
     * 
     * @return the direction of the neighbor to remove.
     */
    public int getDirection() {
        return this.direction;
    }

    /**
     * {@inheritDoc}
     */
    public BooleanResponseOperation handle(StructuredOverlay overlay) {
        ((AbstractCANOverlay) overlay).getNeighborTable().get(
        		this.dimension, this.direction).put(
        				this.entry.getId(), this.entry);
        return new BooleanResponseOperation(true);
    }

}
