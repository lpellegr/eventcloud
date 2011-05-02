package org.objectweb.proactive.extensions.p2p.structured.overlay.can;

import junit.framework.Assert;

import org.etsi.uri.gcm.api.control.GCMLifeCycleController;
import org.etsi.uri.gcm.util.GCM;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.fractal.api.Interface;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.p2p.structured.api.PeerFactory;
import org.objectweb.proactive.extensions.p2p.structured.intializers.CANNetworkInitializer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.BasicCanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

/**
 * Test cases for {@link NeighborTable}.
 * 
 * @author lpellegr
 */
public class NeighborTableTest extends CANNetworkInitializer {

    private Peer peer;

    private Peer componentPeer;

    @Before
    public void setUp() throws Exception {
        this.peer = PeerFactory.newActivePeer(new BasicCanOverlay());
        this.componentPeer =
                PeerFactory.newComponentPeer(new BasicCanOverlay());
    }

    @Test
    public void testAddAll() {
        Peer firstPeer = PeerFactory.newActivePeer(new BasicCanOverlay());
        Peer secondPeer = PeerFactory.newActivePeer(new BasicCanOverlay());

        NeighborTable neighborTable = new NeighborTable();
        neighborTable.add(new NeighborEntry(firstPeer), (byte) 0, (byte) 1);
        neighborTable.add(new NeighborEntry(secondPeer), (byte) 0, (byte) 0);

        Assert.assertTrue(neighborTable.contains(
                firstPeer.getId(), (byte) 0, (byte) 1));
        Assert.assertTrue(neighborTable.contains(
                secondPeer.getId(), (byte) 0, (byte) 0));

        BasicCanOverlay overlay =
                ((BasicCanOverlay) PAFuture.getFutureValue(this.peer.getOverlay()));
        overlay.getNeighborTable().addAll(neighborTable);
        this.peer.setOverlay(overlay);

        Assert.assertTrue(((BasicCanOverlay) PAFuture.getFutureValue(this.peer.getOverlay())).getNeighborTable()
                .contains(firstPeer.getId(), (byte) 0, (byte) 1));
        Assert.assertTrue(((BasicCanOverlay) PAFuture.getFutureValue(this.peer.getOverlay())).getNeighborTable()
                .contains(secondPeer.getId(), (byte) 0, (byte) 0));
    }

    @Test
    public void testAddAllComponent() {
        Peer firstPeer = PeerFactory.newComponentPeer(new BasicCanOverlay());
        Peer secondPeer = PeerFactory.newComponentPeer(new BasicCanOverlay());

        NeighborTable neighborTable = new NeighborTable();
        neighborTable.add(new NeighborEntry(firstPeer), (byte) 0, (byte) 1);
        neighborTable.add(new NeighborEntry(secondPeer), (byte) 0, (byte) 0);

        Assert.assertTrue(neighborTable.contains(
                firstPeer.getId(), (byte) 0, (byte) 1));
        Assert.assertTrue(neighborTable.contains(
                secondPeer.getId(), (byte) 0, (byte) 0));

        BasicCanOverlay overlay =
                ((BasicCanOverlay) PAFuture.getFutureValue(this.componentPeer.getOverlay()));
        overlay.getNeighborTable().addAll(neighborTable);
        this.componentPeer.setOverlay(overlay);

        Assert.assertTrue(((BasicCanOverlay) PAFuture.getFutureValue(this.componentPeer.getOverlay())).getNeighborTable()
                .contains(firstPeer.getId(), (byte) 0, (byte) 1));
        Assert.assertTrue(((BasicCanOverlay) PAFuture.getFutureValue(this.componentPeer.getOverlay())).getNeighborTable()
                .contains(secondPeer.getId(), (byte) 0, (byte) 0));
    }

    @After
    public void tearDown() throws Exception {
        PAActiveObject.terminateActiveObject(this.peer, true);
        GCMLifeCycleController lc =
                GCM.getGCMLifeCycleController(((Interface) this.componentPeer).getFcItfOwner());
        lc.stopFc();
        lc.terminateGCMComponent();
    }

}
