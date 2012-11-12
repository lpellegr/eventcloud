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

import java.io.Serializable;

import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;

/**
 * {@code SplitEntry} stores the dimension and the direction of a {@link Zone}
 * when a join operation is performed by a {@link Peer}.
 * 
 * @author lpellegr
 */
public class SplitEntry implements Serializable {

    private static final long serialVersionUID = 130L;

    private byte dimension = 0;

    private byte direction = 0;

    public SplitEntry() {
    }

    /**
     * Constructor.
     * 
     * @param dimension
     *            the dimension of the split.
     * @param direction
     *            the direction of the split.
     */
    public SplitEntry(byte dimension, byte direction) {
        this.dimension = dimension;
        this.direction = direction;
    }

    /**
     * Returns the dimension of the split.
     * 
     * @return the dimension of the split.
     */
    public byte getDimension() {
        return this.dimension;
    }

    /**
     * Returns the direction of a split.
     * 
     * @return the direction of a split.
     */
    public byte getDirection() {
        return this.direction;
    }

}
