package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.api.operations.CanOperations;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkNotJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.intializers.CANNetworkInitializer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.util.Pair;

/**
 * Test cases for the Join operation in {@link AbstractCanOverlay}.
 * 
 * @author lpellegr
 */
public class LeaveOperationTest {

    private CANNetworkInitializer networkInitializer;

    @Before
    public void setUp() {
        this.networkInitializer = new CANNetworkInitializer();
    }

    @Test
    public void testCanMerge() {
        Zone z1 = new Zone();
        Pair<Zone> z1Split = z1.split((byte) 0);
        Zone z2 = z1Split.getFirst();
        Zone z3 = z1Split.getSecond();

        // two zones: z1, z2
        assertTrue(z2.canMerge(z3, (byte) 0));

        Pair<Zone> z3Split = z3.split((byte) 1);
        Zone z4 = z3Split.getFirst();
        Zone z5 = z3Split.getSecond();

        // three zones: z2, z4, z5
        assertFalse(z2.canMerge(z4, (byte) 0));
        assertFalse(z2.canMerge(z5, (byte) 0));
        assertTrue(z4.canMerge(z5, (byte) 1));
    }

    @Test
    public void testLeaveWithOnePeer() {
        this.networkInitializer.initializeNewNetwork(1);
        try {
            assertTrue(this.networkInitializer.get(0).leave());
        } catch (NetworkNotJoinedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testLeaveWithTwoPeers() {
        this.networkInitializer.initializeNewNetwork(2);

        UUID peerLeavingId = this.networkInitializer.get(0).getId();

        try {
            assertTrue(this.networkInitializer.get(0).leave());
        } catch (NetworkNotJoinedException e) {
            e.printStackTrace();
        }

        assertFalse(CanOperations.hasNeighbor(
                this.networkInitializer.get(1), peerLeavingId));
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

    @After
    public void tearDown() throws Exception {
        networkInitializer.clearNetwork();
    }

}
