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
package org.objectweb.proactive.extensions.deployment.scheduler;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.objectweb.proactive.extensions.deployment.scheduler.NodeProviderTask.NodeProviderTaskHolder;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;

public class NodeRequest {
    private static final Logger logger =
            ProActiveLogger.getLogger(SchedulerLoggers.SCHEDULE);

    private UniqueID nodeRequestID;

    private int nbNodes;

    private List<NodeProviderTaskHolder> tasks;

    public NodeRequest(UniqueID nodeRequestId, int nbNodes) {
        this.nodeRequestID = nodeRequestId;
        this.nbNodes = nbNodes;
        this.tasks = new ArrayList<NodeProviderTask.NodeProviderTaskHolder>();
    }

    public void addNodeProviderTask(NodeProviderTaskHolder task) {
        if (this.nbNodes > this.tasks.size()) {
            this.tasks.add(task);

            if (logger.isDebugEnabled()) {
                logger.debug("Received registration of node provider task for node request #"
                        + this.nodeRequestID);
            }
        } else {
            task.terminate();

            if (logger.isDebugEnabled()) {
                logger.debug("Received registration of node provider task for node request #"
                        + this.nodeRequestID
                        + " but required number of nodes has already been reached");
            }
        }
    }

    public boolean isDeploymentFinished() {
        return this.tasks.size() == this.nbNodes;
    }

    public List<Node> getNodes() {
        List<Node> nodes = new ArrayList<Node>();

        for (NodeProviderTaskHolder task : this.tasks) {
            nodes.add(task.getNode());
        }

        return nodes;
    }

    public void releaseNodes() {
        for (NodeProviderTaskHolder task : this.tasks) {
            task.terminate();
        }

        if (logger.isDebugEnabled()) {
            logger.debug(this.tasks.size()
                    + " node provider tasks for node request #"
                    + this.nodeRequestID + " have been released");
        }

        this.tasks.clear();
    }
}
