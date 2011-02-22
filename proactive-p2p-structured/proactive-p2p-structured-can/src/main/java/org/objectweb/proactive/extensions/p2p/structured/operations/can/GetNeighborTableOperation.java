package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import org.objectweb.proactive.extensions.p2p.structured.operations.GenericResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.Operation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCANOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborTable;

/**
 * Operation used to retrieve the {@link NeighborTable} of the peer which
 * handles the message.
 * 
 * @author Laurent Pellegrino
 */
public class GetNeighborTableOperation implements Operation {

    private static final long serialVersionUID = 1L;

    public GetNeighborTableOperation() {

    }

    /**
     * Handles the message.
     * 
     * @param overlay
     *            the overlay which handles the message.
     */
    public GenericResponseOperation<NeighborTable> handle(StructuredOverlay overlay) {
        return new GenericResponseOperation<NeighborTable>(
                        ((AbstractCANOverlay) overlay).getNeighborTable());
    }

}
