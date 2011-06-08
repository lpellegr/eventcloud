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
package org.objectweb.proactive.extensions.p2p.structured.initializers;

import java.util.List;

import org.etsi.uri.gcm.api.control.GCMLifeCycleController;
import org.etsi.uri.gcm.util.GCM;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.p2p.structured.api.TrackerFactory;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;

/**
 * A network initializer is used to setup a structured p2p network. It maintains
 * reference to a tracker that serves as entry-point.
 * 
 * @author lpellegr
 */
public abstract class NetworkInitializer {

    private Tracker tracker;

    private Tracker componentTracker;

    private boolean running;

    /**
     * Creates a new NetworkInitializer that will be initialized when the method
     * {@link #setUp()} is called.
     */
    public NetworkInitializer() {
        this.running = false;
    }

    public synchronized void setUp(int nbPeers) {
        if (this.running) {
            throw new IllegalStateException(
                    "The network is already initialized and running");
        }

        this.running = true;

        this.tracker = this.createActiveTracker();
        this.tracker.setProbabilityToStorePeer(1);

        Peer peerCreated;
        for (int i = 0; i < nbPeers; i++) {
            peerCreated = this.createActivePeer();
            try {
                this.tracker.inject(peerCreated);
            } catch (NetworkAlreadyJoinedException e) {
                e.printStackTrace();
            }
        }

        this.componentTracker = this.createComponentTracker();
        this.componentTracker.setProbabilityToStorePeer(1);

        for (int i = 0; i < nbPeers; i++) {
            peerCreated = this.createComponentPeer();
            try {
                this.componentTracker.inject(peerCreated);
            } catch (NetworkAlreadyJoinedException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void tearDown() {
        if (!this.running) {
            return;
        }

        this.running = false;

        if (this.tracker != null && this.componentTracker != null) {
            for (Peer peer : this.tracker.getPeers()) {
                PAActiveObject.terminateActiveObject(peer, true);
            }
            PAActiveObject.terminateActiveObject(this.tracker, true);

            // TODO checks if termination of components works!
            for (Peer peer : this.componentTracker.getPeers()) {
                try {
                    Component owner = ((Interface) peer).getFcItfOwner();
                    GCMLifeCycleController lcc =
                            GCM.getGCMLifeCycleController(owner);
                    lcc.stopFc();
                    lcc.terminateGCMComponent();
                } catch (IllegalLifeCycleException e) {
                    e.printStackTrace();
                } catch (NoSuchInterfaceException e) {
                    e.printStackTrace();
                }
            }
            try {
                Component owner =
                        ((Interface) this.componentTracker).getFcItfOwner();
                GCMLifeCycleController lcc =
                        GCM.getGCMLifeCycleController(owner);
                lcc.stopFc();
                lcc.terminateGCMComponent();
            } catch (IllegalLifeCycleException e) {
                e.printStackTrace();
            } catch (NoSuchInterfaceException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Creates a new Peer active object.
     * 
     * @return the peer active object created.
     */
    protected abstract Peer createActivePeer();

    /**
     * Creates a new Peer using the ProActive GCM components.
     * 
     * @return the component that have been created.
     */
    protected abstract Peer createComponentPeer();

    /**
     * Creates a new Tracker active object.
     * 
     * @return the tracker active object created.
     */
    protected Tracker createActiveTracker() {
        return TrackerFactory.newActiveTracker();
    }

    /**
     * Creates a new Tracker using the ProActive GCM components.
     * 
     * @return the component that have been created.
     */
    protected Tracker createComponentTracker() {
        return TrackerFactory.newComponentTracker();
    }

    public Peer get(int index) {
        return this.tracker.getPeer(index);
    }

    public Peer getc(int index) {
        return this.componentTracker.getPeer(index);
    }

    public Peer selectPeer() {
        return this.tracker.getRandomPeer();
    }

    public Peer selectComponentPeer() {
        return this.componentTracker.getRandomPeer();
    }

    public List<Peer> getPeers() {
        return this.tracker.getPeers();
    }

    public List<Peer> getComponentPeers() {
        return this.componentTracker.getPeers();
    }

}
