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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
    public void testCompareToLexicographicLessThan() {
        Assert.assertTrue(this.eltA.compareLexicographicallyTo(this.eltB) < 0);
    }

    @Test
    public void testCompareToLexicographicGreaterThan() {
        Assert.assertTrue(this.eltB.compareLexicographicallyTo(this.eltA) > 0);
    }

    @Test
    public void testCompareToLexicographicEquals() {
        Assert.assertEquals(0, this.eltA.compareLexicographicallyTo(this.eltA));
    }

    @Test
    public void testCompareToLexicographicNotEquals() {
        Assert.assertNotSame(0, this.eltA.compareLexicographicallyTo(this.eltB));
    }

    @Test
    public void testCompareToNumericLessThan() {
        Assert.assertTrue(this.eltA.compareTo(this.eltB) < 0);
    }

    @Test
    public void testCompareToNumericGreaterThan() {
        Assert.assertTrue(this.eltB.compareTo(this.eltA) > 0);
    }

    @Test
    public void testCompareToNumericEquals() {
        Assert.assertEquals(0, this.eltA.compareTo(this.eltA));
    }

    @Test
    public void testCompareToNumericNotEquals() {
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
    public void testSplitMiddle() {
        StringElement middle =
                Element.middle(new StringElement("abc"), new StringElement(
                        "efg"));

        Assert.assertEquals("cde", middle.getUnicodeRepresentation());
    }

    @Test
    public void testSplitMiddleRecursively() {
        StringElement middleCoordinate = this.eltA;

        for (int nbSplits = 0; nbSplits < 1e3; nbSplits++) {
            middleCoordinate = Element.middle(middleCoordinate, this.eltB);

            Assert.assertTrue(middleCoordinate.compareLexicographicallyTo(this.eltB) < 0);
        }
    }

    @Test
    public void testStringElementU0000() {
        String MIN_CODEPOINT_STRING =
                Character.toString((char) Character.MIN_CODE_POINT);

        Assert.assertEquals(MIN_CODEPOINT_STRING, new StringElement(
                MIN_CODEPOINT_STRING).getUnicodeRepresentation());
    }

}
