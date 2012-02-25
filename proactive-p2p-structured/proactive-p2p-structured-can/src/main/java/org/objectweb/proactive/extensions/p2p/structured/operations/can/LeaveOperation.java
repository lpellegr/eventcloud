/**
 * Copyright (c) 2011-2012 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import java.util.Set;
import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.operations.EmptyResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.SynchronousOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
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

    private final Set<NeighborEntry> newNeighborsToSet;

    private final Object data;

    public LeaveOperation(UUID peerLeavingId, Zone peerLeavingZone,
            Set<NeighborEntry> newNeighborsToSet, Object data) {
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

    public Set<NeighborEntry> getNewNeighborsToSet() {
        return this.newNeighborsToSet;
    }

    public Object getData() {
        return this.data;
    }

    /**
     * {@inheritDoc}
     */
    public EmptyResponseOperation handle(StructuredOverlay overlay) {
        return ((CanOverlay) overlay).processLeave(this);
    }

}
