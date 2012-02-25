/**
 * Copyright (c) 2011-2012 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package org.objectweb.proactive.extensions.p2p.structured.messages.can;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.p2p.structured.deployment.JunitByClassParameterizedCanNetworkDeployer;
import org.objectweb.proactive.extensions.p2p.structured.deployment.NetworkDeployer;
import org.objectweb.proactive.extensions.p2p.structured.messages.RequestResponseMessage;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.AnycastResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.router.can.AnycastRequestRouter;
import org.objectweb.proactive.extensions.p2p.structured.router.can.AnycastResponseRouter;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.DefaultAnycastConstraintsValidator;

/**
 * Tests associated to {@link AnycastRequestRouter} and
 * {@link AnycastResponseRouter}.
 * 
 * @author lpellegr
 */
public class AnycastLookupRequestTest extends
        JunitByClassParameterizedCanNetworkDeployer {

    public AnycastLookupRequestTest(NetworkDeployer deployer) {
        super(deployer, 1, 10);
    }

    @Test
    public void lookupQueryTest() {
        AnycastLookupResponse response = null;
        StringElement elt = new StringElement("Z");

        try {
            response =
                    (AnycastLookupResponse) PAFuture.getFutureValue(super.getPeer(
                            0)
                            .send(
                                    new AnycastLookupRequest(
                                            new StringCoordinate(
                                                    null, elt, null))));

            Assert.assertTrue(response.getLatency() > 0);
            // the peer to reach can be the initiator of the request
            Assert.assertTrue(response.getHopCount() > 0);
            Assert.assertTrue(response.getInboundHopCount() > 0);
            Assert.assertTrue(response.getOutboundHopCount() > 0);

            // check that all zones retrieved validate the constraints
            for (Zone zone : response.getZonesValidatingConstraints()) {
                Assert.assertEquals(0, zone.contains((byte) 1, elt));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class AnycastLookupRequest extends AnycastRequest {

        private static final long serialVersionUID = 1L;

        private Set<Zone> zonesValidatingConstraints;

        public AnycastLookupRequest(StringCoordinate coordinatesToReach) {
            super(new DefaultAnycastConstraintsValidator(coordinatesToReach));
            this.zonesValidatingConstraints = new HashSet<Zone>();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public AnycastResponse createResponse(StructuredOverlay overlay) {
            return new AnycastLookupResponse(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Router<? extends RequestResponseMessage<StringCoordinate>, StringCoordinate> getRouter() {
            return new AnycastRequestRouter<AnycastLookupRequest>() {
                @Override
                public void onPeerValidatingKeyConstraints(CanOverlay overlay,
                                                           AnycastRequest request) {
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

    public static class AnycastLookupResponse extends AnycastResponse {

        private static final long serialVersionUID = 1L;

        private Set<Zone> zonesValidatingConstraints;

        public AnycastLookupResponse(AnycastLookupRequest request) {
            super(request);
            this.zonesValidatingConstraints =
                    request.getZonesValidatingConstraints();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void addSubResult(AnycastResponse subResponse) {
            super.incrementHopCount(subResponse.getHopCount());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Router<? extends RequestResponseMessage<StringCoordinate>, StringCoordinate> getRouter() {
            return new AnycastResponseRouter<AnycastResponse>();
        }

        public Set<Zone> getZonesValidatingConstraints() {
            return this.zonesValidatingConstraints;
        }

    }

}
