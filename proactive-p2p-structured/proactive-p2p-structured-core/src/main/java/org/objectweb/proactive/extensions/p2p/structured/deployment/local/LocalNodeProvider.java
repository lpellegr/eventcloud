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
package org.objectweb.proactive.extensions.p2p.structured.deployment.local;

import java.io.Serializable;

import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.node.NodeFactory;
import org.objectweb.proactive.extensions.p2p.structured.deployment.NodeProvider;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;

/**
 * LocalNodeProvider is a concrete implementation of {@link NodeProvider} for a
 * local deployment.
 * 
 * @author bsauvan
 */
public class LocalNodeProvider implements NodeProvider, Serializable {

    private static final long serialVersionUID = 140L;

    private Node node;

    /**
     * Constructs a LocalNodeProvider.
     */
    public LocalNodeProvider() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start() {
        if (!this.isStarted()) {
            try {
                this.node = NodeFactory.getDefaultNode();
            } catch (NodeException e) {
                throw new IllegalStateException(e);
            }
        } else {
            throw new IllegalStateException(
                    "Cannot start the local deployment because it has already been started");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isStarted() {
        return this.node != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node getANode() {
        if (this.isStarted()) {
            return this.node;
        } else {
            throw new IllegalStateException(
                    "Cannot get a node because the local deployment has not yet been started");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GCMVirtualNode getGcmVirtualNode(String virtualNodeName) {
        if (this.isStarted()) {
            return null;
        } else {
            throw new IllegalStateException("Cannot get the GCMVirtualNode "
                    + virtualNodeName
                    + " because the local deployment has not yet been started");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void terminate() {
        if (this.isStarted()) {
            this.node = null;
        } else {
            throw new IllegalStateException(
                    "Cannot terminate the local deployment because it has not yet been started");
        }
    }

}
