/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2012 INRIA/University of
 *                 Nice-Sophia Antipolis/ActiveEon
 * Contact: proactive@ow2.org or contact@activeeon.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; version 3 of
 * the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * If needed, contact us to obtain a release under GPL Version 2 or 3
 * or a different license than the AGPL.
 *
 *  Initial developer(s):               The ProActive Team
 *                        http://proactive.inria.fr/team_members.htm
 *  Contributor(s):
 *
 * ################################################################
 * $$PROACTIVE_INITIAL_DEV$$
 */
package org.ow2.proactive.scheduler.job.programming;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.job.programming.NodeProviderTask.NodeProviderTaskHolder;

/**
 * Registry for {@link SchedulerNodeProvider}.
 * 
 * @author The ProActive Team
 */
public class NodeProviderRegistry {
    private static final Logger LOG =
            ProActiveLogger.getLogger(NodeProviderRegistry.class);

    private final Map<UniqueID, NodeRequest> nodeRequests;

    /**
     * Constructs a new {@link NodeProviderRegistry}.
     */
    public NodeProviderRegistry() {
        this.nodeRequests = new HashMap<UniqueID, NodeRequest>();
    }

    /**
     * Returns the URL of the registry.
     * 
     * @return The URL of the registry.
     */
    public String getURL() {
        return PAActiveObject.getUrl(PAActiveObject.getStubOnThis());
    }

    /**
     * Adds a node request to the list of node requests managed by the registry.
     * 
     * @param nodeRequestID
     *            ID of the node request.
     * @param nbNodes
     *            Number of {@link Node nodes} of the node request.
     */
    public void addNodeRequest(UniqueID nodeRequestID, int nbNodes) {
        if (!this.nodeRequests.containsKey(nodeRequestID)) {
            this.nodeRequests.put(nodeRequestID, new NodeRequest(
                    nodeRequestID, nbNodes));

            if (LOG.isDebugEnabled()) {
                LOG.debug("Node request #" + nodeRequestID + " for "
                        + nbNodes + " nodes added");
            }
        }
    }

    /**
     * Registers a {@link NodeProviderTask}.
     * 
     * @param task
     *            {@link NodeProviderTask} to register.
     */
    public void registerNodeProviderTask(NodeProviderTaskHolder task) {
        UniqueID nodeRequestID = task.getNodeRequestID();

        if (this.nodeRequests.containsKey(nodeRequestID)) {
            this.nodeRequests.get(nodeRequestID).addNodeProviderTask(task);
        } else {
            task.release();

            LOG.error("Node provider task tried to register to node request #"
                    + nodeRequestID + " but there is no such node request ID");
        }
    }

    /**
     * Indicates if the deployment of the node request specified by the given ID
     * is finished or not (ie. all {@link NodeProviderTask} have registered or
     * not).
     * 
     * @param nodeRequestID
     *            ID of the node request.
     * @return True if the deployment for the node request specified by the
     *         given ID is finished, false otherwise.
     */
    public boolean isDeploymentFinished(UniqueID nodeRequestID) {
        if (this.nodeRequests.containsKey(nodeRequestID)) {
            return this.nodeRequests.get(nodeRequestID).isDeploymentFinished();
        } else {
            LOG.error("No such node request #" + nodeRequestID);

            return false;
        }
    }

    /**
     * Returns the {@link Node nodes} of the node request specified by the given
     * ID.
     * 
     * @param nodeRequestID
     *            ID of the node request.
     * @return The {@link Node nodes} of the node request specified by the given
     *         ID.
     */
    public List<Node> getNodes(UniqueID nodeRequestID) {
        if (this.nodeRequests.containsKey(nodeRequestID)) {
            return this.nodeRequests.get(nodeRequestID).getNodes();
        } else {
            LOG.error("No such node request #" + nodeRequestID);

            return null;
        }
    }

    /**
     * Releases the {@link Node nodes} of the node request specified by the
     * given ID.
     * 
     * @param nodeRequestID
     *            ID of the node request.
     */
    public void releaseNodes(UniqueID nodeRequestID) {
        if (this.nodeRequests.containsKey(nodeRequestID)) {
            this.nodeRequests.remove(nodeRequestID).releaseNodes();
        } else {
            LOG.error("No such node request #" + nodeRequestID);
        }
    }
}
