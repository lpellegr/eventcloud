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
import org.objectweb.proactive.extensions.p2p.structured.deployment.local.LocalNodeProvider;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;
import org.objectweb.proactive.extensions.p2p.structured.utils.ComponentUtils;

import fr.inria.eventcloud.EventCloudDescription;
import fr.inria.eventcloud.factories.EventCloudComponentsManagerFactory;
import fr.inria.eventcloud.overlay.SemanticPeer;
import fr.inria.eventcloud.providers.SemanticOverlayProvider;
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

    private static final long serialVersionUID = 160L;

    private final EventCloudDescription eventCloudDescription;

    private EventCloudComponentsManager componentPoolManager;

    private boolean componentPoolManagerCreatedInternally;

    private List<PublishProxy> publishProxies;

    private List<PutGetProxy> putgetProxies;

    private List<SubscribeProxy> subscribeProxies;

    public EventCloudDeployer(EventCloudDescription description,
            EventCloudDeploymentDescriptor deploymentDescriptor) {
        this(description, deploymentDescriptor, null);
    }

    /**
     * Creates a new EventCloud deployer.
     * 
     * @param description
     *            the description used to create the EventCloud.
     * @param deploymentDescriptor
     *            the deployment descriptor to use.
     * @param componentPoolManager
     *            the pool manager used to retrieve entities such as peers,
     *            trackers, etc. It is up to the programmer to start the
     *            instance of the pool manager before to pass it as an argument.
     *            Similarly, the programmer should stop manually the instance of
     *            the pool manager used after a call to {@link #undeploy()}. The
     *            pool's lifecycle is not managed by the deployer because the
     *            pool may be shared between several deployers.
     */
    public EventCloudDeployer(EventCloudDescription description,
            EventCloudDeploymentDescriptor deploymentDescriptor,
            EventCloudComponentsManager componentPoolManager) {
        super(deploymentDescriptor);

        this.eventCloudDescription = description;
        this.componentPoolManager = componentPoolManager;

        this.publishProxies =
                Collections.synchronizedList(new ArrayList<PublishProxy>());
        this.putgetProxies =
                Collections.synchronizedList(new ArrayList<PutGetProxy>());
        this.subscribeProxies =
                Collections.synchronizedList(new ArrayList<SubscribeProxy>());

        // sets stream URL on persistent overlay provider
        if (deploymentDescriptor.getOverlayProvider() instanceof SemanticOverlayProvider) {
            ((SemanticOverlayProvider) deploymentDescriptor.getOverlayProvider()).setStreamURL(description.getId()
                    .getStreamUrl());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deploy(int nbTrackers, int nbPeers) {
        if (this.componentPoolManager == null) {
            this.componentPoolManager =
                    EventCloudComponentsManagerFactory.newComponentsManager(
                            new LocalNodeProvider(), nbTrackers, nbPeers, 0, 0,
                            0);
            this.componentPoolManager.start();
            this.componentPoolManagerCreatedInternally = true;
        } else {
            this.componentPoolManagerCreatedInternally = false;
        }

        super.deploy(nbTrackers, nbPeers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized Peer createPeer() {
        return this.componentPoolManager.getPeer(
                super.descriptor.getDeploymentConfiguration(),
                super.descriptor.getOverlayProvider());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized Tracker createTracker(String networkName) {
        return this.componentPoolManager.getTracker(
                super.descriptor.getDeploymentConfiguration(), networkName);
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

    public EventCloudComponentsManager getComponentPoolManager() {
        return this.componentPoolManager;
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
        super.internalUndeploy();

        ComponentUtils.terminateComponents(this.publishProxies);
        ComponentUtils.terminateComponents(this.putgetProxies);
        ComponentUtils.terminateComponents(this.subscribeProxies);

        if (this.componentPoolManagerCreatedInternally) {
            this.componentPoolManager.stop();
        }
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
