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

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.p2p.structured.deployment.JunitByClassParameterizedCanNetworkDeployer;
import org.objectweb.proactive.extensions.p2p.structured.deployment.NetworkDeployer;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.LookupRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.LookupResponse;
import org.objectweb.proactive.extensions.p2p.structured.operations.CanOperations;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

/**
 * Test case for {@link LookupRequest}.
 * 
 * @author lpellegr
 */
public class UnicastLookupRequestTest extends
        JunitByClassParameterizedCanNetworkDeployer {

    public UnicastLookupRequestTest(NetworkDeployer deployer) {
        super(deployer, 15);
    }

    @Test
    public void testLookupQuery() {
        LookupResponse response = null;
        Peer targetedPeer = super.getPeer(8);

        try {
            response =
                    (LookupResponse) PAFuture.getFutureValue(super.getPeer(0)
                            .send(
                                    new LookupRequest(
                                            CanOperations.getIdAndZoneResponseOperation(
                                                    targetedPeer)
                                                    .getPeerZone()
                                                    .getLowerBound())));

            Assert.assertTrue(response.getLatency() > 0);
            Assert.assertTrue(response.getHopCount() > 0);
            Assert.assertTrue(response.getInboundHopCount() > 0);
            Assert.assertTrue(response.getOutboundHopCount() > 0);

            Assert.assertEquals(targetedPeer, response.getPeerFound());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
