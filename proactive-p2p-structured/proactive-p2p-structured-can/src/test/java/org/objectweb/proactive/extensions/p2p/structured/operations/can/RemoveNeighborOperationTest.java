package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import java.util.UUID;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.api.operations.CANOperations;
import org.objectweb.proactive.extensions.p2p.structured.intializers.CANNetworkInitializer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayType;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

/**
 * Test cases for {@link RemoveNeighborOperation}.
 * 
 * @author Laurent Pellegrino
 */
public class RemoveNeighborOperationTest extends CANNetworkInitializer {

    @Before
    public void setUp() throws Exception {
        super.initializeNewNetwork(OverlayType.CAN, 2);
    }

    @Test
    public void testMessage() {
        Peer firstPeer = super.get(0);
        Peer secondPeer = super.get(1);

        UUID firstPeerID = firstPeer.getId();
        UUID secondPeerID = secondPeer.getId();

        Assert.assertTrue(CANOperations.hasNeighbor(firstPeer, secondPeerID));
        Assert.assertTrue(CANOperations.hasNeighbor(secondPeer, firstPeerID));

        CANOperations.removeNeighbor(firstPeer, secondPeerID);

        Assert.assertFalse(CANOperations.hasNeighbor(firstPeer, secondPeerID));
        Assert.assertTrue(CANOperations.hasNeighbor(secondPeer, firstPeerID));
    }

    @After
    public void tearDown() {
        super.clearNetwork();
    }

}
