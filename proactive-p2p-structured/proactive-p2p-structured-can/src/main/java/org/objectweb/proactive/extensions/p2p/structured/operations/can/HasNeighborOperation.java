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

import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.operations.BooleanResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.SynchronousOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;

/**
 * Operation used to know if the peer which handles the operation has a neighbor
 * corresponding to the given UUID.
 * 
 * @author lpellegr
 * 
 * @see CanOverlay#hasNeighbor(UUID)
 */
public class HasNeighborOperation implements SynchronousOperation {

    private static final long serialVersionUID = 1L;

    private final UUID uuid;

    public HasNeighborOperation(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BooleanResponseOperation handle(StructuredOverlay overlay) {
        return new BooleanResponseOperation(
                ((CanOverlay) overlay).hasNeighbor(this.uuid));
    }

}
