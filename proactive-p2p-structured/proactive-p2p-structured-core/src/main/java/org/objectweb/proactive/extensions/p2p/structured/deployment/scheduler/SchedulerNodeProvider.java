/**
 * Copyright (c) 2011-2013 INRIA.
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.p2p.structured.deployment.NodeProvider;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.ow2.proactive.scheduler.job.programming.NodeProviderException;
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

    private static final long serialVersionUID = 140L;

    private static final Logger log =
            LoggerFactory.getLogger(SchedulerNodeProvider.class);

    private final String schedulerUrl;

    private final String username;

    private final String password;

    private final String credentialsPath;

    private final String dataFolder;

    private final List<String> jvmArguments;

    private final List<GcmVirtualNodeEntry> virtualNodeEntries;

    // TODO remove transient
    private final transient org.ow2.proactive.scheduler.job.programming.SchedulerNodeProvider schedulerNodeProvider;

    private boolean isStarted;

    private final Map<String, GCMVirtualNode> virtualNodes;

    private List<Node> nodes;

    private int nodeIndex;

    /**
     * Constructs a SchedulerNodeProvider.
     * 
     * @param schedulerUrl
     *            URL of the ProActive Scheduler.
     * @param username
     *            the username to connect to the ProActive Scheduler.
     * @param password
     *            the password to connect to the ProActive Scheduler.
     * @param dataFolder
     *            the folder containing the data to transfer to the nodes.
     * @param jvmArguments
     *            the JVM arguments to use for the nodes.
     * @param entries
     *            the {@link GcmVirtualNodeEntry GcmVirtualNodeEntries} defining
     *            the GCMVirtualNodes to deploy.
     */
    public SchedulerNodeProvider(String schedulerUrl, String username,
            String password, String dataFolder, List<String> jvmArguments,
            List<GcmVirtualNodeEntry> entries) {
        this(schedulerUrl, username, password, null, dataFolder, jvmArguments,
                entries);
    }

    /**
     * Constructs a SchedulerNodeProvider.
     * 
     * @param schedulerUrl
     *            URL of the ProActive Scheduler.
     * @param credentialsPath
     *            the path of the credentials to connect to the scheduler.
     * @param dataFolder
     *            the folder containing the data to transfer to the nodes.
     * @param jvmArguments
     *            the JVM arguments to use for the nodes.
     * @param entries
     *            the {@link GcmVirtualNodeEntry GcmVirtualNodeEntries} defining
     *            the GCMVirtualNodes to deploy.
     */
    public SchedulerNodeProvider(String schedulerUrl, String credentialsPath,
            String dataFolder, List<String> jvmArguments,
            List<GcmVirtualNodeEntry> entries) {
        this(schedulerUrl, null, null, credentialsPath, dataFolder,
                jvmArguments, entries);
    }

    private SchedulerNodeProvider(String schedulerUrl, String username,
            String password, String credentialsPath, String dataFolder,
            List<String> jvmArguments, List<GcmVirtualNodeEntry> entries) {
        this.schedulerUrl = schedulerUrl;
        this.username = username;
        this.password = password;
        this.credentialsPath = credentialsPath;
        this.dataFolder = dataFolder;
        this.jvmArguments = jvmArguments;
        this.virtualNodeEntries = entries;
        try {
            this.schedulerNodeProvider =
                    new org.ow2.proactive.scheduler.job.programming.SchedulerNodeProvider();
        } catch (ProActiveException pe) {
            throw new IllegalStateException(
                    "Failed to instantiate SchedulerNodeProvider", pe);
        }
        this.isStarted = false;
        this.virtualNodes = new HashMap<String, GCMVirtualNode>();
        this.nodes = null;
        this.nodeIndex = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        if (!this.isStarted()) {
            log.debug(
                    "Submitting {} jobs to the ProActive Scheduler located at {}",
                    this.virtualNodeEntries.size(), this.schedulerUrl);

            for (GcmVirtualNodeEntry virtualNodeEntry : this.virtualNodeEntries) {
                log.debug(
                        "Submitting a job to acquire {} nodes for the GCMVirtualNode {}",
                        virtualNodeEntry.getNbNodes(),
                        virtualNodeEntry.getVirtualNodeName());

                try {
                    if (this.username != null) {
                        virtualNodeEntry.nodeRequestId =
                                this.schedulerNodeProvider.submitNodeRequest(
                                        this.schedulerUrl, this.username,
                                        this.password,
                                        virtualNodeEntry.getNbNodes(),
                                        this.dataFolder, this.jvmArguments,
                                        virtualNodeEntry.getNodeSourceNames());
                    } else {
                        virtualNodeEntry.nodeRequestId =
                                this.schedulerNodeProvider.submitNodeRequest(
                                        this.schedulerUrl,
                                        this.credentialsPath,
                                        virtualNodeEntry.getNbNodes(),
                                        this.dataFolder, this.jvmArguments,
                                        virtualNodeEntry.getNodeSourceNames());
                    }
                } catch (NodeProviderException npe) {
                    throw new IllegalStateException(
                            "Failed to submit job for GCMVirtualNode "
                                    + virtualNodeEntry.getVirtualNodeName(),
                            npe);
                }
            }

            this.isStarted = true;
        } else {
            throw new IllegalStateException(
                    "Cannot submit jobs because they have already been submitted");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStarted() {
        return this.isStarted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Node getANode() {
        if (this.isStarted()) {
            try {
                if (this.nodes == null) {
                    List<UniqueID> nodeRequestIds = new ArrayList<UniqueID>();

                    for (GcmVirtualNodeEntry virtualNodeEntry : this.virtualNodeEntries) {
                        nodeRequestIds.add(virtualNodeEntry.nodeRequestId);
                    }
                    this.nodes =
                            this.schedulerNodeProvider.getNodes(nodeRequestIds.toArray(new UniqueID[] {}));
                }

                if (this.nodeIndex < this.nodes.size()) {
                    Node node = this.nodes.get(this.nodeIndex);

                    this.nodeIndex++;

                    return node;
                } else {
                    throw new IllegalStateException("No node available");
                }
            } catch (NodeProviderException npe) {
                throw new IllegalStateException("Failed to get a node", npe);
            }
        } else {
            throw new IllegalStateException(
                    "Cannot get a node because the jobs have not yet been submitted");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized GCMVirtualNode getGcmVirtualNode(String virtualNodeName) {
        if (this.isStarted()) {
            if (!this.virtualNodes.containsKey(virtualNodeName)) {
                UniqueID nodeRequestId = null;

                for (GcmVirtualNodeEntry virtualNodeEntry : this.virtualNodeEntries) {
                    if (virtualNodeEntry.getVirtualNodeName().equals(
                            virtualNodeName)) {
                        nodeRequestId = virtualNodeEntry.nodeRequestId;
                        break;
                    }
                }

                if (nodeRequestId != null) {
                    try {
                        log.debug(
                                "Getting the GCMVirtualNode {}",
                                virtualNodeName);

                        this.virtualNodes.put(
                                virtualNodeName,
                                this.schedulerNodeProvider.getGCMVirtualNode(
                                        virtualNodeName, nodeRequestId));
                    } catch (NodeProviderException npe) {
                        throw new IllegalStateException(
                                "Failed to get GCMVirtualNode "
                                        + virtualNodeName, npe);
                    }
                } else {
                    return null;
                }
            }

            return this.virtualNodes.get(virtualNodeName);
        } else {
            throw new IllegalStateException("Cannot get the GCMVirtualNode "
                    + virtualNodeName
                    + " because the jobs have not yet been submitted");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void terminate() {
        if (this.isStarted()) {
            log.debug(
                    "Terminating the {} jobs submitted to the ProActive Scheduler located at {}",
                    this.virtualNodeEntries.size(), this.schedulerUrl);

            for (GcmVirtualNodeEntry virtualNodeEntry : this.virtualNodeEntries) {
                this.schedulerNodeProvider.releaseNodes(virtualNodeEntry.nodeRequestId);
            }

            this.isStarted = false;
        } else {
            throw new IllegalStateException(
                    "Cannot terminate jobs because they have not yet been submitted");
        }
    }

}
