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
package org.objectweb.proactive.extensions.p2p.structured.overlay.can;

import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;

/**
 * Extended neighbor entry to know dimension and direction associated to the
 * neighbor entry (relative to the peer from which the entry is created).
 * 
 * @author lpellegr
 *
 * @param <E>
 *            the {@link Coordinate}s type manipulated.
 */
public class ExtendedNeighborEntry<E extends Coordinate> extends
        NeighborEntry<E> {

    private static final long serialVersionUID = 1L;

    private final byte dimension;

    private final byte direction;

    public ExtendedNeighborEntry(NeighborEntry<E> entry, byte dimension,
            byte direction) {
        super(entry.getId(), entry.getStub(), entry.getZone());
        this.dimension = dimension;
        this.direction = direction;
    }

    public byte getDimension() {
        return dimension;
    }

    public byte getDirection() {
        return direction;
    }

}
