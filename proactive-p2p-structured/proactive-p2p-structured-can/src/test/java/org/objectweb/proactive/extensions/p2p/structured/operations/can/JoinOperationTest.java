package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.api.PeerFactory;
import org.objectweb.proactive.extensions.p2p.structured.api.operations.CANOperations;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.overlay.BasicCANOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCANOverlay;

/**
 * Test cases for the Join operation in {@link AbstractCANOverlay}.
 * 
 * @author Alexandre Trovato
 * @author Fanny Kilanga
 * @author Laurent Pellegrino
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
        Assert.assertTrue(CANOperations.hasNeighbor(firstPeer, secondPeer.getId()));
        Assert.assertTrue(CANOperations.hasNeighbor(secondPeer, firstPeer.getId()));

        // Check if a peer has itself in it neighbors list
        Assert.assertFalse(CANOperations.hasNeighbor(firstPeer, firstPeer.getId()));
        Assert.assertFalse(CANOperations.hasNeighbor(secondPeer, secondPeer.getId()));

        // Check if the zone of the peers are bordered
        Assert.assertNotSame(
                CANOperations.getIdAndZoneResponseOperation(firstPeer)
                    .getPeerZone().neighbors(
                            CANOperations.getIdAndZoneResponseOperation(secondPeer).getPeerZone()), -1);
    }

    @Test
    public void testThirdPeer() {
        try {
            Assert.assertTrue(JoinOperationTest.thirdPeer.join(JoinOperationTest.secondPeer));
        } catch (NetworkAlreadyJoinedException e) {
            e.printStackTrace();
        }

        // Check that the peers are neighbors
        Assert.assertTrue(CANOperations.hasNeighbor(firstPeer, thirdPeer.getId()));
        Assert.assertTrue(CANOperations.hasNeighbor(secondPeer, thirdPeer.getId()));
        Assert.assertTrue(CANOperations.hasNeighbor(thirdPeer, firstPeer.getId()));
        Assert.assertTrue(CANOperations.hasNeighbor(thirdPeer, secondPeer.getId()));

        // Check if a peer has itself in it neighbors list
        Assert.assertFalse(CANOperations.hasNeighbor(thirdPeer, thirdPeer.getId()));

        // Check if the zone of the peers are bordered
        Assert.assertNotSame(
                CANOperations.getIdAndZoneResponseOperation(firstPeer)
                    .getPeerZone().neighbors(
                        CANOperations.getIdAndZoneResponseOperation(thirdPeer).getPeerZone()), -1);
        Assert.assertNotSame(
                CANOperations.getIdAndZoneResponseOperation(secondPeer)
                    .getPeerZone().neighbors(
                        CANOperations.getIdAndZoneResponseOperation(thirdPeer).getPeerZone()), -1);
    }

    @Test
    public void testFourthPeer() {
        try {
            Assert.assertTrue(JoinOperationTest.fourthPeer.join(JoinOperationTest.thirdPeer));
        } catch (NetworkAlreadyJoinedException e) {
            e.printStackTrace();
        }

        // Check that the peers are neighbors
        Assert.assertTrue(CANOperations.hasNeighbor(secondPeer, fourthPeer.getId()));
        Assert.assertTrue(CANOperations.hasNeighbor(thirdPeer, fourthPeer.getId()));
        Assert.assertTrue(CANOperations.hasNeighbor(fourthPeer, secondPeer.getId()));
        Assert.assertTrue(CANOperations.hasNeighbor(fourthPeer, thirdPeer.getId()));

        // Check if a peer has itself in it neighbors list
        Assert.assertFalse(CANOperations.hasNeighbor(fourthPeer, fourthPeer.getId()));

        // Check if the zone of the peers are bordered
        Assert.assertNotSame(
                CANOperations.getIdAndZoneResponseOperation(firstPeer)
                    .getPeerZone().neighbors(
                            CANOperations.getIdAndZoneResponseOperation(thirdPeer)
                                .getPeerZone()), -1);

        if (CANOperations.getIdAndZoneResponseOperation(firstPeer)
                .getPeerZone().neighbors(
                        CANOperations.getIdAndZoneResponseOperation(thirdPeer).getPeerZone()) != -1) {
            Assert.assertTrue(CANOperations.hasNeighbor(firstPeer, thirdPeer.getId()));
            Assert.assertTrue(CANOperations.hasNeighbor(thirdPeer, firstPeer.getId()));
        } else {
            Assert.assertFalse(CANOperations.hasNeighbor(firstPeer, thirdPeer.getId()));
            Assert.assertFalse(CANOperations.hasNeighbor(thirdPeer, firstPeer.getId()));
        }

        if (CANOperations.getIdAndZoneResponseOperation(firstPeer)
                .getPeerZone().neighbors(
                        CANOperations.getIdAndZoneResponseOperation(fourthPeer).getPeerZone()) != -1) {
            Assert.assertTrue(CANOperations.hasNeighbor(firstPeer, fourthPeer.getId()));
            Assert.assertTrue(CANOperations.hasNeighbor(fourthPeer, firstPeer.getId()));
        } else {
            Assert.assertFalse(CANOperations.hasNeighbor(firstPeer, fourthPeer.getId()));
            Assert.assertFalse(CANOperations.hasNeighbor(fourthPeer, firstPeer.getId()));
        }

    }

}
