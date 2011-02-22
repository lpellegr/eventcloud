package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import org.objectweb.proactive.extensions.p2p.structured.operations.EmptyResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.Operation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCANOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborTable;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.Zone;

/**
 * Operation used in order to update a {@link Zone} of a neighbor cached in the
 * {@link NeighborTable} of the peer which handles it.
 * 
 * @author Laurent Pellegrino
 */
public class UpdateNeighborOperation implements Operation {

    private static final long serialVersionUID = 1L;

    private final NeighborEntry entry;

    private final int dimension;
    
    private final int direction;
    
    /**
     * Constructs a new UpdateNeighborOperation with the specified
     * <code>entry</code>, <code>dimension</code> and <code>direction</code>.
     * 
     * @param entry 
     * 			the entry to containing the identifier of the peer to
     * 			update and the new information to set.
     * 
     * @param dimension
     * 			the dimension on which the neighbor to update is.
     * 
     * @param direction
     * 			the direction on which the neighbor to update is.	
     */
    public UpdateNeighborOperation(NeighborEntry entry, int dimension, int direction) {
        this.entry = entry;
        this.dimension = dimension;
        this.direction = direction;
    }

    /**
     * Handles a {@link UpdateNeighborOperation}.
     * 
     * @param overlay
     *            the overlay which handles the message.
     */
    public EmptyResponseOperation handle(StructuredOverlay overlay) {
		((AbstractCANOverlay) overlay).getNeighborTable()
				.get(this.dimension, this.direction)
					.replace(this.entry.getId(), this.entry);

        return new EmptyResponseOperation();
    }

}
