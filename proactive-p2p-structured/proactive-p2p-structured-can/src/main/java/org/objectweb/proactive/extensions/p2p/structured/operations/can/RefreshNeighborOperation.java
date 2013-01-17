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
package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.operations.CallableOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.CanOperations;
import org.objectweb.proactive.extensions.p2p.structured.operations.EmptyResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;

/**
 * @param <E>
 *            the {@link Element}s type manipulated.
 * 
 * @author lpellegr
 */
public class RefreshNeighborOperation<E extends Element> extends
        CallableOperation {

    private static final long serialVersionUID = 140L;

    private final byte dimension;

    private final byte direction;

    public RefreshNeighborOperation() {
        this.dimension = -1;
        this.direction = -1;
    }

    public RefreshNeighborOperation(byte dimension, byte direction) {
        this.dimension = dimension;
        this.direction = direction;
    }

    @Override
    @SuppressWarnings("unchecked")
    public EmptyResponseOperation handle(StructuredOverlay overlay) {
        CanOverlay<E> canOverlay = (CanOverlay<E>) overlay;

        for (byte dimension = 0; dimension < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dimension++) {
            for (byte direction = 0; direction < 2; direction++) {
                for (NeighborEntry<E> neighborEntry : canOverlay.getNeighborTable()
                        .get(dimension, direction)
                        .values()) {
                    if (this.dimension != -1 && dimension == this.dimension
                            && direction != -1 && direction == this.direction) {
                        neighborEntry.setZone(CanOperations.<E> getIdAndZoneResponseOperation(
                                neighborEntry.getStub())
                                .getPeerZone());
                    }
                }
            }
        }

        return new EmptyResponseOperation();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLeaveOperation() {
        return true;
    }

}
