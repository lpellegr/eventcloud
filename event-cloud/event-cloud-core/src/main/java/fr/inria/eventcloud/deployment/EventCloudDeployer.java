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

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.deployment.DeploymentConfiguration;
import org.objectweb.proactive.extensions.p2p.structured.deployment.EmptyDeploymentConfiguration;
import org.objectweb.proactive.extensions.p2p.structured.deployment.NetworkDeployer;
import org.objectweb.proactive.extensions.p2p.structured.deployment.NodeProvider;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.objectweb.proactive.extensions.p2p.structured.factories.AbstractFactory;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;

import fr.inria.eventcloud.factories.SemanticFactory;
import fr.inria.eventcloud.messages.request.can.ShutdownRequest;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.overlay.SemanticPeer;
import fr.inria.eventcloud.providers.SemanticPersistentOverlayProvider;
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

    /**
     * Constructs a new eventcloud deployer with an
     * {@link EmptyDeploymentConfiguration} and a
     * {@link SemanticPersistentOverlayProvider} but with no
     * {@link NodeProvider}.
     */
    public EventCloudDeployer() {
        this(new EmptyDeploymentConfiguration(),
                new SemanticPersistentOverlayProvider(), null);
    }

    public EventCloudDeployer(DeploymentConfiguration configuration,
            SerializableProvider<? extends SemanticCanOverlay> overlayProvider) {
        this(configuration, overlayProvider, null);
    }

    public EventCloudDeployer(DeploymentConfiguration mode,
            SerializableProvider<? extends SemanticCanOverlay> overlayProvider,
            NodeProvider nodeProvider) {
        super(mode, overlayProvider, nodeProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized Peer createPeer() {
        if (this.nodeProvider != null) {
            return SemanticFactory.newSemanticPeer(
                    this.overlayProvider,
                    this.nodeProvider.getGcmVirtualNode(P2PStructuredProperties.PEER_VN.getValue()));
        } else {
            return SemanticFactory.newSemanticPeer(this.overlayProvider);
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
            AbstractFactory.terminateComponent(peer);
        }

        for (Tracker tracker : this.getTrackers()) {
            AbstractFactory.terminateComponent(tracker);
        }
    }

}
