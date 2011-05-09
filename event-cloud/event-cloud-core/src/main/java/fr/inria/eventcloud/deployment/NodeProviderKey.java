/**
 * Copyright (c) 2011 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.deployment;

import org.objectweb.proactive.core.descriptor.data.VirtualNode;

/**
 * Enumeration used to retrieve nodes acquired by key.
 * 
 * @author lpellegr
 */
public enum NodeProviderKey {

    NODE_PROVIDER("nodeProviderVN"), TRACKERS("trackersVN"), PEERS_CAN(
            "canPeersVN"), PEERS_CHORD("chordPeersVN");

    private String vnName;

    private int maxNodesToAcquire;

    /**
     * Constructs a new node provider key with the specified virtual node name.
     * 
     * @param vnName
     *            the virtual node name;
     */
    private NodeProviderKey(String vnName) {
        this.vnName = vnName;
    }

    /**
     * Returns the {@link VirtualNode} name associated to the key.
     * 
     * @return the {@link VirtualNode} name associated to the key.
     */
    public String getVirtualNodeName() {
        return this.vnName;
    }

    /**
     * Returns the maximum number of nodes to acquire.
     * 
     * @return the maximum number of nodes to acquire.
     */
    public int getMaximumNodesToAcquire() {
        return this.maxNodesToAcquire;
    }

    /**
     * Sets the maximum number of nodes to acquire.
     * 
     * @param value
     *            the new value to set.
     */
    public void setMaximumNodesToAcquire(int value) {
        this.maxNodesToAcquire = value;
    }

}
