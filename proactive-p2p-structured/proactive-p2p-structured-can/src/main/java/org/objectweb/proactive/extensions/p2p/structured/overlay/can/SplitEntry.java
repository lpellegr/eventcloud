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
package org.objectweb.proactive.extensions.p2p.structured.overlay.can;

import java.io.Serializable;

import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;

import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;

/**
 * {@code SplitEntry} stores the dimension and the direction of a {@link Zone}
 * when a join operation is performed by a {@link Peer}.
 * 
 * @author lpellegr
 */
public class SplitEntry implements Serializable {

    private static final long serialVersionUID = 151L;

    private final byte dimension;

    private final byte direction;

    private final long timestamp;

    /**
     * Constructor.
     * 
     * @param dimension
     *            the dimension of the split.
     * @param direction
     *            the direction of the split.
     * @param timestamp
     *            a timestamp value that acts as an identifier.
     */
    public SplitEntry(byte dimension, byte direction, long timestamp) {
        this.dimension = dimension;
        this.direction = direction;
        this.timestamp = timestamp;
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

    /**
     * Returns a timestamp value that indicates when the split entry has been
     * created. This value acts as an identifier.
     * 
     * @return the timestamp value.
     */
    public long getTimestamp() {
        return this.timestamp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof SplitEntry) {
            SplitEntry that = (SplitEntry) other;

            return this.dimension == that.dimension
                    && this.direction == that.direction;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return 31 * (31 + this.dimension) + this.direction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        ToStringHelper toStringHelper = Objects.toStringHelper(this.getClass());
        toStringHelper.add("dimension", this.dimension);
        toStringHelper.add("direction", this.direction);

        return toStringHelper.toString();
    }

}
