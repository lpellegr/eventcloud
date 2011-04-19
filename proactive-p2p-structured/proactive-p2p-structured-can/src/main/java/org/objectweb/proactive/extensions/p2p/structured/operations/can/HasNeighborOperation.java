package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.operations.BooleanResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.SynchronousOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCanOverlay;

/**
 * Operation used to know if the peer which handles the operation has a neighbor
 * corresponding to the given UUID.
 * 
 * @author lpellegr
 * 
 * @see AbstractCanOverlay#hasNeighbor(UUID)
 */
public class HasNeighborOperation implements SynchronousOperation {

    private static final long serialVersionUID = 1L;

    private final UUID uuid;

    public HasNeighborOperation(UUID uuid) {
        this.uuid = uuid;
    }

    /**
     * {@inheritDoc}
     */
    public BooleanResponseOperation handle(StructuredOverlay overlay) {
        return new BooleanResponseOperation(
                ((AbstractCanOverlay) overlay).hasNeighbor(this.uuid));
    }

}
