package fr.inria.eventcloud.operations.can;

import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.Zone;

/**
 * @author lpellegr
 */
@SuppressWarnings("serial")
public class GetZoneInfoResponseOperation implements ResponseOperation {

    private Zone zone;

    private boolean hasMetSynchronousMessage = false;

    public GetZoneInfoResponseOperation(Zone zone, boolean hasMet) {
        this.zone = zone;
        this.hasMetSynchronousMessage = hasMet;
    }

    public Zone getZone() {
        return this.zone;
    }

    public boolean hasMetSynchronousMessage() {
        return this.hasMetSynchronousMessage;
    }

}
