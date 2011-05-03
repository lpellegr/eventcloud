package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.operations.EmptyResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.SynchronousOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborTable;
import org.objectweb.proactive.extensions.p2p.structured.util.Pair;

/**
 * Operation used in order to replace a neighbor by an another from a
 * {@link NeighborTable}.
 * 
 * @author lpellegr
 */
public class ReplaceNeighborOperation implements SynchronousOperation {

    private static final long serialVersionUID = 1L;

    private final UUID peerIdToReplace;

    private final NeighborEntry entry;

    public ReplaceNeighborOperation(UUID peerIdToReplace, NeighborEntry entry) {
        super();
        this.peerIdToReplace = peerIdToReplace;
        this.entry = entry;
    }

    public EmptyResponseOperation handle(StructuredOverlay overlay) {
        NeighborTable table = ((AbstractCanOverlay) overlay).getNeighborTable();

        Pair<Byte> dimAndDir = table.remove(this.peerIdToReplace);
        table.add(this.entry, dimAndDir.getFirst(), dimAndDir.getSecond());

        return new EmptyResponseOperation();
    }

}
