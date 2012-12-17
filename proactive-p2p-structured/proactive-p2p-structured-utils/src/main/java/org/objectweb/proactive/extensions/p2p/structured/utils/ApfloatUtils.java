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

import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;
import org.apfloat.Apint;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;

/**
 * Some utility methods to convert a String to {@link Apfloat} and vice versa.
 * 
 * @author lpellegr
 */
public class ApfloatUtils {

    public static long DEFAULT_PRECISION = 350;

    private static final Apint RADIX = new Apint(
            P2PStructuredProperties.CAN_UPPER_BOUND.getValue() + 1);

    public static Apfloat toFloatRadix10(String value) {
        return toFloatRadix10(value, RADIX);
    }

    /**
     * Converts a string to an integer radix 10 by assuming that each character
     * is a digit radix StringElement#RADIX
     */
    public static Apfloat toFloatRadix10(String value, Apint radix) {
        return toFloatRadix10(value, radix, DEFAULT_PRECISION);
    }

    public static Apfloat toFloatRadix10(String value, long precision) {
        return toFloatRadix10(value, RADIX, precision);
    }

    public static Apfloat toFloatRadix10(String value, Apint radix,
                                         long precision) {
        int[] codepoints = UnicodeUtils.toCodePointArray(value);

        // codepoints[0] x radix^0 = codepoints[0]
        Apfloat result = new Apfloat(codepoints[0], precision);

        // the radix point is automatically set to 1 because all inserted data
        // are supposed to be between a lower and upper bound that is made of
        // one character, and hence one digit radix the upper bound value (e.g.
        // 2^16).
        Apint pow = new Apint(1);

        for (int i = 1; i < codepoints.length; i++) {
            pow = pow.multiply(radix);

            Apfloat division = new Apfloat(1, precision).divide(pow);

            result =
                    result.add(new Apfloat(codepoints[i], precision).multiply(division));

            if (precision > 0 && i == precision) {
                break;
            }
        }

        return result;
    }

    public static String toString(Apfloat apfloat) {
        return toString(apfloat, DEFAULT_PRECISION);
    }

    public static String toString(Apfloat apfloat, long precision) {
        Apint apint = apfloat.truncate();
        Apint quotient = apint;

        // handle the integer part
        StringBuilder integerPart = new StringBuilder();
        quotient = divideQuotientRecursively(integerPart, quotient);
        integerPart.reverse();

        // handle the fractional part
        // as explained at http://goo.gl/OKovZ
        Apfloat fractionalPart = ApfloatMath.modf(apfloat)[1];

        // this algorithm may not terminate and loop infinitively if the
        // apfloat precision is set as infinite
        // TODO: detect set of digits that repeat and stop after n
        // repetitions
        int loop = 0;
        while (fractionalPart.compareTo(Apfloat.ZERO) > 0) {
            // simple test to stop after PRECISION loops and thus to avoid
            // infinite loop
            if (loop > precision) {
                break;
            }

            // shift radix point to right by 1.
            fractionalPart = fractionalPart.multiply(RADIX);

            if (fractionalPart.compareTo(Apfloat.ONE) >= 0) {
                Apint truncatedPart = fractionalPart.truncate();
                // keep only the integer part
                integerPart.append(Character.toChars(truncatedPart.intValue()));
                // get rid of value left of radix point
                fractionalPart = fractionalPart.subtract(truncatedPart);
            } else {
                integerPart.append((char) 0);
            }

            loop++;
        }

        if (integerPart.length() == 0) {
            integerPart.append((char) Character.MIN_CODE_POINT);
        }

        return integerPart.toString();
    }

    private static Apint divideQuotientRecursively(StringBuilder result,
                                                   Apint quotient) {
        while (quotient.compareTo(Apint.ZERO) != 0) {
            int remainder = quotient.mod(RADIX).intValue();
            result.append(new String(Character.toChars(remainder)));
            quotient = quotient.divide(RADIX);
        }

        return quotient;
    }

}
