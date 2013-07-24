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
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.OptimalBroadcastRequest;
import org.objectweb.proactive.extensions.p2p.structured.operations.CanOperations;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.CoordinateFactory;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;
import org.objectweb.proactive.extensions.p2p.structured.utils.RandomUtils;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.BroadcastConstraintsValidator;
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
        final ExecutorService threadPool = Executors.newFixedThreadPool(4);
        final Peer[] peers = new Peer[34];

        log.info("Preallocating {} peers", peers.length);

        for (int i = 0; i < peers.length; i++) {
            peers[i] = createCustomPeer();
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

        if (!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
            Assert.fail("Concurrent joins timeout");
        }
    }

    @Test
    public void testConcurrentLeaveRequests() throws InterruptedException,
            NetworkAlreadyJoinedException {
        final Peer[] extraPeers = new Peer[35];

        log.info("Inserting {} additional peers", extraPeers.length);

        for (int i = 0; i < extraPeers.length; i++) {
            super.getRandomTracker().inject(createCustomPeer());
        }

        log.info("Performing concurrent leaves");

        final ExecutorService threadPool = Executors.newFixedThreadPool(4);

        for (int i = 0; i < extraPeers.length - 1; i++) {
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        CanTest.this.getRandomTracker()
                                .removeRandomPeer()
                                .leave();
                    } catch (NetworkNotJoinedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        threadPool.shutdown();

        if (!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
            Assert.fail("Concurrent leaves timeout");
        }
    }

    @Test
    public void testConcurrentJoinAndLeaveRequests()
            throws NetworkAlreadyJoinedException, InterruptedException {
        final RequestType[] requests = new RequestType[34];

        final ConcurrentLinkedQueue<Peer> preAllocatedPeers =
                new ConcurrentLinkedQueue<Peer>();

        int nbLeaveRequests = 0;

        // pre-allocates peers since it is not possible to instantiate them in
        // parallel and takes advantage of this operation to pre define the
        // number of join and leave requests to perform
        for (int i = 0; i < requests.length; i++) {
            if (RandomUtils.nextInt(2) == 0) {
                requests[i] = RequestType.JOIN;
                preAllocatedPeers.add(createCustomPeer());
            } else {
                requests[i] = RequestType.LEAVE;
                nbLeaveRequests++;
            }
        }

        log.info("Inserting additional peers for leave requests");

        for (int i = 0; i < nbLeaveRequests; i++) {
            super.getRandomTracker().inject(createCustomPeer());
        }

        log.info(
                "At the beginning {} peers are maintained by the trackers",
                this.getRandomTracker().getPeers().size());

        log.info(
                "Scheduled {} join and {} leave requests",
                preAllocatedPeers.size(), requests.length
                        - preAllocatedPeers.size());

        log.info("Performing concurrent join or leave requests");

        final ExecutorService threadPool = Executors.newFixedThreadPool(4);
        for (int i = 0; i < requests.length; i++) {
            final int k = i;

            threadPool.execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        if (requests[k] == RequestType.JOIN) {
                            preAllocatedPeers.poll().join(
                                    CanTest.this.getRandomPeer());
                        } else if (requests[k] == RequestType.LEAVE) {
                            CanTest.this.getRandomTracker()
                                    .removeRandomPeer()
                                    .leave();
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

        if (!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
            Assert.fail("Concurrent join and leave requests timeout");
        }

        log.info(
                "At the end {} peers are still maintained by the trackers",
                this.getRandomTracker().getPeers().size());
    }

    @Test
    public void testConcurrentJoinAndRoutingRequests()
            throws InterruptedException {
        final ConcurrentLinkedQueue<Peer> preAllocatedPeers =
                new ConcurrentLinkedQueue<Peer>();

        for (int i = 0; i < 34; i++) {
            preAllocatedPeers.add(createCustomPeer());
        }

        final ExecutorService threadPool = Executors.newFixedThreadPool(4);

        for (int i = 0; i < preAllocatedPeers.size(); i++) {
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (RandomUtils.nextInt(2) == 0) {
                            CanTest.this.getProxy().send(
                                    createFloodingRequest());
                        } else {
                            preAllocatedPeers.poll().join(
                                    CanTest.this.getRandomPeer());
                        }
                    } catch (NetworkAlreadyJoinedException e) {
                        e.printStackTrace();
                    } catch (PeerNotActivatedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        threadPool.shutdown();

        if (!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
            Assert.fail("Concurrent join and routing requests timeout");
        }
    }

    @Test
    public void testConcurrentLeaveAndRoutingRequests()
            throws NetworkAlreadyJoinedException, InterruptedException {
        final ConcurrentLinkedQueue<Peer> preAllocatedPeers =
                new ConcurrentLinkedQueue<Peer>();

        for (int i = 0; i < 34; i++) {
            super.getRandomTracker().inject(createCustomPeer());
        }

        final ExecutorService threadPool = Executors.newFixedThreadPool(4);

        for (int i = 0; i < preAllocatedPeers.size(); i++) {

            threadPool.execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        if (RandomUtils.nextInt(2) == 0) {
                            CanTest.this.getProxy().send(
                                    createFloodingRequest());
                        } else {
                            CanTest.this.getRandomTracker()
                                    .removeRandomPeer()
                                    .leave();
                        }
                    } catch (NetworkNotJoinedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        threadPool.shutdown();

        if (!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
            Assert.fail("Concurrent leave and routing requests timeout");
        }
    }

    @Test
    public void testConcurrentJoinLeaveAndRoutingRequests()
            throws NetworkAlreadyJoinedException, InterruptedException {

        int nbLeaveRequests = 0;

        final RequestType[] requests = new RequestType[34];

        final ConcurrentLinkedQueue<Peer> preAllocatedPeers =
                new ConcurrentLinkedQueue<Peer>();

        // pre-allocates peers since it is not possible to instantiate them in
        // parallel and takes advantage of this operation to pre define the
        // number of join, leave and routing requests to perform
        for (int i = 0; i < requests.length; i++) {
            requests[i] = electRandomRequestType();

            if (requests[i] == RequestType.JOIN) {
                preAllocatedPeers.add(createCustomPeer());
            }

            if (requests[i] == RequestType.LEAVE) {
                nbLeaveRequests++;
            }
        }

        log.info("Inserting additional peers for leave requests");

        for (int i = 0; i < nbLeaveRequests; i++) {
            super.getRandomTracker().inject(createCustomPeer());
        }

        log.info("Performing concurrent join, leave and routing requests");

        final ExecutorService threadPool = Executors.newFixedThreadPool(4);
        for (int i = 0; i < requests.length; i++) {
            final int k = i;

            threadPool.execute(new Runnable() {

                @Override
                public void run() {
                    try {
                        switch (requests[k]) {
                            case JOIN:
                                preAllocatedPeers.poll().join(
                                        CanTest.this.getRandomPeer());
                                break;
                            case LEAVE:
                                CanTest.this.getRandomTracker()
                                        .removeRandomPeer()
                                        .leave();
                                break;
                            case ROUTING:
                                CanTest.this.getProxy().send(
                                        createFloodingRequest());
                                break;
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

        if (!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
            Assert.fail("Concurrent join, leave and routing requests timeout");
        }
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

    private enum RequestType {
        JOIN, LEAVE, ROUTING
    }

    private static OptimalBroadcastRequest<StringElement> createFloodingRequest() {
        return new OptimalBroadcastRequest<StringElement>(
                new BroadcastConstraintsValidator<StringElement>(
                        CoordinateFactory.newStringCoordinate()));
    }

    private static Peer createCustomPeer() {
        return PeerFactory.newPeer(new CustomOverlayProvider());
    }

    private static RequestType electRandomRequestType() {
        switch (RandomUtils.nextInt(3)) {
            case 0:
                return RequestType.JOIN;
            case 1:
                return RequestType.LEAVE;
            case 2:
                return RequestType.ROUTING;
        }

        throw new IllegalStateException();
    }

    private static class CustomOverlayProvider extends
            SerializableProvider<StringCanOverlay> {
        private static final long serialVersionUID = 160L;

        @Override
        public StringCanOverlay get() {
            return new StringCanOverlay();
        }
    }

}
