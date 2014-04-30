/**
 * Copyright (c) 2011-2014 INRIA.
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
package fr.inria.eventcloud.api.generators;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.Range;

/**
 * Test cases associated to {@link StringGenerator}.
 * 
 * @author lpellegr
 */
public class StringGeneratorTest {

    private final int NB_GENERATIONS = 1000;

    @Test
    public void testStringGenerator() {
        String prefix = "http://";
        int size = 50;

        for (int i = 0; i < this.NB_GENERATIONS; i++) {
            String generatedValue =
                    StringGenerator.random(
                            size, prefix, StringGenerator.BMP_RANGES);

            this.assertValid(
                    generatedValue, prefix.length(), size,
                    StringGenerator.BMP_RANGES);
        }
    }

    private void assertValid(String value, int prefixLength, int length,
                             Range<Character>[] ranges) {
        int expectedLength = prefixLength + length;

        Assert.assertEquals("Invalid length: " + value.length() + " whereas "
                + expectedLength + " expected", expectedLength, value.length());

        for (int i = 0; i < value.length(); i++) {
            Assert.assertTrue(
                    "The following character is not within the specified ranges: "
                            + value.charAt(i), this.contains(
                            ranges, value.charAt(i)));
        }
    }

    private boolean contains(Range<Character>[] ranges, int codepoint) {
        for (Range<Character> range : ranges) {
            return range.contains((char) codepoint);
        }

        return false;
    }

}
