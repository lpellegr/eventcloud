/**
 * Copyright (c) 2011-2013 INRIA.
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

import org.objectweb.proactive.extensions.p2p.structured.operations.EmptyResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.MaintenanceId;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;

/**
 * This operation is used to insert a {@link NeighborEntry} at the specified
 * {@code dimension} and {@code direction} from the peer that receives the
 * message.
 * 
 * @param <E>
 *            the {@link Coordinate}s type manipulated.
 * 
 * @author lpellegr
 */
public class InsertNeighborOperation<E extends Coordinate> extends
        JoinNeighborsManagementOperation {

    private static final long serialVersionUID = 160L;

    /**
     * The neighbor peer to remove.
     */
    private final NeighborEntry<E> entry;

    /**
     * The dimension of the neighbor to remove.
     */
    private final byte dimension;

    /**
     * The direction of the neighbor to remove.
     */
    private final byte direction;

    /**
     * Constructor.
     * 
     * @param neighborEntry
     *            the neighbor entry to insert.
     * @param dimension
     *            the dimension of the neighbor to remove.
     * @param direction
     *            the direction of the neighbor to remove
     */
    public InsertNeighborOperation(NeighborEntry<E> neighborEntry,
            byte dimension, byte direction, MaintenanceId maintenanceId) {
        super(maintenanceId);
        this.entry = neighborEntry;
        this.dimension = dimension;
        this.direction = direction;
    }

    public NeighborEntry<E> getEntry() {
        return this.entry;
    }

    /**
     * Returns the dimension of the neighbor to remove.
     * 
     * @return the dimension of the neighbor to remove.
     */
    public byte getDimension() {
        return this.dimension;
    }

    /**
     * Returns the direction of the neighbor to remove.
     * 
     * @return the direction of the neighbor to remove.
     */
    public byte getDirection() {
        return this.direction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public ResponseOperation handle(StructuredOverlay overlay) {
        ((CanOverlay<E>) overlay).getNeighborTable().get(
                this.dimension, this.direction).put(
                this.entry.getId(), this.entry);

        return EmptyResponseOperation.getInstance();
    }

}
