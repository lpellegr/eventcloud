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
package org.objectweb.proactive.extensions.p2p.structured.overlay;

import java.io.Serializable;
import java.util.UUID;

/**
 * Overlay Identifier.
 * 
 * @author lpellegr
 */
public final class OverlayId implements Comparable<OverlayId>, Serializable {

    private static final long serialVersionUID = 160L;

    private final UUID uuid;

    /**
     * Cached hashCode value (class is immutable)
     */
    private transient int hashCode;

    /**
     * Cached toString value (class is immutable)
     */
    private transient String toString;

    public OverlayId() {
        this.uuid = UUID.randomUUID();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(OverlayId other) {
        return this.uuid.compareTo(other.uuid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        return other instanceof OverlayId
                && this.uuid.equals(((OverlayId) other).uuid);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        if (this.hashCode == 0) {
            this.hashCode = this.uuid.hashCode();
        }

        return this.hashCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (this.toString == null) {
            this.toString = this.uuid.toString();
        }

        return this.toString;
    }

}
