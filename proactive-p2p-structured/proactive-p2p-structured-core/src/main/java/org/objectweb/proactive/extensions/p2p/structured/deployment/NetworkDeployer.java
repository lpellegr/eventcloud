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
package org.objectweb.proactive.extensions.p2p.structured.deployment;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;
import org.objectweb.proactive.extensions.p2p.structured.utils.Observable;
import org.objectweb.proactive.extensions.p2p.structured.utils.RandomUtils;
import org.objectweb.proactive.extensions.p2p.structured.utils.SystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A network deployer is an abstract class that provides all the necessary
 * functionalities to setup and to deploy a structured p2p network. Once the
 * network is undeployed it is possible to reuse the deployer for a new
 * deployment operation.
 * 
 * @author lpellegr
 */
public abstract class NetworkDeployer extends
        Observable<NetworkDeployerListener> implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger log =
            LoggerFactory.getLogger(NetworkDeployer.class);

    private static final int INJECTION_THRESHOLD = 10;

    private Tracker[] trackers;

    protected final NodeProvider nodeProvider;

    // this atomic reference is used to detect the deployer state and the
    // interleaving of some methods in multi-threaded environment.
    private AtomicReference<NetworkDeployerState> state;

    public NetworkDeployer() {
        this(new EmptyDeploymentConfiguration(), null);
    }

    public NetworkDeployer(DeploymentConfiguration configuration) {
        this(configuration, null);
    }

    public NetworkDeployer(DeploymentConfiguration configuration,
            NodeProvider nodeProvider) {
        this.state =
                new AtomicReference<NetworkDeployerState>(
                        NetworkDeployerState.STANDBY);
        this.nodeProvider = nodeProvider;

        configuration.configure();
    }

    public void deploy(int nbPeers) {
        this.deploy(1, nbPeers);
    }

    public void deploy(int nbTrackers, int nbPeers) {
        if (nbTrackers < 1) {
            throw new IllegalArgumentException(
                    "At least one tracker must be deployed");
        }

        if (!this.state.compareAndSet(
                NetworkDeployerState.STANDBY, NetworkDeployerState.DEPLOYING)) {
            switch (this.state.get()) {
                case DEPLOYING:
                    throw new IllegalStateException(
                            "A call to deploy is already being handled");
                case DEPLOYED:
                    throw new IllegalStateException(
                            "Once the network is deployed you can only undeploy it");
                case UNDEPLOYING:
                    throw new IllegalStateException(
                            "A call to undeploy is being handled");
            }
        }

        this.notifyDeploymentStarted();

        this.deployTrackers(nbTrackers);
        this.injectPeers(nbPeers);

        this.state.set(NetworkDeployerState.DEPLOYED);

        this.notifyDeploymentEnded();
    }

    private void deployTrackers(int nbTrackers) {
        this.notifyDeployingTrackers();

        this.trackers = new Tracker[nbTrackers];

        String networkName = UUID.randomUUID().toString();
        for (int i = 0; i < nbTrackers; i++) {
            this.trackers[i] = this.createTracker(networkName);
            if (i > 0) {
                this.trackers[i].join(this.trackers[i - 1]);
            }
        }

        this.notifyTrackersDeployed();
    }

    private void injectPeers(int nbPeers) {
        this.notifyInjectingPeers();

        if (nbPeers > INJECTION_THRESHOLD) {
            log.debug(
                    "Creates and injects {} peers on the network in parallel",
                    nbPeers);
            this.injectPeersInParallel(nbPeers);
        } else {
            log.debug(
                    "Creates and use sequential injection for the {} peer(s) to insert on the network",
                    nbPeers);
            Peer peerCreated;
            for (int i = 0; i < nbPeers; i++) {
                peerCreated = this.createPeer();
                try {
                    this.getRandomTracker().inject(peerCreated);
                } catch (NetworkAlreadyJoinedException e) {
                    e.printStackTrace();
                }
            }
        }

        this.notifyPeersInjected();
    }

    private void injectPeersInParallel(int nbPeers) {
        ExecutorService threadsPool =
                Executors.newFixedThreadPool(SystemUtil.getOptimalNumberOfThreads());
        final CountDownLatch doneSignal = new CountDownLatch(nbPeers);

        for (int i = 0; i < nbPeers; i++) {
            threadsPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        NetworkDeployer.this.getRandomTracker().inject(
                                NetworkDeployer.this.createPeer());
                    } catch (NetworkAlreadyJoinedException e) {
                        e.printStackTrace();
                    } finally {
                        doneSignal.countDown();
                    }
                }
            });
        }

        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            threadsPool.shutdownNow();
        }
    }

    public void undeploy() {
        if (!this.state.compareAndSet(
                NetworkDeployerState.DEPLOYED, NetworkDeployerState.UNDEPLOYING)) {
            switch (this.state.get()) {
                case STANDBY:
                    throw new IllegalStateException(
                            "It is impossible to undeploy a network which has not been deployed");
                case DEPLOYING:
                    throw new IllegalStateException(
                            "A call to deploy is being handled");
                case UNDEPLOYING:
                    throw new IllegalStateException(
                            "A call to undeploy is already being handled");
            }
        }

        this.notifyUndeploymentStarted();

        this.internalUndeploy();
        this.reset();

        this.state.set(NetworkDeployerState.STANDBY);
        this.notifyUndeploymentEnded();
    }

    protected void internalUndeploy() {
        // to be overridden if necessary
    }

    private void reset() {
        this.trackers = null;
    }

    /**
     * Creates a new {@link Peer} and deploy it to by using the
     * {@code nodeProvider}. If the {@link NodeProvider} is {@code null}, the
     * peer will be deployed on the local machine.
     * 
     * @return the new peer created.
     */
    protected abstract Peer createPeer();

    /**
     * Creates a new {@link Tracker} and deploy it to by using the
     * {@code nodeProvider}. If the {@link NodeProvider} is {@code null}, the
     * tracker will be deployed on the local machine.
     * 
     * @param networkName
     *            the network name managed by the tracker that is created
     * 
     * @return the new tracker created.
     */
    protected abstract Tracker createTracker(String networkName);

    /**
     * Returns the peer associated to the specified index from the list of peers
     * managed by a tracker.
     * 
     * @param index
     *            the peer index.
     * 
     * @return the peer associated to the specified index from the list of peers
     *         managed by a tracker.
     */
    public Peer getPeer(int index) {
        return this.getRandomTracker().getPeer(index);
    }

    /**
     * Returns a peer randomly selected from the network.
     * 
     * @return a peer randomly selected from the network.
     */
    public Peer getRandomPeer() {
        return this.getRandomTracker().getRandomPeer();
    }

    /**
     * Returns a tracker randomly selected from the network.
     * 
     * @return a tracker randomly selected from the network.
     */
    public Tracker getRandomTracker() {
        return this.trackers[RandomUtils.nextInt(this.trackers.length)];
    }

    /**
     * Returns the trackers created by the deployer.
     * 
     * @return the trackers created by the deployer.
     */
    public Tracker[] getTrackers() {
        return this.trackers;
    }

    /**
     * Returns the network deployer state.
     * 
     * @return the network deployer state.
     */
    public NetworkDeployerState getState() {
        return this.state.get();
    }

    /**
     * Returns the instance of the {@link NodeProvider} that is used to deploy
     * the active objects. Or {@code null} if no {@link NodeProvider} is used.
     * 
     * @return returns the instance of the {@link NodeProvider} that is used to
     *         deploy the active objects. Or {@code null} if no
     *         {@link NodeProvider} is used.
     */
    public NodeProvider getNodeProvider() {
        return this.nodeProvider;
    }

    /*
     * Methods for notifying the observers which have been registered
     */
    private void notifyDeploymentStarted() {
        super.notify(new NotificationAction<NetworkDeployerListener>() {
            @Override
            public void execute(NetworkDeployerListener observer) {
                observer.deploymentStarted();
            }
        });
    }

    private void notifyDeployingTrackers() {
        super.notify(new NotificationAction<NetworkDeployerListener>() {
            @Override
            public void execute(NetworkDeployerListener observer) {
                observer.deployingTrackers();
            }
        });
    }

    private void notifyTrackersDeployed() {
        super.notify(new NotificationAction<NetworkDeployerListener>() {
            @Override
            public void execute(NetworkDeployerListener observer) {
                observer.trackersDeployed();
            }
        });
    }

    private void notifyInjectingPeers() {
        super.notify(new NotificationAction<NetworkDeployerListener>() {
            @Override
            public void execute(NetworkDeployerListener observer) {
                observer.injectingPeers();
            }
        });
    }

    private void notifyPeersInjected() {
        super.notify(new NotificationAction<NetworkDeployerListener>() {
            @Override
            public void execute(NetworkDeployerListener observer) {
                observer.peersInjected();
            }
        });
    }

    private void notifyDeploymentEnded() {
        super.notify(new NotificationAction<NetworkDeployerListener>() {
            @Override
            public void execute(NetworkDeployerListener observer) {
                observer.deploymentEnded();
            }
        });
    }

    private void notifyUndeploymentStarted() {
        super.notify(new NotificationAction<NetworkDeployerListener>() {
            @Override
            public void execute(NetworkDeployerListener observer) {
                observer.undeploymentStarted();
            }
        });
    }

    private void notifyUndeploymentEnded() {
        super.notify(new NotificationAction<NetworkDeployerListener>() {
            @Override
            public void execute(NetworkDeployerListener observer) {
                observer.undeploymentEnded();
            }
        });
    }

}
