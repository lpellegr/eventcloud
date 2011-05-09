package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import org.objectweb.proactive.extensions.p2p.structured.operations.EmptyResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.SynchronousOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborTable;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;

/**
 * Operation used in order to update a {@link Zone} of a neighbor cached in the
 * {@link NeighborTable} of the peer which handles it.
 * 
 * @author lpellegr
 */
public class UpdateNeighborOperation implements SynchronousOperation {

    private static final long serialVersionUID = 1L;

    private final NeighborEntry entry;

    private final byte dimension;

    private final byte direction;

    /**
     * Constructs a new UpdateNeighborOperation with the specified {@code entry}
     * , {@code dimension} and {@code direction}.
     * 
     * @param entry
     *            the entry to containing the identifier of the peer to update
     *            and the new information to set.
     * 
     * @param dimension
     *            the dimension on which the neighbor to update is.
     * 
     * @param direction
     *            the direction on which the neighbor to update is.
     */
    public UpdateNeighborOperation(NeighborEntry entry, byte dimension,
            byte direction) {
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
        ((CanOverlay) overlay).getNeighborTable().get(
                this.dimension, this.direction).replace(
                this.entry.getId(), this.entry);

        return new EmptyResponseOperation();
    }

}
