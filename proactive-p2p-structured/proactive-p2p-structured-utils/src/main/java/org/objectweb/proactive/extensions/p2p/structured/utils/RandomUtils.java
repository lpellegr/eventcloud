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

import java.util.Random;

/**
 * Provides an easy way to get random values for a {@link Random} generator
 * shared by the whole JVM.
 * 
 * @author lpellegr
 */
public class RandomUtils {

    public static final Random JVM_RANDOM = new Random();

    private RandomUtils() {
    }

    /**
     * @see java.util.Random#nextBytes(byte[])
     */
    public static void nextBytes(byte[] bytes) {
        JVM_RANDOM.nextBytes(bytes);
    }

    /**
     * @see java.util.Random#nextInt()
     */
    public static int nextInt() {
        return JVM_RANDOM.nextInt();
    }

    /**
     * @see java.util.Random#nextInt(int)
     */
    public static int nextInt(int n) {
        return JVM_RANDOM.nextInt(n);
    }

    /**
     * @see java.util.Random#nextLong()
     */
    public static long nextLong() {
        return JVM_RANDOM.nextLong();
    }

    /**
     * @see java.util.Random#nextBoolean()
     */
    public static boolean nextBoolean() {
        return JVM_RANDOM.nextBoolean();
    }

    /**
     * @see java.util.Random#nextFloat()
     */
    public static float nextFloat() {
        return JVM_RANDOM.nextFloat();
    }

    /**
     * @see java.util.Random#nextDouble()
     */
    public static double nextDouble() {
        return JVM_RANDOM.nextDouble();
    }

    /**
     * @see java.util.Random#nextGaussian()
     */
    public static double nextGaussian() {
        return JVM_RANDOM.nextGaussian();
    }

    /**
     * Returns a pseudorandom, uniformly distributed int value whose the value
     * is greater than or equal to {@code lower} and less than or equal to
     * {@code upper}.
     * 
     * @return a pseudorandom, uniformly distributed int value whose the value
     *         is greater than or equal to {@code lower} and less than or equal
     *         to {@code upper}.
     * 
     * @throws IllegalArgumentException
     *             if lower is greater than upper.
     */
    public static int nextIntClosed(int lower, int upper) {
        checkBounds(lower, upper);
        return lower + JVM_RANDOM.nextInt(upper - lower + 1);
    }

    /**
     * Returns a pseudorandom, uniformly distributed int value whose the value
     * is greater than or equal to {@code lower} and strictly less than
     * {@code upper}.
     * 
     * @return a pseudorandom, uniformly distributed int value whose the value
     *         is greater than or equal to {@code lower} and strictly less than
     *         {@code upper}.
     * 
     * @throws IllegalArgumentException
     *             if lower is greater than upper.
     */
    public static int nextIntClosedOpen(int lower, int upper) {
        checkBounds(lower, upper);
        return lower + JVM_RANDOM.nextInt(upper - lower);
    }

    /**
     * Returns a pseudorandom, uniformly distributed int value whose the value
     * is strictly greater than {@code lower} and strictly less than
     * {@code upper}.
     * 
     * @return a pseudorandom, uniformly distributed int value whose the value
     *         is strictly greater than {@code lower} and strictly less than
     *         {@code upper}.
     * 
     * @throws IllegalArgumentException
     *             if lower is greater than upper.
     */
    public static int nextIntOpen(int lower, int upper) {
        checkBounds(lower, upper);
        return lower - 1 + JVM_RANDOM.nextInt(upper - lower);
    }

    /**
     * Returns a pseudorandom, uniformly distributed int value whose the value
     * is strictly greater than lower and {@code less} than or equal to
     * {@code upper}.
     * 
     * @return a pseudorandom, uniformly distributed int value whose the value
     *         is strictly greater than {@code lower} and less than or equal to
     *         {@code upper}.
     * 
     * @throws IllegalArgumentException
     *             if lower is greater than upper.
     */
    public static int nextIntOpenClosed(int lower, int upper) {
        checkBounds(lower, upper);
        return lower - 1 + JVM_RANDOM.nextInt(upper - lower + 1);
    }

    private static void checkBounds(int lower, int upper) {
        if (lower > upper) {
            throw new IllegalArgumentException("Invalid range: lower > upper");
        }
    }

}
