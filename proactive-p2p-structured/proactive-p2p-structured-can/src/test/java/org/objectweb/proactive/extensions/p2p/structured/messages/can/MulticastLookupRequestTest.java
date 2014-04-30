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

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.deployment.CanDeploymentDescriptor;
import org.objectweb.proactive.extensions.p2p.structured.deployment.JunitByClassCanNetworkDeployer;
import org.objectweb.proactive.extensions.p2p.structured.messages.Message;
import org.objectweb.proactive.extensions.p2p.structured.messages.Response;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.EfficientBroadcastRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.MulticastRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.OptimalBroadcastRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.MulticastResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.StringCanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.points.Point;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.points.PointFactory;
import org.objectweb.proactive.extensions.p2p.structured.providers.InjectionConstraintsProvider;
import org.objectweb.proactive.extensions.p2p.structured.providers.ResponseProvider;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.router.can.BroadcastResponseRouter;
import org.objectweb.proactive.extensions.p2p.structured.router.can.EfficientBroadcastRequestRouter;
import org.objectweb.proactive.extensions.p2p.structured.router.can.FloodingBroadcastRequestRouter;
import org.objectweb.proactive.extensions.p2p.structured.router.can.OptimalBroadcastRequestRouter;
import org.objectweb.proactive.extensions.p2p.structured.runners.EachRoutingAlgorithm;
import org.objectweb.proactive.extensions.p2p.structured.runners.RoutingAlgorithm;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.BroadcastConstraintsValidator;

/**
 * Tests associated to {@link FloodingBroadcastRequestRouter} and
 * {@link BroadcastResponseRouter}.
 * 
 * @author lpellegr
 */
@RunWith(EachRoutingAlgorithm.class)
public class MulticastLookupRequestTest extends JunitByClassCanNetworkDeployer {

    private static final int NB_PEERS = 10;

    public MulticastLookupRequestTest() {
        super(
                new CanDeploymentDescriptor<StringCoordinate>(
                        new SerializableProvider<StringCanOverlay>() {
                            private static final long serialVersionUID = 160L;

                            @Override
                            public StringCanOverlay get() {
                                return new CustomCanOverlay();
                            }
                        }).setInjectionConstraintsProvider(InjectionConstraintsProvider.newFractalInjectionConstraintsProvider()),
                1, NB_PEERS);
    }

    @Override
    public void setUp() {
        super.setUp();
    }

    @Test
    public void testMulticastRequestWithResponse() {
        StringCoordinate elt =
                new StringCoordinate(
                        new String(
                                Character.toChars(P2PStructuredProperties.CAN_UPPER_BOUND.getValue() - 1)));

        GetZonesValidatingConstraintsResponse response =
                (GetZonesValidatingConstraintsResponse) PAFuture.getFutureValue(super.getProxy()
                        .send(
                                createGetZoneValidatingConstraintsRequest(new Point<StringCoordinate>(
                                        null, elt, null)), super.getPeer(0)));

        checkResponse(response);

        // check that all zones retrieved validate the constraints
        for (Zone<StringCoordinate> zone : response.getZonesValidatingConstraints()) {
            Assert.assertEquals(0, zone.contains((byte) 1, elt));
        }
    }

    @Test
    public void testMulticastRequestWithoutResponse()
            throws InterruptedException {
        super.getProxy().sendv(createSetValuesRequest());

        // sleep because the previous call is supposed to be asynchronous
        Thread.sleep(1000);

        GetValuesResponse response =
                (GetValuesResponse) PAFuture.getFutureValue(super.getProxy()
                        .send(createGetValuesRequest()));

        checkResponse(response);

        Assert.assertEquals(NB_PEERS, response.getResult().size());

        for (boolean value : response.getResult()) {
            Assert.assertTrue(value);
        }
    }

    @Override
    public void tearDown() {
        super.tearDown();
    }

    private static MulticastRequest<StringCoordinate> createGetZoneValidatingConstraintsRequest(Point<StringCoordinate> coordinatesToReach) {
        switch (RoutingAlgorithm.toUse()) {
            case EFFICIENT_BROADCAST:
                return new GetZonesValidatingConstraintsEfficientRequest(
                        coordinatesToReach);
            case FLOODING_BROADCAST:
                return new GetZonesValidatingConstraintsFloodingRequest(
                        coordinatesToReach);
            case OPTIMAL_BROADCAST:
                return new GetZonesValidatingConstraintsOptimalRequest(
                        coordinatesToReach);
        }

        throw new IllegalStateException();
    }

    private static MulticastRequest<StringCoordinate> createSetValuesRequest() {
        switch (RoutingAlgorithm.toUse()) {
            case EFFICIENT_BROADCAST:
                return new SetValuesEfficientRequest();
            case FLOODING_BROADCAST:
                return new SetValuesFloodingRequest();
            case OPTIMAL_BROADCAST:
                return new SetValuesOptimalRequest();
        }

        throw new IllegalStateException();
    }

    private static MulticastRequest<StringCoordinate> createGetValuesRequest() {
        switch (RoutingAlgorithm.toUse()) {
            case EFFICIENT_BROADCAST:
                return new GetValuesEfficientRequest();
            case FLOODING_BROADCAST:
                return new GetValuesFloodingRequest();
            case OPTIMAL_BROADCAST:
                return new GetValuesOptimalRequest();
        }

        throw new IllegalStateException();
    }

    private static class CustomCanOverlay extends StringCanOverlay {

        private boolean value = false;

    }

    private static class SetValuesFloodingRequest extends
            MulticastRequest<StringCoordinate> {

        private static final long serialVersionUID = 160L;

        public SetValuesFloodingRequest() {
            super(new BroadcastConstraintsValidator<StringCoordinate>(
                    PointFactory.newStringCoordinate()));
        }

        @Override
        public Router<? extends Message<Point<StringCoordinate>>, Point<StringCoordinate>> getRouter() {
            return new FloodingBroadcastRequestRouter<GetZonesValidatingConstraintsFloodingRequest, StringCoordinate>() {
                @Override
                public void onPeerValidatingKeyConstraints(CanOverlay<StringCoordinate> overlay,
                                                           MulticastRequest<StringCoordinate> request) {

                    ((CustomCanOverlay) overlay).value = true;
                };
            };
        }

    }

    private static class SetValuesEfficientRequest extends
            EfficientBroadcastRequest<StringCoordinate> {

        private static final long serialVersionUID = 160L;

        public SetValuesEfficientRequest() {
            super(new BroadcastConstraintsValidator<StringCoordinate>(
                    PointFactory.newStringCoordinate()));
        }

        @Override
        public Router<? extends Message<Point<StringCoordinate>>, Point<StringCoordinate>> getRouter() {
            return new EfficientBroadcastRequestRouter<GetZonesValidatingConstraintsFloodingRequest, StringCoordinate>() {
                @Override
                public void onPeerValidatingKeyConstraints(CanOverlay<StringCoordinate> overlay,
                                                           MulticastRequest<StringCoordinate> request) {

                    ((CustomCanOverlay) overlay).value = true;
                };
            };
        }

    }

    private static class SetValuesOptimalRequest extends
            OptimalBroadcastRequest<StringCoordinate> {

        private static final long serialVersionUID = 160L;

        public SetValuesOptimalRequest() {
            super(new BroadcastConstraintsValidator<StringCoordinate>(
                    PointFactory.newStringCoordinate()));
        }

        @Override
        public Router<? extends Message<Point<StringCoordinate>>, Point<StringCoordinate>> getRouter() {
            return new OptimalBroadcastRequestRouter<GetZonesValidatingConstraintsFloodingRequest, StringCoordinate>() {
                @Override
                public void onPeerValidatingKeyConstraints(CanOverlay<StringCoordinate> overlay,
                                                           MulticastRequest<StringCoordinate> request) {

                    ((CustomCanOverlay) overlay).value = true;
                };
            };
        }

    }

    private static class GetValuesFloodingRequest extends
            MulticastRequest<StringCoordinate> {

        private static final long serialVersionUID = 160L;

        public GetValuesFloodingRequest() {
            super(
                    new BroadcastConstraintsValidator<StringCoordinate>(
                            PointFactory.newStringCoordinate()),
                    new ResponseProvider<MulticastResponse<StringCoordinate>, Point<StringCoordinate>>() {
                        private static final long serialVersionUID = 160L;

                        @Override
                        public MulticastResponse<StringCoordinate> get() {
                            return new GetValuesResponse();
                        }
                    });
        }

        @Override
        public Router<? extends Message<Point<StringCoordinate>>, Point<StringCoordinate>> getRouter() {
            return RoutingAlgorithm.createRouterToUse();
        }

    }

    private static class GetValuesEfficientRequest extends
            EfficientBroadcastRequest<StringCoordinate> {

        private static final long serialVersionUID = 160L;

        public GetValuesEfficientRequest() {
            super(
                    new BroadcastConstraintsValidator<StringCoordinate>(
                            PointFactory.newStringCoordinate()),
                    new ResponseProvider<MulticastResponse<StringCoordinate>, Point<StringCoordinate>>() {
                        private static final long serialVersionUID = 160L;

                        @Override
                        public MulticastResponse<StringCoordinate> get() {
                            return new GetValuesResponse();
                        }
                    });
        }

        @Override
        public Router<? extends Message<Point<StringCoordinate>>, Point<StringCoordinate>> getRouter() {
            return RoutingAlgorithm.createRouterToUse();
        }

    }

    private static class GetValuesOptimalRequest extends
            OptimalBroadcastRequest<StringCoordinate> {

        private static final long serialVersionUID = 160L;

        public GetValuesOptimalRequest() {
            super(
                    new BroadcastConstraintsValidator<StringCoordinate>(
                            PointFactory.newStringCoordinate()),
                    new ResponseProvider<MulticastResponse<StringCoordinate>, Point<StringCoordinate>>() {
                        private static final long serialVersionUID = 160L;

                        @Override
                        public MulticastResponse<StringCoordinate> get() {
                            return new GetValuesResponse();
                        }
                    });
        }

        @Override
        public Router<? extends Message<Point<StringCoordinate>>, Point<StringCoordinate>> getRouter() {
            return RoutingAlgorithm.createRouterToUse();
        }

    }

    private static class GetValuesResponse extends
            MulticastResponse<StringCoordinate> {

        private static final long serialVersionUID = 160L;

        private List<Boolean> result = new ArrayList<Boolean>();

        @Override
        public void beforeSendingBackResponse(StructuredOverlay overlay) {
            if (this.validatesRequestKeyConstraints(overlay)) {
                this.result.add(((CustomCanOverlay) overlay).value);
            }
        }

        @Override
        public void mergeAttributes(MulticastResponse<StringCoordinate> responseReceived) {
            this.result.addAll(((GetValuesResponse) responseReceived).getResult());
        }

        public List<Boolean> getResult() {
            return this.result;
        }

    }

    private static class GetZonesValidatingConstraintsFloodingRequest extends
            MulticastRequest<StringCoordinate> {

        private static final long serialVersionUID = 160L;

        public GetZonesValidatingConstraintsFloodingRequest(
                Point<StringCoordinate> coordinatesToReach) {
            super(
                    new BroadcastConstraintsValidator<StringCoordinate>(
                            coordinatesToReach),
                    new ResponseProvider<MulticastResponse<StringCoordinate>, Point<StringCoordinate>>() {
                        private static final long serialVersionUID = 160L;

                        @Override
                        public MulticastResponse<StringCoordinate> get() {
                            return new GetZonesValidatingConstraintsResponse();
                        }
                    });
        }

        @Override
        public Router<? extends Message<Point<StringCoordinate>>, Point<StringCoordinate>> getRouter() {
            return RoutingAlgorithm.createRouterToUse();
        }

    }

    private static class GetZonesValidatingConstraintsEfficientRequest extends
            EfficientBroadcastRequest<StringCoordinate> {

        private static final long serialVersionUID = 160L;

        public GetZonesValidatingConstraintsEfficientRequest(
                Point<StringCoordinate> coordinatesToReach) {
            super(
                    new BroadcastConstraintsValidator<StringCoordinate>(
                            coordinatesToReach),
                    new ResponseProvider<MulticastResponse<StringCoordinate>, Point<StringCoordinate>>() {
                        private static final long serialVersionUID = 160L;

                        @Override
                        public MulticastResponse<StringCoordinate> get() {
                            return new GetZonesValidatingConstraintsResponse();
                        }
                    });
        }

        @Override
        public Router<? extends Message<Point<StringCoordinate>>, Point<StringCoordinate>> getRouter() {
            return RoutingAlgorithm.createRouterToUse();
        }

    }

    private static class GetZonesValidatingConstraintsOptimalRequest extends
            OptimalBroadcastRequest<StringCoordinate> {

        private static final long serialVersionUID = 160L;

        public GetZonesValidatingConstraintsOptimalRequest(
                Point<StringCoordinate> coordinatesToReach) {
            super(
                    new BroadcastConstraintsValidator<StringCoordinate>(
                            coordinatesToReach),
                    new ResponseProvider<MulticastResponse<StringCoordinate>, Point<StringCoordinate>>() {
                        private static final long serialVersionUID = 160L;

                        @Override
                        public MulticastResponse<StringCoordinate> get() {
                            return new GetZonesValidatingConstraintsResponse();
                        }
                    });
        }

        @Override
        public Router<? extends Message<Point<StringCoordinate>>, Point<StringCoordinate>> getRouter() {
            return RoutingAlgorithm.createRouterToUse();
        }

    }

    private static class GetZonesValidatingConstraintsResponse extends
            MulticastResponse<StringCoordinate> {

        private static final long serialVersionUID = 160L;

        private List<Zone<StringCoordinate>> zonesValidatingConstraints =
                new ArrayList<Zone<StringCoordinate>>();

        @Override
        public void beforeSendingBackResponse(StructuredOverlay overlay) {
            if (this.validatesRequestKeyConstraints(overlay)) {
                this.zonesValidatingConstraints.add(((StringCanOverlay) overlay).getZone());
            }
        }

        @Override
        public void mergeAttributes(MulticastResponse<StringCoordinate> responseReceived) {
            this.zonesValidatingConstraints.addAll(((GetZonesValidatingConstraintsResponse) responseReceived).zonesValidatingConstraints);
        }

        public List<Zone<StringCoordinate>> getZonesValidatingConstraints() {
            return this.zonesValidatingConstraints;
        }

    }

    private static <T> void checkResponse(Response<T> response) {
        Assert.assertTrue(response.getLatency() > 0);
        Assert.assertTrue(response.getOutboundHopCount() > 0);
        Assert.assertTrue(response.getInboundHopCount() > 0);

        // the following condition should be true for the current implementation
        // of the anycast router because the reverse path is the same as the
        // forward path
        Assert.assertEquals(
                response.getInboundHopCount(), response.getOutboundHopCount());
    }

}
