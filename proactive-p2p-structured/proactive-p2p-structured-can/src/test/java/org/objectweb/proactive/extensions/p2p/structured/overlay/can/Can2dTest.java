package org.objectweb.proactive.extensions.p2p.structured.overlay.can;

import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.api.operations.CanOperations;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.initializers.CanNetworkInitializer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

/**
 * Test used to check visually that all neighbors are correct for each peers
 * when the join operations have finished (in a 2 dimensional CAN network).
 * 
 * @author lpellegr
 */
public class Can2dTest {

    private static CanNetworkInitializer networkInitializer =
            new CanNetworkInitializer();

    @BeforeClass
    public static void setUp() {
        try {
            networkInitializer.initializeNewNetwork(10);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testNeighborhood() {
        P2PStructuredProperties.TRACKER_STORAGE_PROBABILITY.setValue(1.0);

        for (Peer peer : networkInitializer.getPeers()) {
            NeighborTable table = CanOperations.getNeighborTable(peer);
            for (byte dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
                for (byte dir = 0; dir < 2; dir++) {
                    for (NeighborEntry entry : table.get(dim, dir).values()) {
                        Assert.assertTrue(CanOperations.getIdAndZoneResponseOperation(
                                peer)
                                .getPeerZone()
                                .neighbors(entry.getZone()) != -1);
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        P2PStructuredProperties.CAN_REFRESH_TASK_INTERVAL.setValue(1000);
        P2PStructuredProperties.CAN_NB_DIMENSIONS.setValue((byte) 2);
        networkInitializer.initializeNewNetwork(20);

        final List<Peer> peers = new ArrayList<Peer>();
        for (Peer peer : networkInitializer.getPeers()) {
            peers.add(peer);
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Network2DVisualizer(peers).setVisible(true);
            }
        });
    }

}
