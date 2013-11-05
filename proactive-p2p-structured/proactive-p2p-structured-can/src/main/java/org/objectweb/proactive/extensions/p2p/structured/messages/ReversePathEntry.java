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
package org.objectweb.proactive.extensions.p2p.structured.messages;

import java.io.Serializable;

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.MulticastRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayId;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.points.Point;

/**
 * Contains information about {@link Peer}s met while routing a
 * {@link MulticastRequest}.
 * 
 * @author lpellegr
 */
public class ReversePathEntry<E extends Coordinate> implements Serializable {

    private static final long serialVersionUID = 160L;

    private final OverlayId peerId;

    private final Point<E> peerLowerCoordinate;

    public OverlayId getPeerId() {
        return this.peerId;
    }

    public ReversePathEntry(OverlayId peerID, Point<E> peerLowerCoordinate) {
        this.peerId = peerID;
        this.peerLowerCoordinate = peerLowerCoordinate;
    }

    public Point<E> getPeerCoordinate() {
        return this.peerLowerCoordinate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "[peerCoordinateHashCode="
                + this.peerLowerCoordinate.hashCode() + "]";
    }

}
