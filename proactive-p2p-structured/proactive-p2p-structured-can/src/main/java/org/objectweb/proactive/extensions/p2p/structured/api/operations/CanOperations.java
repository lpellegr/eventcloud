package org.objectweb.proactive.extensions.p2p.structured.api.operations;

import java.util.UUID;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.extensions.p2p.structured.operations.BooleanResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.GenericResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.GetIdAndZoneOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.GetIdAndZoneResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.GetNeighborTableOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.HasNeighborOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.InsertNeighborOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.RemoveNeighborOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.UpdateNeighborOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborTable;

/**
 * CANOperations provides several static methods to perform operations on a
 * specified {@link Peer} of type CAN by hiding ProActive mechanisms.
 * <p>
 * All methods are susceptible to thrown a {@link ProActiveRuntimeException} if
 * a communication problem occurs while the operation is dispatched to the
 * remote node.
 * 
 * @author lpellegr
 */
public class CanOperations {

    public static GetIdAndZoneResponseOperation getIdAndZoneResponseOperation(Peer peer) {
        return (GetIdAndZoneResponseOperation) PAFuture.getFutureValue(peer.receiveImmediateService(new GetIdAndZoneOperation()));
    }

    public static boolean hasNeighbor(Peer peer, UUID neighborID) {
        return ((BooleanResponseOperation) PAFuture.getFutureValue(peer.receiveImmediateService(new HasNeighborOperation(
                neighborID)))).getValue();
    }

    public static BooleanResponseOperation insertNeighbor(Peer peer,
                                                          NeighborEntry entry,
                                                          byte dimension,
                                                          byte direction) {
        return (BooleanResponseOperation) PAFuture.getFutureValue(peer.receiveImmediateService(new InsertNeighborOperation(
                entry, dimension, direction)));
    }

    public static BooleanResponseOperation removeNeighbor(Peer peer,
                                                          UUID peerIdentifier) {
        return (BooleanResponseOperation) PAFuture.getFutureValue(peer.receiveImmediateService(new RemoveNeighborOperation(
                peerIdentifier)));
    }

    public static BooleanResponseOperation removeNeighbor(Peer peer,
                                                          UUID peerIdentifier,
                                                          byte dimension,
                                                          byte direction) {
        return (BooleanResponseOperation) PAFuture.getFutureValue(peer.receiveImmediateService(new RemoveNeighborOperation(
                peerIdentifier, dimension, direction)));
    }

    public static void updateNeighborOperation(Peer peer, NeighborEntry entry,
                                               byte dimension, byte direction) {
        PAFuture.waitFor(peer.receiveImmediateService(new UpdateNeighborOperation(
                entry, dimension, direction)));
    }

    @SuppressWarnings("unchecked")
    public static NeighborTable getNeighborTable(Peer peer) {
        return ((GenericResponseOperation<NeighborTable>) PAFuture.getFutureValue(peer.receiveImmediateService(new GetNeighborTableOperation()))).getValue();
    }

}
