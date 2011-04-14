package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import org.objectweb.proactive.extensions.p2p.structured.operations.GenericResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.SynchronousOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborTable;


/**
 * Operation used to retrieve the {@link NeighborTable} of the peer which
 * handles the message.
 * 
 * @author lpellegr
 */
public class GetNeighborTableOperation implements SynchronousOperation {

    private static final long serialVersionUID = 1L;

    public GetNeighborTableOperation() {

    }

    /**
     * {@inheritDoc}
     */
    public GenericResponseOperation<NeighborTable> handle(StructuredOverlay overlay) {
        return new GenericResponseOperation<NeighborTable>(((AbstractCanOverlay) overlay).getNeighborTable());
    }

}
