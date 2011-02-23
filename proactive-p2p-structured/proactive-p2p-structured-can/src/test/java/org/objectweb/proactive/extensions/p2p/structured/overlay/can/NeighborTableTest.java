package org.objectweb.proactive.extensions.p2p.structured.overlay.can;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.p2p.structured.api.PeerFactory;
import org.objectweb.proactive.extensions.p2p.structured.intializers.CANNetworkInitializer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.BasicCANOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborTable;

/**
 * Test cases for {@link NeighborTable}.
 * 
 * @author lpellegr
 */
public class NeighborTableTest extends CANNetworkInitializer {

    private Peer peer;

    @Before
    public void setUp() throws Exception {
        this.peer = PeerFactory.newActivePeer(new BasicCANOverlay());
    }

    @Test
    public void testAddAll() throws ActiveObjectCreationException, NodeException {
        Peer firstPeer = PeerFactory.newActivePeer(new BasicCANOverlay());
        Peer secondPeer = PeerFactory.newActivePeer(new BasicCANOverlay());

        NeighborTable neighborTable = new NeighborTable();
        neighborTable.add(new NeighborEntry(firstPeer), 0, 1);
        neighborTable.add(new NeighborEntry(secondPeer), 0, 0);

        Assert.assertTrue(neighborTable.contains(firstPeer.getId(), 0, 1));
        Assert.assertTrue(neighborTable.contains(secondPeer.getId(), 0, 0));

        BasicCANOverlay overlay = 
            ((BasicCANOverlay) PAFuture.getFutureValue(
                    this.peer.getStructuredOverlay()));
        overlay.getNeighborTable().addAll(neighborTable);
        this.peer.setStructuredOverlay(overlay);

        Assert.assertTrue(
                ((BasicCANOverlay) PAFuture.getFutureValue(
                        this.peer.getStructuredOverlay())).getNeighborTable().contains(
                                firstPeer.getId(), 0, 1));
        Assert.assertTrue(((BasicCANOverlay) PAFuture.getFutureValue(
                this.peer.getStructuredOverlay())).getNeighborTable().contains(
                        secondPeer.getId(), 0, 0));
    }

    @After
    public void tearDown() throws Exception {
        PAActiveObject.terminateActiveObject(peer, true);
    }

}
