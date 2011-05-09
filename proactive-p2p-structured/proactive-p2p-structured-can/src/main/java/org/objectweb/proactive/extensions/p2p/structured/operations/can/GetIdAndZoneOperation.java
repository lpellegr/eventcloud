package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import org.objectweb.proactive.extensions.p2p.structured.operations.SynchronousOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;

/**
 * Operation used to retrieve the identifier and the zone of the peer which
 * handles the message.
 * 
 * @author lpellegr
 */
public class GetIdAndZoneOperation implements SynchronousOperation {

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
                overlay.getId(), ((CanOverlay) overlay).getZone());
    }

}
