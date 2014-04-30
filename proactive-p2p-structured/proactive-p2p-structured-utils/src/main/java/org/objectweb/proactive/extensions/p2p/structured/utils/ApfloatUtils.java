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
package org.objectweb.proactive.extensions.p2p.structured.utils;

import org.apfloat.Apfloat;
import org.apfloat.Apint;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;

/**
 * Some utility methods to convert a String to {@link Apfloat} and vice versa.
 * 
 * @author lpellegr
 */
public class ApfloatUtils {

    /**
     * The constant two associated to default apfloat radix.
     */
    public static Apfloat TWO = new Apfloat(2);

    private static final Apint RADIX = new Apint(
            P2PStructuredProperties.CAN_UPPER_BOUND.getValue()
                    - P2PStructuredProperties.CAN_LOWER_BOUND.getValue() + 1);

    public static Apfloat toFloatRadix10(String value) {
        return toFloatRadix10(value, RADIX);
    }

    /**
     * Converts a string to an integer radix 10 by assuming that each character
     * is a digit radix StringElement#RADIX
     */
    public static Apfloat toFloatRadix10(String value, Apint radix) {
        return toFloatRadix10(
                value, radix,
                P2PStructuredProperties.CAN_COORDINATES_PRECISION.getValue());
    }

    public static Apfloat toFloatRadix10(String value, long precision) {
        return toFloatRadix10(value, RADIX, precision);
    }

    public static Apfloat toFloatRadix10(String value, Apint radix,
                                         long precision) {
        int[] codepoints = UnicodeUtils.toCodePointArray(value);

        for (int i = 0; i < codepoints.length; i++) {
            codepoints[i] =
                    codepoints[i]
                            - P2PStructuredProperties.CAN_LOWER_BOUND.getValue();
        }

        // codepoints[0] x radix^0 = codepoints[0]
        Apfloat result = new Apfloat(codepoints[0], precision);

        // the radix point is automatically set to 1 because all inserted data
        // are supposed to be between a lower and upper bound that is made of
        // one character, and hence one digit radix the upper bound value (e.g.
        // 2^16).
        Apint pow = Apint.ONE;

        // ... + codepoints[i]*RADIX^{-i} + codepoints[i+1]*RADIX^{-i-1} + ...
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
        return toString(
                apfloat,
                P2PStructuredProperties.CAN_COORDINATES_PRECISION.getValue());
    }

    // http://mathforum.org/library/drmath/view/55744.html
    public static String toString(Apfloat apfloat, long precision) {
        StringBuilder result = new StringBuilder();

        // handle integer part
        Apint integerPart = apfloat.truncate();

        Apint quotient = integerPart;
        while (quotient.compareTo(Apint.ZERO) > 0) {
            Apint remainder = quotient.mod(RADIX);
            quotient = quotient.divide(RADIX);

            result.append(Character.toChars(P2PStructuredProperties.CAN_LOWER_BOUND.getValue()
                    + remainder.intValue()));
        }
        result.reverse();

        // handle the fractional part
        Apfloat fractionalPart = apfloat.frac();

        for (int i = 0; i < precision; i++) {
            if (i > 0) {
                fractionalPart = fractionalPart.subtract(integerPart);
            }

            // shift radix point to right by 1.
            fractionalPart = fractionalPart.multiply(RADIX);

            if (fractionalPart.compareTo(RADIX) >= 0
                    || fractionalPart.compareTo(Apfloat.ZERO) == 0) {
                break;
            }

            integerPart = fractionalPart.truncate();

            // keep only the integer part
            result.append(Character.toChars(P2PStructuredProperties.CAN_LOWER_BOUND.getValue()
                    + integerPart.intValue()));

            i++;
        }

        return result.toString();
    }

}
