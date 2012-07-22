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

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.utils.Pair;

/**
 * Test cases associated to {@link DecimalBigInt}.
 * 
 * @author lpellegr
 */
public class DecimalBigIntTest {

    @Test
    public void testEquality() {
        Assert.assertEquals(DecimalBigInt.create(1), DecimalBigInt.create(1));

        Assert.assertEquals(
                DecimalBigInt.create(0, 0, 1), DecimalBigInt.create(1));

        Assert.assertEquals(
                DecimalBigInt.create(new int[] {1, 0, 0}, 0),
                DecimalBigInt.create(1));

        Assert.assertEquals(
                DecimalBigInt.create(new int[] {0, 0, 1}, 2),
                DecimalBigInt.create(new int[] {0, 0, 1, 0}, 2));

        Assert.assertFalse(DecimalBigInt.create(1).equals(
                DecimalBigInt.create(2)));

        Assert.assertFalse(DecimalBigInt.create(new int[] {3}, 0, 4).equals(
                DecimalBigInt.create(new int[] {3}, 0, 10)));
    }

    @Test
    public void testComparison() {
        // 1 = 1
        Assert.assertEquals(0, DecimalBigInt.create(1).compareTo(
                DecimalBigInt.create(1)));

        // 99912340.123 > 099912340.121
        Assert.assertTrue(DecimalBigInt.create(
                new int[] {9, 9, 9, 1, 2, 3, 4, 0, 1, 2, 3}, 7).compareTo(
                DecimalBigInt.create(new int[] {
                        0, 9, 9, 9, 1, 2, 3, 4, 0, 1, 2, 1}, 8)) > 0);

        // 99912340.1231 > 099912340.123
        Assert.assertTrue(DecimalBigInt.create(
                new int[] {9, 9, 9, 1, 2, 3, 4, 0, 1, 2, 3, 1}, 7).compareTo(
                DecimalBigInt.create(new int[] {
                        0, 9, 9, 9, 1, 2, 3, 4, 0, 1, 2, 3}, 8)) > 0);

        // 99912340.123 = 099912340.12300
        Assert.assertTrue(DecimalBigInt.create(
                new int[] {9, 9, 9, 1, 2, 3, 4, 0, 1, 2, 3}, 7).compareTo(
                DecimalBigInt.create(new int[] {
                        0, 9, 9, 9, 1, 2, 3, 4, 0, 1, 2, 3, 0, 0}, 8)) == 0);
    }

    @Test
    public void testAddition() {
        // 199 + 11 = 210
        Assert.assertEquals(
                DecimalBigInt.create(2, 1, 0), DecimalBigInt.create(1, 9, 9)
                        .plus(DecimalBigInt.create(1, 1)));

        // 199 + 1.1 = 200.1
        Assert.assertEquals(
                DecimalBigInt.create(new int[] {2, 0, 0, 1}, 2),
                DecimalBigInt.create(1, 9, 9).plus(
                        DecimalBigInt.create(new int[] {1, 1}, 0)));

        // 0.99 + 0.01 = 1
        Assert.assertEquals(DecimalBigInt.create(1), DecimalBigInt.create(
                new int[] {0, 9, 9}, 0).plus(
                DecimalBigInt.create(new int[] {0, 0, 1}, 0)));

        // 9.9 + 9.1 = 19
        Assert.assertEquals(DecimalBigInt.create(1, 9), DecimalBigInt.create(
                new int[] {9, 9}, 0).plus(
                DecimalBigInt.create(new int[] {9, 1}, 0)));
    }

    @Test
    public void testMultiplication() {
        // 99 * 3 = 297
        Assert.assertEquals(
                DecimalBigInt.create(2, 9, 7), DecimalBigInt.create(9, 9)
                        .times(DecimalBigInt.create(3)));

        // 9.9 * 9.1 = 90.09
        Assert.assertEquals(
                DecimalBigInt.create(new int[] {9, 0, 0, 9}, 1),
                DecimalBigInt.create(new int[] {9, 9}, 0).times(
                        DecimalBigInt.create(new int[] {9, 1}, 0)));
    }

    @Test
    public void testDivision() {
        // 14 / 2 = 7
        Assert.assertEquals(DecimalBigInt.create(7), DecimalBigInt.create(1, 4)
                .divideByTwo());

        // 0.95 / 2 = 0.475
        Assert.assertEquals(
                DecimalBigInt.create(new int[] {0, 4, 7, 5}, 0),
                DecimalBigInt.create(new int[] {0, 9, 5}, 0).divideByTwo());

        // 0.1 / 2 = 0.05
        Assert.assertEquals(
                DecimalBigInt.create(new int[] {0, 0, 5}, 0),
                DecimalBigInt.create(new int[] {0, 1}, 0).divideByTwo());
    }

    @Test
    public void testRemovalOfNonSignificantZeros() {
        Assert.assertEquals(1, DecimalBigInt.create(
                new int[] {9, 0, 0, 0, 0}, 0).length());
        Assert.assertEquals(5, DecimalBigInt.create(
                new int[] {0, 0, 0, 0, 9}, 0).length());
        Assert.assertEquals(1, DecimalBigInt.create(
                new int[] {0, 0, 9, 0, 0}, 2).length());
    }

    @Test
    public void testNormalization() {
        for (Pair<DecimalBigInt, DecimalBigInt> pair : this.getConfigurationsForTestingNormalization()) {
            DecimalBigInt n1 = pair.getFirst();
            DecimalBigInt n2 = pair.getSecond();

            DecimalBigInt.normalize(n1, n2);

            Assert.assertEquals(n1.length(), n2.length());
            Assert.assertEquals(
                    n1.getDecimalSeparatorIndex(),
                    n2.getDecimalSeparatorIndex());
        }
    }

    @SuppressWarnings("unchecked")
    public List<Pair<DecimalBigInt, DecimalBigInt>> getConfigurationsForTestingNormalization() {
        return Arrays.asList(
        // A > B && A.decimalSeparatorIndex > B.decimalSeparatorIndex
                Pair.create(
                        DecimalBigInt.create(new int[] {1, 2, 3, 4, 5}, 2),
                        DecimalBigInt.create(new int[] {1, 2, 3}, 1)),
                // A > B && A.decimalSeparatorIndex < B.decimalSeparatorIndex
                Pair.create(
                        DecimalBigInt.create(new int[] {1, 2, 3, 4, 5}, 1),
                        DecimalBigInt.create(new int[] {1, 2, 3}, 2)),
                // A > B && A.decimalSeparatorIndex = B.decimalSeparatorIndex
                Pair.create(
                        DecimalBigInt.create(new int[] {1, 2, 3, 4, 5}, 2),
                        DecimalBigInt.create(new int[] {1, 2, 3}, 2)),
                // A < B && A.decimalSeparatorIndex > B.decimalSeparatorIndex
                Pair.create(
                        DecimalBigInt.create(new int[] {1, 2, 3}, 2),
                        DecimalBigInt.create(new int[] {1, 2, 3, 4, 5}, 1)),
                // A < B && A.decimalSeparatorIndex < B.decimalSeparatorIndex
                Pair.create(
                        DecimalBigInt.create(new int[] {1, 2, 3}, 1),
                        DecimalBigInt.create(new int[] {1, 2, 3, 4, 5}, 2)),
                // A < B && A.decimalSeparatorIndex = B.decimalSeparatorIndex
                Pair.create(
                        DecimalBigInt.create(new int[] {1, 2, 3}, 2),
                        DecimalBigInt.create(new int[] {1, 2, 3, 4, 5}, 2)),
                // A = B && A.decimalSeparatorIndex > B.decimalSeparatorIndex
                Pair.create(
                        DecimalBigInt.create(new int[] {1, 2, 3, 4, 5}, 3),
                        DecimalBigInt.create(new int[] {1, 2, 3, 4, 5}, 2)),
                // A = B && A.decimalSeparatorIndex < B.decimalSeparatorIndex
                Pair.create(
                        DecimalBigInt.create(new int[] {1, 2, 3, 4, 5}, 2),
                        DecimalBigInt.create(new int[] {1, 2, 3, 4, 5}, 3)),
                // A = B && A.decimalSeparatorIndex = B.decimalSeparatorIndex
                Pair.create(
                        DecimalBigInt.create(new int[] {1, 2, 3, 4, 5}, 2),
                        DecimalBigInt.create(new int[] {1, 2, 3, 4, 5}, 2)));
    }

}
