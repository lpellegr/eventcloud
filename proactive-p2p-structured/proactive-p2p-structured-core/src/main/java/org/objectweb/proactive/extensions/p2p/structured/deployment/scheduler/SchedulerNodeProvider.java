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

import com.google.common.base.Preconditions;

/**
 * Concrete implementation of {@link NodeProvider} for the ProActive Scheduler.
 * 
 * @author bsauvan
 * @author lpellegr
 */
public class SchedulerNodeProvider implements NodeProvider, Serializable {

    private static final long serialVersionUID = 160L;

    private static final Logger LOG =
            LoggerFactory.getLogger(SchedulerNodeProvider.class);

    private String schedulerUrl;

    private String username;

    private String password;

    private String credentialsPath;

    private String dataFolder;

    private List<String> jvmArguments;

    private List<GcmVirtualNodeEntry> virtualNodeEntries;

    // TODO remove transient
    private transient org.ow2.proactive.scheduler.job.programming.SchedulerNodeProvider schedulerNodeProvider;

    private boolean isStarted;

    private Map<String, GCMVirtualNode> virtualNodes;

    private List<Node> nodes;

    private int nodeIndex;

    /**
     * Constructs a {@link SchedulerNodeProvider}.
     */
    public SchedulerNodeProvider() {
        this(null, null, null, null, null, null, null);
    }

    /**
     * Constructs a {@link SchedulerNodeProvider}.
     * 
     * @param schedulerUrl
     *            the URL of the ProActive Scheduler.
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
     *            the URL of the ProActive Scheduler.
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
        this.isStarted = false;
    }

    /**
     * Returns the URL of the ProActive Scheduler.
     * 
     * @return the URL of the ProActive Scheduler.
     */
    public String getSchedulerUrl() {
        return this.schedulerUrl;
    }

    /**
     * Sets the URL of the ProActive Scheduler.
     * 
     * @param schedulerUrl
     *            the URL of the ProActive Scheduler.
     */
    public void setSchedulerUrl(String schedulerUrl) {
        Preconditions.checkState(
                !this.isStarted(),
                "Cannot set the URL of the ProActive Scheduler because the jobs have already been submitted");

        this.schedulerUrl = schedulerUrl;
    }

    /**
     * Returns the username to connect to the ProActive Scheduler.
     * 
     * @return the username to connect to the ProActive Scheduler.
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Sets the username to connect to the ProActive Scheduler.
     * 
     * @param username
     *            the username to connect to the ProActive Scheduler.
     */
    public void setUsername(String username) {
        Preconditions.checkState(
                !this.isStarted(),
                "Cannot set the username to connect to the ProActive Scheduler because the jobs have already been submitted");

        this.username = username;
    }

    /**
     * Returns the password to connect to the ProActive Scheduler.
     * 
     * @return the password to connect to the ProActive Scheduler.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Sets the password to connect to the ProActive Scheduler.
     * 
     * @param password
     *            the password to connect to the ProActive Scheduler.
     */
    public void setPassword(String password) {
        Preconditions.checkState(
                !this.isStarted(),
                "Cannot set the password to connect to the ProActive Scheduler because the jobs have already been submitted");

        this.password = password;
    }

    /**
     * Returns the path of the credentials to connect to the scheduler.
     * 
     * @return the path of the credentials to connect to the scheduler.
     */
    public String getCredentialsPath() {
        return this.credentialsPath;
    }

    /**
     * Sets the path of the credentials to connect to the scheduler.
     * 
     * @param credentialsPath
     *            the path of the credentials to connect to the scheduler.
     */
    public void setCredentialsPath(String credentialsPath) {
        Preconditions.checkState(
                !this.isStarted(),
                "Cannot set the path of the credentials to connect to the scheduler because the jobs have already been submitted");

        this.credentialsPath = credentialsPath;
    }

    /**
     * Returns the folder containing the data to transfer to the nodes.
     * 
     * @return the folder containing the data to transfer to the nodes.
     */
    public String getDataFolder() {
        return this.dataFolder;
    }

    /**
     * Sets the folder containing the data to transfer to the nodes.
     * 
     * @param dataFolder
     *            the folder containing the data to transfer to the nodes.
     */
    public void setDataFolder(String dataFolder) {
        Preconditions.checkState(
                !this.isStarted(),
                "Cannot set the folder containing the data to transfer to the nodes because the jobs have already been submitted");

        this.dataFolder = dataFolder;
    }

    /**
     * Returns the JVM arguments to use for the nodes.
     * 
     * @return the JVM arguments to use for the nodes.
     */
    public List<String> getJvmArguments() {
        return this.jvmArguments;
    }

    /**
     * Sets the JVM arguments to use for the nodes.
     * 
     * @param jvmArguments
     *            the JVM arguments to use for the nodes.
     */
    public void setJvmArguments(List<String> jvmArguments) {
        Preconditions.checkState(
                !this.isStarted(),
                "Cannot set the JVM arguments to use for the nodes because the jobs have already been submitted");

        this.jvmArguments = jvmArguments;
    }

    /**
     * Returns the {@link GcmVirtualNodeEntry GcmVirtualNodeEntries} defining
     * the GCMVirtualNodes to deploy.
     * 
     * @return the {@link GcmVirtualNodeEntry GcmVirtualNodeEntries} defining
     *         the GCMVirtualNodes to deploy.
     */
    public List<GcmVirtualNodeEntry> getVirtualNodeEntries() {
        return this.virtualNodeEntries;
    }

    /**
     * Sets the {@link GcmVirtualNodeEntry GcmVirtualNodeEntries} defining the
     * GCMVirtualNodes to deploy.
     * 
     * @param virtualNodeEntries
     *            the {@link GcmVirtualNodeEntry GcmVirtualNodeEntries} defining
     *            the GCMVirtualNodes to deploy.
     */
    public void setVirtualNodeEntries(List<GcmVirtualNodeEntry> virtualNodeEntries) {
        Preconditions.checkState(
                !this.isStarted(),
                "Cannot set the GcmVirtualNodeEntries defining the GCMVirtualNodes to deploy because the jobs have already been submitted");

        this.virtualNodeEntries = virtualNodeEntries;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        Preconditions.checkState(
                !this.isStarted(),
                "Cannot submit jobs because they have already been submitted");

        this.init();

        LOG.debug(
                "Submitting {} jobs to the ProActive Scheduler located at {}",
                this.virtualNodeEntries.size(), this.schedulerUrl);

        for (GcmVirtualNodeEntry virtualNodeEntry : this.virtualNodeEntries) {
            LOG.debug(
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
                                    this.schedulerUrl, this.credentialsPath,
                                    virtualNodeEntry.getNbNodes(),
                                    this.dataFolder, this.jvmArguments,
                                    virtualNodeEntry.getNodeSourceNames());
                }
            } catch (NodeProviderException npe) {
                throw new IllegalStateException(
                        "Failed to submit job for GCMVirtualNode "
                                + virtualNodeEntry.getVirtualNodeName(), npe);
            }
        }

        this.isStarted = true;
    }

    private void init() {
        Preconditions.checkNotNull(
                this.schedulerUrl,
                "Cannot initialize because no URL for the ProActive Scheduler has been given");
        Preconditions.checkNotNull(
                (((this.username != null) && (this.password != null)) || (this.credentialsPath != null))
                        ? true : null,
                "Cannot initialize because no username/password or credentials have been given");
        Preconditions.checkNotNull(
                this.virtualNodeEntries,
                "Cannot initialize because no GcmVirtualNodeEntries have been given");

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
    public boolean isStarted() {
        return this.isStarted;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Node getANode() {
        Preconditions.checkState(
                this.isStarted(),
                "Cannot get a node because the jobs have not yet been submitted");

        try {
            if (this.nodes == null) {
                List<UniqueID> nodeRequestIds = new ArrayList<UniqueID>();

                for (GcmVirtualNodeEntry virtualNodeEntry : this.virtualNodeEntries) {
                    nodeRequestIds.add(virtualNodeEntry.nodeRequestId);
                }
                this.nodes =
                        this.schedulerNodeProvider.getNodes(nodeRequestIds.toArray(new UniqueID[nodeRequestIds.size()]));
            }

            Preconditions.checkElementIndex(
                    this.nodeIndex, this.nodes.size(), "No node available");

            Node node = this.nodes.get(this.nodeIndex);

            this.nodeIndex++;

            return node;
        } catch (NodeProviderException npe) {
            throw new IllegalStateException("Failed to get a node", npe);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized GCMVirtualNode getGcmVirtualNode(String virtualNodeName) {
        Preconditions.checkState(
                this.isStarted(), "Cannot get the GCMVirtualNode "
                        + virtualNodeName
                        + " because the jobs have not yet been submitted");

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
                    LOG.debug("Getting the GCMVirtualNode {}", virtualNodeName);

                    this.virtualNodes.put(
                            virtualNodeName,
                            this.schedulerNodeProvider.getGCMVirtualNode(
                                    virtualNodeName, nodeRequestId));
                } catch (NodeProviderException npe) {
                    throw new IllegalStateException(
                            "Failed to get GCMVirtualNode " + virtualNodeName,
                            npe);
                }
            } else {
                return null;
            }
        }

        return this.virtualNodes.get(virtualNodeName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void terminate() {
        Preconditions.checkState(
                this.isStarted(),
                "Cannot terminate jobs because they have not yet been submitted");

        LOG.debug(
                "Terminating the {} jobs submitted to the ProActive Scheduler located at {}",
                this.virtualNodeEntries.size(), this.schedulerUrl);

        for (GcmVirtualNodeEntry virtualNodeEntry : this.virtualNodeEntries) {
            this.schedulerNodeProvider.releaseNodes(virtualNodeEntry.nodeRequestId);
        }

        this.isStarted = false;
    }

}
