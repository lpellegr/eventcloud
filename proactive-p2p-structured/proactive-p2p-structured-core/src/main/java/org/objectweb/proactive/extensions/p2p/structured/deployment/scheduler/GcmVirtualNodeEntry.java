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
package org.objectweb.proactive.extensions.p2p.structured.deployment.scheduler;

import java.io.Serializable;

import org.objectweb.proactive.core.UniqueID;

/**
 * Basic entry to use with the {@link SchedulerNodeProvider}. It defines the
 * following elements:
 * <ul>
 * <li>the name of the GCMVirtualNode to create.</li>
 * <li>the number of ProActive nodes of the GCMVirtualNode to create.</li>
 * <li>the names of the node sources to use to get the ProActive nodes that will
 * compose the GCMVirtualNode to create.</li>
 * </ul>
 * 
 * @author bsauvan
 * @author lpellegr
 */
public class GcmVirtualNodeEntry implements Serializable {

    private static final long serialVersionUID = 160L;

    private String virtualNodeName;

    private String[] nodeSourceNames;

    private int nbNodes;

    protected UniqueID nodeRequestId;

    /**
     * Constructs a {@link GcmVirtualNodeEntry}.
     */
    public GcmVirtualNodeEntry() {
    }

    /**
     * Constructs a {@link GcmVirtualNodeEntry}.
     * 
     * @param vnName
     *            the name of the GCMVirtualNode to create.
     * @param nbNodes
     *            the number of ProActive nodes of the GCMVirtualNode to create.
     * @param nodeSourceNames
     *            the names of the node sources to use to get the ProActive
     *            nodes that will compose the GCMVirtualNode to create.
     */
    public GcmVirtualNodeEntry(String vnName, int nbNodes,
            String... nodeSourceNames) {
        this.virtualNodeName = vnName;
        this.nodeSourceNames = nodeSourceNames;
        this.nbNodes = nbNodes;
    }

    /**
     * Returns the name of the GCMVirtualNode to create.
     * 
     * @return the name of the GCMVirtualNode to create.
     */
    public String getVirtualNodeName() {
        return this.virtualNodeName;
    }

    /**
     * Sets the name of the GCMVirtualNode to create.
     * 
     * @param virtualNodeName
     *            the virtualNodeName to set
     */
    public void setVirtualNodeName(String virtualNodeName) {
        this.virtualNodeName = virtualNodeName;
    }

    /**
     * Returns the number of ProActive nodes of the GCMVirtualNode to create.
     * 
     * @return the number of ProActive nodes of the GCMVirtualNode to create.
     */
    public int getNbNodes() {
        return this.nbNodes;
    }

    /**
     * Sets the number of ProActive nodes of the GCMVirtualNode to create.
     * 
     * @param nbNodes
     *            the number of ProActive nodes of the GCMVirtualNode to create.
     */
    public void setNbNodes(int nbNodes) {
        this.nbNodes = nbNodes;
    }

    /**
     * Returns the names of the node sources to use to get the ProActive nodes
     * that will compose the GCMVirtualNode to create.
     * 
     * @return the names of the node sources to use to get the ProActive nodes
     *         that will compose the GCMVirtualNode to create.
     */
    public String[] getNodeSourceNames() {
        return this.nodeSourceNames;
    }

    /**
     * Sets the names of the node sources to use to get the ProActive nodes that
     * will compose the GCMVirtualNode to create.
     * 
     * @param nodeSourceNames
     *            the names of the node sources to use to get the ProActive
     *            nodes that will compose the GCMVirtualNode to create.
     */
    public void setNodeSourceNames(String[] nodeSourceNames) {
        this.nodeSourceNames = nodeSourceNames;
    }

}
