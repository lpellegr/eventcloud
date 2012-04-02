/**
 * Copyright (c) 2011-2012 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.deployment;

import org.etsi.uri.gcm.api.control.GCMLifeCycleController;
import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.deployment.DeploymentConfiguration;
import org.objectweb.proactive.extensions.p2p.structured.deployment.EmptyDeploymentConfiguration;
import org.objectweb.proactive.extensions.p2p.structured.deployment.NetworkDeployer;
import org.objectweb.proactive.extensions.p2p.structured.deployment.NodeProvider;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;

import fr.inria.eventcloud.factories.SemanticFactory;
import fr.inria.eventcloud.messages.request.can.ShutdownRequest;
import fr.inria.eventcloud.overlay.SemanticPeer;
import fr.inria.eventcloud.tracker.SemanticTracker;

/**
 * Initializes an Event Cloud (i.e. a Content-Addressable-Network composed of
 * four dimensions) on a local machine or by distributing the components on
 * several machines.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public class EventCloudDeployer extends NetworkDeployer {

    private static final long serialVersionUID = 1L;

    public EventCloudDeployer() {
        this(new EmptyDeploymentConfiguration());
    }

    public EventCloudDeployer(DeploymentConfiguration mode) {
        this(mode, null);
    }

    public EventCloudDeployer(NodeProvider nodeProvider) {
        super(new EmptyDeploymentConfiguration(), nodeProvider);
    }

    public EventCloudDeployer(DeploymentConfiguration mode,
            NodeProvider nodeProvider) {
        super(mode, nodeProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized Peer createPeer() {
        if (this.nodeProvider != null) {
            return SemanticFactory.newSemanticPeer(this.nodeProvider.getGcmVirtualNode(P2PStructuredProperties.PEER_VN.getValue()));
        } else {
            return SemanticFactory.newSemanticPeer();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized Tracker createTracker(String networkName) {
        if (this.nodeProvider != null) {
            return SemanticFactory.newSemanticTracker(
                    networkName,
                    this.nodeProvider.getGcmVirtualNode(P2PStructuredProperties.TRACKER_VN.getValue()));
        } else {
            return SemanticFactory.newSemanticTracker(networkName);
        }
    }

    public SemanticPeer getRandomSemanticPeer() {
        return (SemanticPeer) PAFuture.getFutureValue(super.getRandomTracker()
                .getRandomPeer());
    }

    public SemanticTracker getRandomSemanticTracker() {
        return (SemanticTracker) PAFuture.getFutureValue(super.getRandomTracker());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void internalUndeploy() {
        try {
            PAFuture.waitFor(this.getRandomPeer().send(new ShutdownRequest()));
        } catch (DispatchException e) {
            e.printStackTrace();
        }

        for (Peer peer : super.getRandomTracker().getPeers()) {
            this.terminateComponent(peer);
        }

        for (Tracker tracker : this.getTrackers()) {
            this.terminateComponent(tracker);
        }
    }

    private void terminateComponent(Object component) {
        try {
            Component owner = ((Interface) component).getFcItfOwner();
            GCMLifeCycleController lcc = GCM.getGCMLifeCycleController(owner);
            lcc.stopFc();
            lcc.terminateGCMComponent();
        } catch (IllegalLifeCycleException e) {
            e.printStackTrace();
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
        }
    }

}
