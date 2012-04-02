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
package org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;

/**
 * Test cases for {@link Element}.
 * 
 * @author lpellegr
 */
public class ElementTest {

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
        Assert.assertFalse(this.coordinateElementA.isBetween(
                this.coordinateElementB, this.coordinateElementC));

        this.coordinateElementA = new StringElement("g");
        Assert.assertTrue(this.coordinateElementA.isBetween(
                this.coordinateElementB, this.coordinateElementC));

        this.coordinateElementA = new StringElement("eff");
        Assert.assertTrue(this.coordinateElementA.isBetween(
                this.coordinateElementB, this.coordinateElementC));

        this.coordinateElementA = new StringElement("lee");
        Assert.assertFalse(this.coordinateElementA.isBetween(
                this.coordinateElementB, this.coordinateElementC));
    }

    @Test
    public void testMiddle() {
        Assert.assertEquals(new StringElement("h\u7fff"), Element.middle(
                this.coordinateElementB, this.coordinateElementC));

        Assert.assertNotSame(new StringElement("e"), Element.middle(
                this.coordinateElementB, this.coordinateElementC));
    }

    @Test
    public void testGetMiddle() {
        StringElement middleCoordinate = this.coordinateElementB;

        for (int nbSplits = 0; nbSplits < 10e2; nbSplits++) {
            middleCoordinate =
                    (StringElement) Element.middle(
                            middleCoordinate, this.coordinateElementC);

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
