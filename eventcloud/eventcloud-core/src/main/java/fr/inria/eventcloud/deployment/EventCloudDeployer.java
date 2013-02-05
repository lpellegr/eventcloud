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
package fr.inria.eventcloud.deployment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.p2p.structured.deployment.NetworkDeployer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;
import org.objectweb.proactive.extensions.p2p.structured.utils.ComponentUtils;

import fr.inria.eventcloud.EventCloudDescription;
import fr.inria.eventcloud.factories.SemanticFactory;
import fr.inria.eventcloud.overlay.SemanticPeer;
import fr.inria.eventcloud.providers.SemanticPersistentOverlayProvider;
import fr.inria.eventcloud.proxies.PublishProxy;
import fr.inria.eventcloud.proxies.PutGetProxy;
import fr.inria.eventcloud.proxies.SubscribeProxy;
import fr.inria.eventcloud.tracker.SemanticTracker;

/**
 * Initializes an EventCloud (i.e. a Content-Addressable-Network composed of
 * four dimensions) on a local machine or by distributing the components on
 * several machines.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public class EventCloudDeployer extends NetworkDeployer {

    private static final long serialVersionUID = 140L;

    private final EventCloudDescription eventCloudDescription;

    private List<PublishProxy> publishProxies;

    private List<PutGetProxy> putgetProxies;

    private List<SubscribeProxy> subscribeProxies;

    public EventCloudDeployer(EventCloudDescription description,
            EventCloudDeploymentDescriptor deploymentDescriptor) {
        super(deploymentDescriptor);
        this.eventCloudDescription = description;

        this.publishProxies =
                Collections.synchronizedList(new ArrayList<PublishProxy>());
        this.putgetProxies =
                Collections.synchronizedList(new ArrayList<PutGetProxy>());
        this.subscribeProxies =
                Collections.synchronizedList(new ArrayList<SubscribeProxy>());

        // sets stream URL on persistent overlay provider
        if (deploymentDescriptor.getOverlayProvider() instanceof SemanticPersistentOverlayProvider) {
            ((SemanticPersistentOverlayProvider) deploymentDescriptor.getOverlayProvider()).setStreamUrl(description.getId()
                    .getStreamUrl());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized Peer createPeer() {
        return SemanticFactory.newSemanticPeer(
                super.descriptor.getOverlayProvider(),
                super.descriptor.getNodeProvider());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized Tracker createTracker(String networkName) {
        return SemanticFactory.newSemanticTracker(
                networkName, super.descriptor.getNodeProvider());
    }

    /**
     * Registers a publish proxy to the list of publish proxies.
     * 
     * @param proxy
     *            the publish proxy to register.
     */
    public void registerProxy(PublishProxy proxy) {
        this.publishProxies.add(proxy);
    }

    /**
     * Registers a putget proxy to the list of putget proxies.
     * 
     * @param proxy
     *            the putget proxy to register.
     */
    public void registerProxy(PutGetProxy proxy) {
        this.putgetProxies.add(proxy);
    }

    /**
     * Registers a subscribe proxy to the list of subscribe proxies.
     * 
     * @param proxy
     *            the subscribe proxy to register.
     */
    public void registerProxy(SubscribeProxy proxy) {
        this.subscribeProxies.add(proxy);
    }

    /**
     * Unregisters a publish proxy from the list of publish proxies.
     * 
     * @param proxy
     *            the publish proxy to unregister.
     * 
     * @return {@code true} if the proxy has been successfully unregistered,
     *         {@code false} otherwise.
     */
    public boolean unregisterProxy(PublishProxy proxy) {
        return this.publishProxies.remove(proxy);
    }

    /**
     * Unregisters a putget proxy from the list of putget proxies.
     * 
     * @param proxy
     *            the putget proxy to unregister.
     * 
     * @return {@code true} if the proxy has been successfully unregistered,
     *         {@code false} otherwise.
     */
    public boolean unregisterProxy(PutGetProxy proxy) {
        return this.putgetProxies.remove(proxy);
    }

    /**
     * Unregisters a subscribe proxy from the list of subscribe proxies.
     * 
     * @param proxy
     *            the subscribe proxy to unregister.
     * 
     * @return {@code true} if the proxy has been successfully unregistered,
     *         {@code false} otherwise.
     */
    public boolean unregisterProxy(SubscribeProxy proxy) {
        return this.subscribeProxies.remove(proxy);
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

    public List<PublishProxy> getPublishProxies() {
        return this.publishProxies;
    }

    public List<PutGetProxy> getPutGetProxies() {
        return this.putgetProxies;
    }

    public List<SubscribeProxy> getSubscribeProxies() {
        return this.subscribeProxies;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void internalUndeploy() {
        ComponentUtils.terminateComponents(this.publishProxies);
        ComponentUtils.terminateComponents(this.putgetProxies);
        ComponentUtils.terminateComponents(this.subscribeProxies);
        super.internalUndeploy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void reset() {
        super.reset();
        this.publishProxies = null;
        this.putgetProxies = null;
        this.subscribeProxies = null;
    }

}
