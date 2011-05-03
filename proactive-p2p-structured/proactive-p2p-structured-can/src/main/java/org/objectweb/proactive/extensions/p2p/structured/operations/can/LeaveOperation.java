package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import java.util.Collection;
import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.operations.EmptyResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.SynchronousOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;

/**
 * A {@code LeaveOperation} is used to transfer information from the peer which
 * left to the peer which takes over the zone.
 * 
 * @author lpellegr
 */
public class LeaveOperation implements SynchronousOperation {

    private static final long serialVersionUID = 1L;

    private final UUID peerLeavingId;

    private final Zone peerLeavingZone;

    private final Collection<NeighborEntry> newNeighborsToSet;

    private final Object data;

    public LeaveOperation(UUID peerLeavingId, Zone peerLeavingZone,
            Collection<NeighborEntry> newNeighborsToSet, Object data) {
        this.peerLeavingId = peerLeavingId;
        this.peerLeavingZone = peerLeavingZone;
        this.newNeighborsToSet = newNeighborsToSet;
        this.data = data;
    }

    public UUID getPeerLeavingId() {
        return this.peerLeavingId;
    }

    public Zone getPeerLeavingZone() {
        return this.peerLeavingZone;
    }

    public Collection<NeighborEntry> getNewNeighborsToSet() {
        return this.newNeighborsToSet;
    }

    public Object getData() {
        return this.data;
    }

    /**
     * {@inheritDoc}
     */
    public EmptyResponseOperation handle(StructuredOverlay overlay) {
        return ((AbstractCanOverlay) overlay).processLeave(this);
    }

}
