package org.objectweb.proactive.extensions.p2p.structured.overlay.can;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.configuration.DefaultProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.ZoneException;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Element;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.StringElement;

/**
 * Test cases for class {@link Zone}.
 * 
 * @author Laurent Pellegrino
 */
public class ZoneTest {

    private static Zone zone;

    @BeforeClass
    public static void setUp() throws Exception {
        ZoneTest.zone = new Zone();
    }

    @Test
    public void testZone() {
        Element[] elts = new Element[DefaultProperties.CAN_NB_DIMENSIONS.getValue()];
        for (int i = 0; i < DefaultProperties.CAN_NB_DIMENSIONS.getValue(); i++) {
            elts[i] = new StringElement(DefaultProperties.CAN_LOWER_BOUND.getValueAsString());
        }
        Assert.assertEquals(ZoneTest.zone.getLowerBound(), new Coordinate(elts));

        for (int i = 0; i < DefaultProperties.CAN_NB_DIMENSIONS.getValue(); i++) {
        	elts[i] = new StringElement(DefaultProperties.CAN_UPPER_BOUND.getValueAsString());
        }
        Assert.assertEquals(ZoneTest.zone.getUpperBound(), new Coordinate(elts));
    }

    @Test
    public void testSplitAndMerge() throws ZoneException {
        Zone[] newZones = ZoneTest.zone.split(0);
        Element[] elts = new Element[DefaultProperties.CAN_NB_DIMENSIONS.getValue()];

        for (int i = 0; i < DefaultProperties.CAN_NB_DIMENSIONS.getValue(); i++) {
            elts[i] = new StringElement(
            			DefaultProperties.CAN_LOWER_BOUND.getValueAsString());
        }
        Assert.assertEquals(newZones[0].getLowerBound(), new Coordinate(elts));

        elts[0] = Element.middle(new StringElement(
        								DefaultProperties.CAN_LOWER_BOUND.getValueAsString()),
        						 new StringElement(
        								 DefaultProperties.CAN_UPPER_BOUND.getValueAsString()));
        
        for (int i = 1; i < DefaultProperties.CAN_NB_DIMENSIONS.getValue(); i++) {
            elts[i] = new StringElement(
            				DefaultProperties.CAN_UPPER_BOUND.getValueAsString());
        }
        Assert.assertEquals(newZones[0].getUpperBound(), new Coordinate(elts));

        elts[0] = Element.middle(
        				new StringElement(
        						DefaultProperties.CAN_LOWER_BOUND.getValueAsString()),
                		new StringElement(DefaultProperties.CAN_UPPER_BOUND.getValueAsString()));
        
        for (int i = 1; i < DefaultProperties.CAN_NB_DIMENSIONS.getValue(); i++) {
            elts[i] = new StringElement(
            		DefaultProperties.CAN_LOWER_BOUND.getValueAsString());
        }
        Assert.assertEquals(newZones[1].getLowerBound(), new Coordinate(elts));

        for (int i = 0; i < DefaultProperties.CAN_NB_DIMENSIONS.getValue(); i++) {
            elts[i] = new StringElement(
            		DefaultProperties.CAN_UPPER_BOUND.getValueAsString());
        }
        Assert.assertEquals(newZones[1].getUpperBound(), new Coordinate(elts));

        Assert.assertEquals(newZones[0].neighbors(newZones[1]), 0);
        Assert.assertTrue(newZones[0].neighbors(newZones[1]) == 0);

        Zone mergedZone = newZones[0].merge(newZones[1]);
        Assert.assertEquals(mergedZone, ZoneTest.zone);
    }

    @AfterClass
    public static void tearDown() {
        ZoneTest.zone = null;
    }

}
