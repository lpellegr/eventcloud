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
package org.objectweb.proactive.extensions.p2p.structured.deployment.gcmdeployment;

import java.io.File;
import java.io.Serializable;
import java.util.List;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.extensions.p2p.structured.deployment.NodeProvider;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * Concrete implementation of {@link NodeProvider} for the GCM deployment.
 * 
 * @author bsauvan
 */
public class GcmDeploymentNodeProvider implements NodeProvider, Serializable {

    private static final long serialVersionUID = 151L;

    private static final Logger log =
            LoggerFactory.getLogger(GcmDeploymentNodeProvider.class);

    private String gcmaPath;

    private GCMApplication gcma;

    private List<Node> nodes;

    private int nodeIndex;

    /**
     * Constructs a {@link GcmDeploymentNodeProvider}.
     */
    public GcmDeploymentNodeProvider() {
    }

    /**
     * Constructs a {@link GcmDeploymentNodeProvider}.
     * 
     * @param gcmaPath
     *            the path to the GCM Application descriptor.
     */
    public GcmDeploymentNodeProvider(String gcmaPath) {
        this.gcmaPath = gcmaPath;
    }

    /**
     * Returns the path to the GCM Application descriptor.
     * 
     * @return the path to the GCM Application descriptor.
     */
    public String getGcmaPath() {
        return this.gcmaPath;
    }

    /**
     * Sets the path to the GCM Application descriptor.
     * 
     * @param gcmaPath
     *            the path to the GCM Application descriptor.
     */
    public void setGcmaPath(String gcmaPath) {
        Preconditions.checkState(
                !this.isStarted(),
                "Cannot set the path to the GCM Application descriptor because the GCM deployment has already been started");

        this.gcmaPath = gcmaPath;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        Preconditions.checkState(
                !this.isStarted(),
                "Cannot start the GCM deployment because it has already been started");

        this.init();

        log.debug(
                "Starting GCM deployment described in the GCM Application descriptor located at {}",
                this.gcmaPath);

        this.gcma.startDeployment();
        this.gcma.waitReady();
    }

    private void init() {
        Preconditions.checkNotNull(
                this.gcmaPath,
                "Cannot initialize the GCM deployment because no path for the GCM Application descriptor has been given");

        try {
            this.gcma =
                    PAGCMDeployment.loadApplicationDescriptor(new File(
                            this.gcmaPath));
        } catch (ProActiveException pe) {
            throw new IllegalStateException(
                    "Failed to load GCM Application descriptor located at "
                            + this.gcmaPath, pe);
        }
        this.nodes = null;
        this.nodeIndex = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStarted() {
        return (this.gcma != null) && this.gcma.isStarted();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getANode() {
        Preconditions.checkState(
                this.isStarted(),
                "Cannot get a node because the GCM deployment has not yet been started");

        if (this.nodes == null) {
            this.nodes = this.gcma.getAllNodes();
        }

        Preconditions.checkElementIndex(
                this.nodeIndex, this.nodes.size(), "No node available");

        Node node = this.nodes.get(this.nodeIndex);

        this.nodeIndex++;

        return node;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GCMVirtualNode getGcmVirtualNode(String virtualNodeName) {
        Preconditions.checkState(
                this.isStarted(),
                "Cannot get the GCMVirtualNode "
                        + virtualNodeName
                        + " because the GCM deployment has not yet been started");

        return this.gcma.getVirtualNode(virtualNodeName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void terminate() {
        Preconditions.checkState(
                this.isStarted(),
                "Cannot terminate the GCM deployment because it has not yet been started");

        log.debug(
                "Terminating the GCM deployment described in the GCM Application descriptor located at {}",
                this.gcmaPath);

        this.gcma.kill();
    }

}
