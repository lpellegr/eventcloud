package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.api.PeerFactory;
import org.objectweb.proactive.extensions.p2p.structured.api.operations.CanOperations;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.overlay.BasicCanOverlay;
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

    private static Peer firstComponentPeer;

    private static Peer secondComponentPeer;

    private static Peer thirdComponentPeer;

    private static Peer fourthComponentPeer;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        firstPeer = PeerFactory.newActivePeer(new BasicCanOverlay());
        secondPeer = PeerFactory.newActivePeer(new BasicCanOverlay());
        thirdPeer = PeerFactory.newActivePeer(new BasicCanOverlay());
        fourthPeer = PeerFactory.newActivePeer(new BasicCanOverlay());
        firstComponentPeer =
                PeerFactory.newComponentPeer(new BasicCanOverlay());
        secondComponentPeer =
                PeerFactory.newComponentPeer(new BasicCanOverlay());
        thirdComponentPeer =
                PeerFactory.newComponentPeer(new BasicCanOverlay());
        fourthComponentPeer =
                PeerFactory.newComponentPeer(new BasicCanOverlay());

        firstPeer.create();
        firstComponentPeer.create();
    }

    @Test
    public void testSecondPeer() {
        try {
            Assert.assertTrue(secondPeer.join(JoinOperationTest.firstPeer));
        } catch (NetworkAlreadyJoinedException e) {
            e.printStackTrace();
        }

        // Check that the peers are neighbors
        Assert.assertTrue(CanOperations.hasNeighbor(
                firstPeer, secondPeer.getId()));
        Assert.assertTrue(CanOperations.hasNeighbor(
                secondPeer, firstPeer.getId()));

        // Check if a peer has itself in it neighbors list
        Assert.assertFalse(CanOperations.hasNeighbor(
                firstPeer, firstPeer.getId()));
        Assert.assertFalse(CanOperations.hasNeighbor(
                secondPeer, secondPeer.getId()));

        // Check if the zone of the peers are bordered
        Assert.assertNotSame(CanOperations.getIdAndZoneResponseOperation(
                firstPeer).getPeerZone().neighbors(
                CanOperations.getIdAndZoneResponseOperation(secondPeer)
                        .getPeerZone()), -1);
    }

    @Test
    public void testThirdPeer() {
        try {
            Assert.assertTrue(JoinOperationTest.thirdPeer.join(JoinOperationTest.secondPeer));
        } catch (NetworkAlreadyJoinedException e) {
            e.printStackTrace();
        }

        // Check that the peers are neighbors
        Assert.assertTrue(CanOperations.hasNeighbor(
                firstPeer, thirdPeer.getId()));
        Assert.assertTrue(CanOperations.hasNeighbor(
                secondPeer, thirdPeer.getId()));
        Assert.assertTrue(CanOperations.hasNeighbor(
                thirdPeer, firstPeer.getId()));
        Assert.assertTrue(CanOperations.hasNeighbor(
                thirdPeer, secondPeer.getId()));

        // Check if a peer has itself in it neighbors list
        Assert.assertFalse(CanOperations.hasNeighbor(
                thirdPeer, thirdPeer.getId()));

        // Check if the zone of the peers are bordered
        Assert.assertNotSame(CanOperations.getIdAndZoneResponseOperation(
                firstPeer).getPeerZone().neighbors(
                CanOperations.getIdAndZoneResponseOperation(thirdPeer)
                        .getPeerZone()), -1);
        Assert.assertNotSame(CanOperations.getIdAndZoneResponseOperation(
                secondPeer).getPeerZone().neighbors(
                CanOperations.getIdAndZoneResponseOperation(thirdPeer)
                        .getPeerZone()), -1);
    }

    @Test
    public void testFourthPeer() {
        try {
            Assert.assertTrue(JoinOperationTest.fourthPeer.join(JoinOperationTest.thirdPeer));
        } catch (NetworkAlreadyJoinedException e) {
            e.printStackTrace();
        }

        // Check that the peers are neighbors
        Assert.assertTrue(CanOperations.hasNeighbor(
                secondPeer, fourthPeer.getId()));
        Assert.assertTrue(CanOperations.hasNeighbor(
                thirdPeer, fourthPeer.getId()));
        Assert.assertTrue(CanOperations.hasNeighbor(
                fourthPeer, secondPeer.getId()));
        Assert.assertTrue(CanOperations.hasNeighbor(
                fourthPeer, thirdPeer.getId()));

        // Check if a peer has itself in it neighbors list
        Assert.assertFalse(CanOperations.hasNeighbor(
                fourthPeer, fourthPeer.getId()));

        // Check if the zone of the peers are bordered
        Assert.assertNotSame(CanOperations.getIdAndZoneResponseOperation(
                firstPeer).getPeerZone().neighbors(
                CanOperations.getIdAndZoneResponseOperation(thirdPeer)
                        .getPeerZone()), -1);

        if (CanOperations.getIdAndZoneResponseOperation(firstPeer)
                .getPeerZone()
                .neighbors(
                        CanOperations.getIdAndZoneResponseOperation(thirdPeer)
                                .getPeerZone()) != -1) {
            Assert.assertTrue(CanOperations.hasNeighbor(
                    firstPeer, thirdPeer.getId()));
            Assert.assertTrue(CanOperations.hasNeighbor(
                    thirdPeer, firstPeer.getId()));
        } else {
            Assert.assertFalse(CanOperations.hasNeighbor(
                    firstPeer, thirdPeer.getId()));
            Assert.assertFalse(CanOperations.hasNeighbor(
                    thirdPeer, firstPeer.getId()));
        }

        if (CanOperations.getIdAndZoneResponseOperation(firstPeer)
                .getPeerZone()
                .neighbors(
                        CanOperations.getIdAndZoneResponseOperation(fourthPeer)
                                .getPeerZone()) != -1) {
            Assert.assertTrue(CanOperations.hasNeighbor(
                    firstPeer, fourthPeer.getId()));
            Assert.assertTrue(CanOperations.hasNeighbor(
                    fourthPeer, firstPeer.getId()));
        } else {
            Assert.assertFalse(CanOperations.hasNeighbor(
                    firstPeer, fourthPeer.getId()));
            Assert.assertFalse(CanOperations.hasNeighbor(
                    fourthPeer, firstPeer.getId()));
        }
    }

    @Test
    public void testSecondComponentPeer() {
        try {
            Assert.assertTrue(secondComponentPeer.join(JoinOperationTest.firstComponentPeer));
        } catch (NetworkAlreadyJoinedException e) {
            e.printStackTrace();
        }

        // Check that the peers are neighbors
        Assert.assertTrue(CanOperations.hasNeighbor(
                firstComponentPeer, secondComponentPeer.getId()));
        Assert.assertTrue(CanOperations.hasNeighbor(
                secondComponentPeer, firstComponentPeer.getId()));

        // Check if a peer has itself in it neighbors list
        Assert.assertFalse(CanOperations.hasNeighbor(
                firstComponentPeer, firstComponentPeer.getId()));
        Assert.assertFalse(CanOperations.hasNeighbor(
                secondComponentPeer, secondComponentPeer.getId()));

        // Check if the zone of the peers are bordered
        Assert.assertNotSame(CanOperations.getIdAndZoneResponseOperation(
                firstComponentPeer)
                .getPeerZone()
                .neighbors(
                        CanOperations.getIdAndZoneResponseOperation(
                                secondComponentPeer).getPeerZone()), -1);
    }

    @Test
    public void testThirdComponentPeer() {
        try {
            Assert.assertTrue(JoinOperationTest.thirdComponentPeer.join(JoinOperationTest.secondComponentPeer));
        } catch (NetworkAlreadyJoinedException e) {
            e.printStackTrace();
        }

        // Check that the peers are neighbors
        Assert.assertTrue(CanOperations.hasNeighbor(
                firstComponentPeer, thirdComponentPeer.getId()));
        Assert.assertTrue(CanOperations.hasNeighbor(
                secondComponentPeer, thirdComponentPeer.getId()));
        Assert.assertTrue(CanOperations.hasNeighbor(
                thirdComponentPeer, firstComponentPeer.getId()));
        Assert.assertTrue(CanOperations.hasNeighbor(
                thirdComponentPeer, secondComponentPeer.getId()));

        // Check if a peer has itself in it neighbors list
        Assert.assertFalse(CanOperations.hasNeighbor(
                thirdComponentPeer, thirdComponentPeer.getId()));

        // Check if the zone of the peers are bordered
        Assert.assertNotSame(CanOperations.getIdAndZoneResponseOperation(
                firstComponentPeer).getPeerZone().neighbors(
                CanOperations.getIdAndZoneResponseOperation(thirdComponentPeer)
                        .getPeerZone()), -1);
        Assert.assertNotSame(CanOperations.getIdAndZoneResponseOperation(
                secondComponentPeer).getPeerZone().neighbors(
                CanOperations.getIdAndZoneResponseOperation(thirdComponentPeer)
                        .getPeerZone()), -1);
    }

    @Test
    public void testFourthComponentPeer() {
        try {
            Assert.assertTrue(JoinOperationTest.fourthComponentPeer.join(JoinOperationTest.thirdComponentPeer));
        } catch (NetworkAlreadyJoinedException e) {
            e.printStackTrace();
        }

        // Check that the peers are neighbors
        Assert.assertTrue(CanOperations.hasNeighbor(
                secondComponentPeer, fourthComponentPeer.getId()));
        Assert.assertTrue(CanOperations.hasNeighbor(
                thirdComponentPeer, fourthComponentPeer.getId()));
        Assert.assertTrue(CanOperations.hasNeighbor(
                fourthComponentPeer, secondComponentPeer.getId()));
        Assert.assertTrue(CanOperations.hasNeighbor(
                fourthComponentPeer, thirdComponentPeer.getId()));

        // Check if a peer has itself in it neighbors list
        Assert.assertFalse(CanOperations.hasNeighbor(
                fourthComponentPeer, fourthComponentPeer.getId()));

        // Check if the zone of the peers are bordered
        Assert.assertNotSame(CanOperations.getIdAndZoneResponseOperation(
                firstComponentPeer).getPeerZone().neighbors(
                CanOperations.getIdAndZoneResponseOperation(thirdComponentPeer)
                        .getPeerZone()), -1);

        if (CanOperations.getIdAndZoneResponseOperation(firstComponentPeer)
                .getPeerZone()
                .neighbors(
                        CanOperations.getIdAndZoneResponseOperation(
                                thirdComponentPeer).getPeerZone()) != -1) {
            Assert.assertTrue(CanOperations.hasNeighbor(
                    firstComponentPeer, thirdComponentPeer.getId()));
            Assert.assertTrue(CanOperations.hasNeighbor(
                    thirdComponentPeer, firstComponentPeer.getId()));
        } else {
            Assert.assertFalse(CanOperations.hasNeighbor(
                    firstComponentPeer, thirdComponentPeer.getId()));
            Assert.assertFalse(CanOperations.hasNeighbor(
                    thirdComponentPeer, firstComponentPeer.getId()));
        }

        if (CanOperations.getIdAndZoneResponseOperation(firstComponentPeer)
                .getPeerZone()
                .neighbors(
                        CanOperations.getIdAndZoneResponseOperation(
                                fourthComponentPeer).getPeerZone()) != -1) {
            Assert.assertTrue(CanOperations.hasNeighbor(
                    firstComponentPeer, fourthComponentPeer.getId()));
            Assert.assertTrue(CanOperations.hasNeighbor(
                    fourthComponentPeer, firstComponentPeer.getId()));
        } else {
            Assert.assertFalse(CanOperations.hasNeighbor(
                    firstComponentPeer, fourthComponentPeer.getId()));
            Assert.assertFalse(CanOperations.hasNeighbor(
                    fourthComponentPeer, firstComponentPeer.getId()));
        }
    }

}
