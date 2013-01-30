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

/**
 * GcmDeploymentNodeProvider is a concrete implementation of
 * {@link NodeProvider} for the GCM deployment.
 * 
 * @author bsauvan
 */
public class GcmDeploymentNodeProvider implements NodeProvider, Serializable {

    private static final long serialVersionUID = 140L;

    private static final Logger log =
            LoggerFactory.getLogger(GcmDeploymentNodeProvider.class);

    private final GCMApplication gcma;

    private List<Node> nodes;

    private int nodeIndex;

    /**
     * Constructs a GcmDeploymentNodeProvider.
     * 
     * @param gcmaPath
     *            the path to the GCM Application descriptor.
     */
    public GcmDeploymentNodeProvider(String gcmaPath) {
        try {
            this.gcma =
                    PAGCMDeployment.loadApplicationDescriptor(new File(gcmaPath));
        } catch (ProActiveException pe) {
            throw new IllegalStateException(
                    "Failed to load GCM Application descriptor located at "
                            + gcmaPath, pe);
        }
        this.nodes = null;
        this.nodeIndex = 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        if (!this.gcma.isStarted()) {
            log.debug(
                    "Starting GCM deployment described in the GCM Application descriptor located at {}",
                    this.gcma.getDescriptorURL().getPath());

            this.gcma.startDeployment();
            this.gcma.waitReady();
        } else {
            throw new IllegalStateException(
                    "Cannot start the GCM deployment because it has already been started");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStarted() {
        return this.gcma.isStarted();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getANode() {
        if (this.gcma.isStarted()) {
            if (this.nodes == null) {
                this.nodes = this.gcma.getAllNodes();
            }

            if (this.nodeIndex < this.nodes.size()) {
                Node node = this.nodes.get(this.nodeIndex);

                this.nodeIndex++;

                return node;
            } else {
                throw new IllegalStateException("No node available");
            }
        } else {
            throw new IllegalStateException(
                    "Cannot get a node because the GCM deployment has not yet been started");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GCMVirtualNode getGcmVirtualNode(String virtualNodeName) {
        if (this.gcma.isStarted()) {
            return this.gcma.getVirtualNode(virtualNodeName);
        } else {
            throw new IllegalStateException("Cannot get the GCMVirtualNode "
                    + virtualNodeName
                    + " because the GCM deployment has not yet been started");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void terminate() {
        if (this.gcma.isStarted()) {
            log.debug(
                    "Terminating the GCM deployment described in the GCM Application descriptor located at {}",
                    this.gcma.getDescriptorURL().getPath());

            this.gcma.kill();
        } else {
            throw new IllegalStateException(
                    "Cannot terminate the GCM deployment because it has not yet been started");
        }
    }

}
