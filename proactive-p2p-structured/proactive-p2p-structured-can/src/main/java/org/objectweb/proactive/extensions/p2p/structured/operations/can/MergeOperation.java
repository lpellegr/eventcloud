package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.operations.EmptyResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.Operation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborTable;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;

/**
 * A {@code MergeOperation} is used when a {@link Peer} have to merge with an
 * another. It consists in transferring data from one peer to another, to merge
 * the two zone and to update the neighbors.
 * 
 * @author lpellegr
 */
public class MergeOperation implements Operation {

    private static final long serialVersionUID = 1L;

    /**
     * The current dimension of the leaving peer.
     */
    private final int dimension;

    /**
     * The current direction of the leaving peer.
     */
    private final int direction;

    /**
     * The neighbors of the leaving peer.
     */
    private final NeighborTable neighborsToReallocate;

    private final UUID peerToMergeWith;

    private final Object dataToReallocate;

    private final Zone zoneToReallocate;

    /**
     * Constructor.
     * 
     * @param dimension
     *            the dimension of the peer to merge with.
     * @param direction
     *            the direction of the peer to merge with.
     * @param peerToMergeWith
     *            the identifier of the peer to merge with.
     * @param zone
     * @param neighbors
     *            the neighbors to reallocate.
     * @param data
     *            data to reallocate.
     */
    public MergeOperation(int dimension, int direction, UUID peerToMergeWith, Zone zone,
            NeighborTable neighbors, Object data) {
        if (zone == null) {
            throw new NullPointerException();
        }

        this.peerToMergeWith = peerToMergeWith;
        this.dimension = dimension;
        this.direction = direction;
        this.neighborsToReallocate = neighbors;
        this.dataToReallocate = data;
        this.zoneToReallocate = zone;
    }

    public int getDimension() {
        return this.dimension;
    }

    public int getDirection() {
        return this.direction;
    }

    public NeighborTable getNeighborsToReallocate() {
        return this.neighborsToReallocate;
    }

    public UUID getPeerToMergeWith() {
        return this.peerToMergeWith;
    }

    public Object getDataToReallocate() {
        return this.dataToReallocate;
    }

    public Zone getZoneToReallocate() {
        return this.zoneToReallocate;
    }

    /**
     * {@inheritDoc}
     */
    public EmptyResponseOperation handle(StructuredOverlay overlay) {
        return ((AbstractCanOverlay) overlay).handleMergeMessage(this);
    }

}
