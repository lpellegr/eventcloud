/**
 * Copyright (c) 2011 INRIA.
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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.deployment.JunitByClassParameterizedCanNetworkDeployer;
import org.objectweb.proactive.extensions.p2p.structured.deployment.NetworkDeployer;

/**
 * Test cases for {@link NeighborTable}.
 * 
 * @author lpellegr
 */
public class NeighborTableTest extends
        JunitByClassParameterizedCanNetworkDeployer {

    public NeighborTableTest(NetworkDeployer deployer) {
        super(deployer, 2);
    }

    @Test
    public void testContains() {
        NeighborTable neighborTable = new NeighborTable();
        neighborTable.add(
                new NeighborEntry(super.getPeer(0)), (byte) 0, (byte) 1);
        neighborTable.add(
                new NeighborEntry(super.getPeer(1)), (byte) 0, (byte) 0);

        assertTrue(neighborTable.contains(
                super.getPeer(0).getId(), (byte) 0, (byte) 1));
        assertTrue(neighborTable.contains(
                super.getPeer(1).getId(), (byte) 0, (byte) 0));
    }

    @Test
    public void testAddAll() {
        NeighborTable neighborTable = new NeighborTable();
        neighborTable.add(
                new NeighborEntry(super.getPeer(0)), (byte) 0, (byte) 1);
        neighborTable.add(
                new NeighborEntry(super.getPeer(1)), (byte) 0, (byte) 0);

        assertTrue(neighborTable.contains(
                super.getPeer(0).getId(), (byte) 0, (byte) 1));
        assertTrue(neighborTable.contains(
                super.getPeer(1).getId(), (byte) 0, (byte) 0));

        NeighborTable neighborTable2 = new NeighborTable();
        neighborTable2.addAll(neighborTable);

        assertEquals(neighborTable.size(), neighborTable2.size());
        assertEquals(0, neighborTable2.findDimension(super.getPeer(0).getId()));
        assertEquals(0, neighborTable2.findDimension(super.getPeer(1).getId()));
        assertEquals(1, neighborTable2.findDirection(super.getPeer(0).getId()));
        assertEquals(0, neighborTable2.findDirection(super.getPeer(1).getId()));

    }

}
