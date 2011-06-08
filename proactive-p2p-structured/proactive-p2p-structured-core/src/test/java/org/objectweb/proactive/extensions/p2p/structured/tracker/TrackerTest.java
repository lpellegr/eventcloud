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
package org.objectweb.proactive.extensions.p2p.structured.tracker;

import java.io.Serializable;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.api.PeerFactory;
import org.objectweb.proactive.extensions.p2p.structured.api.TrackerFactory;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayType;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;

/**
 * Tests associated to {@link TrackerImpl} and {@link TrackerComponentImpl}.
 * 
 * @author lpellegr
 */
public class TrackerTest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Test(expected = IllegalArgumentException.class)
    public void testAddOnNetworkWithWrongOverlayType() {
        Tracker tracker = TrackerFactory.newActiveTracker();

        Peer peerWithMockOverlay = PeerFactory.newActivePeer(new MockOverlay());
        Peer peerWithAnonymousMockOverlay =
                PeerFactory.newActivePeer(new MockOverlay() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public OverlayType getType() {
                        return OverlayType.CAN;
                    }
                });

        try {
            tracker.inject(peerWithMockOverlay);
            tracker.inject(peerWithAnonymousMockOverlay);
        } catch (NetworkAlreadyJoinedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testJoin() {
        Peer peer = PeerFactory.newActivePeer(new MockOverlay());

        Tracker tracker1 = TrackerFactory.newActiveTracker();
        Tracker tracker2 = TrackerFactory.newActiveTracker();
        Tracker tracker3 = TrackerFactory.newActiveTracker();

        tracker3.join(tracker2);
        tracker1.join(tracker2);

        try {
            tracker1.inject(peer);
        } catch (NetworkAlreadyJoinedException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(
                "After join, the trackers does not share the same peer references",
                peer.toString(), tracker1.getRandomPeer().toString());

        Assert.assertEquals(
                "After join, the trackers does not share the same peer references",
                peer.toString(), tracker2.getRandomPeer().toString());

        Assert.assertEquals(
                "After join, the trackers does not share the same peer references",
                peer.toString(), tracker3.getRandomPeer().toString());
    }

    @Test
    public void testJoinWithComponents() {
        Peer peer = PeerFactory.newComponentPeer(new MockOverlay());

        Tracker tracker1 = TrackerFactory.newComponentTracker();
        Tracker tracker2 = TrackerFactory.newComponentTracker();
        Tracker tracker3 = TrackerFactory.newComponentTracker();

        tracker3.join(tracker2);
        tracker1.join(tracker2);

        try {
            tracker1.inject(peer);
        } catch (NetworkAlreadyJoinedException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(
                "After join, the trackers does not share the same peer references",
                peer.toString(), tracker1.getRandomPeer().toString());

        Assert.assertEquals(
                "After join, the trackers does not share the same peer references",
                peer.toString(), tracker2.getRandomPeer().toString());

        Assert.assertEquals(
                "After join, the trackers does not share the same peer references",
                peer.toString(), tracker3.getRandomPeer().toString());
    }

    @Test
    public void testRegister() {
        Tracker tracker = TrackerFactory.newActiveTracker();
        String bindingName = tracker.register();

        Assert.assertTrue(
                "The tracker must register into the registry by using the '/tracker/'"
                        + " keyword in order to have the possibility to retrieve all"
                        + " the tracker instances on a given machine",
                bindingName.contains("/tracker/"));

    }

    private static class MockOverlay extends StructuredOverlay {

        private static final long serialVersionUID = 1L;

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean create() {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean join(Peer landmarkPeer) {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean leave() {
            return true;
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

}
