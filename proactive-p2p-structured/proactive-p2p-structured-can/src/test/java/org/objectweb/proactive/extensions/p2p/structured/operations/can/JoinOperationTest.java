package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.api.PeerFactory;
import org.objectweb.proactive.extensions.p2p.structured.api.operations.CanOperations;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.overlay.BasicCANOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCanOverlay;

/**
 * Test cases for the Join operation in {@link AbstractCanOverlay}.
 * 
 * @author Alexandre Trovato
 * @author Fanny Kilanga
 * @author lpellegr
 */
public class JoinOperationTest {

    private static Peer firstPeer;

    private static Peer secondPeer;

    private static Peer thirdPeer;

    private static Peer fourthPeer;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        firstPeer = PeerFactory.newActivePeer(new BasicCANOverlay());
        secondPeer = PeerFactory.newActivePeer(new BasicCANOverlay());
        thirdPeer = PeerFactory.newActivePeer(new BasicCANOverlay());
        fourthPeer = PeerFactory.newActivePeer(new BasicCANOverlay());
        
        firstPeer.create();
    }

    @Test
    public void testSecondPeer() {
        try {
            Assert.assertTrue(secondPeer.join(JoinOperationTest.firstPeer));
        } catch (NetworkAlreadyJoinedException e) {
            e.printStackTrace();
        }

        // Check that the peers are neighbors
        Assert.assertTrue(CanOperations.hasNeighbor(firstPeer, secondPeer.getId()));
        Assert.assertTrue(CanOperations.hasNeighbor(secondPeer, firstPeer.getId()));

        // Check if a peer has itself in it neighbors list
        Assert.assertFalse(CanOperations.hasNeighbor(firstPeer, firstPeer.getId()));
        Assert.assertFalse(CanOperations.hasNeighbor(secondPeer, secondPeer.getId()));

        // Check if the zone of the peers are bordered
        Assert.assertNotSame(
                CanOperations.getIdAndZoneResponseOperation(firstPeer)
                    .getPeerZone().neighbors(
                            CanOperations.getIdAndZoneResponseOperation(secondPeer).getPeerZone()), -1);
    }

    @Test
    public void testThirdPeer() {
        try {
            Assert.assertTrue(JoinOperationTest.thirdPeer.join(JoinOperationTest.secondPeer));
        } catch (NetworkAlreadyJoinedException e) {
            e.printStackTrace();
        }

        // Check that the peers are neighbors
        Assert.assertTrue(CanOperations.hasNeighbor(firstPeer, thirdPeer.getId()));
        Assert.assertTrue(CanOperations.hasNeighbor(secondPeer, thirdPeer.getId()));
        Assert.assertTrue(CanOperations.hasNeighbor(thirdPeer, firstPeer.getId()));
        Assert.assertTrue(CanOperations.hasNeighbor(thirdPeer, secondPeer.getId()));

        // Check if a peer has itself in it neighbors list
        Assert.assertFalse(CanOperations.hasNeighbor(thirdPeer, thirdPeer.getId()));

        // Check if the zone of the peers are bordered
        Assert.assertNotSame(
                CanOperations.getIdAndZoneResponseOperation(firstPeer)
                    .getPeerZone().neighbors(
                        CanOperations.getIdAndZoneResponseOperation(thirdPeer).getPeerZone()), -1);
        Assert.assertNotSame(
                CanOperations.getIdAndZoneResponseOperation(secondPeer)
                    .getPeerZone().neighbors(
                        CanOperations.getIdAndZoneResponseOperation(thirdPeer).getPeerZone()), -1);
    }

    @Test
    public void testFourthPeer() {
        try {
            Assert.assertTrue(JoinOperationTest.fourthPeer.join(JoinOperationTest.thirdPeer));
        } catch (NetworkAlreadyJoinedException e) {
            e.printStackTrace();
        }

        // Check that the peers are neighbors
        Assert.assertTrue(CanOperations.hasNeighbor(secondPeer, fourthPeer.getId()));
        Assert.assertTrue(CanOperations.hasNeighbor(thirdPeer, fourthPeer.getId()));
        Assert.assertTrue(CanOperations.hasNeighbor(fourthPeer, secondPeer.getId()));
        Assert.assertTrue(CanOperations.hasNeighbor(fourthPeer, thirdPeer.getId()));

        // Check if a peer has itself in it neighbors list
        Assert.assertFalse(CanOperations.hasNeighbor(fourthPeer, fourthPeer.getId()));

        // Check if the zone of the peers are bordered
        Assert.assertNotSame(
                CanOperations.getIdAndZoneResponseOperation(firstPeer)
                    .getPeerZone().neighbors(
                            CanOperations.getIdAndZoneResponseOperation(thirdPeer)
                                .getPeerZone()), -1);

        if (CanOperations.getIdAndZoneResponseOperation(firstPeer)
                .getPeerZone().neighbors(
                        CanOperations.getIdAndZoneResponseOperation(thirdPeer).getPeerZone()) != -1) {
            Assert.assertTrue(CanOperations.hasNeighbor(firstPeer, thirdPeer.getId()));
            Assert.assertTrue(CanOperations.hasNeighbor(thirdPeer, firstPeer.getId()));
        } else {
            Assert.assertFalse(CanOperations.hasNeighbor(firstPeer, thirdPeer.getId()));
            Assert.assertFalse(CanOperations.hasNeighbor(thirdPeer, firstPeer.getId()));
        }

        if (CanOperations.getIdAndZoneResponseOperation(firstPeer)
                .getPeerZone().neighbors(
                        CanOperations.getIdAndZoneResponseOperation(fourthPeer).getPeerZone()) != -1) {
            Assert.assertTrue(CanOperations.hasNeighbor(firstPeer, fourthPeer.getId()));
            Assert.assertTrue(CanOperations.hasNeighbor(fourthPeer, firstPeer.getId()));
        } else {
            Assert.assertFalse(CanOperations.hasNeighbor(firstPeer, fourthPeer.getId()));
            Assert.assertFalse(CanOperations.hasNeighbor(fourthPeer, firstPeer.getId()));
        }

    }

}
