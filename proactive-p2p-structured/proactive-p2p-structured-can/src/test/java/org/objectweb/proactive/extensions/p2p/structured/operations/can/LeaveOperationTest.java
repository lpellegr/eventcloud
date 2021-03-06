/**
 * Copyright (c) 2011-2014 INRIA.
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.deployment.JunitByMethodCanNetworkDeployer;
import org.objectweb.proactive.extensions.p2p.structured.deployment.StringCanDeploymentDescriptor;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkNotJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.operations.CanOperations;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayId;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.StringZone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.UnicodeZone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.utils.HomogenousPair;

/**
 * Test cases for the leave operation in {@link CanOverlay}.
 * 
 * @author lpellegr
 */
public class LeaveOperationTest extends
        JunitByMethodCanNetworkDeployer<StringCoordinate> {

    public LeaveOperationTest() {
        super(new StringCanDeploymentDescriptor());
    }

    @Test
    public void testCanMerge() {
        UnicodeZone<StringCoordinate> z1 = new StringZone();
        HomogenousPair<UnicodeZone<StringCoordinate>> z1Split =
                z1.split((byte) 0);
        UnicodeZone<StringCoordinate> z2 = z1Split.getFirst();
        UnicodeZone<StringCoordinate> z3 = z1Split.getSecond();

        // two zones: z1, z2
        assertTrue(z2.canMerge(z3, (byte) 0));

        HomogenousPair<UnicodeZone<StringCoordinate>> z3Split =
                z3.split((byte) 1);
        UnicodeZone<StringCoordinate> z4 = z3Split.getFirst();
        UnicodeZone<StringCoordinate> z5 = z3Split.getSecond();

        // three zones: z2, z4, z5
        assertFalse(z2.canMerge(z4, (byte) 0));
        assertFalse(z2.canMerge(z5, (byte) 0));
        assertTrue(z4.canMerge(z5, (byte) 1));
    }

    @Test
    public void testLeaveWithOnePeer() throws NetworkNotJoinedException {
        super.deploy(1);
        super.getPeer(0).leave();
    }

    @Test
    public void testLeaveWithTwoPeers() throws NetworkNotJoinedException {
        super.deploy(2);

        OverlayId peerLeavingId = super.getPeer(0).getId();

        super.getPeer(0).leave();

        assertFalse(CanOperations.hasNeighbor(super.getPeer(1), peerLeavingId));
    }

    // @Test
    // public void testLeave() {
    // int second = CanOperations.getNeighborTable(super.get(1)).size();
    //
    // try {
    // Assert.assertTrue(super.get(3).leave());
    // } catch (StructuredP2PException e) {
    // e.printStackTrace();
    // }
    //
    // Assert.assertEquals(second - 1, CanOperations.getNeighborTable(
    // super.get(2)).size());
    //
    // // Does the remote reference of the peer which has left has been
    // // removed from the neighbors table ?
    // Assert.assertFalse(CanOperations.hasNeighbor(super.get(0),
    // super.get(3)
    // .getId()));
    // Assert.assertFalse(CanOperations.hasNeighbor(super.get(1),
    // super.get(3)
    // .getId()));
    // Assert.assertFalse(CanOperations.hasNeighbor(super.get(2),
    // super.get(3)
    // .getId()));
    //
    // // Do peers contain themself in neighbor table ?
    // Assert.assertFalse(CanOperations.hasNeighbor(super.get(0),
    // super.get(0)
    // .getId()));
    // Assert.assertFalse(CanOperations.hasNeighbor(super.get(1),
    // super.get(1)
    // .getId()));
    // Assert.assertFalse(CanOperations.hasNeighbor(super.get(2),
    // super.get(2)
    // .getId()));
    // }

    // @Ignore
    // public void testLeaveComponent() {
    // int second = CanOperations.getNeighborTable(super.getc(1)).size();
    //
    // try {
    // Assert.assertTrue(super.getc(3).leave());
    // } catch (StructuredP2PException e) {
    // e.printStackTrace();
    // }
    //
    // Assert.assertEquals(second - 1, CanOperations.getNeighborTable(
    // super.getc(2)).size());
    //
    // // Does the remote reference of the peer which has left has been
    // // removed from the neighbors table ?
    // Assert.assertFalse(CanOperations.hasNeighbor(super.getc(0), super.getc(
    // 3).getId()));
    // Assert.assertFalse(CanOperations.hasNeighbor(super.getc(1), super.getc(
    // 3).getId()));
    // Assert.assertFalse(CanOperations.hasNeighbor(super.getc(2), super.getc(
    // 3).getId()));
    //
    // // Do peers contain themself in neighbor table ?
    // Assert.assertFalse(CanOperations.hasNeighbor(super.getc(0), super.getc(
    // 0).getId()));
    // Assert.assertFalse(CanOperations.hasNeighbor(super.getc(1), super.getc(
    // 1).getId()));
    // Assert.assertFalse(CanOperations.hasNeighbor(super.getc(2), super.getc(
    // 2).getId()));
    // }

}
