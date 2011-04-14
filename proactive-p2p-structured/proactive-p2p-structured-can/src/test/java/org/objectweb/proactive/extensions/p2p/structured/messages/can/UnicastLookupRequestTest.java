package org.objectweb.proactive.extensions.p2p.structured.messages.can;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.p2p.structured.api.operations.CanOperations;
import org.objectweb.proactive.extensions.p2p.structured.intializers.CANNetworkInitializer;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.LookupRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.LookupResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayType;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

/**
 * Test case for {@link LookupRequest}.
 * 
 * @author lpellegr
 */
public class UnicastLookupRequestTest extends CANNetworkInitializer {

    @Before
    public void setUp() throws Exception {
        super.initializeNewNetwork(OverlayType.CAN, 10);
    }

    @Test
    public void testLookupQuery() {
        LookupResponse response;
        Peer targetedPeer = super.getRandomPeer();
        
        try {
            response = (LookupResponse) PAFuture.getFutureValue(
            				super.get(0).send(new LookupRequest(
            						CanOperations.getIdAndZoneResponseOperation(
            								targetedPeer).getPeerZone().getLowerBound())));
            
            Assert.assertTrue(response.getLatency() > 0);
            // the peer to reach is maybe the initiator of the request
            Assert.assertTrue(response.getHopCount() >= 0);
            Assert.assertTrue(response.getInboundHopCount() >= 0);
            Assert.assertTrue(response.getOutboundHopCount() >= 0);
             
            Assert.assertEquals(targetedPeer, response.getPeerFound());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() {
        // TODO uncomment when CAN leave works
        // super.clearNetwork();
    }
    
}
