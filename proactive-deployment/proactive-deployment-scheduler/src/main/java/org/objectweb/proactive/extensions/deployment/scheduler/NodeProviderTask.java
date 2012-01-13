/*
 * ################################################################
 *
 * ProActive Parallel Suite(TM): The Java(TM) library for
 *    Parallel, Distributed, Multi-Core Computing for
 *    Enterprise Grids & Clouds
 *
 * Copyright (C) 1997-2011 INRIA/University of
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

import java.io.IOException;
import java.io.Serializable;

import org.apache.log4j.Logger;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.log.ProActiveLogger;
import org.ow2.proactive.scheduler.common.task.TaskResult;
import org.ow2.proactive.scheduler.common.task.executable.JavaExecutable;
import org.ow2.proactive.scheduler.common.util.SchedulerLoggers;

public class NodeProviderTask extends JavaExecutable {
    private static final Logger logger =
            ProActiveLogger.getLogger(SchedulerLoggers.SCHEDULE);

    private String registryURL;

    private UniqueID nodeRequestID;

    public Serializable execute(TaskResult... arg0) throws Throwable {
        if (logger.isDebugEnabled()) {
            logger.debug("Node provider task for node request #"
                    + nodeRequestID + " has started");
        }

        NodeProviderTaskHolder taskHolder =
                PAActiveObject.newActive(
                        NodeProviderTaskHolder.class, new Object[] {
                                this.registryURL, this.nodeRequestID});
        taskHolder.notifyRegistry();

        while (!taskHolder.hasTerminated()) {
            Thread.sleep(1000);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Node provider task for node request #"
                    + nodeRequestID + " has terminated");
        }

        return null;
    }

    public static class NodeProviderTaskHolder {
        private final String registryURL;

        private final UniqueID nodeRequestID;

        private boolean terminated;

        public NodeProviderTaskHolder() {
            this.registryURL = null;
            this.nodeRequestID = null;
            this.terminated = false;
        }

        public NodeProviderTaskHolder(String registryURL,
                UniqueID nodesRequestID) {
            this.registryURL = registryURL;
            this.nodeRequestID = nodesRequestID;
            this.terminated = false;
        }

        public UniqueID getNodeRequestID() {
            return this.nodeRequestID;
        }

        public boolean hasTerminated() {
            return this.terminated;
        }

        public void terminate() {
            this.terminated = true;
        }

        public void notifyRegistry() throws ActiveObjectCreationException,
                IOException {
            NodeProviderRegistry registry =
                    PAActiveObject.lookupActive(
                            NodeProviderRegistry.class, this.registryURL);
            registry.addNodeProviderTask((NodeProviderTaskHolder) PAActiveObject.getStubOnThis());

            if (logger.isDebugEnabled()) {
                logger.debug("Node provider task for node request #"
                        + nodeRequestID + " has registered to registry");
            }
        }

        public Node getNode() {
            try {
                return PAActiveObject.getNode();
            } catch (NodeException ne) {
                logger.error("Unable to get Node for node request #"
                        + nodeRequestID + ": " + ne.getMessage(), ne);

                return null;
            }
        }
    }
}
