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
package org.objectweb.proactive.extensions.p2p.structured.operations.mutual_exclusion;

import org.objectweb.proactive.extensions.p2p.structured.mutual_exclusion.RicartAgrawalaManager;
import org.objectweb.proactive.extensions.p2p.structured.operations.EmptyResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.MaintenanceId;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayId;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;

/**
 * Request operation associated to the Ricart Agrawala algorithm.
 * 
 * @author lpellegr
 */
public class RicartAgrawalaRequest extends MutualExclusionOperation {

    private static final long serialVersionUID = 160L;

    private final Peer requester;

    private final MaintenanceId maintenanceId;

    private final OverlayId requesterId;

    private final long requesterSequenceNumber;

    public RicartAgrawalaRequest(MaintenanceId maintenanceId, Peer requester,
            OverlayId requesterId, long requesterSequenceNumber) {
        this.requester = requester;
        this.maintenanceId = maintenanceId;
        this.requesterId = requesterId;
        this.requesterSequenceNumber = requesterSequenceNumber;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ResponseOperation handle(StructuredOverlay overlay) {
        ((RicartAgrawalaManager) overlay.getMutualExclusionManager()).receiveRequest(this);
        return EmptyResponseOperation.getInstance();
    }

    public MaintenanceId getMaintenanceId() {
        return this.maintenanceId;
    }

    public Peer getRequester() {
        return this.requester;
    }

    public OverlayId getRequesterId() {
        return this.requesterId;
    }

    public long getRequesterSequenceNumber() {
        return this.requesterSequenceNumber;
    }

}
