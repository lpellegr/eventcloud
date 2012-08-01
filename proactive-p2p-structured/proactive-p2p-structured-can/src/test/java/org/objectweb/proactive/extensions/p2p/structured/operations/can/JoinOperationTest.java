/**
 * Copyright (c) 2011-2012 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.deployment.JunitByMethodCanNetworkDeployer;
import org.objectweb.proactive.extensions.p2p.structured.deployment.StringCanDeploymentDescriptor;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.factories.PeerFactory;
import org.objectweb.proactive.extensions.p2p.structured.operations.CanOperations;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.StringCanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;

/**
 * Test cases associated to {@link CanOverlay#join(Peer)}.
 * 
 * @author lpellegr
 */
public class JoinOperationTest extends
        JunitByMethodCanNetworkDeployer<StringElement> {

    public JoinOperationTest() {
        super(new StringCanDeploymentDescriptor());
    }

    @Test
    public void testNeighborsAfterJoinOperationWithTwoPeers() {
        super.deploy(2);

        Peer firstPeer = super.getPeer(0);
        Peer secondPeer = super.getPeer(1);

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
        Peer landmarkPeer = super.createPeer();
        landmarkPeer.create();

        Peer joiner = super.createPeer();
        joiner.join(landmarkPeer);
        joiner.join(landmarkPeer);
    }

    @Test
    public void testJoinOnPeerNotActivated()
            throws NetworkAlreadyJoinedException {
        Peer landmarkPeer = super.createPeer();
        Peer joiner = super.createPeer();

        Assert.assertFalse(joiner.join(landmarkPeer));
    }

    @Test
    public void testConcurrentJoin() throws NetworkAlreadyJoinedException,
            InterruptedException, ExecutionException {
        final Peer landmarkPeer =
                PeerFactory.newPeer(SerializableProvider.create(StringCanOverlay.class));
        landmarkPeer.create();

        int nbPeersToJoin = 20;

        List<Peer> peers = new ArrayList<Peer>(nbPeersToJoin);
        for (int i = 0; i < nbPeersToJoin; i++) {
            peers.add(PeerFactory.newPeer(SerializableProvider.create(StringCanOverlay.class)));
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
            joinResult &= future.get();
        }

        // a concurrent join may have occurred.
        // hence, joinResult must has a false value
        Assert.assertFalse(
                "No concurrent join has been detected whereas one is expected",
                joinResult);

        doneSignal.await();
    }

}
