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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.ProActiveTimeoutException;
import org.objectweb.proactive.core.UniqueID;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.objectweb.proactive.gcmdeployment.Topology;

/**
 * Basic implementation of {@link GCMVirtualNode} interface. <br>
 * This implementation is useful for creating manually {@link GCMVirtualNode}
 * from a list of {@link Node}s. This implies that the {@link GCMVirtualNode}
 * has a fixed list of {@link Node} which is the one given at its creation. <br>
 * Thus in this implementation, the {@link GCMVirtualNode}:
 * <ul>
 * <li>is not greedy.</li>
 * <li>is always ready.</li>
 * <li>does nothing when {@link #waitReady()} and {@link #waitReady(long)}
 * methods are called.</li>
 * <li>returns the same number when calling both methods
 * {@link #getNbRequiredNodes()} and {@link #getNbCurrentNodes()} (ie. the size
 * of the given {@link Node} list).</li>
 * <li>returns an empty list when calling {@link #getNewNodes()} method.</li>
 * <li>does not support subscriptions for node attachment notifications and
 * isReady notifications.</li>
 * <li>does not have a {@link Topology}.</li>
 * 
 * @author The ProActive Team
 * @see GCMVirtualNode
 */
public class GCMVirtualNodeImpl implements GCMVirtualNode, Serializable {

    private static final long serialVersionUID = 1L;

    private UniqueID uniqueID;

    private String name;

    private List<Node> nodes;

    private int newNodeIndex;

    public GCMVirtualNodeImpl(String name, List<Node> nodes) {
        this.uniqueID = new UniqueID();
        this.name = name;
        this.nodes = nodes;
        this.newNodeIndex = 0;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public boolean isGreedy() {
        return false;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void waitReady() {
    }

    @Override
    public void waitReady(long arg0) throws ProActiveTimeoutException {
    }

    @Override
    public long getNbRequiredNodes() {
        return this.nodes.size();
    }

    @Override
    public long getNbCurrentNodes() {
        return this.nodes.size();
    }

    @Override
    public List<Node> getCurrentNodes() {
        return this.nodes;
    }

    @Override
    public List<Node> getNewNodes() {
        return new ArrayList<Node>(0);
    }

    @Override
    public Node getANode() {
        return this.getANode(0);
    }

    @Override
    public Node getANode(int timeout) {
        if (this.nodes.size() > this.newNodeIndex) {
            Node node = this.nodes.get(this.newNodeIndex);
            this.newNodeIndex++;
            return node;
        }
        return null;
    }

    @Override
    public void subscribeNodeAttachment(Object arg0, String arg1, boolean arg2)
            throws ProActiveException {
        throw new ProActiveRuntimeException(
                "Not available in this implementation of GCMVirtualNode");
    }

    @Override
    public void unsubscribeNodeAttachment(Object arg0, String arg1)
            throws ProActiveException {
        throw new ProActiveRuntimeException(
                "Not available in this implementation of GCMVirtualNode");
    }

    @Override
    public void subscribeIsReady(Object arg0, String arg1)
            throws ProActiveException {
        throw new ProActiveRuntimeException(
                "Not available in this implementation of GCMVirtualNode");
    }

    @Override
    public void unsubscribeIsReady(Object arg0, String arg1)
            throws ProActiveException {
        throw new ProActiveRuntimeException(
                "Not available in this implementation of GCMVirtualNode");
    }

    @Override
    public Topology getCurrentTopology() {
        throw new ProActiveRuntimeException(
                "Not available in this implementation of GCMVirtualNode");
    }

    @Override
    public void updateTopology(Topology arg0) {
        throw new ProActiveRuntimeException(
                "Not available in this implementation of GCMVirtualNode");
    }

    @Override
    public UniqueID getUniqueID() {
        return this.uniqueID;
    }
}
