/**
 * Copyright (c) 2011-2013 INRIA.
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
package org.objectweb.proactive.extensions.p2p.structured.overlay.can;

import java.io.Serializable;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayId;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;

/**
 * An entry in an {@link NeighborTableWrapper}.
 * 
 * @author acraciun
 */
public class NeighborEntryWrapper<E extends Coordinate> implements Serializable {

    private static final long serialVersionUID = 160L;

    private NeighborEntry<E> neighborEntry;
    // The directions on which the peer needs to forward the broadcast request.
    private byte[][] directions;
    // The coordinates of the plan(if not null) that needs to be contained by
    // the neighbors of the current peer, in order to receive the broadcast
    // request
    private Coordinate[] splitPlans;
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
                new Coordinate[P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue()];
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

    public Coordinate[] getSplitPlans() {
        return this.splitPlans;
    }

    public Coordinate getSplitPlan(int index) {
        return this.splitPlans[index];
    }

    public void setSplitPlans(Coordinate[] splitPlans) {
        this.splitPlans = splitPlans;
    }

    public void setSplitPlan(int dimension, Coordinate plan) {
        this.splitPlans[dimension] = plan;
    }

    public NeighborEntry<E> getNeighborEntry() {
        return this.neighborEntry;
    }

    public void setNeighborEntry(NeighborEntry<E> neighborEntry) {
        this.neighborEntry = neighborEntry;
    }

    public OverlayId getId() {
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
