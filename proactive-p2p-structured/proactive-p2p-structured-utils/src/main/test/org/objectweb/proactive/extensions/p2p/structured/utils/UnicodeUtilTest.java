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
 * Test cases associated {@link UnicodeUtil}.
 * 
 * @author lpellegr
 */
public final class UnicodeUtilTest {

    private static final String CASE1 = "\u0000\u8888\uFFFF";

    // \uD801\uDC00 and \uD800\uDFFF are supplementary characters
    private static final String CASE2 =
            "\u0000\u8888\uFFFF\uD801\uDC00\uD800\uDFFF";

    @Test
    public void testToCodePointArray1() {
        Assert.assertEquals(3, UnicodeUtil.toCodePointArray(CASE1).length);
    }

    @Test
    public void testToCodePointArray2() {
        Assert.assertEquals(5, UnicodeUtil.toCodePointArray(CASE2).length);
    }

    @Test
    public void testToStringUtf16() {
        // by using the UTF-16 notation (U+) each character has a
        // String representation (or scalar value) whose the size is 6
        Assert.assertEquals(CASE2.length(), UnicodeUtil.toStringUtf16(
                UnicodeUtil.toCodePointArray(CASE2)).length() / 6);
    }

    @Test
    public void testToStringUtf32() {
        // by using the UTF-32 notation (U+) each character has a
        // String representation (or scalar value) whose the size is 6
        // with UTF-32 \uD801\uDC00 and \uD800\uDFFF are each merged into one
        Assert.assertEquals(CASE2.length() - 2, UnicodeUtil.toStringUtf32(
                UnicodeUtil.toCodePointArray(CASE2)).length() / 8);
    }

}
