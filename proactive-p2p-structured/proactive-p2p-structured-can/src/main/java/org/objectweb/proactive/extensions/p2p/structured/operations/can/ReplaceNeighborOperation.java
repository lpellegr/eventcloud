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

import org.objectweb.proactive.extensions.p2p.structured.operations.EmptyResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayId;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborTable;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;
import org.objectweb.proactive.extensions.p2p.structured.utils.HomogenousPair;

/**
 * Operation used in order to replace a neighbor by an another from a
 * {@link NeighborTable}.
 * 
 * @param <E>
 *            the {@link Element}s type manipulated.
 * 
 * @author lpellegr
 */
public class ReplaceNeighborOperation<E extends Element> extends
        JoinNeighborsManagementOperation {

    private static final long serialVersionUID = 160L;

    private final OverlayId peerIdToReplace;

    private final NeighborEntry<E> entry;

    public ReplaceNeighborOperation(OverlayId peerIdToReplace,
            NeighborEntry<E> entry) {
        super();
        this.peerIdToReplace = peerIdToReplace;
        this.entry = entry;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ResponseOperation handle(StructuredOverlay overlay) {
        NeighborTable<E> table = ((CanOverlay<E>) overlay).getNeighborTable();

        HomogenousPair<Byte> dimAndDir = table.remove(this.peerIdToReplace);
        table.add(this.entry, dimAndDir.getFirst(), dimAndDir.getSecond());

        return EmptyResponseOperation.getInstance();
    }

}
