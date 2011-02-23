package fr.inria.eventcloud.messages.can;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.api.operations.CanOperations;
import org.objectweb.proactive.extensions.p2p.structured.configuration.DefaultProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborTable;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.BigDecimalElement;

import fr.inria.eventcloud.initializers.SpaceNetworkInitializer;
import fr.inria.eventcloud.util.RDF2GoBuilder;

/**
 * Test used to check that all neighbors are correct for each peers 
 * after the join operations have finished in a 2 dimensional CAN
 * network.
 * 
 * @author lpellegr
 */
public class Can2dTest {

    private static SpaceNetworkInitializer spaceNetworkInitializer = new SpaceNetworkInitializer(
            RDF2GoBuilder.createURI("http://www.inria.fr"));

    @BeforeClass
    public static void setUp() {
    	spaceNetworkInitializer.setUpNetworkOnLocalMachine(10);
    }
    
    @Test
    public void testNeighborhood() {
    	DefaultProperties.TRACKER_STORAGE_PROBABILITY.setValue(1.0);
    	
    	for (Peer peer : spaceNetworkInitializer.getRandomTracker().getStoredPeers()) {
    		NeighborTable table = CanOperations.getNeighborTable(peer);
    		for (int dim=0; dim<DefaultProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
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
        DefaultProperties.CAN_REFRESH_TASK_INTERVAL.setValue(500);
        DefaultProperties.CAN_NB_DIMENSIONS.setValue(2);
        DefaultProperties.CAN_UPPER_BOUND.setValue("1");
        DefaultProperties.CAN_COORDINATE_TYPE.setValue(BigDecimalElement.class.getCanonicalName());
        spaceNetworkInitializer.setUpNetworkOnLocalMachine(10);
        
        new Thread(new Runnable() {
			@Override
			public void run() {
				new Network2DVisualizer(
						spaceNetworkInitializer.getRandomTracker().getStoredPeers())
							.setVisible(true);
			}
		}).start();
    }
    
}
