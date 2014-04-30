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
package org.objectweb.proactive.extensions.p2p.structured.overlay.can;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayId;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;

/**
 * Data structure used to store the neighbors of a peer grouped by dimension and
 * direction that contains more information that the ordinary NeighborTable, in
 * order to facilitate the computation of the neighbors that need to receive the
 * broadcast request.
 * 
 * @author acraciun
 */
public class NeighborTableWrapper<E extends Coordinate> implements Serializable {

    private static final long serialVersionUID = 160L;

    @SuppressWarnings("unchecked")
    private List<NeighborEntryWrapper<E>>[][] entries =
            new ArrayList[P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue()][2];

    public NeighborTableWrapper() {
        for (int i = 0; i < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); i++) {
            this.entries[i][NeighborTable.DIRECTION_INFERIOR] =
                    new ArrayList<NeighborEntryWrapper<E>>();
            this.entries[i][NeighborTable.DIRECTION_SUPERIOR] =
                    new ArrayList<NeighborEntryWrapper<E>>();
        }
    }

    public NeighborTableWrapper(NeighborTableWrapper<E> tableToCopy) {
        this.entries = tableToCopy.entries;
    }

    public void add(byte dimension, byte direction,
                    NeighborEntryWrapper<E> peerToAdd) {
        this.entries[dimension][direction].add(peerToAdd);
    }

    public void addAll(byte dimension, byte direction,
                       Map<OverlayId, NeighborEntry<E>> peers) {
        for (NeighborEntry<E> neighEntry : peers.values()) {
            this.add(dimension, direction, new NeighborEntryWrapper<E>(
                    neighEntry, dimension, direction));
        }
    }

    public void addAll(byte dimension, byte direction,
                       List<NeighborEntryWrapper<E>> peers) {
        for (NeighborEntryWrapper<E> neighEntry : peers) {
            this.add(dimension, direction, neighEntry);
        }
    }

    public List<NeighborEntryWrapper<E>> get(byte dimension, byte direction) {
        return this.entries[dimension][direction];
    }

    public NeighborEntryWrapper<E> get(byte dimension, byte direction, int index) {
        return this.entries[dimension][direction].get(index);
    }

    public NeighborEntryWrapper<E> getPeerInformation(OverlayId peerIdentifier) {
        for (byte dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
            for (byte direction = 0; direction < 2; direction++) {
                for (int position = 0; position < this.entries[dim][direction].size(); position++) {
                    if (this.entries[dim][direction].get(position).getId() == peerIdentifier) {
                        return this.entries[dim][direction].get(position);
                    }
                }
            }
        }
        return null;
    }

    public boolean remove(OverlayId peerIdentifier) {
        for (byte dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
            for (byte direction = 0; direction < 2; direction++) {
                NeighborEntryWrapper<E> peer =
                        this.getPeerInformation(peerIdentifier);
                if (this.entries[dim][direction].remove(peer) == true) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean remove(OverlayId peerIdentifier, byte dimension,
                          byte direction) {
        return this.entries[dimension][direction].remove(peerIdentifier) == true;
    }

    public void removeAll() {
        for (byte dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
            for (byte direction = 0; direction < 2; direction++) {
                this.removeAll(dim, direction);
            }
        }
    }

    public void removeAll(byte dimension, byte direction) {
        this.entries[dimension][direction].clear();
    }

    /**
     * Computes the total number of peers(on all directions/dimensions).
     * 
     * @return the total number of peers.
     */
    public int size() {
        int size = 0;
        for (int i = 0; i < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); i++) {
            for (int j = 0; j < 2; j++) {
                size += this.entries[i][j].size();
            }
        }
        return size;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();

        for (byte dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
            for (byte direction = 0; direction < 2; direction++) {
                buf.append("\nDimension: ")
                        .append(dim)
                        .append(" Direction ")
                        .append(direction)
                        .append(": ");
                int i = 0;
                for (NeighborEntryWrapper<E> entry : this.entries[dim][direction]) {
                    buf.append(entry.getNeighborEntry().getZone());
                    if (i < this.entries[dim][direction].size() - 1) {
                        buf.append(",");
                    }
                    i++;
                    buf.append(entry);

                }
            }
        }
        return buf.toString();
    }
}
