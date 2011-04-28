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

    public void initializeNewNetwork(int nbPeersToCreate) {
        super.initializeNewNetwork(OverlayType.CAN, nbPeersToCreate);
    }

    public Peer createActivePeer() {
        return PeerFactory.newActivePeer(new BasicCanOverlay());
    }

    public Peer createComponentPeer() {
        return PeerFactory.newComponentPeer(new BasicCanOverlay());
    }

}
