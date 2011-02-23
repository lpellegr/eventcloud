package fr.inria.eventcloud.deployment;

import java.util.List;

import org.objectweb.proactive.core.node.Node;

/**
 * Stores {@link Node}s acquired for a {@link NodeProviderKey}.
 * 
 * @author lpellegr
 */
public class NodeProviderEntry {

    private int index;
    
    private List<Node> nodes;
    
    public NodeProviderEntry(List<Node> nodes) {
        this.nodes = nodes;
    }
    
    public Node getNextNode() {
        if (this.index > this.nodes.size() - 1) {
            this.index = 0;
        }

        return this.nodes.get(this.index++);
    }

    public List<Node> getNodes() {
        return this.nodes;
    }
    
}
