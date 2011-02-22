package org.objectweb.proactive.extensions.p2p.structured.intializers;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.p2p.structured.api.PeerFactory;
import org.objectweb.proactive.extensions.p2p.structured.initializers.NetworkInitializer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.BasicCANOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

/**
 * Specific network initializer for CAN network.
 * 
 * @author Laurent Pellegrino
 */
public class CANNetworkInitializer extends NetworkInitializer {

    public Peer createPeer() throws ActiveObjectCreationException, NodeException {
        return PeerFactory.newActivePeer(new BasicCANOverlay());
    }

}
