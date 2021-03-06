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

import java.io.Serializable;
import java.util.LinkedList;
import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborTable;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.SplitEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;

/**
 * Response associated to {@link JoinIntroduceOperation}. This response contains
 * some information that have to be affected to the peer which join the network.
 * 
 * @param <E>
 *            the {@link Coordinate}s type manipulated.
 * 
 * @author lpellegr
 */
public class JoinIntroduceResponseOperation<E extends Coordinate> implements
        ResponseOperation {

    private static final long serialVersionUID = 160L;

    private final UUID peerId;

    private final Zone<E> zone;

    private final LinkedList<SplitEntry> splitHistory;

    private final NeighborTable<E> neighbors;

    private final Serializable data;

    public JoinIntroduceResponseOperation(UUID peerId, Zone<E> zone,
            LinkedList<SplitEntry> splitHistory, NeighborTable<E> neighbors,
            Serializable data) {
        this.peerId = peerId;
        this.zone = zone;
        this.splitHistory = splitHistory;
        this.neighbors = neighbors;
        this.data = data;
    }

    public UUID getPeerId() {
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

}
