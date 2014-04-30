/**
 * Copyright (c) 2011-2014 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package org.objectweb.proactive.extensions.p2p.structured.operations;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.GetIdAndZoneOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.GetIdAndZoneResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.GetNeighborTableOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.HasNeighborOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.InsertNeighborOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.RemoveNeighborOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.UpdateNeighborOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.MaintenanceId;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayId;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborTable;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;

/**
 * CanOperations provides several static methods to perform operations on a
 * specified {@link Peer} of type CAN by hiding ProActive mechanisms.
 * <p>
 * All methods are susceptible to thrown a {@link ProActiveRuntimeException} if
 * a communication problem occurs while the operation is dispatched to the
 * remote node.
 * 
 * @author lpellegr
 */
public final class CanOperations {

    private CanOperations() {

    }

    @SuppressWarnings("unchecked")
    public static <E extends Coordinate> GetIdAndZoneResponseOperation<E> getIdAndZoneResponseOperation(Peer peer) {
        return (GetIdAndZoneResponseOperation<E>) PAFuture.getFutureValue(peer.receive(new GetIdAndZoneOperation<E>()));
    }

    public static <E extends Coordinate> boolean hasNeighbor(Peer peer,
                                                             OverlayId neighborID) {
        return ((BooleanResponseOperation) PAFuture.getFutureValue(peer.receive(new HasNeighborOperation<E>(
                neighborID)))).getValue();
    }

    public static <E extends Coordinate> void insertNeighbor(Peer peer,
                                                             NeighborEntry<E> entry,
                                                             byte dimension,
                                                             byte direction,
                                                             MaintenanceId maintenanceId) {
        PAFuture.waitFor(peer.receive(new InsertNeighborOperation<E>(
                entry, dimension, direction, maintenanceId)));
    }

    public static <E extends Coordinate> BooleanResponseOperation removeNeighbor(Peer peer,
                                                                                 OverlayId peerIdentifier,
                                                                                 MaintenanceId maintenanceId) {
        return (BooleanResponseOperation) PAFuture.getFutureValue(peer.receive(new RemoveNeighborOperation<E>(
                peerIdentifier, maintenanceId)));
    }

    public static <E extends Coordinate> BooleanResponseOperation removeNeighbor(Peer peer,
                                                                                 OverlayId peerIdentifier,
                                                                                 byte dimension,
                                                                                 byte direction,
                                                                                 MaintenanceId maintenanceId) {
        return (BooleanResponseOperation) PAFuture.getFutureValue(peer.receive(new RemoveNeighborOperation<E>(
                peerIdentifier, dimension, direction, maintenanceId)));
    }

    public static <E extends Coordinate> void updateNeighborOperation(Peer peer,
                                                                      NeighborEntry<E> entry,
                                                                      byte dimension,
                                                                      byte direction,
                                                                      MaintenanceId maintenanceId) {
        PAFuture.waitFor(peer.receive(new UpdateNeighborOperation<E>(
                entry, dimension, direction, maintenanceId)));
    }

    @SuppressWarnings("unchecked")
    public static <E extends Coordinate> NeighborTable<E> getNeighborTable(Peer peer) {
        return ((GenericResponseOperation<NeighborTable<E>>) PAFuture.getFutureValue(peer.receive(new GetNeighborTableOperation<E>()))).getValue();
    }

}
