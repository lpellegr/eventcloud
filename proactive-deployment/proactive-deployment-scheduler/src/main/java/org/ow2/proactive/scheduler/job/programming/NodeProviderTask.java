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

/**
 * Task for {@link NodeProviderJob}.
 * 
 * @author The ProActive Team
 */
public class NodeProviderTask extends JavaExecutable {
    private static final Logger logger =
            ProActiveLogger.getLogger(SchedulerLoggers.SCHEDULE);

    private String registryURL;

    private UniqueID nodeRequestID;

    /**
     * {@inheritDoc}
     */
    @Override
    public Serializable execute(TaskResult... arg0) throws Throwable {
        if (logger.isDebugEnabled()) {
            logger.debug("Node provider task for node request #"
                    + this.nodeRequestID + " has started");
        }

        NodeProviderTaskHolder taskHolder =
                PAActiveObject.newActive(
                        NodeProviderTaskHolder.class, new Object[] {
                                this.registryURL, this.nodeRequestID},
                        PAActiveObject.getNode());
        taskHolder.registerToRegistry();

        while (!taskHolder.isReleased()) {
            Thread.sleep(1000);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Node provider task for node request #"
                    + this.nodeRequestID + " has terminated");
        }

        return null;
    }

    /**
     * Task holder for {@link NodeProviderTask}.
     * 
     * @author The ProActive Team
     */
    public static class NodeProviderTaskHolder {
        private final String registryURL;

        private final UniqueID nodeRequestID;

        private boolean isReleased;

        /**
         * Constructs a new {@link NodeProviderTaskHolder}.
         */
        public NodeProviderTaskHolder() {
            this.registryURL = null;
            this.nodeRequestID = null;
            this.isReleased = false;
        }

        /**
         * Constructs a new {@link NodeProviderTaskHolder}.
         * 
         * @param registryURL
         *            URL of the {@link NodeProviderRegistry registry} on which
         *            to register.
         * @param nodeRequestID
         *            ID of the node request.
         */
        public NodeProviderTaskHolder(String registryURL, UniqueID nodeRequestID) {
            this.registryURL = registryURL;
            this.nodeRequestID = nodeRequestID;
            this.isReleased = false;
        }

        /**
         * Returns the ID of the node request.
         * 
         * @return The ID of the node request.
         */
        public UniqueID getNodeRequestID() {
            return this.nodeRequestID;
        }

        /**
         * Indicates if the task has been released or not.
         * 
         * @return True if the task has been released, false otherwise.
         */
        public boolean isReleased() {
            return this.isReleased;
        }

        /**
         * Releases the task.
         */
        public void release() {
            this.isReleased = true;
        }

        /**
         * Registers the task to the {@link NodeProviderRegistry registry}.
         * 
         * @throws ActiveObjectCreationException
         *             If a problem occurs when retrieving a reference of the
         *             {@link NodeProviderRegistry registry}.
         * @throws IOException
         *             If a problem occurs when retrieving a reference of the
         *             {@link NodeProviderRegistry registry}.
         */
        public void registerToRegistry() throws ActiveObjectCreationException,
                IOException {
            NodeProviderRegistry registry =
                    PAActiveObject.lookupActive(
                            NodeProviderRegistry.class, this.registryURL);
            registry.registerNodeProviderTask((NodeProviderTaskHolder) PAActiveObject.getStubOnThis());

            if (logger.isDebugEnabled()) {
                logger.debug("Node provider task for node request #"
                        + this.nodeRequestID + " has registered to registry");
            }
        }

        /**
         * Returns the {@link Node} on which the task is running.
         * 
         * @return The {@link Node} on which the task is running.
         */
        public Node getNode() {
            try {
                return PAActiveObject.getNode();
            } catch (NodeException ne) {
                logger.error("Unable to get Node for node request #"
                        + this.nodeRequestID + ": " + ne.getMessage(), ne);

                return null;
            }
        }
    }
}
