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
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.DecimalBigInt;
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
        this.coordinateElementA = null;

        // 101 <-> "e"
        this.coordinateElementB =
                new StringElement(DecimalBigInt.create(
                        new int[] {101}, 0,
                        P2PStructuredProperties.CAN_UPPER_BOUND.getValue()));
        // 108 <-> "l"
        this.coordinateElementC =
                new StringElement(DecimalBigInt.create(
                        new int[] {108}, 0,
                        P2PStructuredProperties.CAN_UPPER_BOUND.getValue()));
    }

    @Test
    public void testLexicographicCompareTo() {
        Assert.assertTrue(this.coordinateElementB.compareLexicographicallyTo(this.coordinateElementC) < 0);
        Assert.assertTrue(this.coordinateElementC.compareLexicographicallyTo(this.coordinateElementB) > 0);

        this.coordinateElementA = new StringElement("e");
        Assert.assertTrue(this.coordinateElementB.compareLexicographicallyTo(this.coordinateElementA) == 0);
        Assert.assertTrue(this.coordinateElementA.compareLexicographicallyTo(this.coordinateElementB) == 0);

        this.coordinateElementA = new StringElement("a");
        Assert.assertTrue(this.coordinateElementC.compareLexicographicallyTo(this.coordinateElementA) > 0);
        Assert.assertTrue(this.coordinateElementA.compareLexicographicallyTo(this.coordinateElementC) < 0);

        this.coordinateElementA = new StringElement("l");
        Assert.assertTrue(this.coordinateElementC.compareLexicographicallyTo(this.coordinateElementA) == 0);
        Assert.assertTrue(this.coordinateElementA.compareLexicographicallyTo(this.coordinateElementC) == 0);
    }

    @Test
    public void testLexicographicIsBetween() {
        this.coordinateElementA = new StringElement("a");
        Assert.assertFalse(this.coordinateElementA.isLexicographicallyBetween(
                this.coordinateElementB, this.coordinateElementC));

        this.coordinateElementA = new StringElement("g");
        Assert.assertTrue(this.coordinateElementA.isLexicographicallyBetween(
                this.coordinateElementB, this.coordinateElementC));

        this.coordinateElementA = new StringElement("eff");
        Assert.assertTrue(this.coordinateElementA.isLexicographicallyBetween(
                this.coordinateElementB, this.coordinateElementC));

        this.coordinateElementA = new StringElement("lee");
        Assert.assertFalse(this.coordinateElementA.isLexicographicallyBetween(
                this.coordinateElementB, this.coordinateElementC));
    }

    @Test
    public void testMiddle() {
        Assert.assertEquals(
                new StringElement(
                        DecimalBigInt.create(
                                new int[] {
                                        104,
                                        P2PStructuredProperties.CAN_UPPER_BOUND.getValue() / 2},
                                0,
                                P2PStructuredProperties.CAN_UPPER_BOUND.getValue())),
                Element.middle(this.coordinateElementB, this.coordinateElementC));
    }

    @Test
    public void testGetMiddle() {
        StringElement middleCoordinate = this.coordinateElementB;

        for (int nbSplits = 0; nbSplits < 10e2; nbSplits++) {
            middleCoordinate =
                    (StringElement) Element.middle(
                            middleCoordinate, this.coordinateElementC);

            Assert.assertTrue(middleCoordinate.compareLexicographicallyTo(this.coordinateElementC) < 0);
        }
    }

    @After
    public void tearDown() {
        this.coordinateElementA = null;
        this.coordinateElementC = null;
        this.coordinateElementB = null;
    }

}
