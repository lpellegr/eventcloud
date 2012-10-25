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

import org.objectweb.proactive.extensions.p2p.structured.operations.CallableOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.EmptyResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborTable;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;

/**
 * Operation used in order to update a {@link Zone} of a neighbor cached in the
 * {@link NeighborTable} of the peer which handles it.
 * 
 * @param <E>
 *            the {@link Element}s type manipulated.
 * 
 * @author lpellegr
 */
public class UpdateNeighborOperation<E extends Element> implements
        CallableOperation {

    private static final long serialVersionUID = 130L;

    private final NeighborEntry<E> entry;

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
    public UpdateNeighborOperation(NeighborEntry<E> entry, byte dimension,
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
    @Override
    @SuppressWarnings("unchecked")
    public EmptyResponseOperation handle(StructuredOverlay overlay) {
        ((CanOverlay<E>) overlay).getNeighborTable().get(
                this.dimension, this.direction).replace(
                this.entry.getId(), this.entry);

        return new EmptyResponseOperation();
    }

}
