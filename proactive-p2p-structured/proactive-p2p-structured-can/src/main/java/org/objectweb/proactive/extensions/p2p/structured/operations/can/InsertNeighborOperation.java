/**
 * Copyright (c) 2011 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import org.objectweb.proactive.extensions.p2p.structured.operations.BooleanResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.SynchronousOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborEntry;

/**
 * This operation is used to insert a {@link NeighborEntry} at the specified
 * <code>dimension</code> and <code>direction</code> of the peer receiving the
 * message.
 * 
 * @author lpellegr
 */
public class InsertNeighborOperation implements SynchronousOperation {

    private static final long serialVersionUID = 1L;

    /**
     * The neighbor peer to remove.
     */
    private final NeighborEntry entry;

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
    public InsertNeighborOperation(NeighborEntry neighborEntry, byte dimension,
            byte direction) {
        this.entry = neighborEntry;
        this.dimension = dimension;
        this.direction = direction;
    }

    public NeighborEntry getEntry() {
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
    public BooleanResponseOperation handle(StructuredOverlay overlay) {
        ((CanOverlay) overlay).getNeighborTable().get(
                this.dimension, this.direction).put(
                this.entry.getId(), this.entry);
        return new BooleanResponseOperation(true);
    }

}
