package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import java.util.Collection;
import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.operations.EmptyResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.Operation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCANOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborTable;

/**
 * A {@code LeaveOperation} is used in order to notify the neighbors to remove the
 * peer which leaves from their {@link NeighborTable} and to update it with new
 * correct neighbors.
 * 
 * @author Laurent Pellegrino
 */
public class LeaveOperation implements Operation {

    private static final long serialVersionUID = 1L;

    private final int dimension;

    private final int direction;

    private final Collection<NeighborEntry> neighborsToMergeWith;

    private final UUID peerHavingLeft;

    /**
     * Constructor.
     * 
     * @param peerHavingLeft
     *            the identifier of the peer having left the network.
     * @param neighborsToMergeWith
     *            the neighbors to merge with.
     * @param dimension
     *            the dimension of the peer having left.
     * @param direction
     *            the direction of the peer having left.
     */
    public LeaveOperation(UUID peerHavingLeft, Collection<NeighborEntry> neighborsToMergeWith,
            int dimension,
            int direction) {
        this.peerHavingLeft = peerHavingLeft;
        this.neighborsToMergeWith = neighborsToMergeWith;
        this.dimension = dimension;
        this.direction = direction;
    }

    public int getDimension() {
        return this.dimension;
    }

    public int getDirection() {
        return this.direction;
    }

    public Collection<NeighborEntry> getNeighborsToMergeWith() {
        return this.neighborsToMergeWith;
    }

    public UUID getPeerHavingLeft() {
        return this.peerHavingLeft;
    }

    /**
     * {@inheritDoc}
     */
    public EmptyResponseOperation handle(StructuredOverlay overlay) {
        return ((AbstractCANOverlay) overlay).handleLeaveMessage(this);
    }

}
