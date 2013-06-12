/**
 * Copyright (c) 2011-2013 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package org.objectweb.proactive.extensions.p2p.structured.overlay.can;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.deployment.JunitByClassCanNetworkDeployer;
import org.objectweb.proactive.extensions.p2p.structured.deployment.StringCanDeploymentDescriptor;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkNotJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.PeerNotActivatedException;
import org.objectweb.proactive.extensions.p2p.structured.factories.PeerFactory;
import org.objectweb.proactive.extensions.p2p.structured.operations.CanOperations;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;
import org.objectweb.proactive.extensions.p2p.structured.utils.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * This class tests some properties of the Content-Addressable Network protocol.
 * 
 * @author lpellegr
 */
public class CanTest extends JunitByClassCanNetworkDeployer {

    private static final Logger log = LoggerFactory.getLogger(CanTest.class);

    public CanTest() {
        super(new StringCanDeploymentDescriptor(), 1, 10);
    }

    @Test
    public void testConcurrentJoinRequests() throws InterruptedException {
        final ExecutorService threadPool = Executors.newFixedThreadPool(8);
        final Peer[] peers = new Peer[64];

        log.info("Preallocating {} peers", peers.length);

        for (int i = 0; i < peers.length; i++) {
            peers[i] = PeerFactory.newPeer(new CustomOverlayProvider());
        }

        log.info("Performing concurrent joins");

        final List<Peer> handledPeers =
                Collections.synchronizedList(Lists.<Peer> newArrayList());

        for (int i = 0; i < peers.length; i++) {
            final int k = i;
            threadPool.execute(new Runnable() {

                @Override
                public void run() {
                    Peer landmarkPeer;

                    if (handledPeers.isEmpty() || RandomUtils.nextInt(2) == 0) {
                        landmarkPeer = CanTest.this.getRandomPeer();
                    } else {
                        landmarkPeer =
                                handledPeers.get(RandomUtils.nextInt(handledPeers.size()));
                    }

                    try {
                        peers[k].join(landmarkPeer);
                        handledPeers.add(peers[k]);
                    } catch (NetworkAlreadyJoinedException e) {
                        e.printStackTrace();
                    } catch (PeerNotActivatedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        threadPool.shutdown();

        if (!threadPool.awaitTermination(100, TimeUnit.SECONDS)) {
            Assert.fail("Concurrent joins timeout");
        }
    }

    @Test
    public void testConcurrentLeaveRequests() throws InterruptedException,
            NetworkAlreadyJoinedException, PeerNotActivatedException {
        final Peer[] extraPeers = new Peer[55];

        log.info("Inserting {} additional peers", extraPeers.length);

        for (int i = 0; i < extraPeers.length; i++) {
            extraPeers[i] = PeerFactory.newPeer(new CustomOverlayProvider());
            extraPeers[i].join(this.getRandomPeer());
        }

        log.info("Performing concurrent leaves");

        final ExecutorService threadPool = Executors.newFixedThreadPool(8);
        for (int i = 0; i < extraPeers.length - 1; i++) {
            final int k = i;
            threadPool.execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        extraPeers[k].leave();
                    } catch (NetworkNotJoinedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        threadPool.shutdown();

        if (!threadPool.awaitTermination(100, TimeUnit.SECONDS)) {
            Assert.fail("Concurrent leaves timeout");
        }
    }

    @Test
    public void testMixedConcurrentJoinAndLeaveRequests()
            throws NetworkAlreadyJoinedException, PeerNotActivatedException,
            InterruptedException {
        // among the pre-defined number of loops, select some to inject peers
        log.info("Inserting additional peers");

        for (int i = 0; i < 45; i++) {
            Peer newPeer = PeerFactory.newPeer(new CustomOverlayProvider());
            super.getRandomTracker().inject(newPeer);
        }

        log.info(
                "At the beginning {} peers are maintained by the trackers",
                this.getRandomTracker().getPeers().size());

        // 0 means join, 1 leave
        final byte[] operations = new byte[54];

        final ConcurrentLinkedQueue<Peer> preAllocatedPeers =
                new ConcurrentLinkedQueue<Peer>();
        // pre-allocates peers since it is not possible to instantiate them in
        // parallel and takes advantage of this operation to pre define the
        // number of join and leave requests to perform
        for (int i = 0; i < operations.length; i++) {
            if (RandomUtils.nextInt(2) == 0) {
                preAllocatedPeers.add(PeerFactory.newPeer(new CustomOverlayProvider()));
                operations[i] = 0;
            } else {
                operations[i] = 1;
            }
        }

        log.info(
                "Scheduled {} join and {} leave requests",
                preAllocatedPeers.size(), operations.length
                        - preAllocatedPeers.size());

        log.info("Performing concurrent join or leave requests");

        final ExecutorService threadPool = Executors.newFixedThreadPool(4);
        for (int i = 0; i < operations.length; i++) {
            final int k = i;

            threadPool.execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        if (operations[k] == 1) {
                            CanTest.this.getRandomTracker().takeout(
                                    CanTest.this.getRandomPeer());
                        } else if (operations[k] == 0) {
                            preAllocatedPeers.poll().join(
                                    CanTest.this.getRandomPeer());
                        }
                    } catch (NetworkNotJoinedException e) {
                        e.printStackTrace();
                    } catch (NetworkAlreadyJoinedException e) {
                        e.printStackTrace();
                    } catch (PeerNotActivatedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        threadPool.shutdown();

        if (!threadPool.awaitTermination(100, TimeUnit.SECONDS)) {
            Assert.fail("Concurrent join and leave requests timeout");
        }

        log.info(
                "At the end {} peers are still maintained by the trackers",
                this.getRandomTracker().getPeers().size());
    }

    @Test
    public void testNeighborhood() {
        for (Peer peer : super.getRandomTracker().getPeers()) {

            NeighborTable<StringElement> table =
                    CanOperations.getNeighborTable(peer);

            for (byte dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
                for (byte dir = 0; dir < 2; dir++) {
                    for (NeighborEntry<StringElement> entry : table.get(
                            dim, dir).values()) {
                        Zone<StringElement> zone =
                                CanOperations.<StringElement> getIdAndZoneResponseOperation(
                                        peer)
                                        .getPeerZone();

                        Assert.assertTrue(zone.neighbors(entry.getZone()) != -1);
                    }
                }
            }
        }
    }

    private static class CustomOverlayProvider extends
            SerializableProvider<StringCanOverlay> {
        private static final long serialVersionUID = 150L;

        @Override
        public StringCanOverlay get() {
            return new StringCanOverlay();
        }
    }

}
