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
package org.objectweb.proactive.extensions.p2p.structured.tracker;

import java.io.Serializable;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.factories.PeerFactory;
import org.objectweb.proactive.extensions.p2p.structured.factories.TrackerFactory;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayType;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.RequestResponseManager;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;

/**
 * Tests associated to {@link TrackerImpl}.
 * 
 * @author lpellegr
 */
public class TrackerTest implements Serializable {

    private static final long serialVersionUID = 140L;

    @Test(expected = IllegalArgumentException.class)
    public void testAddOnNetworkWithWrongOverlayType() {
        Tracker tracker = TrackerFactory.newTracker();

        Peer peerWithMockOverlay =
                PeerFactory.newPeer(SerializableProvider.create(MockOverlay.class));
        Peer peerWithAnonymousMockOverlay =
                PeerFactory.newPeer(SerializableProvider.create(CanMockOverlay.class));

        try {
            tracker.inject(peerWithMockOverlay);
            tracker.inject(peerWithAnonymousMockOverlay);
        } catch (NetworkAlreadyJoinedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testJoin() {
        Peer peer =
                PeerFactory.newPeer(SerializableProvider.create(MockOverlay.class));

        Tracker tracker1 = TrackerFactory.newTracker();
        Tracker tracker2 = TrackerFactory.newTracker();
        Tracker tracker3 = TrackerFactory.newTracker();

        tracker3.join(tracker2);
        tracker1.join(tracker2);

        try {
            tracker1.inject(peer);
        } catch (NetworkAlreadyJoinedException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(
                "After join, the trackers does not share the same peer references",
                peer.getId(), tracker1.getRandomPeer().getId());

        Assert.assertEquals(
                "After join, the trackers does not share the same peer references",
                peer.getId(), tracker2.getRandomPeer().getId());

        Assert.assertEquals(
                "After join, the trackers does not share the same peer references",
                peer.getId(), tracker3.getRandomPeer().getId());
    }

    @Test
    public void testRegister() {
        Tracker tracker = TrackerFactory.newTracker();
        String bindingName = tracker.register();

        Assert.assertTrue(
                "The tracker must register into the registry by using the '/tracker/'"
                        + " keyword in order to have the possibility to retrieve all"
                        + " the tracker instances on a given machine",
                bindingName.contains("/tracker/"));

    }

    public static class MockOverlay extends StructuredOverlay {

        public MockOverlay() {
            super(new RequestResponseManager() {
                private static final long serialVersionUID = 140L;
            });
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void create() {

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void join(Peer landmarkPeer) {

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void leave() {

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public OverlayType getType() {
            return OverlayType.UNKNOWN;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return this.id.toString();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String dump() {
            return this.toString();
        }

    }

    public static final class CanMockOverlay extends MockOverlay {

        /**
         * {@inheritDoc}
         */
        @Override
        public OverlayType getType() {
            return OverlayType.CAN;
        }

    }

}
