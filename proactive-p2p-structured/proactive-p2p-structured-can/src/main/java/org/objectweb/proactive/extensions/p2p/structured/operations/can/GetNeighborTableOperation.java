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
package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import org.objectweb.proactive.extensions.p2p.structured.operations.CallableOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.GenericResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborTable;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;

/**
 * Operation used to retrieve the {@link NeighborTable} of the peer which
 * handles the message.
 * 
 * @param <E>
 *            the {@link Coordinate}s type manipulated.
 * 
 * @author lpellegr
 */
public class GetNeighborTableOperation<E extends Coordinate> extends
        CallableOperation {

    private static final long serialVersionUID = 160L;

    public GetNeighborTableOperation() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public GenericResponseOperation<NeighborTable<E>> handle(StructuredOverlay overlay) {
        return new GenericResponseOperation<NeighborTable<E>>(
                ((CanOverlay<E>) overlay).getNeighborTable());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCompatibleWithRouting() {
        return true;
    }

}
