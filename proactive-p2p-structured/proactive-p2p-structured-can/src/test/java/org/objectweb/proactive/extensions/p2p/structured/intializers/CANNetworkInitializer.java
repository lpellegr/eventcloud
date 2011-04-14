package org.objectweb.proactive.extensions.p2p.structured.intializers;

import org.objectweb.proactive.extensions.p2p.structured.api.PeerFactory;
import org.objectweb.proactive.extensions.p2p.structured.initializers.NetworkInitializer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.BasicCanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayType;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

/**
 * Specific network initializer for CAN network.
 * 
 * @author lpellegr
 */
public class CANNetworkInitializer extends NetworkInitializer {

    public Peer createPeer() {
        return PeerFactory.newActivePeer(new BasicCanOverlay());
    }

    public void initializeNewNetwork(int nbPeersToCreate) {
        super.initializeNewNetwork(OverlayType.CAN, nbPeersToCreate);
    }
    
}
