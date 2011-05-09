/**
 * Copyright (c) 2011 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;

/**
 * Response associated to {@link GetIdAndZoneOperation}.
 * 
 * @author lpellegr
 */
public class GetIdAndZoneResponseOperation implements ResponseOperation {

    private static final long serialVersionUID = 1L;

    private final UUID peerIdentifier;

    private final Zone peerZone;

    public GetIdAndZoneResponseOperation(UUID peerIdentifier, Zone peerZone) {
        this.peerIdentifier = peerIdentifier;
        this.peerZone = peerZone;
    }

    public UUID getPeerIdentifier() {
        return this.peerIdentifier;
    }

    public Zone getPeerZone() {
        return this.peerZone;
    }

}
