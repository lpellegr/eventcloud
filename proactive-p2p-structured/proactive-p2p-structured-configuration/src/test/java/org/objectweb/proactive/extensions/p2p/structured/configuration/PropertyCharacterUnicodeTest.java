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
package org.objectweb.proactive.extensions.p2p.structured.configuration;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test cases associated to {@link PropertyCharacterUnicode}.
 * 
 * @author lpellegr
 */
public class PropertyCharacterUnicodeTest {

    private static String DEFAULT_PROPERTY_NAME = "property.name";

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgument1() {
        new PropertyCharacterUnicode(DEFAULT_PROPERTY_NAME, "");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgument2() {
        new PropertyCharacterUnicode(DEFAULT_PROPERTY_NAME, "\u10FFFF");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgument3() {
        // \uDCFF is out of the high-surrogates range (\uD800-\uDBFF)
        new PropertyCharacterUnicode(DEFAULT_PROPERTY_NAME, "\uDCFF\uDFFF");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegalArgument4() {
        new PropertyCharacterUnicode(
                DEFAULT_PROPERTY_NAME, "\uDBFF\uDFFF\uA3A3");
    }

    @Test
    public void testLegalArgument1() {
        Assert.assertEquals("z", new PropertyCharacterUnicode(
                DEFAULT_PROPERTY_NAME, "z").getValueAsString());
    }

    @Test
    public void testLegalArgument3() {
        Assert.assertEquals("\uF9F9", new PropertyCharacterUnicode(
                DEFAULT_PROPERTY_NAME, "\uF9F9").getValueAsString());
    }

    @Test
    public void testLegalArgument2() {
        Assert.assertEquals("\uDBFF\uDFFF", new PropertyCharacterUnicode(
                DEFAULT_PROPERTY_NAME, "\uDBFF\uDFFF").getValueAsString());
    }

}
