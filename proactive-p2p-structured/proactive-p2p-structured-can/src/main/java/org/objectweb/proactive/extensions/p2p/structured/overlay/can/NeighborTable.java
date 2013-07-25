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
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayId;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;
import org.objectweb.proactive.extensions.p2p.structured.utils.HomogenousPair;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

/**
 * Data structure used in order to store the neighbors of a {@link Peer} of type
 * CAN where neighbors are in a specified dimension and direction.
 * 
 * @param <E>
 *            the {@link Element}s type manipulated.
 * 
 * @author lpellegr
 */
public class NeighborTable<E extends Element> implements Serializable {

    private static final long serialVersionUID = 151L;

    /**
     * Any direction : inferior or superior.
     */
    public static final byte DIRECTION_ANY = -1;

    /**
     * Inferior direction in comparison to a specified peer on a given
     * dimension.
     */
    public static final byte DIRECTION_INFERIOR = 0;

    /**
     * Superior direction in comparison to a specified peer on a given
     * dimension.
     */
    public static final byte DIRECTION_SUPERIOR = 1;

    /**
     * Contains neighbors categorized by dimension, direction. The neighbors are
     * in a two-dimensional array of {@link ConcurrentHashMap}. Each line
     * corresponds to a dimension and the number of columns is always equal to
     * two (which corresponds to the upper and lower directions).
     */
    @SuppressWarnings("unchecked")
    private ConcurrentMap<OverlayId, NeighborEntry<E>>[][] entries =
            new ConcurrentHashMap[P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue()][2];

    /**
     * Constructs a new NeighborTable.
     */
    public NeighborTable() {
        // for a uniformly partitioned space with n nodes and d dimensions
        // the number of neighbors per node is 2*d
        int nbNeighbors =
                P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue() * 2;

        for (int i = 0; i < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); i++) {
            // we over provision the number of neighbors per dimension but not
            // too much: 2*d per dimension for a total of 2*d^2
            this.entries[i][NeighborTable.DIRECTION_INFERIOR] =
                    new ConcurrentHashMap<OverlayId, NeighborEntry<E>>(
                            nbNeighbors,
                            0.75f,
                            P2PStructuredProperties.MAO_SOFT_LIMIT_PEERS.getValue());
            this.entries[i][NeighborTable.DIRECTION_SUPERIOR] =
                    new ConcurrentHashMap<OverlayId, NeighborEntry<E>>(
                            nbNeighbors,
                            0.75f,
                            P2PStructuredProperties.MAO_SOFT_LIMIT_PEERS.getValue());
        }
    }

    /**
     * Adds a new neighbor at the specified {@code dimension} and
     * {@code direction}.
     * 
     * @param entry
     *            the {@link NeighborEntry} to add.
     * @param dimension
     *            the dimension index (must be in {@code 0} and
     *            {@link P2PStructuredProperties#CAN_NB_DIMENSIONS - 1}
     *            include).
     * @param direction
     *            the direction ({@link #DIRECTION_INFERIOR} or
     *            {@link #DIRECTION_SUPERIOR}).
     */
    public void add(NeighborEntry<E> entry, byte dimension, byte direction) {
        this.entries[dimension][direction].put(entry.getId(), entry);
    }

    /**
     * Adds all the neighbors from the specified {@link NeighborTable}.
     * 
     * @param table
     *            the neighbors to add.
     */
    public void addAll(NeighborTable<E> table) {
        for (byte dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
            for (byte direction = 0; direction < 2; direction++) {
                this.entries[dim][direction].putAll(table.get(dim, direction));
            }
        }
    }

    public List<Peer> getStubs() {
        Builder<Peer> result = ImmutableList.builder();

        for (byte dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
            for (byte direction = 0; direction < 2; direction++) {
                for (NeighborEntry<E> entry : this.entries[dim][direction].values()) {
                    result.add(entry.getStub());
                }
            }
        }

        return result.build();
    }

    /**
     * Returns all the neighbors on the specified {@code dimension} and
     * {@code direction}.
     * 
     * @param dimension
     *            the dimension to use (dimension start to {@code 0} and max is
     *            defined by {@link P2PStructuredProperties#CAN_NB_DIMENSIONS} -
     *            1).
     * @param direction
     *            the direction ({@link #DIRECTION_INFERIOR} or
     *            {@link #DIRECTION_SUPERIOR}).
     * 
     * @return all the neighbors on the specified dimension and direction.
     */
    public ConcurrentMap<OverlayId, NeighborEntry<E>> get(byte dimension,
                                                          byte direction) {
        return this.entries[dimension][direction];
    }

    public ConcurrentMap<OverlayId, NeighborEntry<E>>[] get(byte dimension) {
        return this.entries[dimension];
    }

    public NeighborEntry<E> getMergeableNeighbor(Zone<E> zone) {
        for (byte dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
            for (byte direction = 0; direction < 2; direction++) {
                for (NeighborEntry<E> entry : this.entries[dim][direction].values()) {
                    if (zone.canMerge(entry.getZone(), dim)) {
                        return entry;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Returns an entry from the specified {@link Peer} identifier if it is
     * found.
     * 
     * @param peerIdentifier
     *            the identifier used for the lookup.
     * 
     * @return the {@link NeighborEntry} found or <code>null</code>.
     */
    public NeighborEntry<E> getNeighborEntry(OverlayId peerIdentifier) {
        for (byte dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
            for (byte direction = 0; direction < 2; direction++) {
                NeighborEntry<E> result =
                        this.entries[dim][direction].get(peerIdentifier);

                if (result != null) {
                    return result;
                }
            }
        }

        return null;
    }

    /**
     * Indicates if a {@link NeighborEntry} identified by the specified
     * {@link Peer} identifier is contained by this data structure or not.
     * 
     * @param peerIdentifier
     *            the identifier to use for checking.
     * 
     * @return <code>true</code> if the data structure contains the peer
     *         identifier, <code>false</code> otherwise.
     */
    public boolean contains(OverlayId peerIdentifier) {
        for (byte dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
            for (byte direction = 0; direction < 2; direction++) {
                if (this.entries[dim][direction].containsKey(peerIdentifier)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Indicates if the data structure contains the specified {@link Peer} as
     * neighbor at the specified {@code dimension} and {@code direction}.
     * 
     * @param peerIdentifier
     *            the identifier to use for checking.
     * @param dimension
     *            the dimension.
     * @param direction
     *            the direction.
     * 
     * @return {@code true }if the data structure contains the peer as neighbor,
     *         {@code false} otherwise.
     */
    public boolean contains(OverlayId peerIdentifier, byte dimension,
                            byte direction) {
        return this.entries[dimension][direction].containsKey(peerIdentifier);
    }

    public HomogenousPair<Byte> findDimensionAndDirection(OverlayId peerIdentifier) {
        for (byte dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
            for (byte direction = 0; direction < 2; direction++) {
                if (this.entries[dim][direction].containsKey(peerIdentifier)) {
                    return HomogenousPair.createHomogenous(dim, direction);
                }
            }
        }

        return null;
    }

    public byte findDimension(OverlayId peerIdentifier) {
        for (byte dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
            for (byte direction = 0; direction < 2; direction++) {
                if (this.entries[dim][direction].containsKey(peerIdentifier)) {
                    return dim;
                }
            }
        }

        return -1;
    }

    public byte findDirection(OverlayId peerIdentifier) {
        for (byte dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
            for (byte direction = 0; direction < 2; direction++) {
                if (this.entries[dim][direction].containsKey(peerIdentifier)) {
                    return direction;
                }
            }
        }

        return -1;
    }

    /**
     * Removes the {@link NeighborEntry} identified by the specified peer
     * identifier.
     * 
     * @param peerIdentifier
     *            the identifier to use for the removal operation.
     * 
     * @return the dimension and the direction on which the peer was in the
     *         table or {@code null} if the peer identified with
     *         {@code peerIdentifier} has not been found in the table.
     */
    public HomogenousPair<Byte> remove(OverlayId peerIdentifier) {
        for (byte dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
            for (byte direction = 0; direction < 2; direction++) {
                if (this.entries[dim][direction].remove(peerIdentifier) != null) {
                    return HomogenousPair.createHomogenous(dim, direction);
                }
            }
        }

        return null;
    }

    public boolean remove(OverlayId peerIdentifier, byte dimension,
                          byte direction) {
        return this.entries[dimension][direction].remove(peerIdentifier) != null;
    }

    /**
     * Clears the data structure.
     */
    public void removeAll() {
        for (byte dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
            for (byte direction = 0; direction < 2; direction++) {
                this.removeAll(dim, direction);
            }
        }
    }

    /**
     * Removes all neighbors on a specified dimension and direction.
     * 
     * @param dimension
     *            the dimension index (must be in {@code 0} and
     *            {@link P2PStructuredProperties#CAN_NB_DIMENSIONS - 1}
     *            include).
     * @param direction
     *            the direction ({@link #DIRECTION_INFERIOR} or
     *            {@link #DIRECTION_SUPERIOR}).
     */
    public void removeAll(byte dimension, byte direction) {
        this.entries[dimension][direction].clear();
    }

    /**
     * Returns the number of neighbors the data structure manages.
     * 
     * @return the number of neighbors the data structure manages.
     */
    public int size() {
        int nbEntries = 0;
        for (byte dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
            for (byte direction = 0; direction < 2; direction++) {
                nbEntries += this.entries[dim][direction].size();
            }
        }
        return nbEntries;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();

        for (byte dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
            for (byte direction = 0; direction < 2; direction++) {
                buf.append('[');
                int i = 0;
                for (NeighborEntry<E> entry : this.entries[dim][direction].values()) {
                    buf.append(entry.getZone());
                    if (i < this.entries[dim][direction].values().size() - 1) {
                        buf.append(',');
                    }
                    i++;
                }
                buf.append(']');

                if (direction == 0) {
                    buf.append("<--(dim ");
                    buf.append(dim);
                    buf.append(")-->");
                } else {
                    buf.append('\n');
                }
            }
        }
        return buf.toString();
    }

}
