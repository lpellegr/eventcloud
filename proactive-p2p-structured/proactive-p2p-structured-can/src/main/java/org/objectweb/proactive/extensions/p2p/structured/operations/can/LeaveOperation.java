package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import java.util.Collection;
import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.operations.EmptyResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.SynchronousOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborTable;

/**
 * A {@code LeaveOperation} is used in order to notify the neighbors to remove
 * the peer which leaves from their {@link NeighborTable} and to update it with
 * new correct neighbors.
 * 
 * @author lpellegr
 */
public class LeaveOperation implements SynchronousOperation {

    private static final long serialVersionUID = 1L;

    private final byte dimension;

    private final byte direction;

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
    public LeaveOperation(UUID peerHavingLeft,
            Collection<NeighborEntry> neighborsToMergeWith, byte dimension,
            byte direction) {
        this.peerHavingLeft = peerHavingLeft;
        this.neighborsToMergeWith = neighborsToMergeWith;
        this.dimension = dimension;
        this.direction = direction;
    }

    public byte getDimension() {
        return this.dimension;
    }

    public byte getDirection() {
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
        return ((AbstractCanOverlay) overlay).handleLeaveMessage(this);
    }

}
