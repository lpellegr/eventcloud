package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.objectweb.proactive.extensions.p2p.structured.api.operations.CanOperations;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.StructuredP2PException;
import org.objectweb.proactive.extensions.p2p.structured.intializers.CANNetworkInitializer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayType;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCanOverlay;

/**
 * Test cases for the Join operation in {@link AbstractCanOverlay}.
 * 
 * @author lpellegr
 */
public class LeaveOperationTest extends CANNetworkInitializer {

    @Before
    public void setUp() throws Exception {
        super.initializeNewNetwork(OverlayType.CAN, 4);
    }

    @Ignore
    public void testLeave() {
        int second = CanOperations.getNeighborTable(super.get(1)).size();

        try {
            Assert.assertTrue(super.get(3).leave());
        } catch (StructuredP2PException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(second - 1, CanOperations.getNeighborTable(
                super.get(2)).size());

        // Does the remote reference of the peer which has left has been
        // removed from the neighbors table ?
        Assert.assertFalse(CanOperations.hasNeighbor(super.get(0), super.get(3)
                .getId()));
        Assert.assertFalse(CanOperations.hasNeighbor(super.get(1), super.get(3)
                .getId()));
        Assert.assertFalse(CanOperations.hasNeighbor(super.get(2), super.get(3)
                .getId()));

        // Do peers contain themself in neighbor table ?
        Assert.assertFalse(CanOperations.hasNeighbor(super.get(0), super.get(0)
                .getId()));
        Assert.assertFalse(CanOperations.hasNeighbor(super.get(1), super.get(1)
                .getId()));
        Assert.assertFalse(CanOperations.hasNeighbor(super.get(2), super.get(2)
                .getId()));
    }

    @Ignore
    public void testLeaveComponent() {
        int second = CanOperations.getNeighborTable(super.getc(1)).size();

        try {
            Assert.assertTrue(super.getc(3).leave());
        } catch (StructuredP2PException e) {
            e.printStackTrace();
        }

        Assert.assertEquals(second - 1, CanOperations.getNeighborTable(
                super.getc(2)).size());

        // Does the remote reference of the peer which has left has been
        // removed from the neighbors table ?
        Assert.assertFalse(CanOperations.hasNeighbor(super.getc(0), super.getc(
                3).getId()));
        Assert.assertFalse(CanOperations.hasNeighbor(super.getc(1), super.getc(
                3).getId()));
        Assert.assertFalse(CanOperations.hasNeighbor(super.getc(2), super.getc(
                3).getId()));

        // Do peers contain themself in neighbor table ?
        Assert.assertFalse(CanOperations.hasNeighbor(super.getc(0), super.getc(
                0).getId()));
        Assert.assertFalse(CanOperations.hasNeighbor(super.getc(1), super.getc(
                1).getId()));
        Assert.assertFalse(CanOperations.hasNeighbor(super.getc(2), super.getc(
                2).getId()));
    }

    @After
    public void tearDown() throws Exception {
        // super.clearNetwork();
    }

}
