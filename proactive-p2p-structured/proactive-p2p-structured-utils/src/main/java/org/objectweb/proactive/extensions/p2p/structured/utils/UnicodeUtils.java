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

import java.util.List;

/**
 * Some utility methods for unicode strings manipulation.
 * 
 * @author lpellegr
 */
public final class UnicodeUtils {

    /**
     * Compares two strings lexicographically by using the UTF-32 representation
     * (i.e. directly the code point value) in contrast to the
     * {@link String#compareTo(String)} method that uses the UTF-16
     * representation.
     * 
     * @param s1
     *            a string to be compared.
     * @param s2
     *            a string to be compared.
     * 
     * @return the value {@code 0} if the argument string is equal to this
     *         string; a value less than {@code 0} if this string is
     *         lexicographically less than the string argument; and a value
     *         greater than {@code 0} if this string is lexicographically
     *         greater than the string argument.
     */
    public static int compareUtf32(String s1, String s2) {
        int[] cpts1 = toCodePointArray(s1);
        int[] cpts2 = toCodePointArray(s2);

        int len1 = cpts1.length;
        int len2 = cpts2.length;

        int min = Math.min(len1, len2);

        for (int i = 0; i < min; i++) {
            int c1 = cpts1[i];
            int c2 = cpts2[i];

            if (c1 != c2) {
                return c1 - c2;
            }
        }

        return len1 - len2;
    }

    /**
     * Returns a representation of the specified {@code string} by using the
     * Unicode notation.
     * 
     * @param string
     *            the string value.
     * 
     * @return a representation that uses the unicode notation.
     */
    public static String toStringUtf32(String string) {
        return toStringUtf32(toCodePointArray(string));
    }

    public static String toStringUtf32(int... codePoints) {
        StringBuilder result = new StringBuilder(codePoints.length);

        for (int codePoint : codePoints) {
            result.append(getScalarValue(codePoint, true));
        }

        return result.toString();
    }

    /**
     * Returns a representation of the specified {@code string} by using the
     * Unicode notation.
     * 
     * @param string
     *            the string value.
     * 
     * @return a representation that uses the unicode notation.
     */
    public static String toStringUtf16(String string) {
        return toStringUtf16(toCodePointArray(string));
    }

    public static String toStringUtf16(int... codePoints) {
        StringBuilder result = new StringBuilder(codePoints.length);

        for (int codePoint : codePoints) {
            result.append(getScalarValueUtf16(codePoint));
        }

        return result.toString();
    }

    public static String translate(String s, int shift) {
        int[] codePoints = UnicodeUtils.toCodePointArray(s);

        for (int i = 0; i < codePoints.length; i++) {
            codePoints[i] = codePoints[i] + shift;
        }

        return UnicodeUtils.toString(codePoints);
    }

    public static String getScalarValueUtf16(int codePoint) {
        if (Character.isSupplementaryCodePoint(codePoint)) {
            return getScalarValue(highSurrogate(codePoint), false)
                    + getScalarValue(lowSurrogate(codePoint), false);
        } else {
            return getScalarValue(codePoint, false);
        }
    }

    private static String getScalarValue(int codePoint,
                                         boolean maybeSupplementaryCharacter) {
        if (!Character.isValidCodePoint(codePoint)) {
            throw new IllegalArgumentException("Invalid code point: "
                    + codePoint);
        }

        String hexValue = Integer.toHexString(codePoint);

        int nbZeros = 4 - hexValue.length();

        if (maybeSupplementaryCharacter) {
            nbZeros = 6 - hexValue.length();
        }

        StringBuilder result = new StringBuilder("\\u");
        for (int i = 0; i < nbZeros; i++) {
            result.append('0');
        }
        result.append(hexValue.toUpperCase());

        return result.toString();
    }

    /*
     * The next two methods have been copied from JDK7
     */

    /**
     * Returns the leading surrogate (a <a
     * href="http://www.unicode.org/glossary/#high_surrogate_code_unit"> high
     * surrogate code unit</a>) of the <a
     * href="http://www.unicode.org/glossary/#surrogate_pair"> surrogate
     * pair</a> representing the specified supplementary character (Unicode code
     * point) in the UTF-16 encoding. If the specified character is not a <a
     * href="Character.html#supplementary">supplementary character</a>, an
     * unspecified {@code char} is returned.
     * 
     * @param codePoint
     *            a supplementary character (Unicode code point)
     * @return the leading surrogate code unit used to represent the character
     *         in the UTF-16 encoding
     */
    public static char highSurrogate(int codePoint) {
        return (char) ((codePoint >>> 10) + (Character.MIN_HIGH_SURROGATE - (Character.MIN_SUPPLEMENTARY_CODE_POINT >>> 10)));
    }

    /**
     * Returns the trailing surrogate (a <a
     * href="http://www.unicode.org/glossary/#low_surrogate_code_unit"> low
     * surrogate code unit</a>) of the <a
     * href="http://www.unicode.org/glossary/#surrogate_pair"> surrogate
     * pair</a> representing the specified supplementary character (Unicode code
     * point) in the UTF-16 encoding. If the specified character is not a <a
     * href="Character.html#supplementary">supplementary character</a>, an
     * unspecified {@code char} is returned.
     * 
     * @param codePoint
     *            a supplementary character (Unicode code point)
     * @return the trailing surrogate code unit used to represent the character
     *         in the UTF-16 encoding
     */
    public static char lowSurrogate(int codePoint) {
        return (char) ((codePoint & 0x3ff) + Character.MIN_LOW_SURROGATE);
    }

    /**
     * Transforms a String to its representative array of unicode code points.
     * 
     * @param string
     *            the string value to transform.
     * 
     * @return an array of unicode code points.
     */
    public static int[] toCodePointArray(String string) {
        // the char array is copied from the string using toCharArray() because
        // direct access to an array is faster than indirect access through a
        // method
        char[] sarray = string.toCharArray();

        int[] result =
                new int[Character.codePointCount(sarray, 0, sarray.length)];

        for (int i = 0, j = 0, codePoint = 0; i < sarray.length; i +=
                Character.charCount(codePoint)) {
            codePoint = Character.codePointAt(sarray, i);
            result[j++] = codePoint;
        }

        return result;
    }

    /**
     * Transforms an array of unicode code points to a String.
     * 
     * @param codePoints
     *            the array to transform.
     * 
     * @return a String.
     */
    public static String toString(int... codePoints) {
        return new String(codePoints, 0, codePoints.length);
    }

    /**
     * Transforms a list of unicode code points to a String.
     * 
     * @param codePoints
     *            the list to transform.
     * 
     * @return a String.
     */
    public static String toString(List<Integer> codePoints) {
        int[] cpa = new int[codePoints.size()];

        for (int i = 0; i < codePoints.size(); i++) {
            cpa[i] = codePoints.get(i);
        }

        return toString(cpa);
    }

}
