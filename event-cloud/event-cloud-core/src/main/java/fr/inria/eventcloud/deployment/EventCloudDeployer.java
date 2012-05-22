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

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.p2p.structured.deployment.NetworkDeployer;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;
import org.objectweb.proactive.extensions.p2p.structured.utils.ComponentUtils;

import fr.inria.eventcloud.EventCloudDescription;
import fr.inria.eventcloud.factories.SemanticFactory;
import fr.inria.eventcloud.messages.request.can.ShutdownRequest;
import fr.inria.eventcloud.overlay.SemanticPeer;
import fr.inria.eventcloud.overlay.SemanticPeerImpl;
import fr.inria.eventcloud.proxies.Proxy;
import fr.inria.eventcloud.tracker.SemanticTracker;
import fr.inria.eventcloud.tracker.SemanticTrackerImpl;

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

    private final EventCloudDescription eventCloudDescription;

    private List<Proxy> proxies;

    public EventCloudDeployer(EventCloudDescription description,
            EventCloudDeploymentDescriptor deploymentDescriptor) {
        super(deploymentDescriptor);
        this.eventCloudDescription = description;
        this.proxies = new ArrayList<Proxy>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized Peer createPeer() {
        if (super.descriptor.getNodeProvider() != null) {
            return SemanticFactory.newSemanticPeer(
                    super.descriptor.getOverlayProvider(),
                    super.descriptor.getNodeProvider().getGcmVirtualNode(
                            SemanticPeerImpl.PEER_VN));
        } else {
            return SemanticFactory.newSemanticPeer(super.descriptor.getOverlayProvider());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized Tracker createTracker(String networkName) {
        if (super.descriptor.getNodeProvider() != null) {
            return SemanticFactory.newSemanticTracker(
                    networkName, super.descriptor.getNodeProvider()
                            .getGcmVirtualNode(SemanticTrackerImpl.TRACKER_VN));
        } else {
            return SemanticFactory.newSemanticTracker(networkName);
        }
    }

    /**
     * Registers a proxy to the list of proxies.
     * 
     * @param proxy
     *            the proxy to register.
     */
    public synchronized void registerProxy(Proxy proxy) {
        this.proxies.add(proxy);
    }

    /**
     * Unregisters a proxy from the list of proxies.
     * 
     * @param proxy
     *            the proxy to unregister.
     * @return true if the proxy has been successfully unregistered, false
     *         otherwise.
     */
    public synchronized boolean unregisterProxy(Proxy proxy) {
        return this.proxies.remove(proxy);
    }

    public EventCloudDescription getEventCloudDescription() {
        return this.eventCloudDescription;
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
        for (Proxy proxy : this.proxies) {
            ComponentUtils.terminateComponent(proxy);
        }

        try {
            PAFuture.waitFor(this.getRandomPeer().send(new ShutdownRequest()));
        } catch (DispatchException e) {
            e.printStackTrace();
        }

        for (Peer peer : super.getRandomTracker().getPeers()) {
            ComponentUtils.terminateComponent(peer);
        }

        for (Tracker tracker : this.getTrackers()) {
            ComponentUtils.terminateComponent(tracker);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        this.proxies = null;
    }

}
