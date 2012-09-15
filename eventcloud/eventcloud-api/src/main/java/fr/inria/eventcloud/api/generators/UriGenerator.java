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
package fr.inria.eventcloud.api.generators;

import com.google.common.collect.Range;
import com.google.common.collect.Ranges;

/**
 * Provides static methods to generate random Uniform Resource Identifiers
 * (URI). URIs are a string of characters used to identify a name or a resource.
 * Such identification enables interaction with representations of the resource
 * over a network (typically the World Wide Web) using specific protocols
 * 
 * @author lpellegr
 */
public class UriGenerator extends Generator {

    @SuppressWarnings("unchecked")
    public static final Range<Character>[] URL_UNRESERVED_RANGES = new Range[] {
            StringGenerator.ALPHABETIC_LOWERCASE_RANGE,
            StringGenerator.ALPHABETIC_UPPERCASE_RANGE,
            StringGenerator.NUMERIC_RANGE,
            // - and . characters
            Ranges.closed('\u002D', '\u002E'),
            // _ character
            Ranges.closed('\u005F', '\u005F'),
            // ~ character
            Ranges.closed('\u007E', '\u007E')};

    /**
     * Creates a random URI with the specified schemeName and whose length for
     * randomly generate characters is the number of characters specified.
     * 
     * @param schemeName
     *            consists of a sequence of characters beginning with a letter
     *            and followed by any combination of letters, digits, plus
     *            ("+"), period ("."), or hyphen ("-").
     * 
     * @return the random URI.
     */
    public static String random(String schemeName) {
        return random(DEFAULT_LENGTH, schemeName);
    }

    /**
     * Creates a random URI with the specified schemeName and whose length for
     * randomly generate characters is the number of characters specified.
     * 
     * @param length
     *            the length of random part to create.
     * @param schemeName
     *            consists of a sequence of characters beginning with a letter
     *            and followed by any combination of letters, digits, plus
     *            ("+"), period ("."), or hyphen ("-").
     * 
     * @return the random URI.
     */
    public static String random(int length, String schemeName) {
        StringBuilder result = new StringBuilder(schemeName);
        result.append("://");
        result.append(StringGenerator.random(
                length, StringGenerator.ALPHANUMERIC_RANGES));

        return result.toString();
    }

    /**
     * Creates an URI with the specified {@code prefix} and a randomly generated
     * suffix whose length is the number of characters specified.
     * 
     * @param length
     *            the length of random part to create.
     * @param prefix
     *            the prefix to concatenate.
     * 
     * @return the random URI.
     */
    public static String randomPrefixed(int length, String prefix) {
        StringBuilder result = new StringBuilder(prefix);
        result.append(StringGenerator.random(
                length, StringGenerator.ALPHANUMERIC_RANGES));

        return result.toString();
    }

    public static void main(String[] args) {
        System.out.println("randomString");
        System.out.println(random("http"));
        System.out.println("randomIntString");
        System.out.println(random(5, "ftp"));
        System.out.println("randomPrefixed");
        System.out.println(randomPrefixed(5, "http://www.inria.fr/"));
    }

}
