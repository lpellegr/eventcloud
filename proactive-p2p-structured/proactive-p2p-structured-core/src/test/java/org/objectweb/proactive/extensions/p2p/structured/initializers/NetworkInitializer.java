package org.objectweb.proactive.extensions.p2p.structured.initializers;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.extensions.p2p.structured.api.TrackerFactory;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayType;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;

/**
 * Abstract network initializer used in order to initialize a new structured p2p
 * network for tests. A network initializer maintains reference to ALL peers
 * which have join the network associated to it in order to simplify some tests.
 * 
 * @author lpellegr
 */
public abstract class NetworkInitializer {

    private List<Peer> peers;

    private List<Peer> componentPeers;

    private Tracker tracker;

    private Tracker componentTracker;

    private OverlayType type;

    public NetworkInitializer() {
        this.peers = new ArrayList<Peer>();
        this.componentPeers = new ArrayList<Peer>();
    }

    protected abstract Peer createActivePeer();

    protected abstract Peer createComponentPeer();

    protected void initializeNewNetwork(OverlayType type, int nbPeersToCreate) {
        this.type = type;
        Peer peerCreated;

        this.tracker = TrackerFactory.newActiveTracker(type);

        for (int i = 0; i < nbPeersToCreate; i++) {
            peerCreated = this.createActivePeer();
            this.tracker.addOnNetwork(peerCreated);
            this.peers.add(peerCreated);
        }

        this.componentTracker = TrackerFactory.newComponentTracker(type);
        for (int i = 0; i < nbPeersToCreate; i++) {
            peerCreated = this.createComponentPeer();
            this.componentTracker.addOnNetwork(peerCreated);
            this.componentPeers.add(peerCreated);
        }
    }

    public void clearNetwork() {
//        for (Peer peer : this.peers) {
//            try {
//                peer.leave();
//            } catch (StructuredP2PException e) {
//                e.printStackTrace();
//            }
//        }
//
//        this.peers.clear();
//
//        for (Peer peer : this.componentPeers) {
//            try {
//                peer.leave();
//            } catch (StructuredP2PException e) {
//                e.printStackTrace();
//            }
//        }
//
//        this.componentPeers.clear();
    }

    public List<Peer> getAllPeers() {
        for (int i = 0; i < this.peers.size(); i++) {
            if (!this.checkAlertness(this.peers.get(i))) {
                this.peers.remove(i);
            }
        }

        return this.peers;
    }

    public List<Peer> getAllComponentPeers() {
        for (int i = 0; i < this.componentPeers.size(); i++) {
            if (!this.checkAlertness(this.componentPeers.get(i))) {
                this.componentPeers.remove(i);
            }
        }

        return this.componentPeers;
    }

    private boolean checkAlertness(Peer peer) {
        try {
            return peer.isActivated();
        } catch (ProActiveRuntimeException e) {
            return false;
        }
    }

    public Peer get(int index) {
        if (index < 0 || index >= this.peers.size()) {
            throw new IndexOutOfBoundsException();
        }
        if (this.checkAlertness(this.peers.get(index))) {
            return this.peers.get(index);
        } else {
            return null;
        }
    }

    public Peer getc(int index) {
        if (index < 0 || index >= this.componentPeers.size()) {
            throw new IndexOutOfBoundsException();
        }
        if (this.checkAlertness(this.componentPeers.get(index))) {
            return this.componentPeers.get(index);
        } else {
            return null;
        }
    }

    public Peer getRandomPeer() {
        if (this.peers.size() == 0) {
            return null;
        }

        int randomPeerIndex = ProActiveRandom.nextInt(this.peers.size());
        Peer randomPeer = this.peers.get(randomPeerIndex);

        if (this.checkAlertness(randomPeer)) {
            return randomPeer;
        } else {
            this.peers.remove(randomPeerIndex);
            return this.getRandomPeer();
        }
    }

    public Peer getRandomComponentPeer() {
        if (this.componentPeers.size() == 0) {
            return null;
        }

        int randomPeerIndex =
                ProActiveRandom.nextInt(this.componentPeers.size());
        Peer randomPeer = this.componentPeers.get(randomPeerIndex);

        if (this.checkAlertness(randomPeer)) {
            return randomPeer;
        } else {
            this.componentPeers.remove(randomPeerIndex);
            return this.getRandomComponentPeer();
        }
    }

    public Tracker getTracker() {
        return this.tracker;
    }

    public OverlayType getType() {
        return this.type;
    }

}
