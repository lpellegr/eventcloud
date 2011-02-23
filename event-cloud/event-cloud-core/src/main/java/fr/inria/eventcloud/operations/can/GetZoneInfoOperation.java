package fr.inria.eventcloud.operations.can;

import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.operations.Operation;
import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;

import fr.inria.eventcloud.overlay.SemanticQueryManager;
import fr.inria.eventcloud.overlay.can.SemanticSpaceCanOverlay;

/**
 * @author lpellegr
 */
public class GetZoneInfoOperation implements Operation {

    private static final long serialVersionUID = 1L;
    
    private UUID msgId;

    public GetZoneInfoOperation(UUID msgId) {
        this.msgId = msgId;
    }

    public ResponseOperation handle(StructuredOverlay overlay) {
        return new GetZoneInfoResponseOperation(
        				((SemanticSpaceCanOverlay) overlay).getZone(),
        				((SemanticQueryManager)((SemanticSpaceCanOverlay) overlay)
        						.getQueryManager()).getQueriesIdentifierMet()
        							.contains(this.msgId));
    }

}
