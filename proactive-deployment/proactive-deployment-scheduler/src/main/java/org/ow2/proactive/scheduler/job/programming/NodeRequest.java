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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;
import org.ow2.proactive.scheduler.job.programming.NodeProviderTask.NodeProviderTaskHolder;

/**
 * Node request for {@link NodeProviderRegistry}.
 * 
 * @author The ProActive Team
 */
public class NodeRequest {
    private static final Logger logger =
            ProActiveLogger.getLogger(SchedulerLoggers.SCHEDULE);

    private UniqueID nodeRequestID;

    private int nbNodes;

    private List<NodeProviderTaskHolder> tasks;

    /**
     * Constructs a new {@link NodeRequest}.
     * 
     * @param nodeRequestID
     *            ID of the node request.
     * @param nbNodes
     *            Number of {@link Node nodes} of the node request.
     */
    public NodeRequest(UniqueID nodeRequestID, int nbNodes) {
        this.nodeRequestID = nodeRequestID;
        this.nbNodes = nbNodes;
        this.tasks =
                new ArrayList<NodeProviderTask.NodeProviderTaskHolder>(nbNodes);
    }

    public void addNodeProviderTask(NodeProviderTaskHolder task) {
        if (this.nbNodes > this.tasks.size()) {
            this.tasks.add(task);

            if (logger.isDebugEnabled()) {
                logger.debug("Received registration of node provider task for node request #"
                        + this.nodeRequestID);
            }
        } else {
            task.release();

            if (logger.isDebugEnabled()) {
                logger.debug("Received registration of node provider task for node request #"
                        + this.nodeRequestID
                        + " but required number of nodes has already been reached");
            }
        }
    }

    /**
     * Indicates whether the deployment is finished or not.
     * 
     * @return True if the deployment is finished, false otherwise.
     */
    public boolean isDeploymentFinished() {
        return this.tasks.size() == this.nbNodes;
    }

    /**
     * Returns the registered {@link Node nodes}.
     * 
     * @return The registered {@link Node nodes}.
     */
    public List<Node> getNodes() {
        if (!this.isDeploymentFinished()) {
            logger.warn("Deployment of node request #" + this.nodeRequestID
                    + " is not finished, only " + this.tasks.size() + " of "
                    + this.nbNodes + " tasks are registered");
        }

        List<Node> nodes = new ArrayList<Node>();

        for (NodeProviderTaskHolder task : this.tasks) {
            nodes.add(task.getNode());
        }

        return nodes;
    }

    /**
     * Releases the {@link Node nodes}.
     */
    public void releaseNodes() {
        for (NodeProviderTaskHolder task : this.tasks) {
            task.release();
        }

        if (logger.isDebugEnabled()) {
            logger.debug(this.tasks.size()
                    + " node provider tasks for node request #"
                    + this.nodeRequestID + " have been released");
        }

        this.tasks.clear();
    }
}
