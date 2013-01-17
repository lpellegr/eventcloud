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

import org.objectweb.proactive.extensions.p2p.structured.operations.CallableOperation;
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
public class UpdateNeighborOperation2<E extends Element> extends
        CallableOperation {

    private static final long serialVersionUID = 140L;

    private final Collection<NeighborEntry<E>> possibleNewNeighbors;

    private final byte dimension;

    private final byte direction;

    public UpdateNeighborOperation2(
            Collection<NeighborEntry<E>> possibleNewNeighbors, byte dimension,
            byte direction) {
        this.possibleNewNeighbors = possibleNewNeighbors;
        this.dimension = dimension;
        this.direction = direction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public EmptyResponseOperation handle(StructuredOverlay overlay) {
        CanOverlay<E> canOverlay = (CanOverlay<E>) overlay;

        for (NeighborEntry<E> entry : this.possibleNewNeighbors) {
            if (canOverlay.getZone().neighbors(entry.getZone()) != -1) {
                System.out.println("UpdateNeighborOperation2.handle() ADDED ENTRY");
                canOverlay.getNeighborTable().add(
                        entry, this.dimension, this.direction);
            }
        }

        return new EmptyResponseOperation();
    }
}
