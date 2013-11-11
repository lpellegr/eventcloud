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
import java.util.LinkedList;

import org.objectweb.proactive.extensions.p2p.structured.operations.MaintenanceOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.MaintenanceId;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayId;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborTable;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.SplitEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;

/**
 * Operation used to send back to the peer that joins the network its overlay
 * information.
 * 
 * @param <E>
 *            the {@link Coordinate}s type manipulated.
 * 
 * @author lpellegr
 */
public class JoinWelcomeOperation<E extends Coordinate> extends
        MaintenanceOperation {

    private static final long serialVersionUID = 160L;

    private final OverlayId peerId;

    private final Zone<E> zone;

    private final LinkedList<SplitEntry> splitHistory;

    private final NeighborTable<E> neighbors;

    private final Serializable data;

    public JoinWelcomeOperation(OverlayId peerId, Zone<E> zone,
            LinkedList<SplitEntry> splitHistory, NeighborTable<E> neighbors,
            Serializable data, MaintenanceId maintenanceId) {
        super(maintenanceId);
        this.peerId = peerId;
        this.zone = zone;
        this.splitHistory = splitHistory;
        this.neighbors = neighbors;
        this.data = data;
    }

    public OverlayId getPeerId() {
        return this.peerId;
    }

    public Zone<E> getZone() {
        return this.zone;
    }

    public LinkedList<SplitEntry> getSplitHistory() {
        return this.splitHistory;
    }

    public NeighborTable<E> getNeighbors() {
        return this.neighbors;
    }

    public Serializable getData() {
        return this.data;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public ResponseOperation handle(StructuredOverlay overlay) {
        return ((CanOverlay<E>) overlay).handleJoinWelcomeOperation(this);
    }

}
