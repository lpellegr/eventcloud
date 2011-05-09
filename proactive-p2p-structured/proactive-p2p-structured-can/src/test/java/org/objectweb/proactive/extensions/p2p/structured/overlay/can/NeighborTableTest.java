package org.objectweb.proactive.extensions.p2p.structured.overlay.can;

import static junit.framework.Assert.assertTrue;
import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.initializers.CanNetworkInitializer;
import org.objectweb.proactive.extensions.p2p.structured.initializers.NetworkInitializer;

/**
 * Test cases for {@link NeighborTable}.
 * 
 * @author lpellegr
 */
public class NeighborTableTest {

    private NetworkInitializer initializer;

    @Before
    public void setUp() throws Exception {
        this.initializer = new CanNetworkInitializer();
    }

    @Test
    public void testContains() {
        this.initializer.initializeNewNetwork(2);

        NeighborTable neighborTable = new NeighborTable();
        neighborTable.add(
                new NeighborEntry(this.initializer.get(0)), (byte) 0, (byte) 1);
        neighborTable.add(
                new NeighborEntry(this.initializer.get(1)), (byte) 0, (byte) 0);

        assertTrue(neighborTable.contains(
                this.initializer.get(0).getId(), (byte) 0, (byte) 1));
        assertTrue(neighborTable.contains(
                this.initializer.get(1).getId(), (byte) 0, (byte) 0));
    }

    @Test
    public void testAddAll() {
        this.initializer.initializeNewNetwork(2);

        NeighborTable neighborTable = new NeighborTable();
        neighborTable.add(
                new NeighborEntry(this.initializer.get(0)), (byte) 0, (byte) 1);
        neighborTable.add(
                new NeighborEntry(this.initializer.get(1)), (byte) 0, (byte) 0);

        assertTrue(neighborTable.contains(
                this.initializer.get(0).getId(), (byte) 0, (byte) 1));
        assertTrue(neighborTable.contains(
                this.initializer.get(1).getId(), (byte) 0, (byte) 0));

        NeighborTable neighborTable2 = new NeighborTable();
        neighborTable2.addAll(neighborTable);

        Assert.assertEquals(neighborTable.size(), neighborTable2.size());
        Assert.assertEquals(
                0,
                neighborTable2.findDimension(this.initializer.get(0).getId()));
        Assert.assertEquals(
                0,
                neighborTable2.findDimension(this.initializer.get(1).getId()));
        Assert.assertEquals(
                1,
                neighborTable2.findDirection(this.initializer.get(0).getId()));
        Assert.assertEquals(
                0,
                neighborTable2.findDirection(this.initializer.get(1).getId()));

    }

}
