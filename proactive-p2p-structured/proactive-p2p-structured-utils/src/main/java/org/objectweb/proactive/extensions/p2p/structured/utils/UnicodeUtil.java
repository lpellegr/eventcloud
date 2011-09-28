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
package org.objectweb.proactive.extensions.p2p.structured.utils;

import java.util.LinkedList;

/**
 * Some utility methods for unicode strings manipulation.
 * 
 * @author lpellegr
 */
public class UnicodeUtil {

    private static char CODE_POINTS_SEPARATOR = '.';

    /**
     * Makes an unicode string printable by replacing all non printable ascii
     * characters to their unicode representation by using the {@code \\uxxxx}
     * notation.
     * 
     * @param string
     *            the unicode string to make printable.
     * 
     * @return a printable unicode string.
     */
    public static String makePrintable(String string) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < string.length(); i++) {
            // printable ASCII characters are output
            if (string.codePointAt(i) > 32 && string.codePointAt(i) < 127) {
                result.append(Character.toChars(string.codePointAt(i)));
            } else {
                // tries to get an unicode representation
                result.append("\\u"
                        + Integer.toHexString(string.codePointAt(i) | 0x10000)
                                .substring(1));
            }
        }

        return result.toString();
    }

    /**
     * Transforms a unicode string to a string containing the code points value
     * associated to the original {@code string}. Each code point value is
     * separated by using the default char separator which is defined as
     * {@code .}.
     * 
     * @param string
     *            the unicode string to transform.
     * 
     * @return a list of code points for the given {@code string}.
     */
    public static String asCodePoints(String string) {
        return asCodePoints(string, CODE_POINTS_SEPARATOR);
    }

    /**
     * Transforms a unicode string to a string containing the code points value
     * associated to the original {@code string}. Each code point value is
     * separated by using the specified {@code separator}.
     * 
     * @param string
     *            the unicode string to transform.
     * 
     * @return a list of code points separated by the specified
     *         {@code separator} character for the given {@code string}.
     */
    public static String asCodePoints(String string, char separator) {
        StringBuilder result = new StringBuilder();
        LinkedList<Integer> codePoints = fromStringToUnicode(string);

        for (int i = 0; i < codePoints.size(); i++) {
            result.append(codePoints.get(i));
            if (i < codePoints.size() - 1) {
                result.append(separator);
            }
        }

        return result.toString();
    }

    private static LinkedList<Integer> fromStringToUnicode(String string) {
        LinkedList<Integer> codePtArray = new LinkedList<Integer>();
        for (int i = 0; i < string.length(); i++) {
            int codePt = string.codePointAt(i);
            codePtArray.add(codePt);
        }

        return (codePtArray);
    }

}
