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

import java.util.Collection;

import org.objectweb.proactive.extensions.p2p.structured.operations.EmptyResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;

/**
 * Piggyback peers that are potentially new neighbors of the peer that receive
 * the operation.
 * 
 * @param <E>
 *            the {@link Element}s type manipulated.
 * 
 * @author lpellegr
 */
public class LeaveAddNeighborsOperation<E extends Element> extends
        LeaveNeighborsManagementOperation {

    private static final long serialVersionUID = 150L;

    private final Collection<NeighborEntry<E>> possibleNewNeighbors;

    public LeaveAddNeighborsOperation(
            Collection<NeighborEntry<E>> possibleNewNeighbors) {
        this.possibleNewNeighbors = possibleNewNeighbors;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public EmptyResponseOperation handle(StructuredOverlay overlay) {
        CanOverlay<E> canOverlay = (CanOverlay<E>) overlay;

        for (NeighborEntry<E> entry : this.possibleNewNeighbors) {
            byte abutDimension =
                    canOverlay.getZone().neighbors(entry.getZone());

            if (abutDimension != -1) {
                byte abutDirection =
                        canOverlay.getZone().neighbors(
                                entry.getZone(), abutDimension);

                if (!canOverlay.getNeighborTable().contains(
                        entry.getId(), abutDimension, abutDirection)) {
                    canOverlay.getNeighborTable().add(
                            entry, abutDimension, abutDirection);
                }
            }
        }

        return EmptyResponseOperation.getInstance();
    }

}
