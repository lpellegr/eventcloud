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

import java.util.Random;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import com.google.common.collect.Ranges;

/**
 * Utility class that defines some convenient methods to create random Strings.
 * 
 * @author lpellegr
 */
public final class StringGenerator extends Generator {

    public static final Range<Character> ALPHABETIC_LOWERCASE_RANGE =
            Ranges.closed('\u0061', '\u007A');

    public static final Range<Character> ALPHABETIC_UPPERCASE_RANGE =
            Ranges.closed('\u0041', '\u005A');

    public static final Range<Character> NUMERIC_RANGE = Ranges.closed(
            '\u0030', '\u0039');

    @SuppressWarnings("unchecked")
    public static final Range<Character>[] BMP_RANGES =
            new Range[] {Ranges.closed('\u0000', '\uFFFF')};

    @SuppressWarnings("unchecked")
    public static final Range<Character>[] ASCII_RANGES =
            new Range[] {Ranges.closed('\u0000', '\u007E')};

    @SuppressWarnings("unchecked")
    public static final Range<Character>[] PRINTABLE_ASCII_RANGES =
            new Range[] {Ranges.closed('\u0020', '\u007E')};

    @SuppressWarnings("unchecked")
    public static final Range<Character>[] ALPHABETIC_RANGES = new Range[] {
            ALPHABETIC_LOWERCASE_RANGE, ALPHABETIC_UPPERCASE_RANGE};

    @SuppressWarnings("unchecked")
    public static final Range<Character>[] ALPHANUMERIC_RANGES = new Range[] {
            ALPHABETIC_LOWERCASE_RANGE, ALPHABETIC_UPPERCASE_RANGE,
            NUMERIC_RANGE};

    private StringGenerator() {

    }

    /**
     * Creates a random string whose length is the number of characters
     * specified. Characters are chosen from {@code U+0000} to {@code U+FFFF}
     * which is sometimes referred to as the Basic Multilingual Plane (BMP).
     * 
     * @param length
     *            the length of random string to create.
     * 
     * @return the random string.
     */
    public static String random(long length) {
        return random(length, BMP_RANGES);
    }

    /**
     * Creates a random string whose length is the number of characters
     * specified. Characters are chosen from the set of characters whose ASCII
     * value is between {@code 0} and {@code 127} (inclusive).
     * 
     * @param length
     *            the length of random string to create.
     * 
     * @return the random string.
     */
    public static String randomAscii(long length) {
        return random(length, ASCII_RANGES);
    }

    /**
     * Creates a random string whose length is the number of characters
     * specified. Characters are chosen from the set of characters whose ASCII
     * value is between {@code 32} and {@code 126} (inclusive).
     * 
     * @param length
     *            the length of random string to create.
     * 
     * @return the random string.
     */
    public static String randomPrintableAscii(int length) {
        return random(length, PRINTABLE_ASCII_RANGES);
    }

    /**
     * Creates a random string whose length is the number of characters
     * specified. Characters are chosen from the set of alphabetic characters
     * (upper and lowercase).
     * 
     * @param length
     *            the length of random string to create.
     * 
     * @return the random string.
     */
    public static String randomAlphabetic(int length) {
        return random(length, ALPHABETIC_RANGES);
    }

    /**
     * Creates a random string whose length is the number of characters
     * specified. Characters are chosen from the set of alphabetic lowercase
     * characters.
     * 
     * @param length
     *            the length of random string to create.
     * 
     * @return the random string.
     */
    @SuppressWarnings("unchecked")
    public static String randomLowercaseAlphabetic(int length) {
        return random(length, new Range[] {ALPHABETIC_LOWERCASE_RANGE});
    }

    /**
     * Creates a random string whose length is the number of characters
     * specified. Characters are chosen from the set of alphabetic uppercase
     * characters.
     * 
     * @param length
     *            the length of random string to create.
     * 
     * @return the random string.
     */
    @SuppressWarnings("unchecked")
    public static String randomUppercaseAlphabetic(int length) {
        return random(length, new Range[] {ALPHABETIC_UPPERCASE_RANGE});
    }

    /**
     * Creates a random string whose length is the number of characters
     * specified. Characters are chosen from the set of alpha-numeric
     * characters.
     * 
     * @param length
     *            the length of random string to create.
     * 
     * @return the random string.
     */
    public static String randomAlphanumeric(long length) {
        return random(length, ALPHANUMERIC_RANGES);
    }

    /**
     * Creates a random string whose length is the number of characters
     * specified. Characters are chosen from the set of numeric characters.
     * 
     * @param length
     *            the length of random string to create.
     * 
     * @return the random string.
     */
    @SuppressWarnings("unchecked")
    public static String randomNumeric(long length) {
        return random(length, new Range[] {NUMERIC_RANGE});
    }

    /**
     * Creates a random string based on a variety of options.
     * 
     * @param length
     *            the length of random string to create.
     * @param ranges
     *            an array of ranges where each range specifies a continuous
     *            interval of characters which can be used to generate the
     *            random string.
     * 
     * @return the random string.
     * 
     * @throws IllegalArgumentException
     *             if {@code length} &lt; 0.
     */
    public static String random(long length, Range<Character>... ranges) {
        return random(length, Generator.RANDOM, ranges);
    }

    /**
     * Creates a random String with the specified prefix and whose length for
     * extra characters is the number of characters specified.
     * 
     * @param length
     *            the length of random string to create.
     * 
     * @param prefix
     *            the prefix to append to the string which is randomly
     *            generated.
     * 
     * @return the random string.
     */
    public static String random(long length, String prefix) {
        StringBuilder result = new StringBuilder(prefix);
        result.append(randomAlphanumeric(length));

        return result.toString();
    }

    /**
     * Creates a random String with the specified prefix and whose length for
     * extra characters is the number of characters specified.
     * 
     * @param length
     *            the length of random string to create.
     * 
     * @param prefix
     *            the prefix to append to the string which is randomly
     *            generated.
     * @param ranges
     *            an array of ranges where each range specifies a continuous
     *            interval of characters which can be used to generate the
     *            random string.
     * 
     * @return the random string.
     */
    public static String random(long length, String prefix,
                                Range<Character>... ranges) {
        StringBuilder result = new StringBuilder(prefix);
        result.append(random(length, Generator.RANDOM, ranges));

        return result.toString();
    }

    /**
     * Creates a random string based on a variety of options, using supplied
     * source of randomness.
     * <p>
     * This method accepts a user-supplied {@link Random} instance to use as a
     * source of randomness. By seeding a single {@link Random} instance with a
     * fixed seed and using it for each call, the same random sequence of
     * strings can be generated repeatedly and predictably.
     * </p>
     * 
     * @param length
     *            the length of random string to create.
     * @param random
     *            a source of randomness.
     * @param ranges
     *            an array of ranges where each range specifies a continuous
     *            interval of characters which can be used to generate the
     *            random string.
     * 
     * @return the random string.
     * 
     * @throws IllegalArgumentException
     *             if {@code length} &lt; 0.
     */
    public static String random(long length, Random random,
                                Range<Character>... ranges) {
        if (length == 0) {
            return "";
        } else if (length < 0) {
            throw new IllegalArgumentException("Specified length is negative: "
                    + length);
        }

        // continuous domain size
        int domainSize = 0;
        // size of each interval in discrete domain
        int[] intervals = new int[ranges.length];

        for (int i = 0; i < ranges.length; i++) {
            int lowerBound = ranges[i].lowerEndpoint();
            if (ranges[i].lowerBoundType() == BoundType.OPEN) {
                lowerBound++;
            }

            int upperBound = ranges[i].upperEndpoint();
            if (ranges[i].upperBoundType() == BoundType.OPEN) {
                upperBound--;
            }

            intervals[i] = upperBound - lowerBound + 1;
            domainSize += intervals[i];
        }

        if (domainSize == 0) {
            return "";
        }

        StringBuilder buffer = new StringBuilder(domainSize);

        while (length-- != 0) {
            int ch = random.nextInt(domainSize);
            int index = 0;

            for (int i = 0; i < intervals.length; i++) {
                if (ch < index + intervals[i]) {
                    int extra = 0;
                    if (ranges[i].lowerBoundType() == BoundType.OPEN) {
                        extra = 1;
                    }

                    // maps the value from the continuous domain to one from the
                    // original discrete domain
                    buffer.append((char) (ranges[i].lowerEndpoint() + ch
                            - index + extra));
                    break;
                }

                index += intervals[i];
            }
        }

        return buffer.toString();
    }

    public static void main(String[] args) {
        System.out.println("random");
        System.out.println(random(10));
        System.out.println("randomAscii");
        System.out.println(randomAscii(10));
        System.out.println("randomPrintableAscii");
        System.out.println(randomPrintableAscii(10));
        System.out.println("randomAlphabetic");
        System.out.println(randomAlphabetic(10));
        System.out.println("randomLowercaseAlphabetic");
        System.out.println(randomLowercaseAlphabetic(10));
        System.out.println("randomUppercaseAlphabetic");
        System.out.println(randomUppercaseAlphabetic(10));
        System.out.println("randomAlphanumeric");
        System.out.println(randomAlphanumeric(10));
        System.out.println("randomNumeric");
        System.out.println(randomNumeric(10));
    }

}
