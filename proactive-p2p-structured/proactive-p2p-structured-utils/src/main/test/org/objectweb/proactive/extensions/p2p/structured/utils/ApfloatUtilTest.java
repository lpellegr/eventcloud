/**
 * Copyright (c) 2011-2013 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package org.objectweb.proactive.extensions.p2p.structured.utils;

import junit.framework.Assert;

import org.apfloat.Apfloat;
import org.junit.Test;

/**
 * Test cases associated to {@link ApfloatUtil}.
 * 
 * @author lpellegr
 */
public class ApfloatUtilTest {

    @Test
    public void testToFloatRadix10_1() {
        Assert.assertEquals(
                new Apfloat((char) Character.MIN_CODE_POINT),
                ApfloatUtils.toFloatRadix10(new String(
                        Character.toChars(Character.MIN_CODE_POINT))));
    }

    @Test
    public void testToFloatRadix10_2() {
        Assert.assertEquals(new Apfloat('a'), ApfloatUtils.toFloatRadix10("a"));
    }

}
