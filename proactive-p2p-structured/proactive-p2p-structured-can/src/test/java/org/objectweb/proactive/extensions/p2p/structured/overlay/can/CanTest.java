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
package org.objectweb.proactive.extensions.p2p.structured.overlay.can;

import junit.framework.Assert;

import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.deployment.CanDeploymentDescriptor;
import org.objectweb.proactive.extensions.p2p.structured.deployment.JunitByClassCanNetworkDeployer;
import org.objectweb.proactive.extensions.p2p.structured.operations.CanOperations;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

/**
 * This class tests some properties of the Content-Addressable Network protocol.
 * 
 * @author lpellegr
 */
public class CanTest extends JunitByClassCanNetworkDeployer {

    public CanTest() {
        super(new CanDeploymentDescriptor(), 1, 10);
    }

    @Test
    public void testNeighborhood() {
        for (Peer peer : super.getRandomTracker().getPeers()) {
            NeighborTable table = CanOperations.getNeighborTable(peer);
            for (byte dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
                for (byte dir = 0; dir < 2; dir++) {
                    for (NeighborEntry entry : table.get(dim, dir).values()) {
                        Assert.assertTrue(CanOperations.getIdAndZoneResponseOperation(
                                peer)
                                .getPeerZone()
                                .neighbors(entry.getZone()) != -1);
                    }
                }
            }
        }
    }

}
