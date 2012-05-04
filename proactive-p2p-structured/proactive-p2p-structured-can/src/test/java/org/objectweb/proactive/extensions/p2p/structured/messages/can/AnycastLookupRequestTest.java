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

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.deployment.JunitByClassCanNetworkDeployer;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.objectweb.proactive.extensions.p2p.structured.messages.RequestResponseMessage;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.Response;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.ResponseProvider;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.AnycastResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
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
                1,
                NB_PEERS,
                new SerializableProvider<CustomCanOverlay>() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public CustomCanOverlay get() {
                        return new CustomCanOverlay();
                    }
                },
                InjectionConstraintsProvider.newFractalInjectionConstraintsProvider());
    }

    @Override
    public void setUp() {
        super.setUp();

        this.proxy = Proxies.newProxy(super.getRandomTracker());
    }

    @Test
    public void testAnycastRequestWithResponse() throws DispatchException {
        StringElement elt =
                new StringElement(
                        Character.toString((char) ((int) P2PStructuredProperties.CAN_UPPER_BOUND.getValue() - 1)));

        GetZonesValidatingConstraintsResponse response =
                (GetZonesValidatingConstraintsResponse) this.proxy.send(
                        new GetZonesValidatingConstraintsRequest(
                                new StringCoordinate(null, elt, null)),
                        super.getPeer(0));

        checkResponse(response);

        // check that all zones retrieved validate the constraints
        for (Zone zone : response.getZonesValidatingConstraints()) {
            Assert.assertEquals(0, zone.getUnicodeView()
                    .containsLexicographically((byte) 1, elt));
        }
    }

    @Test
    public void testAnycastRequestWithoutResponse() throws DispatchException,
            InterruptedException {
        this.proxy.sendv(new SetValuesRequest());

        // sleep because the previous call is supposed to be asynchronous
        Thread.sleep(1000);

        GetValuesResponse response =
                (GetValuesResponse) this.proxy.send(new GetValuesRequest());

        checkResponse(response);

        Assert.assertEquals(NB_PEERS, response.getResult().size());

        for (boolean value : response.getResult()) {
            Assert.assertTrue(value);
        }
    }

    private static class CustomCanOverlay extends CanOverlay {

        private boolean value = false;

    }

    private static class SetValuesRequest extends AnycastRequest {

        private static final long serialVersionUID = 1L;

        public SetValuesRequest() {
            super(new DefaultAnycastConstraintsValidator());
        }

        @Override
        public Router<? extends RequestResponseMessage<StringCoordinate>, StringCoordinate> getRouter() {
            return new AnycastRequestRouter<GetZonesValidatingConstraintsRequest>() {
                @Override
                public void onPeerValidatingKeyConstraints(CanOverlay overlay,
                                                           AnycastRequest request) {
                    ((CustomCanOverlay) overlay).value = true;
                };
            };
        }

    }

    private static class GetValuesRequest extends AnycastRequest {

        private static final long serialVersionUID = 1L;

        public GetValuesRequest() {
            super(new DefaultAnycastConstraintsValidator(),
                    new ResponseProvider<AnycastResponse, StringCoordinate>() {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public AnycastResponse get() {
                            return new GetValuesResponse();
                        }
                    });
        }

    }

    private static class GetValuesResponse extends AnycastResponse {

        private static final long serialVersionUID = 1L;

        private List<Boolean> result = new ArrayList<Boolean>();

        @Override
        public void synchronizationPointUnlocked(StructuredOverlay overlay) {
            if (this.validatesKeyConstraints(overlay)) {
                this.result.add(((CustomCanOverlay) overlay).value);
            }
        }

        @Override
        public void mergeAttributes(AnycastResponse responseReceived) {
            this.result.addAll(((GetValuesResponse) responseReceived).getResult());
        }

        public List<Boolean> getResult() {
            return this.result;
        }

    }

    private static class GetZonesValidatingConstraintsRequest extends
            AnycastRequest {

        private static final long serialVersionUID = 1L;

        public GetZonesValidatingConstraintsRequest(
                StringCoordinate coordinatesToReach) {
            super(new DefaultAnycastConstraintsValidator(coordinatesToReach),
                    new ResponseProvider<AnycastResponse, StringCoordinate>() {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public AnycastResponse get() {
                            return new GetZonesValidatingConstraintsResponse();
                        }
                    });
        }

    }

    private static class GetZonesValidatingConstraintsResponse extends
            AnycastResponse {

        private static final long serialVersionUID = 1L;

        private List<Zone> zonesValidatingConstraints = new ArrayList<Zone>();

        @Override
        public void synchronizationPointUnlocked(StructuredOverlay overlay) {
            if (this.validatesKeyConstraints(overlay)) {
                this.zonesValidatingConstraints.add(((CanOverlay) overlay).getZone());
            }
        }

        @Override
        public void mergeAttributes(AnycastResponse responseReceived) {
            this.zonesValidatingConstraints.addAll(((GetZonesValidatingConstraintsResponse) responseReceived).zonesValidatingConstraints);
        }

        public List<Zone> getZonesValidatingConstraints() {
            return this.zonesValidatingConstraints;
        }

    }

    private static <T> void checkResponse(Response<T> response) {
        Assert.assertTrue(response.getLatency() > 0);
        Assert.assertTrue(response.getHopCount() > 0);
        Assert.assertTrue(response.getInboundHopCount() > 0);
        Assert.assertTrue(response.getOutboundHopCount() > 0);
    }

}
