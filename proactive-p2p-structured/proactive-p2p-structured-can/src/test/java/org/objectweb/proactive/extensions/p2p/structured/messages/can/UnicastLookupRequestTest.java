/**
 * Copyright (c) 2011-2014 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package org.objectweb.proactive.extensions.p2p.structured.messages.can;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.p2p.structured.deployment.CanDeploymentDescriptor;
import org.objectweb.proactive.extensions.p2p.structured.deployment.JunitByClassCanNetworkDeployer;
import org.objectweb.proactive.extensions.p2p.structured.messages.Request;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.ForwardRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.LookupRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.LookupResponse;
import org.objectweb.proactive.extensions.p2p.structured.operations.CanOperations;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.StringCanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.points.Point;
import org.objectweb.proactive.extensions.p2p.structured.providers.InjectionConstraintsProvider;
import org.objectweb.proactive.extensions.p2p.structured.providers.ResponseProvider;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.router.can.UnicastRequestRouter;

/**
 * Test cases for {@link LookupRequest} and {@link ForwardRequest}.
 * 
 * @author lpellegr
 */
public class UnicastLookupRequestTest extends JunitByClassCanNetworkDeployer {

    private Peer target;

    private Point<StringCoordinate> targetLowerBound;

    public UnicastLookupRequestTest() {
        super(
                new CanDeploymentDescriptor<StringCoordinate>(
                        new SerializableProvider<CustomCanOverlay>() {
                            private static final long serialVersionUID = 160L;

                            @Override
                            public CustomCanOverlay get() {
                                return new CustomCanOverlay();
                            }
                        }).setInjectionConstraintsProvider(InjectionConstraintsProvider.newFractalInjectionConstraintsProvider()),
                1, 10);
    }

    @Override
    public void setUp() {
        super.setUp();

        this.target = super.getPeer(9);
        this.targetLowerBound =
                CanOperations.<StringCoordinate> getIdAndZoneResponseOperation(
                        this.target).getPeerZone().getLowerBound();
    }

    @Test
    public void testUnicastLookupRequestWithResponse() {
        @SuppressWarnings({"unchecked"})
        LookupResponse<StringCoordinate> response =
                (LookupResponse<StringCoordinate>) PAFuture.getFutureValue(super.getProxy()
                        .send(
                                new LookupRequest<StringCoordinate>(
                                        this.targetLowerBound),
                                super.getPeer(0)));

        checkResponse(response, this.target);
    }

    @Test
    public void testUnicastForwardRequestWithoutResponse() {
        super.getProxy().sendv(
                new SetStateRequest(this.targetLowerBound), super.getPeer(0));

        // sleep because the previous call is supposed to be asynchronous
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        GetStateResponse response =
                (GetStateResponse) PAFuture.getFutureValue(super.getProxy()
                        .send(
                                new GetStateRequest(this.targetLowerBound),
                                super.getPeer(0)));

        Assert.assertTrue(response.getValue());
    }

    @Override
    public void tearDown() {
        super.tearDown();
    }

    private static class CustomCanOverlay extends StringCanOverlay {

        private boolean value = false;

    }

    private static class SetStateRequest extends
            ForwardRequest<StringCoordinate> {

        private static final long serialVersionUID = 160L;

        public SetStateRequest(Point<StringCoordinate> coordinateToReach) {
            super(coordinateToReach, null);
        }

        @Override
        public Router<ForwardRequest<StringCoordinate>, Point<StringCoordinate>> getRouter() {
            return new UnicastRequestRouter<ForwardRequest<StringCoordinate>, StringCoordinate>() {
                @Override
                protected void onDestinationReached(StructuredOverlay overlay,
                                                    ForwardRequest<StringCoordinate> msg) {
                    ((CustomCanOverlay) overlay).value = true;
                }
            };
        }

    }

    private static class GetStateRequest extends
            LookupRequest<StringCoordinate> {

        private static final long serialVersionUID = 160L;

        private boolean value;

        public GetStateRequest(Point<StringCoordinate> coordinateToReach) {
            super(
                    coordinateToReach,
                    new ResponseProvider<GetStateResponse, Point<StringCoordinate>>() {
                        private static final long serialVersionUID = 160L;

                        @Override
                        public GetStateResponse get() {
                            return new GetStateResponse();
                        }
                    });
        }

        @Override
        public Router<ForwardRequest<StringCoordinate>, Point<StringCoordinate>> getRouter() {
            return new UnicastRequestRouter<ForwardRequest<StringCoordinate>, StringCoordinate>() {
                @Override
                protected void onDestinationReached(StructuredOverlay overlay,
                                                    ForwardRequest<StringCoordinate> msg) {
                    GetStateRequest.this.value =
                            ((CustomCanOverlay) overlay).value;
                }
            };
        }
    }

    private static class GetStateResponse extends
            LookupResponse<StringCoordinate> {

        private static final long serialVersionUID = 160L;

        private boolean value;

        @Override
        public void setAttributes(Request<Point<StringCoordinate>> request,
                                  StructuredOverlay overlay) {
            super.setAttributes(request, overlay);

            this.value = ((GetStateRequest) request).value;
        }

        public boolean getValue() {
            return this.value;
        }

    }

    private static <T> void checkResponse(LookupResponse<StringCoordinate> response,
                                          Peer target) {
        Assert.assertTrue(response.getLatency() >= 0);
        Assert.assertTrue(response.getHopCount() >= 0);
        Assert.assertTrue(response.getInboundHopCount() >= 0);
        Assert.assertTrue(response.getOutboundHopCount() > 0);

        Assert.assertEquals(target, response.getPeerFound());
    }

}
