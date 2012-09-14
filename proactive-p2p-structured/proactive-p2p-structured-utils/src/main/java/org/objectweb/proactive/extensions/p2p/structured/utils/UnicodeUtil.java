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

import java.util.List;

/**
 * Some utility methods for unicode strings manipulation.
 * 
 * @author lpellegr
 */
public class UnicodeUtil {

    /**
     * Makes an unicode string printable by replacing each character to their
     * unicode representation thanks to the \\u notation.
     * 
     * @param string
     *            the unicode string to make printable.
     * 
     * @return a printable unicode string where each character is replace to its
     *         unicode representation thanks to the \\u notation.
     */
    public static String makePrintable(String string) {
        return makePrintable(fromStringToCodePoints(string));
    }

    public static String makePrintable(int[] codePoints) {
        StringBuilder result = new StringBuilder();

        int codePoint;
        for (int i = 0; i < codePoints.length; i++) {
            codePoint = codePoints[i];

            if (codePoint > 0xffff) {
                result.append("\\u");
                appendCodePointRepresentation(result, codePoint);
            } else if (codePoint > 0xfff) {
                result.append("\\u");
                appendCodePointRepresentation(result, codePoint);
            } else if (codePoint > 0xff) {
                result.append("\\u0");
                appendCodePointRepresentation(result, codePoint);
            } else if (codePoint > 0xf) {
                result.append("\\u00");
                appendCodePointRepresentation(result, codePoint);
            } else {
                result.append("\\u000");
                appendCodePointRepresentation(result, codePoint);
            }
        }

        return result.toString();
    }

    private static void appendCodePointRepresentation(StringBuilder buffer,
                                                      int codePoint) {
        buffer.append(Integer.toHexString(codePoint));
    }

    /**
     * Transforms a String to its representative array of unicode code points.
     * 
     * @param string
     *            the string value to transform.
     * 
     * @return an array of unicode code points.
     */
    public static int[] fromStringToCodePoints(String string) {
        int[] result = new int[string.length()];

        for (int i = 0; i < string.length();) {
            int codePoint = string.codePointAt(i);

            result[i] = codePoint;

            i += Character.charCount(codePoint);
        }

        return result;
    }

    /**
     * Transforms a list of unicode code points to a String.
     * 
     * @param codePoints
     *            the list to transform.
     * 
     * @return a String.
     */
    public static String fromCodePointsToString(List<Integer> codePoints) {
        StringBuilder result = new StringBuilder(codePoints.size());

        for (int codePoint : codePoints) {
            result.append(Character.toChars(codePoint));
        }

        return result.toString();
    }

}
