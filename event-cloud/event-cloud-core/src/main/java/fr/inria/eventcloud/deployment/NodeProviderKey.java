package fr.inria.eventcloud.deployment;

import org.objectweb.proactive.core.descriptor.data.VirtualNode;

/**
 * Enumeration used to retrieve nodes acquired by key.
 * 
 * @author lpellegr
 */
public enum NodeProviderKey {

    NODE_PROVIDER("nodeProviderVN"),
    TRACKERS("trackersVN"),
    PEERS_CAN("canPeersVN"),
    PEERS_CHORD("chordPeersVN");

    private String vnName;
    
    private int maxNodesToAcquire;
    
    /**
     * Constructs a new node provider key with the specified
     * virtual node name.
     * 
     * @param vnName the virtual node name;
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
     *              the new value to set.
     */
    public void setMaximumNodesToAcquire(int value) {
        this.maxNodesToAcquire = value;
    }
    
}
