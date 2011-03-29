package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.Zone;

/**
 * Response associated to {@link GetIdAndZoneOperation}.
 */
public class GetIdAndZoneResponseOperation implements ResponseOperation {

    private static final long serialVersionUID = 1L;

    private final UUID peerIdentifier;

    private final Zone peerZone;

    public GetIdAndZoneResponseOperation(UUID peerIdentifier, Zone peerZone) {
        super();
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
