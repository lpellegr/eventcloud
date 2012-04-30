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

import java.util.concurrent.ExecutionException;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.deployment.JunitByClassCanNetworkDeployer;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.Request;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.ForwardRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.LookupRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.ResponseProvider;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.LookupResponse;
import org.objectweb.proactive.extensions.p2p.structured.operations.CanOperations;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;
import org.objectweb.proactive.extensions.p2p.structured.proxies.Proxies;
import org.objectweb.proactive.extensions.p2p.structured.proxies.Proxy;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.router.can.UnicastRequestRouter;

/**
 * Test cases for {@link LookupRequest} and {@link ForwardRequest}.
 * 
 * @author lpellegr
 */
public class UnicastLookupRequestTest extends JunitByClassCanNetworkDeployer {

    private Peer target;

    private Proxy proxy;

    private StringCoordinate targetLowerBound;

    public UnicastLookupRequestTest() {
        super(1, 10, new SerializableProvider<CustomCanOverlay>() {
            private static final long serialVersionUID = 1L;

            @Override
            public CustomCanOverlay get() {
                return new CustomCanOverlay();
            }
        });
    }

    @Override
    public void setUp() {
        super.setUp();

        this.target = super.getPeer(8);
        this.targetLowerBound =
                CanOperations.getIdAndZoneResponseOperation(this.target)
                        .getPeerZone()
                        .getLowerBound();

        this.proxy = Proxies.newProxy(super.getRandomTracker());
    }

    @Test
    public void testUnicastLookupRequestWithResponse()
            throws DispatchException, InterruptedException, ExecutionException {

        LookupResponse response =
                (LookupResponse) this.proxy.send(new LookupRequest(
                        this.targetLowerBound), super.getPeer(0));

        checkResponse(response, this.target);
    }

    @Test
    public void testUnicastForwardRequestWithoutResponse()
            throws DispatchException {
        this.proxy.sendv(
                new SetStateRequest(this.targetLowerBound), super.getPeer(0));

        // sleep because the previous call is supposed to be asynchronous
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        GetStateResponse response =
                (GetStateResponse) this.proxy.send(new GetStateRequest(
                        this.targetLowerBound), super.getPeer(0));

        Assert.assertTrue(response.getValue());
    }

    private static class CustomCanOverlay extends CanOverlay {

        private boolean value = false;

    }

    private static class SetStateRequest extends ForwardRequest {

        private static final long serialVersionUID = 1L;

        public SetStateRequest(StringCoordinate coordinateToReach) {
            super(coordinateToReach, null);
        }

        @Override
        public Router<ForwardRequest, StringCoordinate> getRouter() {
            return new UnicastRequestRouter<ForwardRequest>() {
                @Override
                protected void onDestinationReached(StructuredOverlay overlay,
                                                    ForwardRequest msg) {
                    ((CustomCanOverlay) overlay).value = true;
                }
            };
        }
    }

    private static class GetStateRequest extends LookupRequest {

        private static final long serialVersionUID = 1L;

        private boolean value;

        public GetStateRequest(StringCoordinate coordinateToReach) {
            super(coordinateToReach,
                    new ResponseProvider<GetStateResponse, StringCoordinate>() {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public GetStateResponse get() {
                            return new GetStateResponse();
                        }
                    });
        }

        @Override
        public Router<ForwardRequest, StringCoordinate> getRouter() {
            return new UnicastRequestRouter<ForwardRequest>() {
                @Override
                protected void onDestinationReached(StructuredOverlay overlay,
                                                    ForwardRequest msg) {
                    GetStateRequest.this.value =
                            ((CustomCanOverlay) overlay).value;
                }
            };
        }
    }

    private static class GetStateResponse extends LookupResponse {

        private static final long serialVersionUID = 1L;

        private boolean value;

        @Override
        public void setAttributes(Request<StringCoordinate> request,
                                  StructuredOverlay overlay) {
            super.setAttributes(request, overlay);

            this.value = ((GetStateRequest) request).value;
        }

        public boolean getValue() {
            return this.value;
        }

    }

    private static <T> void checkResponse(LookupResponse response, Peer target) {
        Assert.assertTrue(response.getLatency() > 0);
        Assert.assertTrue(response.getHopCount() > 0);
        Assert.assertTrue(response.getInboundHopCount() > 0);
        Assert.assertTrue(response.getOutboundHopCount() > 0);

        Assert.assertEquals(target, response.getPeerFound());
    }

}
