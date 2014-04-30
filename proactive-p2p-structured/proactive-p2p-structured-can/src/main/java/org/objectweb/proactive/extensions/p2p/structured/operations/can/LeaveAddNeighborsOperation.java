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

import java.util.Collection;

import org.objectweb.proactive.extensions.p2p.structured.operations.EmptyResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.MaintenanceId;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.ExtendedNeighborEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;

/**
 * Piggyback peers that are potentially new neighbors of the peer that receive
 * the operation.
 * 
 * @param <E>
 *            the {@link Coordinate}s type manipulated.
 * 
 * @author lpellegr
 */
public class LeaveAddNeighborsOperation<E extends Coordinate> extends
        LeaveNeighborsManagementOperation {

    private static final long serialVersionUID = 160L;

    private final Collection<ExtendedNeighborEntry<E>> possibleNewNeighbors;

    public LeaveAddNeighborsOperation(
            Collection<ExtendedNeighborEntry<E>> possibleNewNeighbors,
            MaintenanceId maintenanceId) {
        super(maintenanceId);
        this.possibleNewNeighbors = possibleNewNeighbors;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public EmptyResponseOperation handle(StructuredOverlay overlay) {
        CanOverlay<E> canOverlay = (CanOverlay<E>) overlay;

        for (ExtendedNeighborEntry<E> entry : this.possibleNewNeighbors) {
            if (entry.getId().equals(canOverlay.getId())) {
                continue;
            }

            if (canOverlay.getZone().neighbors(entry.getZone())) {

                canOverlay.getNeighborTable().putIfAbsent(
                        entry, entry.getDimension(), entry.getDirection());
            }
        }

        return EmptyResponseOperation.getInstance();
    }

}
