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
package org.objectweb.proactive.extensions.p2p.structured.overlay.can;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.deployment.JunitByClassCanNetworkDeployer;
import org.objectweb.proactive.extensions.p2p.structured.deployment.StringCanDeploymentDescriptor;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;

/**
 * Test cases for {@link NeighborTable}.
 * 
 * @author lpellegr
 */
public class NeighborTableTest extends JunitByClassCanNetworkDeployer {

    public NeighborTableTest() {
        super(new StringCanDeploymentDescriptor(), 1, 2);
    }

    @Test
    public void testContains() {
        NeighborTable<StringCoordinate> neighborTable =
                new NeighborTable<StringCoordinate>();
        neighborTable.add(
                new NeighborEntry<StringCoordinate>(super.getPeer(0)),
                (byte) 0, (byte) 1);
        neighborTable.add(
                new NeighborEntry<StringCoordinate>(super.getPeer(1)),
                (byte) 0, (byte) 0);

        assertTrue(neighborTable.contains(
                super.getPeer(0).getId(), (byte) 0, (byte) 1));
        assertTrue(neighborTable.contains(
                super.getPeer(1).getId(), (byte) 0, (byte) 0));
    }

    @Test
    public void testAddAll() {
        NeighborTable<StringCoordinate> neighborTable =
                new NeighborTable<StringCoordinate>();
        neighborTable.add(
                new NeighborEntry<StringCoordinate>(super.getPeer(0)),
                (byte) 0, (byte) 1);
        neighborTable.add(
                new NeighborEntry<StringCoordinate>(super.getPeer(1)),
                (byte) 0, (byte) 0);

        assertTrue(neighborTable.contains(
                super.getPeer(0).getId(), (byte) 0, (byte) 1));
        assertTrue(neighborTable.contains(
                super.getPeer(1).getId(), (byte) 0, (byte) 0));

        NeighborTable<StringCoordinate> neighborTable2 =
                new NeighborTable<StringCoordinate>();
        neighborTable2.addAll(neighborTable);

        assertEquals(neighborTable.size(), neighborTable2.size());
        assertEquals(0, neighborTable2.findDimension(super.getPeer(0).getId()));
        assertEquals(0, neighborTable2.findDimension(super.getPeer(1).getId()));
        assertEquals(1, neighborTable2.findDirection(super.getPeer(0).getId()));
        assertEquals(0, neighborTable2.findDirection(super.getPeer(1).getId()));
    }

}
