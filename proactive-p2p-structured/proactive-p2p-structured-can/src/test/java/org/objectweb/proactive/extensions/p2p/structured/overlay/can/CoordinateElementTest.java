package org.objectweb.proactive.extensions.p2p.structured.overlay.can;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Element;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.StringElement;

/**
 * Test cases for {@link Element}.
 * 
 * @author lpellegr
 */
public class CoordinateElementTest {

    private StringElement coordinateElementA;

    private StringElement coordinateElementB;

    private StringElement coordinateElementC;

    @Before
    public void setUp() throws Exception {
        this.coordinateElementB = new StringElement("e");
        this.coordinateElementC = new StringElement("l");
    }

    @Test
    public void testCompare() {
        Assert.assertTrue(this.coordinateElementB.compareTo(this.coordinateElementC) < 0);
        Assert.assertTrue(this.coordinateElementC.compareTo(this.coordinateElementB) > 0);

        this.coordinateElementA = new StringElement("e");
        Assert.assertTrue(this.coordinateElementB.compareTo(this.coordinateElementA) == 0);
        Assert.assertTrue(this.coordinateElementA.compareTo(this.coordinateElementB) == 0);

        this.coordinateElementA = new StringElement("a");
        Assert.assertTrue(this.coordinateElementC.compareTo(this.coordinateElementA) > 0);
        Assert.assertTrue(this.coordinateElementA.compareTo(this.coordinateElementC) < 0);

        this.coordinateElementA = new StringElement("l");
        Assert.assertTrue(this.coordinateElementC.compareTo(this.coordinateElementA) == 0);
        Assert.assertTrue(this.coordinateElementA.compareTo(this.coordinateElementC) == 0);
    }

    @Test
    public void testIsBetween() {
        this.coordinateElementA = new StringElement("a");
        Assert.assertFalse(
        		this.coordinateElementA.isBetween(
        				this.coordinateElementB,
        				this.coordinateElementC));

        this.coordinateElementA = new StringElement("g");
        Assert.assertTrue(
        		this.coordinateElementA.isBetween(
        				this.coordinateElementB,
        				this.coordinateElementC));

        this.coordinateElementA = new StringElement("eff");
        Assert.assertTrue(
        		this.coordinateElementA.isBetween(
        				this.coordinateElementB,
        				this.coordinateElementC));

        this.coordinateElementA = new StringElement("lee");
        Assert.assertFalse(
        		this.coordinateElementA.isBetween(
        				this.coordinateElementB,
        				this.coordinateElementC));
    }

    @Test
    public void testMiddle() {
        Assert.assertEquals(
        		new StringElement("h\u0001"), 
        		Element.middle(this.coordinateElementB, this.coordinateElementC));

        Assert.assertNotSame(new StringElement("e"), 
        		Element.middle(this.coordinateElementB, this.coordinateElementC));
    }

    @Test
    public void testGetMiddle() {
        StringElement middleCoordinate = this.coordinateElementB;
        for (int nbSplit = 0; nbSplit < 500; nbSplit++) {
            middleCoordinate = Element.middle(middleCoordinate, this.coordinateElementC);
            Assert.assertTrue(middleCoordinate.compareTo(this.coordinateElementC) < 0);
        }
    }

    @After
    public void tearDown() {
        this.coordinateElementA = null;
        this.coordinateElementC = null;
        this.coordinateElementB = null;
    }

}
