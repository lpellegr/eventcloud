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
package org.objectweb.proactive.extensions.p2p.structured.utils;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Test cases associated {@link UnicodeUtils}.
 * 
 * @author lpellegr
 */
public final class UnicodeUtilsTest {

    private static final String CASE1 = "\u0000\u8888\uFFFF";

    // \uD801\uDC00 and \uD800\uDFFF are supplementary characters
    private static final String CASE2 =
            "\u0000\u8888\uFFFF\uD801\uDC00\uD800\uDFFF";

    @Test
    public void testToCodePointArray1() {
        Assert.assertEquals(3, UnicodeUtils.toCodePointArray(CASE1).length);
    }

    @Test
    public void testToCodePointArray2() {
        Assert.assertEquals(5, UnicodeUtils.toCodePointArray(CASE2).length);
    }

    @Test
    public void testToStringUtf16() {
        // by using the UTF-16 notation (U+) each character has a
        // String representation (or scalar value) whose the size is 6
        Assert.assertEquals(CASE2.length(), UnicodeUtils.toStringUtf16(
                UnicodeUtils.toCodePointArray(CASE2)).length() / 6);
    }

    @Test
    public void testToStringUtf32() {
        // by using the UTF-32 notation (U+) each character has a
        // String representation (or scalar value) whose the size is 6
        // with UTF-32 \uD801\uDC00 and \uD800\uDFFF are each merged into one
        Assert.assertEquals(CASE2.length() - 2, UnicodeUtils.toStringUtf32(
                UnicodeUtils.toCodePointArray(CASE2)).length() / 8);
    }

    @Test
    public void testStringComparisonUtf32_1() {
        String s1 = UnicodeUtils.toString(59000);
        String s2 = UnicodeUtils.toString(1114111);

        // Java uses by default the UTF-16 representation for strings
        // comparison. This means that supplementary characters (i.e. characters
        // whose the codepoint representation is greater than 2^16) are
        // represented by using two characters

        // comparing s1 and s2 with the standard Java implementation is
        // equivalent to compare \uE678 with \uDBFF\uDFFF
        Assert.assertFalse(s1.compareTo(s2) < 0);

        Assert.assertTrue(UnicodeUtils.compareUtf32(s1, s2) < 0);
    }

    @Test
    public void testStringComparisonUtf32_2() {
        Assert.assertTrue(UnicodeUtils.compareUtf32("a", "a") == 0);
        Assert.assertTrue(UnicodeUtils.compareUtf32(
                UnicodeUtils.toString(77000), UnicodeUtils.toString(77000)) == 0);
    }

    @Test
    public void testStringComparisonUtf32_3() {
        Assert.assertTrue(UnicodeUtils.compareUtf32("a", "b") < 0);
        Assert.assertTrue(UnicodeUtils.compareUtf32(
                UnicodeUtils.toString(66000), UnicodeUtils.toString(77000)) < 0);
    }

    @Test
    public void testStringComparisonUtf32_4() {
        Assert.assertTrue(UnicodeUtils.compareUtf32("b", "a") > 0);
        Assert.assertTrue(UnicodeUtils.compareUtf32(
                UnicodeUtils.toString(77000), UnicodeUtils.toString(66000)) > 0);
    }

    @Test
    public void testStringComparisonUtf32_5() {
        Assert.assertTrue(UnicodeUtils.compareUtf32("ab", "abc") < 0);
        Assert.assertTrue(UnicodeUtils.compareUtf32(UnicodeUtils.toString(
                66000, 67000), UnicodeUtils.toString(66000, 67000, 68000)) < 0);
    }

    @Test
    public void testStringComparisonUtf32_6() {
        Assert.assertTrue(UnicodeUtils.compareUtf32("abc", "ab") > 0);
        Assert.assertTrue(UnicodeUtils.compareUtf32(UnicodeUtils.toString(
                66000, 67000, 68000), UnicodeUtils.toString(66000, 67000)) > 0);
    }

}
