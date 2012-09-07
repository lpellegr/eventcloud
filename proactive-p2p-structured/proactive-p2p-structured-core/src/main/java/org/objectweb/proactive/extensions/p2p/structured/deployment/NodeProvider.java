/**
 * Copyright (c) 2011-2012 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package org.objectweb.proactive.extensions.p2p.structured.deployment;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

/**
 * A NodeProvider is used to provide ProActive nodes to deploy on several
 * machines a ProActive peer-to-peer network.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public interface NodeProvider {

    /**
     * Initiates the deployment of the ProActive nodes.
     */
    public void init();

    /**
     * Terminates the deployment of the ProActive nodes, ie. releases them.
     */
    public void terminate();

    /**
     * Returns a ProActive node.
     * 
     * @return a ProActive node.
     */
    public Node getANode();

    /**
     * Returns the GCMVirtualNode with the specified name.
     * 
     * @param vnName
     *            the name of the GCMVirtualNode.
     * @return the GCMVirtualNode with the specified name.
     */
    public GCMVirtualNode getGcmVirtualNode(String vnName);

}
