package fr.inria.eventcloud.messages.can;

import javax.swing.SwingUtilities;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.api.operations.CanOperations;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborTable;

import fr.inria.eventcloud.initializers.EventCloudInitializer;

/**
 * Test used to check visually that all neighbors are correct for each peers
 * when the join operations have finished (in a 2 dimensional CAN network).
 * 
 * @author lpellegr
 */
public class Can2dTest {

    private static EventCloudInitializer spaceNetworkInitializer = new EventCloudInitializer();

    @BeforeClass
    public static void setUp() {
    	spaceNetworkInitializer.setUpNetworkOnLocalMachine(20);
    }
    
    @Test
    public void testNeighborhood() {
    	P2PStructuredProperties.TRACKER_STORAGE_PROBABILITY.setValue(1.0);
    	
    	for (Peer peer : spaceNetworkInitializer.getRandomTracker().getStoredPeers()) {
    		NeighborTable table = CanOperations.getNeighborTable(peer);
    		for (int dim=0; dim<P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
    			for (int dir=0; dir<2; dir++) {
    				for (NeighborEntry entry: table.get(dim, dir).values()) {
    					Assert.assertTrue(
    							CanOperations.getIdAndZoneResponseOperation(peer)
    								.getPeerZone().neighbors(entry.getZone()) != -1);
    				}
    			}
    		}
    	}
    }

    public static void main(String[] args) {
        P2PStructuredProperties.CAN_REFRESH_TASK_INTERVAL.setValue(1000);
        P2PStructuredProperties.CAN_NB_DIMENSIONS.setValue(2);
        spaceNetworkInitializer.setUpNetworkOnLocalMachine(20);
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Network2DVisualizer(
                        spaceNetworkInitializer.getRandomTracker()
                            .getStoredPeers()).setVisible(true);
            }
        });
    }
    
}
