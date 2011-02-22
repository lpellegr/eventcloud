package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import org.objectweb.proactive.extensions.p2p.structured.operations.Operation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCANOverlay;

/**
 * Operation used to retrieve the identifier and the zone of the peer which
 * handles the message.
 * 
 * @author Laurent Pellegrino
 */
public class GetIdAndZoneOperation implements Operation {

    private static final long serialVersionUID = 1L;

    public GetIdAndZoneOperation() {

    }

    /**
     * Handles the message.
     * 
     * @param overlay
     *            the overlay which handles the message.
     */
    public GetIdAndZoneResponseOperation handle(StructuredOverlay overlay) {
        return new GetIdAndZoneResponseOperation(
        				overlay.getLocalPeer().getId(),
        				((AbstractCANOverlay) overlay).getZone());
    }

}
