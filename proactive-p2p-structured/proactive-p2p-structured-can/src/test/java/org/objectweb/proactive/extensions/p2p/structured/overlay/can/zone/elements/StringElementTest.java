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
package org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements;

import org.apfloat.Apfloat;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;

/**
 * Test cases for {@link StringElement}.
 * 
 * @author lpellegr
 */
public class StringElementTest {

    private StringElement eltA;

    private StringElement eltB;

    @Before
    public void setUp() {
        this.eltA = new StringElement("hello");
        this.eltB = new StringElement("world");
    }

    @Test
    public void testCloning() throws CloneNotSupportedException {
        Assert.assertNotSame(this.eltA, this.eltA.clone());
    }

    @Test
    public void testCompareToLessThan() {
        Assert.assertTrue(this.eltA.compareTo(this.eltB) < 0);
    }

    @Test
    public void testCompareToGreaterThan() {
        Assert.assertTrue(this.eltB.compareTo(this.eltA) > 0);
    }

    @Test
    public void testCompareToEquals() {
        Assert.assertEquals(0, this.eltA.compareTo(this.eltA));
    }

    @Test
    public void testCompareToNotEquals() {
        Assert.assertNotSame(0, this.eltA.compareTo(this.eltB));
    }

    @Test
    public void testEquals() {
        Assert.assertEquals(this.eltA, this.eltA);
        Assert.assertEquals(this.eltB, this.eltB);
    }

    @Test
    public void testNotEquals() {
        Assert.assertFalse(this.eltA.equals(this.eltB));
    }

    @Test
    public void testHashCode() {
        Assert.assertEquals(this.eltA.hashCode(), this.eltA.hashCode());
    }

    @Test
    public void testHashCodeNotEquals() {
        Assert.assertNotSame(this.eltA.hashCode(), this.eltB.hashCode());
    }

    @Test
    public void testNormalization1() {
        Assert.assertEquals(
                1,
                new StringElement(
                        P2PStructuredProperties.CAN_UPPER_BOUND.getValueAsString()).normalize(
                        0, 1), 1e-9);
    }

    @Test
    public void testNormalization2() {
        Assert.assertEquals(
                0,
                new StringElement(
                        P2PStructuredProperties.CAN_LOWER_BOUND.getValueAsString()).normalize(
                        0, 1), 1e-9);
    }

    @Test
    public void testSplitMiddleRecursively() {
        StringElement middleElement = this.eltA;

        for (int nbSplits = 0; nbSplits < 1e3; nbSplits++) {
            middleElement = Element.middle(middleElement, this.eltB);
            Assert.assertTrue(middleElement.compareTo(this.eltB) < 0);
        }
    }

    @Test
    public void testGetUnicodeRepresentation() {
        StringElement elt1 = new StringElement("a");
        StringElement elt2 = new StringElement("ab");

        String elt1UnicodeRepresentation = elt1.getUnicodeRepresentation();
        String elt2UnicodeRepresentation = elt2.getUnicodeRepresentation();

        Assert.assertTrue(elt1UnicodeRepresentation.length() < elt2UnicodeRepresentation.length());
    }

    @Test
    public void testStringElementU0000() {
        String MIN_CODEPOINT_STRING =
                Character.toString((char) Character.MIN_CODE_POINT);

        Assert.assertEquals(MIN_CODEPOINT_STRING, new StringElement(
                MIN_CODEPOINT_STRING).getUnicodeRepresentation());
    }

    @Test
    public void testToFloatRadix10_1() {
        StringElement se = new StringElement("a");
        Assert.assertEquals(new Apfloat('a'), se.apfloat);
    }

}
