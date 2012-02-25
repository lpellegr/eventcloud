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

import org.objectweb.proactive.extensions.p2p.structured.operations.GenericResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.SynchronousOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborTable;

/**
 * Operation used to retrieve the {@link NeighborTable} of the peer which
 * handles the message.
 * 
 * @author lpellegr
 */
public class GetNeighborTableOperation implements SynchronousOperation {

    private static final long serialVersionUID = 1L;

    public GetNeighborTableOperation() {

    }

    /**
     * {@inheritDoc}
     */
    public GenericResponseOperation<NeighborTable> handle(StructuredOverlay overlay) {
        return new GenericResponseOperation<NeighborTable>(
                ((CanOverlay) overlay).getNeighborTable());
    }

}
