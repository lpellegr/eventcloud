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

import java.io.Serializable;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.operations.CallableOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.EmptyResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;

/**
 * Operation used to update the zone of a peer that takes over the leaving
 * peer's zone during a leave operation based on splits history.
 * 
 * @param <E>
 *            the {@link Element}s type manipulated.
 * 
 * @author lpellegr
 */
public class LeaveEnlargeZoneOperation<E extends Element> extends
        CallableOperation {

    private static final long serialVersionUID = 140L;

    private final int splitEntryIndex;

    private final byte reassignmentDimension;

    private final byte reassignmentDirection;

    private final E element;

    private final Serializable dataToTransfert;

    public LeaveEnlargeZoneOperation(int splitEntryIndex,
            byte reassignmentDimension, byte reassignmentDirection, E element,
            Serializable dataToTransfert) {
        this.splitEntryIndex = splitEntryIndex;
        this.reassignmentDimension = reassignmentDimension;
        this.reassignmentDirection = reassignmentDirection;
        this.element = element;
        this.dataToTransfert = dataToTransfert;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public EmptyResponseOperation handle(StructuredOverlay overlay) {
        CanOverlay<E> canOverlay = ((CanOverlay<E>) overlay);

        canOverlay.assignDataReceived(this.dataToTransfert);

        // enlarge the current zone
        canOverlay.getZone().enlarge(
                this.reassignmentDimension,
                CanOverlay.getOppositeDirection(this.reassignmentDirection),
                this.element);

        canOverlay.getSplitHistory().remove(this.splitEntryIndex);

        for (byte dimension = 0; dimension < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dimension++) {
            for (byte direction = 0; direction < 2; direction++) {
                for (NeighborEntry<E> entry : canOverlay.getNeighborTable()
                        .get(dimension, direction)
                        .values()) {
                    if (dimension != this.reassignmentDimension
                            || direction != CanOverlay.getOppositeDirection(this.reassignmentDirection)) {
                        PAFuture.waitFor(entry.getStub().receive(
                                new LeaveUpdateNeighborsOperation<E>(
                                        new NeighborEntry<E>(
                                                overlay.getId(),
                                                overlay.getStub(),
                                                canOverlay.getZone()))));
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
