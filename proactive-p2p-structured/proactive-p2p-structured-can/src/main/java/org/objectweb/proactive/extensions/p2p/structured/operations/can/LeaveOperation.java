/**
 * Copyright (c) 2011-2014 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import java.io.Serializable;
import java.util.Set;

import org.objectweb.proactive.extensions.p2p.structured.operations.EmptyResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.MaintenanceOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.MaintenanceId;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayId;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;

/**
 * {@code LeaveOperation} is used to transfer information from the peer which
 * leaves to the peer which takes over the zone.
 * 
 * @param <E>
 *            the {@link Coordinate}s type manipulated.
 * 
 * @author lpellegr
 */
public class LeaveOperation<E extends Coordinate> extends MaintenanceOperation {

    private static final long serialVersionUID = 160L;

    private final OverlayId peerLeavingId;

    private final Zone<E> peerLeavingZone;

    private final Set<NeighborEntry<E>> newNeighborsToSet;

    private final Serializable data;

    public LeaveOperation(OverlayId peerLeavingId, Zone<E> peerLeavingZone,
            Set<NeighborEntry<E>> newNeighborsToSet, Serializable data,
            MaintenanceId maintenanceId) {
        super(maintenanceId);
        this.peerLeavingId = peerLeavingId;
        this.peerLeavingZone = peerLeavingZone;
        this.newNeighborsToSet = newNeighborsToSet;
        this.data = data;
    }

    public OverlayId getPeerLeavingId() {
        return this.peerLeavingId;
    }

    public Zone<E> getPeerLeavingZone() {
        return this.peerLeavingZone;
    }

    public Set<NeighborEntry<E>> getNewNeighborsToSet() {
        return this.newNeighborsToSet;
    }

    public Serializable getData() {
        return this.data;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public EmptyResponseOperation handle(StructuredOverlay overlay) {
        return ((CanOverlay<E>) overlay).processLeave(this);
    }

}
