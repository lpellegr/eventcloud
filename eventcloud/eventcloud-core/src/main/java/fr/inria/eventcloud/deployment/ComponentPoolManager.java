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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.p2p.structured.CommonAttributeController;
import org.objectweb.proactive.extensions.p2p.structured.deployment.DeploymentConfiguration;
import org.objectweb.proactive.extensions.p2p.structured.deployment.NodeProvider;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;
import org.objectweb.proactive.extensions.p2p.structured.utils.ComponentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.PublishApi;
import fr.inria.eventcloud.api.PutGetApi;
import fr.inria.eventcloud.api.SubscribeApi;
import fr.inria.eventcloud.api.properties.AlterableElaProperty;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.factories.SemanticFactory;
import fr.inria.eventcloud.overlay.SemanticPeer;
import fr.inria.eventcloud.proxies.PublishProxy;
import fr.inria.eventcloud.proxies.PutGetProxy;
import fr.inria.eventcloud.proxies.SubscribeProxy;
import fr.inria.eventcloud.tracker.SemanticTracker;

/**
 * Manager of component pools for EventClouds. <br>
 * A component pool manager is associated to a {@link NodeProvider node
 * provider} and manages a set of pools for any kind of components that are used
 * in an EventCloud. <br>
 * In particular, it manages pools of:
 * <ul>
 * <li>
 * {@link SemanticTracker trackers}.</li>
 * <li>
 * {@link SemanticPeer peers}.</li>
 * <li>
 * {@link PublishProxy publish proxies}.</li>
 * <li>
 * {@link SubscribeProxy subscribe proxies}.</li>
 * <li>
 * {@link PutGetProxy put/get proxies}.</li>
 * </ul>
 * <br>
 * 
 * @author bsauvan
 */
public class ComponentPoolManager implements Serializable {

    private static final long serialVersionUID = 160L;

    private static final Logger log =
            LoggerFactory.getLogger(ComponentPoolManager.class);

    protected NodeProvider nodeProvider;

    private int nbTrackers;

    private int nbPeers;

    private int nbPublishProxies;

    private int nbSubscribeProxies;

    private int nbPutGetProxies;

    private List<SemanticTracker> trackers;

    private List<SemanticPeer> peers;

    private List<PublishApi> publishProxies;

    private List<SubscribeApi> subscribeProxies;

    private List<PutGetApi> putgetProxies;

    private ComponentPoolManagerThread componentPoolManagerThread;

    /**
     * Empty constructor required by ProActive.
     */
    public ComponentPoolManager() {
    }

    /**
     * Creates a {@link ComponentPoolManager}.
     * 
     * @param nodeProvider
     *            the node provider to be used for deployment of components.
     */
    public ComponentPoolManager(NodeProvider nodeProvider) {
        this.nodeProvider = nodeProvider;

        this.nbTrackers = EventCloudProperties.NB_TRACKERS_POOL.getValue();
        this.nbPeers = EventCloudProperties.NB_PEERS_POOL.getValue();
        this.nbPublishProxies =
                EventCloudProperties.NB_PUBLISH_PROXIES_POOL.getValue();
        this.nbSubscribeProxies =
                EventCloudProperties.NB_SUBSCRIBE_PROXIES_POOL.getValue();
        this.nbPutGetProxies =
                EventCloudProperties.NB_PUTGET_PROXIES_POOL.getValue();

        this.trackers =
                Collections.synchronizedList(new ArrayList<SemanticTracker>(
                        this.nbTrackers));
        this.peers =
                Collections.synchronizedList(new ArrayList<SemanticPeer>(
                        this.nbPeers));
        this.publishProxies =
                Collections.synchronizedList(new ArrayList<PublishApi>(
                        this.nbPublishProxies));
        this.subscribeProxies =
                Collections.synchronizedList(new ArrayList<SubscribeApi>(
                        this.nbSubscribeProxies));
        this.putgetProxies =
                Collections.synchronizedList(new ArrayList<PutGetApi>(
                        this.nbPutGetProxies));

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                ComponentPoolManager.this.stop();
            }
        });
    }

    /**
     * Indicates whether the component pool manager is started or not.
     * 
     * @return true if the component pool manager is started, false otherwise.
     */
    public boolean isStarted() {
        return (this.componentPoolManagerThread != null)
                && this.componentPoolManagerThread.isAlive();
    }

    /**
     * Starts the component pool manager.
     * 
     * @throws IllegalStateException
     *             if the component pool manager is not started yet.
     */
    public void start() throws IllegalStateException {
        Preconditions.checkState(
                !this.isStarted(),
                "EventCloud Component Pool Manager has already been started");

        this.fillPools();

        this.componentPoolManagerThread = new ComponentPoolManagerThread();
        this.componentPoolManagerThread.start();

        log.info("Component pool manager "
                + PAActiveObject.getBodyOnThis().getID() + " started");
    }

    /**
     * Stops the component pool manager.
     */
    public void stop() {
        if (this.isStarted()) {
            this.componentPoolManagerThread.stopThread();
            this.componentPoolManagerThread = null;

            ComponentUtils.terminateComponents(this.trackers);
            this.trackers.clear();
            ComponentUtils.terminateComponents(this.peers);
            this.peers.clear();
            ComponentUtils.terminateComponents(this.publishProxies);
            this.publishProxies.clear();
            ComponentUtils.terminateComponents(this.subscribeProxies);
            this.subscribeProxies.clear();
            ComponentUtils.terminateComponents(this.putgetProxies);
            this.putgetProxies.clear();

            log.info("Component pool manager "
                    + PAActiveObject.getBodyOnThis().getID() + " stopped");
        }
    }

    private synchronized boolean fillPools() {
        while (this.trackers.size() < this.nbTrackers) {
            this.trackers.add(this.newGenericSemanticTracker());
        }

        while (this.peers.size() < this.nbPeers) {
            this.peers.add(this.newGenericSemanticPeer());
        }

        while (this.publishProxies.size() < this.nbPublishProxies) {
            this.publishProxies.add(this.newGenericPublishProxy());
        }

        while (this.subscribeProxies.size() < this.nbSubscribeProxies) {
            this.subscribeProxies.add(this.newGenericSubscribeProxy());
        }

        while (this.putgetProxies.size() < this.nbPutGetProxies) {
            this.putgetProxies.add(this.newGenericPutGetProxy());
        }

        return true;
    }

    /**
     * Creates a new generic semantic tracker.
     * 
     * @return the new generic semantic tracker created.
     */
    protected synchronized SemanticTracker newGenericSemanticTracker() {
        return SemanticFactory.newGenericSemanticTracker(this.nodeProvider);
    }

    /**
     * Creates a new generic semantic peer.
     * 
     * @return the new generic semantic tracker peer.
     */
    protected synchronized SemanticPeer newGenericSemanticPeer() {
        return SemanticFactory.newGenericSemanticPeer(this.nodeProvider);
    }

    /**
     * Creates a new generic publish proxy.
     * 
     * @return the new generic publish proxy created.
     */
    protected synchronized PublishApi newGenericPublishProxy() {
        return ProxyFactory.newGenericPublishProxy(this.nodeProvider);
    }

    /**
     * Creates a new generic subscribe proxy.
     * 
     * @return the new generic subscribe proxy created.
     */
    protected synchronized SubscribeApi newGenericSubscribeProxy() {
        return ProxyFactory.newGenericSubscribeProxy(this.nodeProvider);
    }

    /**
     * Creates a new generic put/get proxy.
     * 
     * @return the new generic put/get proxy created.
     */
    protected synchronized PutGetApi newGenericPutGetProxy() {
        return ProxyFactory.newGenericPutGetProxy(this.nodeProvider);
    }

    /**
     * Returns a semantic tracker started and initialized with the specified
     * parameters.
     * 
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * @param networkName
     *            the network name managed by the tracker.
     * @return the semantic tracker started and initialized with the specified
     *         parameters
     * @throws IllegalStateException
     *             if the component pool manager is not started yet.
     */
    public SemanticTracker getTracker(DeploymentConfiguration deploymentConfiguration,
                                      String networkName)
            throws IllegalStateException {
        Preconditions.checkState(
                this.isStarted(),
                "EventCloud Component Pool Manager is not started yet");

        SemanticTracker tracker;

        if (this.trackers.size() > 0) {
            tracker = this.trackers.remove(0);

        } else {
            tracker = this.newGenericSemanticTracker();
        }

        SemanticFactory.initGenericSemanticTracker(
                tracker, deploymentConfiguration, networkName);

        return tracker;
    }

    /**
     * Returns a semantic peer started and initialized with the specified
     * parameters.
     * 
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * @param overlayProvider
     *            the overlay provider to use.
     * @return the semantic peer started and initialized with the specified
     *         parameters
     * @throws IllegalStateException
     *             if the component pool manager is not started yet.
     */
    public <T extends StructuredOverlay> SemanticPeer getPeer(DeploymentConfiguration deploymentConfiguration,
                                                              SerializableProvider<T> overlayProvider)
            throws IllegalStateException {
        Preconditions.checkState(
                this.isStarted(),
                "EventCloud Component Pool Manager is not started yet");

        SemanticPeer peer;

        if (this.peers.size() > 0) {
            peer = this.peers.remove(0);

        } else {
            peer = this.newGenericSemanticPeer();
        }

        SemanticFactory.initGenericSemanticPeer(
                peer, deploymentConfiguration, overlayProvider);

        return peer;
    }

    /**
     * Returns a publish proxy started and initialized with the specified
     * parameters.
     * 
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * @param registryUrl
     *            the EventClouds registry URL.
     * @param id
     *            the identifier that identify the EventCloud to work on.
     * @return the publish proxy started and initialized with the specified
     *         parameters
     * @throws IllegalStateException
     *             if the component pool manager is not started yet.
     */
    public PublishProxy getPublishProxy(DeploymentConfiguration deploymentConfiguration,
                                        String registryUrl, EventCloudId id)
            throws IllegalStateException, EventCloudIdNotManaged {
        Preconditions.checkState(
                this.isStarted(),
                "EventCloud Component Pool Manager is not started yet");

        PublishProxy publishProxy;

        if (this.publishProxies.size() > 0) {
            publishProxy = (PublishProxy) this.publishProxies.remove(0);

        } else {
            publishProxy = (PublishProxy) this.newGenericPublishProxy();
        }

        ProxyFactory.initGenericPublishProxy(
                publishProxy, deploymentConfiguration, registryUrl, id);

        return publishProxy;
    }

    /**
     * Returns a subscribe proxy started and initialized with the specified
     * parameters.
     * 
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * @param registryUrl
     *            the EventClouds registry URL.
     * @param id
     *            the identifier that identify the EventCloud to work on.
     * @param properties
     *            the ELA properties to set.
     * @return the subscribe proxy started and initialized with the specified
     *         parameters
     * @throws IllegalStateException
     *             if the component pool manager is not started yet.
     */
    public SubscribeProxy getSubscribeProxy(DeploymentConfiguration deploymentConfiguration,
                                            String registryUrl,
                                            EventCloudId id,
                                            AlterableElaProperty... properties)
            throws IllegalStateException, EventCloudIdNotManaged {
        Preconditions.checkState(
                this.isStarted(),
                "EventCloud Component Pool Manager is not started yet");

        SubscribeProxy subscribeProxy;

        if (this.subscribeProxies.size() > 0) {
            subscribeProxy = (SubscribeProxy) this.subscribeProxies.remove(0);

        } else {
            subscribeProxy = (SubscribeProxy) this.newGenericSubscribeProxy();
        }

        ProxyFactory.initGenericSubscribeProxy(
                subscribeProxy, deploymentConfiguration, registryUrl, id,
                properties);

        return subscribeProxy;
    }

    /**
     * Returns a put/get proxy started and initialized with the specified
     * parameters.
     * 
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * @param registryUrl
     *            the EventClouds registry URL.
     * @param id
     *            the identifier that identify the EventCloud to work on.
     * @return the put/get proxy started and initialized with the specified
     *         parameters
     * @throws IllegalStateException
     *             if the component pool manager is not started yet.
     */
    public PutGetProxy getPutGetProxy(DeploymentConfiguration deploymentConfiguration,
                                      String registryUrl, EventCloudId id)
            throws IllegalStateException, EventCloudIdNotManaged {
        Preconditions.checkState(
                this.isStarted(),
                "EventCloud Component Pool Manager is not started yet");

        PutGetProxy putgetProxy;

        if (this.putgetProxies.size() > 0) {
            putgetProxy = (PutGetProxy) this.putgetProxies.remove(0);

        } else {
            putgetProxy = (PutGetProxy) this.newGenericPutGetProxy();
        }

        ProxyFactory.initGenericPutGetProxy(
                putgetProxy, deploymentConfiguration, registryUrl, id);

        return putgetProxy;
    }

    /**
     * Releases the specified semantic trackers and re-injects them in the pool.
     * 
     * @param trackers
     *            the semantic trackers to release.
     * @throws IllegalStateException
     *             if the component pool manager is not started yet.
     */
    public void releaseSemanticTrackers(SemanticTracker... trackers)
            throws IllegalStateException {
        Preconditions.checkState(
                this.isStarted(),
                "EventCloud Component Pool Manager is not started yet");

        for (SemanticTracker tracker : trackers) {
            this.resetComponent((Interface) tracker);
            this.trackers.add(tracker);
        }
    }

    /**
     * Releases the specified semantic peers and re-injects them in the pool.
     * 
     * @param peers
     *            the semantic peers to release.
     * @throws IllegalStateException
     *             if the component pool manager is not started yet.
     */
    public void releaseSemanticPeers(SemanticPeer... peers)
            throws IllegalStateException {
        Preconditions.checkState(
                this.isStarted(),
                "EventCloud Component Pool Manager is not started yet");

        for (SemanticPeer peer : peers) {
            this.resetComponent((Interface) peer);
            this.peers.add(peer);
        }
    }

    /**
     * Releases the specified publish proxies and re-injects them in the pool.
     * 
     * @param publishProxies
     *            the publish proxies to release.
     * @throws IllegalStateException
     *             if the component pool manager is not started yet.
     */
    public void releasePublishProxies(PublishProxy... publishProxies)
            throws IllegalStateException {
        Preconditions.checkState(
                this.isStarted(),
                "EventCloud Component Pool Manager is not started yet");

        for (PublishApi publishProxy : publishProxies) {
            this.resetComponent((Interface) publishProxy);
            this.publishProxies.add(publishProxy);
        }
    }

    /**
     * Releases the specified subscribe proxies and re-injects them in the pool.
     * 
     * @param subscribeProxies
     *            the subscribe proxies to release.
     * @throws IllegalStateException
     *             if the component pool manager is not started yet.
     */
    public void releaseSubscribeProxies(SubscribeProxy... subscribeProxies)
            throws IllegalStateException {
        Preconditions.checkState(
                this.isStarted(),
                "EventCloud Component Pool Manager is not started yet");

        for (SubscribeApi subscribeProxy : subscribeProxies) {
            this.resetComponent((Interface) subscribeProxy);
            this.subscribeProxies.add(subscribeProxy);
        }
    }

    /**
     * Releases the specified put/get proxies and re-injects them in the pool.
     * 
     * @param putgetProxies
     *            the put/get proxies to release.
     * @throws IllegalStateException
     *             if the component pool manager is not started yet.
     */
    public void releasePutGetProxies(PutGetProxy... putgetProxies)
            throws IllegalStateException {
        Preconditions.checkState(
                this.isStarted(),
                "EventCloud Component Pool Manager is not started yet");

        for (PutGetApi putgetProxy : putgetProxies) {
            this.resetComponent((Interface) putgetProxy);
            this.putgetProxies.add(putgetProxy);
        }
    }

    private void resetComponent(Interface componentInterface) {
        Component component = componentInterface.getFcItfOwner();

        try {
            GCM.getLifeCycleController(component).stopFc();
            ((CommonAttributeController) GCM.getAttributeController(component)).resetAttributes();
        } catch (NoSuchInterfaceException e) {
            throw new IllegalStateException(e);
        } catch (IllegalLifeCycleException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Thread for the {@link ComponentPoolManager}.
     * 
     * @author bsauvan
     */
    private class ComponentPoolManagerThread extends Thread {
        private static final int SLEEP = 5000;

        private boolean run;

        /**
         * Creates a {@link ComponentPoolManagerThread}.
         */
        public ComponentPoolManagerThread() {
            this.run = true;
        }

        /**
         * Stops the thread.
         */
        public void stopThread() {
            this.run = false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            while (this.run) {
                ComponentPoolManager.this.fillPools();

                try {
                    Thread.sleep(ComponentPoolManagerThread.SLEEP);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
