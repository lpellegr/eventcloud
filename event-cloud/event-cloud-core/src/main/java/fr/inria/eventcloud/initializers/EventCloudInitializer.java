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
package fr.inria.eventcloud.initializers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.utils.SystemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.SemanticFactory;
import fr.inria.eventcloud.messages.request.can.ShutdownRequest;
import fr.inria.eventcloud.overlay.SemanticPeer;
import fr.inria.eventcloud.tracker.SemanticTracker;

/**
 * Initializes a space network on a local machine or by distributing the active
 * objects on several machines. A space network is represented by a CAN network.
 * <p>
 * TODO: add support for deployment with the Scheduler. I think we just have to
 * provide a new constructor with some parameters which are needed to use a
 * scheduler. Then, we have to update the {@link #createPeer()} and
 * {@link #createTracker()} methods according to the parameters which have been
 * initialized from the constructor.
 * 
 * @author lpellegr
 */
public class EventCloudInitializer {

    private static final Logger log =
            LoggerFactory.getLogger(EventCloudInitializer.class);

    private static final int INJECTION_THRESHOLD = 10;

    private SemanticTracker[] trackers;

    private final int nbPeers;

    private final int nbTrackers;

    private boolean running;

    /**
     * Creates a new EventCloudInitializer that will be initialized with the
     * specified {@code nbTrackers} and {@code nbPeers} when the method
     * {@link #setUp()} is called.
     * 
     * @param nbTrackers
     *            the default number of trackers to create.
     * @param nbPeers
     *            the default number of peers to create.
     */
    public EventCloudInitializer(int nbTrackers, int nbPeers) {
        P2PStructuredProperties.CAN_NB_DIMENSIONS.setValue((byte) 4);
        this.nbTrackers = nbTrackers;
        this.nbPeers = nbPeers;
        this.running = false;
        this.trackers = new SemanticTracker[this.nbTrackers];
    }

    public EventCloudInitializer(int nbPeers) {
        this(1, nbPeers);
    }

    public synchronized void setUp() {
        if (this.running) {
            throw new IllegalStateException(
                    "The network is already initialized and running");
        }

        this.running = true;

        // creates and initializes the trackers
        for (int i = 0; i < this.nbTrackers; i++) {
            this.trackers[i] = this.createTracker();
            if (i > 0) {
                this.trackers[i].join(this.trackers[i - 1]);
            }
        }

        this.injectPeers();
    }

    private void injectPeers() {
        if (this.nbPeers > INJECTION_THRESHOLD) {
            log.debug(
                    "Creates and injects {} peers on the network in parallel",
                    this.nbPeers);
            this.injectPeersInParallel();
        } else {
            log.debug(
                    "Creates and use sequential injection for the {} peer(s) to insert on the network",
                    this.nbPeers);
            Peer peerCreated;
            for (int i = 0; i < this.nbPeers; i++) {
                peerCreated = this.createPeer();
                try {
                    this.getRandomTracker().inject(peerCreated);
                } catch (NetworkAlreadyJoinedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void injectPeersInParallel() {
        ExecutorService threadsPool =
                Executors.newFixedThreadPool(SystemUtil.getOptimalNumberOfThreads());
        final CountDownLatch doneSignal = new CountDownLatch(this.nbPeers);

        for (int i = 0; i < this.nbPeers; i++) {
            threadsPool.execute(new Runnable() {
                public void run() {
                    try {
                        getRandomTracker().inject(createPeer());
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
        }
    }

    public synchronized void tearDown() {
        if (!this.running) {
            return;
        }

        this.running = false;

        try {
            this.selectPeer().send(new ShutdownRequest());
        } catch (DispatchException e) {
            e.printStackTrace();
        }

        // for (Peer peer : this.tracker.getPeers()) {
        // PAActiveObject.terminateActiveObject(peer, true);
        // }
        // PAActiveObject.terminateActiveObject(this.tracker, true);
        //
        // // TODO checks if termination of components works!
        // for (Peer peer : this.componentTracker.getPeers()) {
        // try {
        // Component owner = ((Interface) peer).getFcItfOwner();
        // GCMLifeCycleController lcc =
        // GCM.getGCMLifeCycleController(owner);
        // lcc.stopFc();
        // lcc.terminateGCMComponent();
        // } catch (IllegalLifeCycleException e) {
        // e.printStackTrace();
        // } catch (NoSuchInterfaceException e) {
        // e.printStackTrace();
        // }
        // }
        // try {
        // Component owner =
        // ((Interface) this.componentTracker).getFcItfOwner();
        // GCMLifeCycleController lcc =
        // GCM.getGCMLifeCycleController(owner);
        // lcc.stopFc();
        // lcc.terminateGCMComponent();
        // } catch (IllegalLifeCycleException e) {
        // e.printStackTrace();
        // } catch (NoSuchInterfaceException e) {
        // e.printStackTrace();
        // }
        // }
    }

    /**
     * Creates a new Peer.
     * 
     * @return the peer created.
     */
    protected SemanticPeer createPeer() {
        return SemanticFactory.newSemanticPeer();
    }

    /**
     * Creates a new Tracker active object.
     * 
     * @return the tracker active object created.
     */
    protected SemanticTracker createTracker() {
        return SemanticFactory.newSemanticTracker();
    }

    /**
     * Selects a peer according to some metrics (e.g. the network load).
     * 
     * TODO: implement the selection according to the metric.
     * 
     * @return a peer according to some metrics (e.g. the network load).
     */
    public SemanticPeer selectPeer() {
        return this.getRandomTracker().getRandomPeer();
    }

    public SemanticPeer getRandomPeer() {
        return this.getRandomTracker().getRandomPeer();
    }

    public SemanticTracker getRandomTracker() {
        return this.trackers[ProActiveRandom.nextInt(this.trackers.length)];
    }

}
