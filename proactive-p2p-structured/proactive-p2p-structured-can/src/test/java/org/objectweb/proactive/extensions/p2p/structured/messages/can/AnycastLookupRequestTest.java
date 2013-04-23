/**
 * Copyright (c) 2011-2013 INRIA.
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.deployment.CanDeploymentDescriptor;
import org.objectweb.proactive.extensions.p2p.structured.deployment.JunitByClassCanNetworkDeployer;
import org.objectweb.proactive.extensions.p2p.structured.messages.RequestResponseMessage;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.Response;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.ResponseProvider;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.AnycastResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.StringCanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.CoordinateFactory;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;
import org.objectweb.proactive.extensions.p2p.structured.providers.InjectionConstraintsProvider;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;
import org.objectweb.proactive.extensions.p2p.structured.proxies.Proxies;
import org.objectweb.proactive.extensions.p2p.structured.proxies.Proxy;
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
public class AnycastLookupRequestTest extends JunitByClassCanNetworkDeployer {

    private static final int NB_PEERS = 10;

    private Proxy proxy;

    public AnycastLookupRequestTest() {
        super(
                new CanDeploymentDescriptor<StringElement>(
                        new SerializableProvider<StringCanOverlay>() {
                            private static final long serialVersionUID = 150L;

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

        this.proxy = Proxies.newProxy(super.getRandomTracker());
    }

    @Test
    public void testAnycastRequestWithResponse() {
        StringElement elt =
                new StringElement(
                        new String(
                                Character.toChars(P2PStructuredProperties.CAN_UPPER_BOUND.getValue() - 1)));

        GetZonesValidatingConstraintsResponse response =
                (GetZonesValidatingConstraintsResponse) PAFuture.getFutureValue(this.proxy.send(
                        new GetZonesValidatingConstraintsRequest(
                                new Coordinate<StringElement>(null, elt, null)),
                        super.getPeer(0)));

        checkResponse(response);

        // check that all zones retrieved validate the constraints
        for (Zone<StringElement> zone : response.getZonesValidatingConstraints()) {
            Assert.assertEquals(0, zone.contains((byte) 1, elt));
        }
    }

    @Test
    public void testAnycastRequestWithoutResponse() throws InterruptedException {
        this.proxy.sendv(new SetValuesRequest());

        // sleep because the previous call is supposed to be asynchronous
        Thread.sleep(1000);

        GetValuesResponse response =
                (GetValuesResponse) PAFuture.getFutureValue(this.proxy.send(new GetValuesRequest()));

        checkResponse(response);

        Assert.assertEquals(NB_PEERS, response.getResult().size());

        for (boolean value : response.getResult()) {
            Assert.assertTrue(value);
        }
    }

    @Override
    public void tearDown() {
        super.tearDown();

        try {
            this.proxy.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class CustomCanOverlay extends StringCanOverlay {

        private boolean value = false;

    }

    private static class SetValuesRequest extends AnycastRequest<StringElement> {

        private static final long serialVersionUID = 150L;

        public SetValuesRequest() {
            super(new DefaultAnycastConstraintsValidator<StringElement>(
                    CoordinateFactory.newStringCoordinate()));
        }

        @Override
        public Router<? extends RequestResponseMessage<Coordinate<StringElement>>, Coordinate<StringElement>> getRouter() {
            return new AnycastRequestRouter<GetZonesValidatingConstraintsRequest, StringElement>() {
                @Override
                public void onPeerValidatingKeyConstraints(CanOverlay<StringElement> overlay,
                                                           AnycastRequest<StringElement> request) {
                    ((CustomCanOverlay) overlay).value = true;
                };
            };
        }

    }

    private static class GetValuesRequest extends AnycastRequest<StringElement> {

        private static final long serialVersionUID = 150L;

        public GetValuesRequest() {
            super(
                    new DefaultAnycastConstraintsValidator<StringElement>(
                            CoordinateFactory.newStringCoordinate()),
                    new ResponseProvider<AnycastResponse<StringElement>, Coordinate<StringElement>>() {
                        private static final long serialVersionUID = 150L;

                        @Override
                        public AnycastResponse<StringElement> get() {
                            return new GetValuesResponse();
                        }
                    });
        }

    }

    private static class GetValuesResponse extends
            AnycastResponse<StringElement> {

        private static final long serialVersionUID = 150L;

        private List<Boolean> result = new ArrayList<Boolean>();

        @Override
        public void synchronizationPointUnlocked(StructuredOverlay overlay) {
            if (this.validatesKeyConstraints(overlay)) {
                this.result.add(((CustomCanOverlay) overlay).value);
            }
        }

        @Override
        public void mergeAttributes(AnycastResponse<StringElement> responseReceived) {
            this.result.addAll(((GetValuesResponse) responseReceived).getResult());
        }

        public List<Boolean> getResult() {
            return this.result;
        }

    }

    private static class GetZonesValidatingConstraintsRequest extends
            AnycastRequest<StringElement> {

        private static final long serialVersionUID = 150L;

        public GetZonesValidatingConstraintsRequest(
                Coordinate<StringElement> coordinatesToReach) {
            super(
                    new DefaultAnycastConstraintsValidator<StringElement>(
                            coordinatesToReach),
                    new ResponseProvider<AnycastResponse<StringElement>, Coordinate<StringElement>>() {
                        private static final long serialVersionUID = 150L;

                        @Override
                        public AnycastResponse<StringElement> get() {
                            return new GetZonesValidatingConstraintsResponse();
                        }
                    });
        }

    }

    private static class GetZonesValidatingConstraintsResponse extends
            AnycastResponse<StringElement> {

        private static final long serialVersionUID = 150L;

        private List<Zone<StringElement>> zonesValidatingConstraints =
                new ArrayList<Zone<StringElement>>();

        @Override
        public void synchronizationPointUnlocked(StructuredOverlay overlay) {
            if (this.validatesKeyConstraints(overlay)) {
                this.zonesValidatingConstraints.add(((StringCanOverlay) overlay).getZone());
            }
        }

        @Override
        public void mergeAttributes(AnycastResponse<StringElement> responseReceived) {
            this.zonesValidatingConstraints.addAll(((GetZonesValidatingConstraintsResponse) responseReceived).zonesValidatingConstraints);
        }

        public List<Zone<StringElement>> getZonesValidatingConstraints() {
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
