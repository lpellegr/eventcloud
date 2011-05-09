/**
 * Copyright (c) 2011 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.deployment;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.Node;
import org.objectweb.proactive.extensions.gcmdeployment.PAGCMDeployment;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;
import org.objectweb.proactive.gcmdeployment.GCMApplication;
import org.objectweb.proactive.gcmdeployment.GCMVirtualNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NodeProvider is an active object which is in charge for providing
 * {@link Node}s for peers, trackers and kernels.
 * 
 * @author lpellegr
 */
public class NodeProvider implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger logger =
            LoggerFactory.getLogger(NodeProvider.class);

    private final static int NODES_ACQUISITION_TIMEOUT = 2000;

    private File pathToGCMADescriptor;

    private Map<NodeProviderKey, NodeProviderEntry> entries =
            new HashMap<NodeProviderKey, NodeProviderEntry>();

    private String bindingName;

    private GCMApplication gcmad;

    public NodeProvider() {

    }

    public NodeProvider(File pathToGCMADescriptor) {
        this.pathToGCMADescriptor = pathToGCMADescriptor;
    }

    /**
     * Start deployment by acquiring {@link Node}s.
     * 
     * @param maxTrackerNodesToAcquire
     *            the maximum number of nodes for {@link Tracker}s the
     *            {@link NodeProvider} will acquire. However, the GCMA file must
     *            contain a sufficient number of machines to use in order to
     *            acquire the specified number of nodes. This parameter allows
     *            to acquire less node than specified in the GCMA files. To
     *            acquire all possible nodes as specified in the GCMA file you
     *            can use the value <code>-1</code>.
     * 
     * @param maxChordNodesToAcquire
     *            the maximum number of nodes for Chord {@link Peer}s the
     *            {@link NodeProvider} will acquire. However, the GCMA file must
     *            contain a sufficient number of machines to use in order to
     *            acquire the specified number of nodes. This parameter allows
     *            to acquire less node than specified in the GCMA files. To
     *            acquire all possible nodes as specified in the GCMA file you
     *            can use the value <code>-1</code>.
     * 
     * @param maxCANNodesToAcquire
     *            the maximum number of nodes for CAN {@link Peer}s the
     *            {@link NodeProvider} will acquire. However, the GCMA file must
     *            contain a sufficient number of machines to use in order to
     *            acquire the specified number of nodes. This parameter allows
     *            to acquire less node than specified in the GCMA files. To
     *            acquire all possible nodes as specified in the GCMA file you
     *            can use the value <code>-1</code>.
     */
    public void deploy(int maxTrackerNodesToAcquire,
                       int maxChordNodesToAcquire, int maxCANNodesToAcquire) {
        try {
            this.gcmad =
                    PAGCMDeployment.loadApplicationDescriptor(this.pathToGCMADescriptor);
        } catch (ProActiveException e) {
            e.printStackTrace();
        }
        this.gcmad.startDeployment();
        this.gcmad.waitReady();

        NodeProviderKey.NODE_PROVIDER.setMaximumNodesToAcquire(1);
        NodeProviderKey.PEERS_CAN.setMaximumNodesToAcquire(maxCANNodesToAcquire);
        NodeProviderKey.PEERS_CHORD.setMaximumNodesToAcquire(maxChordNodesToAcquire);
        NodeProviderKey.TRACKERS.setMaximumNodesToAcquire(maxTrackerNodesToAcquire);

        for (NodeProviderKey key : NodeProviderKey.values()) {
            this.entries.put(key, new NodeProviderEntry(
                    key.getMaximumNodesToAcquire() == -1
                            ? this.acquireAllNodes(key.getVirtualNodeName())
                            : this.acquireNodes(
                                    key.getVirtualNodeName(),
                                    key.getMaximumNodesToAcquire())));
        }

        if (logger.isDebugEnabled()) {
            StringBuffer buf = new StringBuffer();
            buf.append("Deployment started (maxTrackerNodes=");
            buf.append(maxTrackerNodesToAcquire);
            buf.append(", maxCANNodes=");
            buf.append(maxCANNodesToAcquire);
            buf.append(", maxChordNodes=");
            buf.append(maxChordNodesToAcquire);
            buf.append(")...\n");

            for (NodeProviderKey key : NodeProviderKey.values()) {
                if (this.entries.get(key).getNodes().size() > 0) {
                    buf.append("* ");
                    buf.append(this.entries.get(key).getNodes().size());
                    buf.append(" nodes available for ");
                    buf.append(key);
                    buf.append("\n");
                    buf.append(this.dump(this.entries.get(key).getNodes()));
                }
            }

            logger.debug(buf.toString());
        }
    }

    private String dump(List<Node> nodes) {
        StringBuffer buf = new StringBuffer();
        for (Node node : nodes) {
            buf.append("    - ");
            buf.append(node.getNodeInformation().getURL());
            buf.append("\n");
        }
        return buf.toString();
    }

    private List<Node> acquireNodes(String virtualNodeId, int nb) {
        GCMVirtualNode virtualNode = this.gcmad.getVirtualNode(virtualNodeId);

        List<Node> nodes = new ArrayList<Node>();
        Node node;

        for (int i = 0; i < nb; i++) {
            node = virtualNode.getANode(NODES_ACQUISITION_TIMEOUT);
            if (node == null) {
                throw new IllegalStateException("Cannot acquire " + nb
                        + " nodes for virtual node '" + virtualNodeId
                        + "' because only " + i + " are available.");
            } else {
                nodes.add(node);
            }
        }

        return nodes;
    }

    private List<Node> acquireAllNodes(String virtualNodeId) {
        boolean waitingForNodesAcquisition = true;
        GCMVirtualNode virtualNode = this.gcmad.getVirtualNode(virtualNodeId);

        List<Node> nodes = virtualNode.getNewNodes();

        while (waitingForNodesAcquisition) {
            Thread.yield();

            List<Node> newNodes = virtualNode.getNewNodes();
            if (newNodes.size() > 0) {
                nodes.addAll(newNodes);
            } else {
                waitingForNodesAcquisition = false;
            }

            if (logger.isInfoEnabled()) {
                logger.info(nodes.size()
                        + " nodes are acquired for virtual node '"
                        + virtualNode + "'");
            }
        }

        return nodes;
    }

    public List<Node> getAllNodes(NodeProviderKey key) {
        return this.entries.get(key).getNodes();
    }

    public Node getNextNode(NodeProviderKey key) {
        return this.entries.get(key).getNextNode();
    }

    public Node[] getNextNodes(NodeProviderKey key, int nb) {
        List<Node> nodes = new ArrayList<Node>(nb);

        for (int i = 0; i < nb; i++) {
            nodes.add(this.getNextNode(key));
        }

        return nodes.toArray(new Node[] {});
    }

    public void killAll() {
        this.gcmad.kill();
    }

    public String register() {
        if (this.bindingName == null) {
            try {
                this.bindingName =
                        PAActiveObject.registerByName(
                                PAActiveObject.getStubOnThis(), "node-provider");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return this.bindingName;
    }

}
