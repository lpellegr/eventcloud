/**
 * Copyright (c) 2011 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
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
import org.objectweb.proactive.extensions.p2p.structured.initializers.CanNetworkInitializer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;

/**
 * Test cases associated to {@link CanOverlay#join(Peer)}.
 * 
 * @author lpellegr
 */
public class JoinOperationTest {

    private CanNetworkInitializer networkInitializer;

    @Before
    public void setUp() {
        this.networkInitializer = new CanNetworkInitializer();
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
        Peer landmarkPeer = PeerFactory.newActivePeer(new CanOverlay());
        try {
            landmarkPeer.create();
        } catch (NetworkAlreadyJoinedException e) {
            e.printStackTrace();
        }
        Peer joiner = PeerFactory.newActivePeer(new CanOverlay());
        joiner.join(landmarkPeer);
        joiner.join(landmarkPeer);
    }

    @Test
    public void testJoinOnPeerNotActivated() {
        Peer landmarkPeer = PeerFactory.newActivePeer(new CanOverlay());
        Peer joiner = PeerFactory.newActivePeer(new CanOverlay());
        try {
            assertFalse(joiner.join(landmarkPeer));
        } catch (NetworkAlreadyJoinedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testConcurrentJoin() {
        final Peer landmarkPeer = PeerFactory.newActivePeer(new CanOverlay());
        try {
            landmarkPeer.create();
        } catch (StructuredP2PException e) {
            e.printStackTrace();
        }

        int nbPeersToJoin = 20;

        List<Peer> peers = new ArrayList<Peer>(nbPeersToJoin);
        for (int i = 0; i < nbPeersToJoin; i++) {
            peers.add(PeerFactory.newActivePeer(new CanOverlay()));
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
        assertFalse("A concurrent join may have occurred", joinResult);

        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
