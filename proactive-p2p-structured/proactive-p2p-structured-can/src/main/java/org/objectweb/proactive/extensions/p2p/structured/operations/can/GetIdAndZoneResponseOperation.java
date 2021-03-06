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

import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayId;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;

/**
 * Response associated to {@link GetIdAndZoneOperation}.
 * 
 * @param <E>
 *            the {@link Coordinate}s type manipulated.
 * 
 * @author lpellegr
 */
public class GetIdAndZoneResponseOperation<E extends Coordinate> implements
        ResponseOperation {

    private static final long serialVersionUID = 160L;

    private final OverlayId peerIdentifier;

    private final Zone<E> peerZone;

    public GetIdAndZoneResponseOperation(OverlayId peerIdentifier,
            Zone<E> peerZone) {
        this.peerIdentifier = peerIdentifier;
        this.peerZone = peerZone;
    }

    public OverlayId getPeerIdentifier() {
        return this.peerIdentifier;
    }

    public Zone<E> getPeerZone() {
        return this.peerZone;
    }

}
