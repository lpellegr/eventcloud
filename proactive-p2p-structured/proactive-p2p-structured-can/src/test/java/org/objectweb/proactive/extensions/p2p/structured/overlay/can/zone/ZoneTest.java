package org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone;

import static org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties.CAN_LOWER_BOUND;
import static org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties.CAN_UPPER_BOUND;
import static org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.CoordinateFactory.createDoubleCoordinate;
import static org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.CoordinateFactory.createStringCoordinate;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.NumericZoneView;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.UnicodeZoneView;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.util.Pair;

/**
 * Test cases for class {@link Zone}.
 * 
 * @author lpellegr
 */
public class ZoneTest {

    private static Zone zone;

    @BeforeClass
    public static void setUp() throws Exception {
        ZoneTest.zone = new Zone();
    }

    @Test
    public void testZone() {
        Assert.assertEquals(
                ZoneTest.zone.getLowerBound(),
                createStringCoordinate(CAN_LOWER_BOUND.getValue()));

        Assert.assertEquals(
                ZoneTest.zone.getUpperBound(),
                createStringCoordinate(CAN_UPPER_BOUND.getValue()));
    }

    @Test
    public void testSplitAndMerge() {
        Zone z =
                new Zone(new UnicodeZoneView(
                        createStringCoordinate("a"),
                        createStringCoordinate("z")), new NumericZoneView(
                        createDoubleCoordinate(0.0),
                        createDoubleCoordinate(1.0)));

        Pair<Zone> newZones = z.split(CanOverlay.getRandomDimension());
        Assert.assertEquals(z, newZones.getFirst().merge(newZones.getSecond()));
    }

    @AfterClass
    public static void tearDown() {
        ZoneTest.zone = null;
    }

}
