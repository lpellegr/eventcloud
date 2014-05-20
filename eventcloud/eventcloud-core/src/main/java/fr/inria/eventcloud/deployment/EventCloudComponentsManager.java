/**
 * Copyright (c) 20112013 INRIA.
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

import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.p2p.structured.deployment.DeploymentConfiguration;
import org.objectweb.proactive.extensions.p2p.structured.deployment.NodeProvider;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;
import org.objectweb.proactive.extensions.p2p.structured.utils.ComponentUtils;
import org.objectweb.proactive.multiactivity.MultiActiveService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;

import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.PublishApi;
import fr.inria.eventcloud.api.PutGetApi;
import fr.inria.eventcloud.api.SubscribeApi;
import fr.inria.eventcloud.api.properties.AlterableElaProperty;
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
 * @author lpellegr
 */
@DefineGroups({@Group(name = "parallel", selfCompatible = true)})
public class EventCloudComponentsManager implements InitActive, RunActive,
        Serializable {

    private static final long serialVersionUID = 160L;

    private static final Logger LOG =
            LoggerFactory.getLogger(EventCloudComponentsManager.class);

    private boolean running;

    protected NodeProvider nodeProvider;

    private int nbTrackers;

    public int nbPeers;

    private int nbPublishProxies;

    private int nbSubscribeProxies;

    private int nbPutGetProxies;

    private TrackerComponentPool trackers;

    private PeerComponentPool peers;

    private PublishProxyComponentPool publishProxies;

    private SubscribeProxyComponentPool subscribeProxies;

    private PutGetProxyComponentPool putgetProxies;

    /**
     * Empty constructor required by ProActive.
     */
    public EventCloudComponentsManager() {
    }

    public EventCloudComponentsManager(NodeProvider nodeProvider,
            int nbTrackers, int nbPeers, int nbPublishProxies,
            int nbSubscribeProxies, int nbPutGetProxies) {

        this.nodeProvider = nodeProvider;

        this.nbTrackers = nbTrackers;
        this.nbPeers = nbPeers;
        this.nbPublishProxies = nbPublishProxies;
        this.nbSubscribeProxies = nbSubscribeProxies;
        this.nbPutGetProxies = nbPutGetProxies;

        this.running = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initActivity(Body body) {
        this.trackers =
                new TrackerComponentPool(new Supplier<SemanticTracker>() {
                    @Override
                    public SemanticTracker get() {
                        return EventCloudComponentsManager.this.newGenericSemanticTracker();
                    }
                });

        this.peers = new PeerComponentPool(new Supplier<SemanticPeer>() {
            @Override
            public SemanticPeer get() {
                return EventCloudComponentsManager.this.newGenericSemanticPeer();
            }
        });

        this.publishProxies =
                new PublishProxyComponentPool(new Supplier<PublishProxy>() {
                    @Override
                    public PublishProxy get() {
                        return (PublishProxy) EventCloudComponentsManager.this.newGenericPublishProxy();
                    }
                });

        this.subscribeProxies =
                new SubscribeProxyComponentPool(new Supplier<SubscribeProxy>() {
                    @Override
                    public SubscribeProxy get() {
                        return (SubscribeProxy) EventCloudComponentsManager.this.newGenericSubscribeProxy();
                    }
                });

        this.putgetProxies =
                new PutGetProxyComponentPool(new Supplier<PutGetProxy>() {
                    @Override
                    public PutGetProxy get() {
                        return (PutGetProxy) EventCloudComponentsManager.this.newGenericPutGetProxy();
                    }
                });
    }

    private void fillUpPools() {
        this.trackers.allocate(this.nbTrackers);
        this.peers.allocate(this.nbPeers);
        this.publishProxies.allocate(this.nbPublishProxies);
        this.subscribeProxies.allocate(this.nbSubscribeProxies);
        this.putgetProxies.allocate(this.nbPutGetProxies);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void runActivity(Body body) {
        MultiActiveService service = new MultiActiveService(body);
        service.multiActiveServing(Runtime.getRuntime().availableProcessors());
    }

    /**
     * Indicates whether the component pool manager is started or not.
     * 
     * @return true if the component pool manager is started, false otherwise.
     */
    public boolean isRunning() {
        return this.running;
    }

    /**
     * Starts the component pool manager.
     */
    public void start() throws IllegalStateException {
        Preconditions.checkState(!this.isRunning(), this.getClass()
                .getSimpleName()
                + " has already been started");

        this.fillUpPools();
        this.running = true;

        LOG.info(
                "Component pool manager {} started",
                PAActiveObject.getBodyOnThis().getID());
    }

    /**
     * Stops the component pool manager.
     */
    public void stop() throws IllegalStateException {
        Preconditions.checkState(this.isRunning(), this.getClass()
                .getSimpleName()
                + " not yet started");

        if (this.isRunning()) {
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

            this.running = false;

            LOG.info(
                    "Component pool manager {} stopped",
                    PAActiveObject.getBodyOnThis().getID());
        }
    }

    /**
     * Creates a new generic semantic tracker.
     * 
     * @return the new generic semantic tracker created.
     */
    protected SemanticTracker newGenericSemanticTracker() {
        return SemanticFactory.newGenericSemanticTracker(this.nodeProvider);
    }

    /**
     * Creates a new generic semantic peer.
     * 
     * @return the new generic semantic tracker peer.
     */
    protected SemanticPeer newGenericSemanticPeer() {
        return SemanticFactory.newGenericSemanticPeer(this.nodeProvider);
    }

    /**
     * Creates a new generic publish proxy.
     * 
     * @return the new generic publish proxy created.
     */
    protected PublishApi newGenericPublishProxy() {
        return ProxyFactory.newGenericPublishProxy(this.nodeProvider);
    }

    /**
     * Creates a new generic subscribe proxy.
     * 
     * @return the new generic subscribe proxy created.
     */
    protected SubscribeApi newGenericSubscribeProxy() {
        return ProxyFactory.newGenericSubscribeProxy(this.nodeProvider);
    }

    /**
     * Creates a new generic put/get proxy.
     * 
     * @return the new generic put/get proxy created.
     */
    protected PutGetApi newGenericPutGetProxy() {
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
    @MemberOf("parallel")
    public SemanticTracker getTracker(DeploymentConfiguration deploymentConfiguration,
                                      String networkName)
            throws IllegalStateException {
        assert this.isRunning();
        return this.trackers.borrow(deploymentConfiguration, networkName);
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
    @MemberOf("parallel")
    public <T extends StructuredOverlay> SemanticPeer getPeer(DeploymentConfiguration deploymentConfiguration,
                                                              SerializableProvider<T> overlayProvider)
            throws IllegalStateException {
        assert this.isRunning();
        return this.peers.borrow(deploymentConfiguration, overlayProvider);
    }

    /**
     * Returns a publish proxy started and initialized with the specified
     * parameters.
     * 
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * @param registryURL
     *            the EventClouds registry URL.
     * @param eventCloudId
     *            the identifier that identify the EventCloud to work on.
     * @return the publish proxy started and initialized with the specified
     *         parameters
     * @throws EventCloudIdNotManaged
     */
    @MemberOf("parallel")
    public PublishProxy getPublishProxy(DeploymentConfiguration deploymentConfiguration,
                                        String registryURL,
                                        EventCloudId eventCloudId)
            throws EventCloudIdNotManaged {
        assert this.isRunning();
        return this.publishProxies.borrow(
                deploymentConfiguration, registryURL, eventCloudId);
    }

    /**
     * Returns a subscribe proxy started and initialized with the specified
     * parameters.
     * 
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * @param registryURL
     *            the EventClouds registry URL.
     * @param eventCloudId
     *            the identifier that identify the EventCloud to work on.
     * @param properties
     *            the ELA properties to set.
     * @return the subscribe proxy started and initialized with the specified
     *         parameters
     * @throws EventCloudIdNotManaged
     */
    @MemberOf("parallel")
    public SubscribeProxy getSubscribeProxy(DeploymentConfiguration deploymentConfiguration,
                                            String registryURL,
                                            EventCloudId eventCloudId,
                                            AlterableElaProperty... properties)
            throws EventCloudIdNotManaged {
        assert this.isRunning();
        return this.subscribeProxies.borrow(
                deploymentConfiguration, registryURL, eventCloudId);
    }

    /**
     * Returns a put/get proxy started and initialized with the specified
     * parameters.
     * 
     * @param deploymentConfiguration
     *            the deployment configuration to use.
     * @param registryURL
     *            the EventClouds registry URL.
     * @param eventCloudId
     *            the identifier that identify the EventCloud to work on.
     * @return the put/get proxy started and initialized with the specified
     *         parameters
     * @throws EventCloudIdNotManaged
     */
    @MemberOf("parallel")
    public PutGetProxy getPutGetProxy(DeploymentConfiguration deploymentConfiguration,
                                      String registryURL,
                                      EventCloudId eventCloudId)
            throws EventCloudIdNotManaged {
        assert this.isRunning();
        return this.putgetProxies.borrow(
                deploymentConfiguration, registryURL, eventCloudId);
    }

    public boolean isPeerComponentPoolEmpty() {
        return this.peers.isEmpty();
    }

    /**
     * Releases the specified semantic trackers and reinjects them in the pool.
     * 
     * @param trackers
     *            the semantic trackers to release.
     * @throws IllegalStateException
     *             if the component pool manager is not started yet.
     */
    @MemberOf("parallel")
    public void releaseSemanticTrackers(Iterable<Tracker> trackers) {
        assert this.isRunning();

        for (Tracker tracker : trackers) {
            this.trackers.release((SemanticTracker) tracker);
        }
    }

    /**
     * Releases the specified semantic peers and reinjects them in the pool.
     * 
     * @param peers
     *            the semantic peers to release.
     * @throws IllegalStateException
     *             if the component pool manager is not started yet.
     */
    @MemberOf("parallel")
    public void releaseSemanticPeers(Iterable<Peer> peers) {
        assert this.isRunning();

        for (Peer peer : peers) {
            this.peers.release((SemanticPeer) peer);
        }
    }

    /**
     * Releases the specified publish proxies and reinjects them in the pool.
     * 
     * @param publishProxies
     *            the publish proxies to release.
     * @throws IllegalStateException
     *             if the component pool manager is not started yet.
     */
    @MemberOf("parallel")
    public void releasePublishProxies(Iterable<PublishProxy> publishProxies)
            throws IllegalStateException {
        assert this.isRunning();

        for (PublishProxy publishProxy : publishProxies) {
            this.publishProxies.release(publishProxy);
        }
    }

    /**
     * Releases the specified subscribe proxies and reinjects them in the pool.
     * 
     * @param subscribeProxies
     *            the subscribe proxies to release.
     * @throws IllegalStateException
     *             if the component pool manager is not started yet.
     */
    @MemberOf("parallel")
    public void releaseSubscribeProxies(Iterable<SubscribeProxy> subscribeProxies)
            throws IllegalStateException {
        assert this.isRunning();

        for (SubscribeProxy subscribeProxy : subscribeProxies) {
            this.subscribeProxies.release(subscribeProxy);
        }
    }

    /**
     * Releases the specified put/get proxies and reinjects them in the pool.
     * 
     * @param putgetProxies
     *            the put/get proxies to release.
     * @throws IllegalStateException
     *             if the component pool manager is not started yet.
     */
    @MemberOf("parallel")
    public void releasePutGetProxies(Iterable<PutGetProxy> putgetProxies)
            throws IllegalStateException {
        assert this.isRunning();

        for (PutGetProxy putgetProxy : putgetProxies) {
            this.putgetProxies.release(putgetProxy);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("trackers", this.trackers)
                .add("peers", this.peers)
                .add("publishProxies", this.publishProxies)
                .add("subscribeProxies", this.subscribeProxies)
                .add("putgetProxies", this.putgetProxies)
                .toString();
    }

    private static class TrackerComponentPool extends
            ComponentPool<SemanticTracker> {

        private static final long serialVersionUID = 160L;

        public TrackerComponentPool(Supplier<? extends SemanticTracker> supplier) {
            super(supplier);
        }

        public SemanticTracker borrow(DeploymentConfiguration deploymentConfiguration,
                                      String networkName) {
            SemanticTracker resource = super.pool.borrow();

            SemanticFactory.initGenericSemanticTracker(
                    resource, deploymentConfiguration, networkName);

            return resource;
        }

    }

    private static class PeerComponentPool extends ComponentPool<SemanticPeer> {

        private static final long serialVersionUID = 160L;

        public PeerComponentPool(Supplier<? extends SemanticPeer> supplier) {
            super(supplier);
        }

        public SemanticPeer borrow(DeploymentConfiguration deploymentConfiguration,
                                   SerializableProvider<? extends StructuredOverlay> overlayProvider) {
            SemanticPeer resource = super.pool.borrow();

            SemanticFactory.initGenericSemanticPeer(
                    resource, deploymentConfiguration, overlayProvider);
            return resource;
        }

    }

    private static class PublishProxyComponentPool extends
            ComponentPool<PublishProxy> {

        private static final long serialVersionUID = 160L;

        public PublishProxyComponentPool(
                Supplier<? extends PublishProxy> supplier) {
            super(supplier);
        }

        public PublishProxy borrow(DeploymentConfiguration deploymentConfiguration,
                                   String registryURL, EventCloudId eventCloudId)
                throws EventCloudIdNotManaged {
            PublishProxy resource = super.pool.borrow();

            ProxyFactory.initGenericPublishProxy(
                    resource, deploymentConfiguration, registryURL,
                    eventCloudId);

            return resource;
        }

    }

    private static class SubscribeProxyComponentPool extends
            ComponentPool<SubscribeProxy> {

        private static final long serialVersionUID = 160L;

        public SubscribeProxyComponentPool(
                Supplier<? extends SubscribeProxy> supplier) {
            super(supplier);
        }

        public SubscribeProxy borrow(DeploymentConfiguration deploymentConfiguration,
                                     String registryURL,
                                     EventCloudId eventCloudId)
                throws EventCloudIdNotManaged {
            SubscribeProxy resource = super.pool.borrow();

            ProxyFactory.initGenericSubscribeProxy(
                    resource, deploymentConfiguration, registryURL,
                    eventCloudId);

            return resource;
        }

    }

    private static class PutGetProxyComponentPool extends
            ComponentPool<PutGetProxy> {

        private static final long serialVersionUID = 160L;

        public PutGetProxyComponentPool(Supplier<? extends PutGetProxy> supplier) {
            super(supplier);
        }

        public PutGetProxy borrow(DeploymentConfiguration deploymentConfiguration,
                                  String registryURL, EventCloudId eventCloudId)
                throws EventCloudIdNotManaged {
            PutGetProxy resource = super.pool.borrow();

            ProxyFactory.initGenericPutGetProxy(
                    resource, deploymentConfiguration, registryURL,
                    eventCloudId);

            return resource;
        }

    }

}
