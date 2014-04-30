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

import java.util.Map;

import org.objectweb.proactive.extensions.p2p.structured.operations.EmptyResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.MaintenanceId;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayId;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;

import com.google.common.collect.ImmutableMap;

/**
 * Operation used to update neighbors' zones of the peer that leaves the
 * network.
 * 
 * @param <E>
 *            the {@link Coordinate}s type manipulated.
 * 
 * @author lpellegr
 */
public class LeaveUpdateNeighborsOperation<E extends Coordinate> extends
        LeaveNeighborsManagementOperation {

    private static final long serialVersionUID = 160L;

    private final OverlayId peerIdToRemove;

    private final Map<OverlayId, NeighborEntry<E>> entries;

    public LeaveUpdateNeighborsOperation(NeighborEntry<E> entry,
            MaintenanceId maintenanceId) {
        super(maintenanceId);
        this.entries = ImmutableMap.of(entry.getId(), entry);
        this.peerIdToRemove = null;
    }

    public LeaveUpdateNeighborsOperation(
            Map<OverlayId, NeighborEntry<E>> entries,
            MaintenanceId maintenanceId) {
        super(maintenanceId);
        this.peerIdToRemove = null;
        this.entries = entries;
    }

    public LeaveUpdateNeighborsOperation(OverlayId peerIdToRemove,
            Map<OverlayId, NeighborEntry<E>> entries,
            MaintenanceId maintenanceId) {
        super(maintenanceId);
        this.peerIdToRemove = peerIdToRemove;
        this.entries = entries;
    }

    /**
     * Handles a {@link LeaveUpdateNeighborsOperation}.
     * 
     * @param overlay
     *            the overlay which handles the message.
     */
    @Override
    @SuppressWarnings("unchecked")
    public EmptyResponseOperation handle(StructuredOverlay overlay) {
        CanOverlay<E> canOverlay = (CanOverlay<E>) overlay;

        if (this.peerIdToRemove != null) {
            canOverlay.getNeighborTable().remove(this.peerIdToRemove);
        }

        for (NeighborEntry<E> entry : this.entries.values()) {
            if (entry.getId().equals(overlay.getId())) {
                continue;
            }

            NeighborEntry<E> found =
                    canOverlay.getNeighborTable().getNeighborEntry(
                            entry.getId());

            if (found != null) {
                found.setZone(entry.getZone());
            }
        }

        return EmptyResponseOperation.getInstance();
    }

}
