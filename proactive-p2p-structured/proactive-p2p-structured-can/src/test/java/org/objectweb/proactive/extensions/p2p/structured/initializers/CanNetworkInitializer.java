package org.objectweb.proactive.extensions.p2p.structured.initializers;

import org.objectweb.proactive.extensions.p2p.structured.api.PeerFactory;
import org.objectweb.proactive.extensions.p2p.structured.initializers.NetworkInitializer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;

/**
 * Network initializer that provides concrete implementation of
 * {@link #createActivePeer()} and {@link #createComponentPeer()} for a
 * Content-Adressable Network.
 * 
 * @author lpellegr
 */
public class CanNetworkInitializer extends NetworkInitializer {

    public Peer createActivePeer() {
        return PeerFactory.newActivePeer(new CanOverlay());
    }

    public Peer createComponentPeer() {
        return PeerFactory.newComponentPeer(new CanOverlay());
    }
    
}
