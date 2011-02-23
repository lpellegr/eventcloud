package org.objectweb.proactive.extensions.p2p.structured.messages.can;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.p2p.structured.intializers.CANNetworkInitializer;
import org.objectweb.proactive.extensions.p2p.structured.messages.reply.can.LookupReply;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.LookupRequest;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.GetIdAndZoneOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.GetIdAndZoneResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayType;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

/**
 * Test case for {@link LookupRequest}.
 * 
 * @author lpellegr
 */
public class LookupRequestTest extends CANNetworkInitializer {

    private LookupRequest query;

    private Peer remotePeerToReach;
    
    @Before
    public void setUp() throws Exception {
        super.initializeNewNetwork(OverlayType.CAN, 5);
        this.remotePeerToReach = super.getRandomPeer();
        this.query = 
            new LookupRequest(
                    ((GetIdAndZoneResponseOperation) PAFuture.getFutureValue(
                            this.remotePeerToReach.receiveOperationIS(
                                    new GetIdAndZoneOperation()))).getPeerZone().getLowerBound());
    }

    @Test
    public void testLookupQuery() {
        LookupReply response = null;
        try {
            response = (LookupReply) PAFuture.getFutureValue(super.get(0).send(this.query));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertTrue(response.getLatency() > 1);
        // the peer to reach can be the initiator of the request
        Assert.assertTrue(response.getHopCount() >= 0);
        Assert.assertTrue(response.getInboundHopCount() >= 0);
        Assert.assertTrue(response.getOutboundHopCount() >= 0);
         
        Assert.assertEquals(this.remotePeerToReach, response.getPeerFound());
    }

    @After
    public void tearDown() {
        // TODO uncomment when CAN leave works
        // super.clearNetwork();
    }
}
