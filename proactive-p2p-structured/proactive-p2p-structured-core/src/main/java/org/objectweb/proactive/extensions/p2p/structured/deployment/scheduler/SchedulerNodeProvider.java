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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package org.objectweb.proactive.extensions.p2p.structured.deployment.scheduler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.p2p.structured.deployment.NodeProvider;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SchedulerNodeProvider is a concrete implementation of {@link NodeProvider}
 * for the ProActive Scheduler.
 * 
 * @author bsauvan
 * @author lpellegr
 */
public class SchedulerNodeProvider implements NodeProvider, Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger log =
            LoggerFactory.getLogger(SchedulerNodeProvider.class);

    private String schedulerUrl;

    private String credentialsPath;

    private String libsPath;

    private List<String> jvmArguments;

    private transient List<GcmVirtualNodeEntry> virtualNodeEntries;

    private transient org.objectweb.proactive.extensions.deployment.scheduler.SchedulerNodeProvider schedulerNodeProvider;

    private Map<String, GCMVirtualNode> virtualNodes;

    public SchedulerNodeProvider(String schedulerUrl, String credentialsPath,
            String libsPath, List<String> jvmArguments,
            List<GcmVirtualNodeEntry> entries) {
        this.schedulerUrl = schedulerUrl;
        this.credentialsPath = credentialsPath;
        this.libsPath = libsPath;
        this.jvmArguments = jvmArguments;
        this.virtualNodeEntries = entries;
        this.virtualNodes = new HashMap<String, GCMVirtualNode>();

        this.init();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {
        try {
            this.schedulerNodeProvider =
                    new org.objectweb.proactive.extensions.deployment.scheduler.SchedulerNodeProvider();

            for (GcmVirtualNodeEntry virtualNodeEntry : this.virtualNodeEntries) {
                for (NodeSourceEntry nodeSourceEntry : virtualNodeEntry.getNodeSourceEntries()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Acquires " + nodeSourceEntry.getNbNodes()
                                + " nodes on source node "
                                + nodeSourceEntry.getNodeSourceName());
                    }

                    UniqueID nodeRequestId =
                            schedulerNodeProvider.deployNodes(
                                    this.schedulerUrl, this.credentialsPath,
                                    nodeSourceEntry.getNbNodes(),
                                    this.libsPath, this.jvmArguments,
                                    nodeSourceEntry.getNodeSourceName());
                    nodeSourceEntry.nodeRequestId = nodeRequestId;
                }
            }
        } catch (Exception e) {
            log.error("Failed to init: " + e.getMessage(), e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void terminate() {
        if (log.isDebugEnabled()) {
            log.debug("Releases all nodes");
        }

        if (this.schedulerNodeProvider != null) {
            this.schedulerNodeProvider.releaseAllNodes();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Node getANode() {
        // TODO implement
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized GCMVirtualNode getGcmVirtualNode(String virtualNodeName) {
        if (this.schedulerNodeProvider != null) {
            if (!this.virtualNodes.containsKey(virtualNodeName)) {
                List<UniqueID> nodeRequestIds = new ArrayList<UniqueID>();

                for (GcmVirtualNodeEntry virtualNodeEntry : this.virtualNodeEntries) {
                    if (virtualNodeEntry.getVirtualNodeName().equals(
                            virtualNodeName)) {
                        for (NodeSourceEntry nodeSourceEntry : virtualNodeEntry.getNodeSourceEntries()) {
                            nodeRequestIds.add(nodeSourceEntry.nodeRequestId);
                        }
                        break;
                    }
                }

                try {
                    this.virtualNodes.put(
                            virtualNodeName,
                            this.schedulerNodeProvider.getGCMVirtualNode(
                                    virtualNodeName, nodeRequestIds));
                } catch (Exception e) {
                    log.error("Failed to get GCMVirtualNode " + virtualNodeName
                            + ": " + e.getMessage(), e);
                }
            }

            return this.virtualNodes.get(virtualNodeName);
        } else {
            return null;
        }
    }

}
