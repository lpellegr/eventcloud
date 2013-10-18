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
package org.objectweb.proactive.extensions.p2p.structured.operations.can;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.deployment.JunitByClassCanNetworkDeployer;
import org.objectweb.proactive.extensions.p2p.structured.deployment.StringCanDeploymentDescriptor;
import org.objectweb.proactive.extensions.p2p.structured.operations.CanOperations;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayId;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

/**
 * Test cases for {@link RemoveNeighborOperation}.
 * 
 * @author lpellegr
 */
public class RemoveNeighborOperationTest extends JunitByClassCanNetworkDeployer {

    public RemoveNeighborOperationTest() {
        super(new StringCanDeploymentDescriptor(), 1, 2);
    }

    @Test
    public void testMessage() {
        Peer firstPeer = super.getPeer(0);
        Peer secondPeer = super.getPeer(1);

        OverlayId firstPeerID = firstPeer.getId();
        OverlayId secondPeerID = secondPeer.getId();

        Assert.assertTrue(CanOperations.hasNeighbor(firstPeer, secondPeerID));
        Assert.assertTrue(CanOperations.hasNeighbor(secondPeer, firstPeerID));

        CanOperations.removeNeighbor(firstPeer, secondPeerID);

        Assert.assertFalse(CanOperations.hasNeighbor(firstPeer, secondPeerID));
        Assert.assertTrue(CanOperations.hasNeighbor(secondPeer, firstPeerID));
    }

}
