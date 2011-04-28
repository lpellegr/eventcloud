package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.api.PeerFactory;
import org.objectweb.proactive.extensions.p2p.structured.api.operations.CanOperations;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.StructuredP2PException;
import org.objectweb.proactive.extensions.p2p.structured.intializers.CANNetworkInitializer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.BasicCanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCanOverlay;

/**
 * Test cases associated to {@link AbstractCanOverlay#join(Peer)}.
 * 
 * @author lpellegr
 */
public class JoinOperationTest {

    private CANNetworkInitializer networkInitializer;

    @Before
    public void setUp() {
        this.networkInitializer = new CANNetworkInitializer();
    }

    @Test
    public void testNeighborsAfterJoinOperationWithTwoPeers() {
        this.networkInitializer.initializeNewNetwork(2);

        Peer firstPeer = this.networkInitializer.get(0);
        Peer secondPeer = this.networkInitializer.get(1);

        // checks that the peers are neighbors
        Assert.assertTrue(CanOperations.hasNeighbor(
                firstPeer, secondPeer.getId()));
        Assert.assertTrue(CanOperations.hasNeighbor(
                secondPeer, firstPeer.getId()));

        // checks whether a peer has itself in its neighbors list
        Assert.assertFalse(CanOperations.hasNeighbor(
                firstPeer, firstPeer.getId()));
        Assert.assertFalse(CanOperations.hasNeighbor(
                secondPeer, secondPeer.getId()));

        // checks whether the peers are abuts
        Assert.assertNotSame(CanOperations.getIdAndZoneResponseOperation(
                firstPeer).getPeerZone().neighbors(
                CanOperations.getIdAndZoneResponseOperation(secondPeer)
                        .getPeerZone()), -1);
    }

    @Test
    public void testNeighborsAfterJoinOperationWithTwoPeerComponents() {
        this.networkInitializer.initializeNewNetwork(2);

        Peer firstPeer = this.networkInitializer.getc(0);
        Peer secondPeer = this.networkInitializer.getc(1);

        // checks that the peers are neighbors
        Assert.assertTrue(CanOperations.hasNeighbor(
                firstPeer, secondPeer.getId()));
        Assert.assertTrue(CanOperations.hasNeighbor(
                secondPeer, firstPeer.getId()));

        // checks whether a peer has itself in its neighbors list
        Assert.assertFalse(CanOperations.hasNeighbor(
                firstPeer, firstPeer.getId()));
        Assert.assertFalse(CanOperations.hasNeighbor(
                secondPeer, secondPeer.getId()));

        // checks whether the peers are abuts
        Assert.assertNotSame(CanOperations.getIdAndZoneResponseOperation(
                firstPeer).getPeerZone().neighbors(
                CanOperations.getIdAndZoneResponseOperation(secondPeer)
                        .getPeerZone()), -1);
    }

    @Test(expected = NetworkAlreadyJoinedException.class)
    public void testJoinWithPeerWhichHasAlreadyJoined()
            throws NetworkAlreadyJoinedException {
        Peer landmarkPeer = PeerFactory.newActivePeer(new BasicCanOverlay());
        try {
            landmarkPeer.create();
        } catch (NetworkAlreadyJoinedException e) {
            e.printStackTrace();
        }
        Peer joiner = PeerFactory.newActivePeer(new BasicCanOverlay());
        joiner.join(landmarkPeer);
        joiner.join(landmarkPeer);
    }

    @Test
    public void testJoinOnPeerNotActivated() {
        Peer landmarkPeer = PeerFactory.newActivePeer(new BasicCanOverlay());
        Peer joiner = PeerFactory.newActivePeer(new BasicCanOverlay());
        try {
            assertFalse(joiner.join(landmarkPeer));
        } catch (NetworkAlreadyJoinedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testConcurrentJoin() {
        final Peer landmarkPeer =
                PeerFactory.newActivePeer(new BasicCanOverlay());
        try {
            landmarkPeer.create();
        } catch (StructuredP2PException e) {
            e.printStackTrace();
        }

        int nbPeersToJoin = 10;

        List<Peer> peers = new ArrayList<Peer>(nbPeersToJoin);
        for (int i = 0; i < nbPeersToJoin; i++) {
            peers.add(PeerFactory.newActivePeer(new BasicCanOverlay()));
        }

        ExecutorService threadPool =
                Executors.newFixedThreadPool(nbPeersToJoin < 20
                        ? nbPeersToJoin : 20);

        boolean joinResult = true;

        List<Future<Boolean>> futures =
                new ArrayList<Future<Boolean>>(nbPeersToJoin);

        final CountDownLatch doneSignal = new CountDownLatch(nbPeersToJoin);
        for (final Peer p : peers) {
            futures.add(threadPool.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    try {
                        return p.join(landmarkPeer);
                    } catch (NetworkAlreadyJoinedException e) {
                        e.printStackTrace();
                        return false;
                    } finally {
                        doneSignal.countDown();
                    }
                }
            }));
        }

        for (Future<Boolean> future : futures) {
            try {
                joinResult &= future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        // a concurrent join may have occurred.
        // hence, joinResult must be set to false
        assertFalse(joinResult);

        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
