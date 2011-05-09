package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import java.util.UUID;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.api.operations.CanOperations;
import org.objectweb.proactive.extensions.p2p.structured.initializers.CanNetworkInitializer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

/**
 * Test cases for {@link RemoveNeighborOperation}.
 * 
 * @author lpellegr
 */
public class RemoveNeighborOperationTest extends CanNetworkInitializer {

    @Before
    public void setUp() throws Exception {
        super.initializeNewNetwork(2);
    }

    @Test
    public void testMessage() {
        Peer firstPeer = super.get(0);
        Peer secondPeer = super.get(1);

        UUID firstPeerID = firstPeer.getId();
        UUID secondPeerID = secondPeer.getId();

        Assert.assertTrue(CanOperations.hasNeighbor(firstPeer, secondPeerID));
        Assert.assertTrue(CanOperations.hasNeighbor(secondPeer, firstPeerID));

        CanOperations.removeNeighbor(firstPeer, secondPeerID);

        Assert.assertFalse(CanOperations.hasNeighbor(firstPeer, secondPeerID));
        Assert.assertTrue(CanOperations.hasNeighbor(secondPeer, firstPeerID));
    }

    @Test
    public void testMessageComponent() {
        Peer firstPeer = super.getc(0);
        Peer secondPeer = super.getc(1);

        UUID firstPeerID = firstPeer.getId();
        UUID secondPeerID = secondPeer.getId();

        Assert.assertTrue(CanOperations.hasNeighbor(firstPeer, secondPeerID));
        Assert.assertTrue(CanOperations.hasNeighbor(secondPeer, firstPeerID));

        CanOperations.removeNeighbor(firstPeer, secondPeerID);

        Assert.assertFalse(CanOperations.hasNeighbor(firstPeer, secondPeerID));
        Assert.assertTrue(CanOperations.hasNeighbor(secondPeer, firstPeerID));
    }

    @After
    public void tearDown() {
//        super.clearNetwork();
    }

}
