package org.objectweb.proactive.extensions.p2p.structured.overlay.can;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

/**
 * Data structure used in order to store the neighbors of a {@link Peer} of type
 * CAN where neighbors are in a specified dimension and direction.
 * 
 * @author lpellegr
 */
public class NeighborTable implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Any direction : inferior or superior.
     */
    public static final short ANY_DIRECTION = -1;

    /**
     * Inferior direction in comparison to a specified peer on a given
     * dimension.
     */
    public static final short INFERIOR_DIRECTION = 0;

    /**
     * Superior direction in comparison to a specified peer on a given
     * dimension.
     */
    public static final short SUPERIOR_DIRECTION = 1;

	/**
	 * Contains neighbors categorized by dimension, direction. The neighbors are
	 * in a two-dimensional array of {@link ConcurrentHashMap}. Each line
	 * corresponds to a dimension and the number of columns is always equal to
	 * two (which corresponds to the upper and lower directions).
	 */
    @SuppressWarnings("unchecked")
    private ConcurrentMap<UUID, NeighborEntry>[][] entries = 
    	new ConcurrentHashMap[P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue()][2];

    /**
     * Constructs a new NeighborTable.
     */
    public NeighborTable() {
        for (int i = 0; i < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); i++) {
            this.entries[i][NeighborTable.INFERIOR_DIRECTION] = 
            	new ConcurrentHashMap<UUID, NeighborEntry>();
            this.entries[i][NeighborTable.SUPERIOR_DIRECTION] = 
            	new ConcurrentHashMap<UUID, NeighborEntry>();
        }
    }

    /**
     * Adds a new neighbor at the specified <code>dimension</code> and 
     * <code>direction</code>.
     * 
     * @param entry
     *            the {@link NeighborEntry} to add.
     * @param dimension
     *            the dimension index (must be in <code>0</code> and
     *            {@link P2PStructuredProperties#CAN_NB_DIMENSIONS - 1} include).
     * @param direction
     *            the direction ({@link #INFERIOR_DIRECTION} or
     *            {@link #SUPERIOR_DIRECTION}).
     */
    public void add(NeighborEntry entry, int dimension, int direction) {
        this.entries[dimension][direction].put(entry.getId(), entry);
    }

    /**
     * Adds all the neighbors from the specified {@link NeighborTable}.
     * 
     * @param table
     *            the neighbors to add.
     */
    public void addAll(NeighborTable table) {
        for (int dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
            for (int direction = 0; direction < 2; direction++) {
                this.entries[dim][direction].putAll(table.get(dim, direction));
            }
        }
    }

    /**
     * Returns all the neighbors on the specified <code>dimension</code> and
     * <code>direction</code> .
     * 
     * @param dimension
     *            the dimension to use (dimension start to <code>0</code> and
     *            max is defined by {@link P2PStructuredProperties#CAN_NB_DIMENSIONS} -
     *            1).
     * @param direction
     *            the direction ({@link #INFERIOR_DIRECTION} or
     *            {@link #SUPERIOR_DIRECTION}).
     * @return all the neighbors on the specified dimension and direction.
     */
    public ConcurrentMap<UUID, NeighborEntry> get(int dimension, int direction) {
        return this.entries[dimension][direction];
    }

    public ConcurrentMap<UUID, NeighborEntry>[] get(int dimension) {
    	return this.entries[dimension];
    }
    
    /**
     * Returns an entry from the specified {@link Peer} identifier if it is
     * found.
     * 
     * @param peerIdentifier
     *            the identifier used for the lookup.
     *            
     * @return the zone found or <code>null</code>.
     */
    public NeighborEntry getNeighborEntry(UUID peerIdentifier) {
        for (int dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
            for (int direction = 0; direction < 2; direction++) {
                if (this.entries[dim][direction].containsKey(peerIdentifier)) {
                    return this.entries[dim][direction].get(peerIdentifier);
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
    public boolean contains(UUID peerIdentifier) {
        for (int dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
            for (int direction = 0; direction < 2; direction++) {
                if (this.entries[dim][direction].containsKey(peerIdentifier)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Indicates if the data structure contains the specified {@link Peer} as
     * neighbor at the specified <code>dimension</code> and
     * <code>direction</code>.
     * 
     * @param peerIdentifier
     *            the identifier to use for checking.
     * @param dimension
     *            the dimension.
     * @param direction
     *            the direction.
     * 
     * @return <code>true</code> if the data structure contains the peer as
     *         neighbor, <code>false</code> otherwise.
     */
    public boolean contains(UUID peerIdentifier, int dimension, int direction) {
        return this.entries[dimension][direction].containsKey(peerIdentifier);
    }

    /**
     * Removes the {@link NeighborEntry} identified by the specified peer
     * identifier.
     * 
     * @param peerIdentifier
     *            the identifier to use for checking.
     *            
     * @return <code>true</code> if the neighbor has been removed,
     *         <code>false</code> otherwise.
     */
    public boolean remove(UUID peerIdentifier) {
        for (int dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
            for (int direction = 0; direction < 2; direction++) {
				if (this.entries[dim][direction].remove(peerIdentifier) != null) {
					return true;
				}
            }
        }

        return false;
    }

    public boolean remove(UUID peerIdentifier, int dimension, int direction) {
    	return this.entries[dimension][direction].remove(peerIdentifier) != null;
    }

    /**
     * Clears the data structure.
     */
    public void removeAll() {
        for (int dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
            for (int direction = 0; direction < 2; direction++) {
                this.removeAll(dim, direction);
            }
        }
    }

    /**
     * Removes all neighbors on a specified dimension and direction.
     * 
     * @param dimension
     *            the dimension index (must be in <code>0</code> and
     *            {@link P2PStructuredProperties#CAN_NB_DIMENSIONS - 1} include).
     * @param direction
     *            the direction ({@link #INFERIOR_DIRECTION} or
     *            {@link #SUPERIOR_DIRECTION}).
     */
    public void removeAll(int dimension, int direction) {
        this.entries[dimension][direction].clear();
    }

    /**
     * Returns the number of neighbors the data structure manages.
     * 
     * @return the number of neighbors the data structure manages.
     */
    public int size() {
        int nbEntries = 0;
        for (int dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
            for (int direction = 0; direction < 2; direction++) {
                nbEntries += this.entries[dim][direction].size();
            }
        }
        return nbEntries;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        StringBuffer buf = new StringBuffer();

        for (int dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
            for (int direction = 0; direction < 2; direction++) {
                buf.append("[");
                int i = 0;
                for (NeighborEntry entry : this.entries[dim][direction].values()) {
                    buf.append(entry.getZone());
                    if (i < this.entries[dim][direction].values().size() - 1) {
                        buf.append(",");
                    }
                    i++;
                }
                buf.append("]");

                if (direction == 0) {
                    buf.append("<--(dim ");
                    buf.append(dim);
                    buf.append(")-->");
                } else {
                    buf.append("\n");
                }
            }
        }
        return buf.toString();
    }

}
