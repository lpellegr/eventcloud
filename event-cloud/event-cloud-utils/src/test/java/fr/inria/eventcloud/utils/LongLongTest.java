/**
 * Copyright (c) 2011 INRIA.
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
package fr.inria.eventcloud.utils;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Test cases associated to {@link LongLong}.
 * 
 * @author lpellegr
 */
public class LongLongTest {

    @Test
    public void fromStringTest() {
        LongLong longlong = new LongLong(1, 2);
        String stringLonglong = longlong.toString();
        Assert.assertEquals(longlong, LongLong.fromString(stringLonglong));
    }

}