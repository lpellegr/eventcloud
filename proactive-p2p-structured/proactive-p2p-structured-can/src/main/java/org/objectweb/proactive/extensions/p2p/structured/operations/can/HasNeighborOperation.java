package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.operations.BooleanResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.Operation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCANOverlay;

/**
 * Operation used to know if the peer which handles the operation has a neighbor
 * corresponding to the given UUID.
 * 
 * @author Laurent Pellegrino

 * @see AbstractCANOverlay#hasNeighbor(UUID)
 */
public class HasNeighborOperation implements Operation {

    private static final long serialVersionUID = 1L;
    
    private final UUID uuid;

    public HasNeighborOperation(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * {@inheritDoc}
     */
    public BooleanResponseOperation handle(StructuredOverlay overlay) {
        return new BooleanResponseOperation(((AbstractCANOverlay) overlay).hasNeighbor(this.uuid));
    }

}
