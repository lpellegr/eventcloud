package org.objectweb.proactive.extensions.p2p.structured.overlay.can;

import java.io.Serializable;

import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;

/**
 * {@code SplitEntry} stores the dimension and the direction of a {@link Zone}
 * when a join operation is performed by a {@link Peer}.
 * 
 * @author lpellegr
 */
public class SplitEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    private byte dimension = 0;

    private byte direction = 0;

    public SplitEntry() {
    }

    /**
     * Constructor.
     * 
     * @param dimension
     *            the dimension of the split.
     * @param direction
     *            the direction of the split.
     */
    public SplitEntry(byte dimension, byte direction) {
        this.dimension = dimension;
        this.direction = direction;
    }

    /**
     * Returns the dimension of the split.
     * 
     * @return the dimension of the split.
     */
    public byte getDimension() {
        return this.dimension;
    }

    /**
     * Returns the direction of a split.
     * 
     * @return the direction of a split.
     */
    public byte getDirection() {
        return this.direction;
    }

}
