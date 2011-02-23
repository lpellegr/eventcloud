package org.objectweb.proactive.extensions.p2p.structured.initializers;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.ProActiveRandom;
import org.objectweb.proactive.extensions.p2p.structured.api.TrackerFactory;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.StructuredP2PException;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayType;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.tracker.Tracker;

/**
 * Abstract network initializer used in order to initialize a new structured p2p
 * network for tests. A network initializer maintains reference to ALL peers
 * which have join the network associated to it in order to simplify some tests.
 * 
 * @author lpellegr
 * 
 * @version $Id: NetworkInitializer.java 5064 2010-09-01 14:21:24Z plaurent $
 */
public abstract class NetworkInitializer {

    private List<Peer> peers = new ArrayList<Peer>();

    private Tracker tracker;

    private OverlayType type;

    protected abstract Peer createPeer() throws ActiveObjectCreationException, NodeException;

    public void initializeNewNetwork(OverlayType type, int nbPeersToCreate) throws Exception {
        this.tracker = TrackerFactory.newActiveTracker(type);
        this.type = type;
        Peer peerCreated;

        for (int i = 0; i < nbPeersToCreate; i++) {
            peerCreated = this.createPeer();
            this.tracker.addOnNetwork(peerCreated);
            this.peers.add(peerCreated);
        }
    }

    public void clearNetwork() {
		for (Peer peer : this.peers) {
			try {
				peer.leave();
			} catch (StructuredP2PException e) {
				e.printStackTrace();
			}
		}

        this.peers.clear();
    }

    public List<Peer> getAllPeers() {
    	for (int i=0; i<this.peers.size(); i++) {
    		if (!this.checkAlertness(this.peers.get(i))) {
    			this.peers.remove(i);
    		}
    	}
    	
        return this.peers;
    }

    private boolean checkAlertness(Peer peer) {
    	try {
    		return peer.isActivated();
    	} catch(ProActiveRuntimeException e) {
    		return false;
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

    public Peer get(int index) {
        if (index < 0 || index >= this.peers.size()) {
        	System.out.println("size=" + this.peers.size());
            throw new IndexOutOfBoundsException();
        }
        if (this.checkAlertness(this.peers.get(index))) {
        	return this.peers.get(index);
        } else {
        	return null;
        }
    }

    public OverlayType getType() {
        return this.type;
    }

}
