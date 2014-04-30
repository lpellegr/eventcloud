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

/**
 * Identifies a maintenance request triggered on a peer (join, leave or
 * reassign).
 * 
 * @author lpellegr
 */
public class MaintenanceId implements Serializable {

    private static final long serialVersionUID = 160L;

    private final OverlayId overlayId;

    private final long sequenceId;

    public MaintenanceId(OverlayId overlayId, long sequenceId) {
        this.overlayId = overlayId;
        this.sequenceId = sequenceId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.overlayId == null)
                ? 0 : this.overlayId.hashCode());
        result =
                prime * result
                        + (int) (this.sequenceId ^ (this.sequenceId >>> 32));
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        MaintenanceId other = (MaintenanceId) obj;
        if (this.overlayId == null) {
            if (other.overlayId != null) {
                return false;
            }
        } else if (!this.overlayId.equals(other.overlayId)) {
            return false;
        }
        if (this.sequenceId != other.sequenceId) {
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.overlayId.toString() + "|" + this.sequenceId;
    }

}
