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
package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.deployment.JunitByMethodCanNetworkDeployer;
import org.objectweb.proactive.extensions.p2p.structured.deployment.StringCanDeploymentDescriptor;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.PeerNotActivatedException;
import org.objectweb.proactive.extensions.p2p.structured.operations.CanOperations;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;

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
            throws NetworkAlreadyJoinedException, PeerNotActivatedException {
        Peer landmarkPeer = super.createPeer();
        landmarkPeer.create();

        Peer joiner = super.createPeer();
        joiner.join(landmarkPeer);
        joiner.join(landmarkPeer);
    }

    @Test(expected = PeerNotActivatedException.class)
    public void testJoinOnPeerNotActivated()
            throws NetworkAlreadyJoinedException, PeerNotActivatedException {
        Peer landmarkPeer = super.createPeer();
        Peer joiner = super.createPeer();

        joiner.join(landmarkPeer);
    }

}
