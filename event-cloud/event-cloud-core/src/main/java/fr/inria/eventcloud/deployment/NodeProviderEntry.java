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
