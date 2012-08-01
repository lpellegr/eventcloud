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
package org.objectweb.proactive.extensions.p2p.structured.overlay.can;

import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;

/**
 * The JoinInformation class is used to store temporary data that are compute on
 * the landmark peer which is joined. These information are computed during the
 * join introduce phase and will be used during the join welcome phase.
 * 
 * @param <E>
 *            the {@link Element}s type manipulated.
 * 
 * @author lpellegr
 */
public class JoinInformation<E extends Element> {

    private final byte dimension;

    private final byte direction;

    // zone of the peer maintaining the JoinInformation object
    private final Zone<E> zone;

    private final NeighborEntry<E> entry;

    public JoinInformation(byte dimension, byte direction, Zone<E> zone,
            NeighborEntry<E> entry) {
        super();
        this.dimension = dimension;
        this.direction = direction;
        this.zone = zone;
        this.entry = entry;
    }

    public byte getDimension() {
        return this.dimension;
    }

    public byte getDirection() {
        return this.direction;
    }

    public Zone<E> getZone() {
        return this.zone;
    }

    public NeighborEntry<E> getEntry() {
        return this.entry;
    }

}
