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

import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.operations.BooleanResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.SynchronousOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborTable;

/**
 * A {@code RemoveNeighborOperation} is used to remove the specified
 * {@link Peer} (by using the given identifier) from the {@link NeighborTable}
 * of the peer which receives this message.
 * 
 * @author lpellegr
 */
public class RemoveNeighborOperation implements SynchronousOperation {

    private static final long serialVersionUID = 1L;

    /**
     * The identifier pointing to the neighbor to remove.
     */
    private final UUID peerIdentifier;

    /**
     * The dimension in which the neighbor to remove is.
     */
    private final byte dimension;

    /**
     * The direction in which the neighbor to remove is.
     */
    private final byte direction;

    /**
     * Constructs a new RemoveNeighborOperation with the specified
     * {@code peerIdentifier}, {@code dimension} and {@code direction}.
     * 
     * @param peerIdentifier
     *            the identifier pointing to the neighbor to remove.
     * 
     * @param dimension
     *            the dimension in which the neighbor to remove is.
     * 
     * @param direction
     *            the direction in which the neighbor to remove is.
     */
    public RemoveNeighborOperation(UUID peerIdentifier, byte dimension,
            byte direction) {
        this.peerIdentifier = peerIdentifier;
        this.dimension = dimension;
        this.direction = direction;
    }

    /**
     * Constructs a new RemoveNeighborOperation with the specified
     * <code>peerIdentifier</code>, <code>dimension</code> and
     * <code>direction</code>.
     * 
     * @param peerIdentifier
     *            the identifier pointing to the neighbor to remove.
     */
    public RemoveNeighborOperation(UUID peerIdentifier) {
        this.peerIdentifier = peerIdentifier;
        this.dimension = -1;
        this.direction = -1;
    }

    /**
     * Returns the identifier pointing to the neighbor to remove.
     * 
     * @return the identifier pointing to the neighbor to remove.
     */
    public UUID getPeerIdentifier() {
        return this.peerIdentifier;
    }

    /**
     * Returns the dimension in which the neighbor to remove is.
     * 
     * @return the dimension in which the neighbor to remove is.
     */
    public byte getDimension() {
        return this.dimension;
    }

    /**
     * Returns the direction in which the neighbor to remove is.
     * 
     * @return the direction in which the neighbor to remove is.
     */
    public byte getDirection() {
        return this.direction;
    }

    /**
     * {@inheritDoc}
     */
    public BooleanResponseOperation handle(StructuredOverlay overlay) {
        NeighborTable table = ((CanOverlay) overlay).getNeighborTable();
        boolean result;

        if (dimension == -1 && direction == -1) {
            result = table.remove(this.peerIdentifier) != null;
        } else {
            result =
                    table.remove(
                            this.peerIdentifier, this.dimension, this.direction);
        }

        return new BooleanResponseOperation(result);
    }

}
