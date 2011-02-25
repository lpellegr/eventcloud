package org.objectweb.proactive.extensions.p2p.structured.messages.can;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.p2p.structured.intializers.CANNetworkInitializer;
import org.objectweb.proactive.extensions.p2p.structured.messages.RequestReplyMessage;
import org.objectweb.proactive.extensions.p2p.structured.messages.reply.can.AnycastReply;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayType;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.AbstractCanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.StringElement;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.router.can.AnycastReplyRouter;
import org.objectweb.proactive.extensions.p2p.structured.router.can.AnycastRequestRouter;
import org.objectweb.proactive.extensions.p2p.structured.validator.ConstraintsValidator;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.AnycastConstraintsValidator;

/**
 * Tests associated to {@link AnycastRequestRouter} and {@link AnycastReplyRouter}. 
 * 
 * @author lpellegr
 */
public class AnycastLookupRequestTest extends CANNetworkInitializer {

    @Before
    public void setUp() throws Exception {
        super.initializeNewNetwork(OverlayType.CAN, 10);
    }

    @Test
    public void testLookupQuery() {
        AnycastLookupReply reply = null;
        StringElement elt = new StringElement("Z");
        
        try {
            reply = (AnycastLookupReply) PAFuture.getFutureValue(
            				super.get(0).send(new AnycastLookupRequest(
            						new Coordinate(null, elt, null))));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertTrue(reply.getLatency() > 1);
        // the peer to reach can be the initiator of the request
        Assert.assertTrue(reply.getHopCount() >= 0);
        Assert.assertTrue(reply.getInboundHopCount() >= 0);
        Assert.assertTrue(reply.getOutboundHopCount() >= 0);

        // check that all zones retrieved validate the constraints
        for (Zone zone : reply.getZonesValidatingConstraints()) {
        	Assert.assertEquals(0, zone.contains(1, elt));
        }
    }

    @After
    public void tearDown() {
        // TODO uncomment when CAN leave works
        // super.clearNetwork();
    }
    
    public static class AnycastLookupRequest extends AnycastRequest {

    	private static final long serialVersionUID = 1L;

    	private Set<Zone> zonesValidatingConstraints;
    	
		public AnycastLookupRequest(Coordinate coordinatesToReach) {
			super(coordinatesToReach);
			this.zonesValidatingConstraints = new HashSet<Zone>();
		}

		@Override
		public AnycastReply createResponseMessage() {
			return new AnycastLookupReply(this, this.getKeyToReach());
		}

		@Override
		public Router<? extends RequestReplyMessage<Coordinate>, Coordinate> getRouter() {
			ConstraintsValidator<Coordinate> validator = new AnycastConstraintsValidator<Coordinate>() {
				public boolean validatesKeyConstraints(StructuredOverlay overlay, Coordinate key) {
					return this.validatesKeyConstraints(
								((AbstractCanOverlay) overlay).getZone(), key);
				}

				public boolean validatesKeyConstraints(Zone zone, Coordinate key) {
					for (int i = 0; i < key.size(); i++) {
						// if coordinate is null we skip the test
						if (key.getElement(i) != null) {
							// the specified overlay does not contains the key
							if (zone.contains(i, key.getElement(i)) != 0) {
								return false;
							}
						}
					}
					return true;
				}
			};
	        
	        return new AnycastRequestRouter<AnycastLookupRequest>(validator) {
	            public void onPeerWhichValidatesKeyConstraints(
	                    AbstractCanOverlay overlay, AnycastRequest request) {
	            	((AnycastLookupRequest) request).add(overlay.getZone());
	            };
	        };
		}

		public boolean add(Zone zone) {
			return this.zonesValidatingConstraints.add(zone);
		}

		public Set<Zone> getZonesValidatingConstraints() {
			return this.zonesValidatingConstraints;
		}

    }
    
    public static class AnycastLookupReply extends AnycastReply {

    	private static final long serialVersionUID = 1L;

    	private Set<Zone> zonesValidatingConstraints;
    	
    	public AnycastLookupReply(AnycastLookupRequest request, Coordinate keyToReach) {
			super(request, keyToReach);
			this.zonesValidatingConstraints = request.getZonesValidatingConstraints();
		}

		@Override
		public void merge(AnycastReply subreply) {
			super.incrementHopCount(subreply.getHopCount());
		}

		@Override
		public Router<? extends RequestReplyMessage<Coordinate>, Coordinate> getRouter() {
			return new AnycastReplyRouter<AnycastReply>();
		}

		public Set<Zone> getZonesValidatingConstraints() {
			return this.zonesValidatingConstraints;
		}
		
    }

}
