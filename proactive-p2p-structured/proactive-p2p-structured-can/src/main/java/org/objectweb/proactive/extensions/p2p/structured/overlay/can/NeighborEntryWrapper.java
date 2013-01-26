package org.objectweb.proactive.extensions.p2p.structured.overlay.can;

import java.io.Serializable;
import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;

/**
 * An entry in an {@link NeighborTableWrapper}.
 * 
 * @author acraciun
 */
public class NeighborEntryWrapper<E extends Element> implements Serializable {

    private static final long serialVersionUID = 130L;
    private NeighborEntry<E> neighborEntry;
    // The directions on which the peer needs to forward the broadcast request.
    private byte[][] directions;
    // The coordinates of the plan(if not null) that needs to be contained by
    // the neighbors of the current peer, in order to receive the broadcast
    // request
    private Element[] splitPlans;
    // The dimension relative to the sender.
    private byte dimension;
    // The direction relative to the sender.
    private byte side;

    public NeighborEntryWrapper(NeighborEntry<E> neighborEntry, byte dimension,
            byte side) {
        this.neighborEntry = neighborEntry;
        this.dimension = dimension;
        this.side = side;
        this.directions = this.initializeByteArray();
        this.splitPlans =
                new Element[P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue()];
    }

    private byte[][] initializeByteArray() {
        byte[][] array =
                new byte[2][P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue()];

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); j++) {
                array[i][j] = -1;
            }
        }

        return array;
    }

    public byte[][] getDirections() {
        return this.directions;
    }

    public byte getDirection(int i, int j) {
        return this.directions[i][j];
    }

    public void setDirections(byte[][] direction) {
        this.directions = direction;
    }

    public void setDirection(int i, int dimension, byte direction) {
        this.directions[i][dimension] = direction;
    }

    public Element[] getSplitPlans() {
        return this.splitPlans;
    }

    public Element getSplitPlan(int index) {
        return this.splitPlans[index];
    }

    public void setSplitPlans(Element[] splitPlans) {
        this.splitPlans = splitPlans;
    }

    public void setSplitPlan(int dimension, Element plan) {
        this.splitPlans[dimension] = plan;
    }

    public NeighborEntry<E> getNeighborEntry() {
        return this.neighborEntry;
    }

    public void setNeighborEntry(NeighborEntry<E> neighborEntry) {
        this.neighborEntry = neighborEntry;
    }

    public UUID getId() {
        return this.neighborEntry.getId();
    }

    public byte getDimension() {
        return this.dimension;
    }

    public void setDimension(byte dimension) {
        this.dimension = dimension;
    }

    public byte getSide() {
        return this.side;
    }

    public void setSide(byte side) {
        this.side = side;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append(this.neighborEntry);
        return buf.toString();
    }
}
