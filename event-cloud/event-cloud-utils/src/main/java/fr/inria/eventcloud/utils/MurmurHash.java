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
package fr.inria.eventcloud.utils;

import java.nio.ByteBuffer;

/**
 * Murmur hash function.
 * <p>
 * The murmur hash is a relative fast hash function from
 * http://murmurhash.googlepages.com/ for platforms with efficient
 * multiplication.
 * <p>
 * This is a re-implementation of the original C code plus some additional
 * features.
 * <p>
 * hash32 and hash64 are Murmur2 whereas hash128 is Murmur3.
 * 
 * @author Viliam Holub
 * @author lpellegr
 * 
 * @version 1.0.2
 */
public final class MurmurHash {

    private MurmurHash() {

    }

    /**
     * Generates 32 bit hash from byte array of the given length and seed.
     * 
     * @param data
     *            byte array to hash.
     * @param length
     *            length of the array to hash.
     * @param seed
     *            initial seed value.
     * 
     * @return 32 bit hash of the given array.
     */

    public static int hash32(ByteBuffer data, int offset, int length, int seed) {
        int m = 0x5bd1e995;
        int r = 24;

        int h = seed ^ length;

        int len_4 = length >> 2;

        for (int i = 0; i < len_4; i++) {
            int i_4 = i << 2;
            int k = data.get(offset + i_4 + 3);
            k = k << 8;
            k = k | (data.get(offset + i_4 + 2) & 0xff);
            k = k << 8;
            k = k | (data.get(offset + i_4 + 1) & 0xff);
            k = k << 8;
            k = k | (data.get(offset + i_4 + 0) & 0xff);
            k *= m;
            k ^= k >>> r;
            k *= m;
            h *= m;
            h ^= k;
        }

        // avoid calculating modulo
        int len_m = len_4 << 2;
        int left = length - len_m;

        if (left != 0) {
            if (left >= 3) {
                h ^= (int) data.get(offset + length - 3) << 16;
            }
            if (left >= 2) {
                h ^= (int) data.get(offset + length - 2) << 8;
            }
            if (left >= 1) {
                h ^= (int) data.get(offset + length - 1);
            }

            h *= m;
        }

        h ^= h >>> 13;
        h *= m;
        h ^= h >>> 15;

        return h;
    }

    /**
     * Generates 32 bit hash from byte array with default seed value.
     * 
     * @param data
     *            byte array to hash.
     * @param length
     *            length of the array to hash.
     * 
     * @return 32 bit hash of the given array.
     */
    public static int hash32(final byte[] data, int length) {
        return hash32(ByteBuffer.wrap(data), 0, length, 0x9747b28c);
    }

    /**
     * Generates 32 bit hash from a string.
     * 
     * @param text
     *            string to hash.
     * 
     * @return 32 bit hash of the given string.
     */
    public static int hash32(final String text) {
        final byte[] bytes = text.getBytes();
        return hash32(bytes, bytes.length);
    }

    /**
     * Generates 32 bits hash from an array of strings.
     * 
     * @param strings
     *            string to hash.
     * 
     * @return 32 bit hash of the given array of strings.
     */
    public static int hash32(final String... strings) {
        return hash32(append(strings));
    }

    /**
     * Generates 32 bit hash from a substring.
     * 
     * @param text
     *            string to hash.
     * @param from
     *            starting index.
     * @param length
     *            length of the substring to hash.
     * 
     * @return 32 bit hash of the given string.
     */
    public static int hash32(final String text, int from, int length) {
        return hash32(text.substring(from, from + length));
    }

    /**
     * Generates 64 bit hash from byte array of the given length and seed.
     * 
     * @param data
     *            byte array to hash.
     * @param length
     *            length of the array to hash.
     * @param seed
     *            initial seed value.
     * 
     * @return 64 bit hash of the given array
     */
    public static long hash64(ByteBuffer data, int offset, int length, long seed) {
        long m64 = 0xc6a4a7935bd1e995L;
        int r64 = 47;

        long h64 = (seed & 0xffffffffL) ^ (m64 * length);

        int lenLongs = length >> 3;

        for (int i = 0; i < lenLongs; ++i) {
            int i_8 = i << 3;

            long k64 =
                    ((long) data.get(offset + i_8 + 0) & 0xff)
                            + (((long) data.get(offset + i_8 + 1) & 0xff) << 8)
                            + (((long) data.get(offset + i_8 + 2) & 0xff) << 16)
                            + (((long) data.get(offset + i_8 + 3) & 0xff) << 24)
                            + (((long) data.get(offset + i_8 + 4) & 0xff) << 32)
                            + (((long) data.get(offset + i_8 + 5) & 0xff) << 40)
                            + (((long) data.get(offset + i_8 + 6) & 0xff) << 48)
                            + (((long) data.get(offset + i_8 + 7) & 0xff) << 56);

            k64 *= m64;
            k64 ^= k64 >>> r64;
            k64 *= m64;

            h64 ^= k64;
            h64 *= m64;
        }

        int rem = length & 0x7;

        switch (rem) {
            case 0:
                break;
            case 7:
                h64 ^= (long) data.get(offset + length - rem + 6) << 48;
            case 6:
                h64 ^= (long) data.get(offset + length - rem + 5) << 40;
            case 5:
                h64 ^= (long) data.get(offset + length - rem + 4) << 32;
            case 4:
                h64 ^= (long) data.get(offset + length - rem + 3) << 24;
            case 3:
                h64 ^= (long) data.get(offset + length - rem + 2) << 16;
            case 2:
                h64 ^= (long) data.get(offset + length - rem + 1) << 8;
            case 1:
                h64 ^= (long) data.get(offset + length - rem);
                h64 *= m64;
        }

        h64 ^= h64 >>> r64;
        h64 *= m64;
        h64 ^= h64 >>> r64;

        return h64;
    }

    /**
     * Generates 64 bit hash from byte array with default seed value.
     * 
     * @param data
     *            byte array to hash.
     * @param length
     *            length of the array to hash.
     * 
     * @return 64 bit hash of the given string.
     */
    public static long hash64(final byte[] data, int length) {
        return hash64(ByteBuffer.wrap(data), 0, length, 0xe17a1465);
    }

    /**
     * Generates 64 bit hash from a string.
     * 
     * @param text
     *            string to hash.
     * 
     * @return 64 bit hash of the given string.
     */
    public static long hash64(final String text) {
        final byte[] bytes = text.getBytes();
        return hash64(bytes, bytes.length);
    }

    /**
     * Generates 64 bits hash from an array of strings.
     * 
     * @param strings
     *            string to hash.
     * 
     * @return 64 bit hash of the given array of strings.
     */
    public static long hash64(final String... strings) {
        return hash64(append(strings));
    }

    /**
     * Generates 64 bit hash from a substring.
     * 
     * @param text
     *            string to hash.
     * @param from
     *            starting index.
     * @param length
     *            length of the substring to hash.
     * 
     * @return 64 bit hash of the given array.
     */
    public static long hash64(final String text, int from, int length) {
        return hash64(text.substring(from, from + length));
    }

    public static LongLong hash128(ByteBuffer key, int offset, int length,
                                   long seed) {
        // process as 128-bit blocks.
        final int nblocks = length >> 4;

        long h1 = seed;
        long h2 = seed;

        long c1 = 0x87c37b91114253d5L;
        long c2 = 0x4cf5ad432745937fL;

        // ----------
        // body

        for (int i = 0; i < nblocks; i++) {
            // int i_8 = i << 4;

            long k1 = getblock(key, offset, i * 2 + 0);
            long k2 = getblock(key, offset, i * 2 + 1);

            k1 *= c1;
            k1 = rotl64(k1, 31);
            k1 *= c2;
            h1 ^= k1;

            h1 = rotl64(h1, 27);
            h1 += h2;
            h1 = h1 * 5 + 0x52dce729;

            k2 *= c2;
            k2 = rotl64(k2, 33);
            k2 *= c1;
            h2 ^= k2;

            h2 = rotl64(h2, 31);
            h2 += h1;
            h2 = h2 * 5 + 0x38495ab5;
        }

        // ----------
        // tail

        // Advance offset to the unprocessed tail of the data.
        offset += nblocks * 16;

        long k1 = 0;
        long k2 = 0;

        switch (length & 15) {
            case 15:
                k2 ^= ((long) key.get(offset + 14)) << 48;
            case 14:
                k2 ^= ((long) key.get(offset + 13)) << 40;
            case 13:
                k2 ^= ((long) key.get(offset + 12)) << 32;
            case 12:
                k2 ^= ((long) key.get(offset + 11)) << 24;
            case 11:
                k2 ^= ((long) key.get(offset + 10)) << 16;
            case 10:
                k2 ^= ((long) key.get(offset + 9)) << 8;
            case 9:
                k2 ^= ((long) key.get(offset + 8)) << 0;
                k2 *= c2;
                k2 = rotl64(k2, 33);
                k2 *= c1;
                h2 ^= k2;

            case 8:
                k1 ^= ((long) key.get(offset + 7)) << 56;
            case 7:
                k1 ^= ((long) key.get(offset + 6)) << 48;
            case 6:
                k1 ^= ((long) key.get(offset + 5)) << 40;
            case 5:
                k1 ^= ((long) key.get(offset + 4)) << 32;
            case 4:
                k1 ^= ((long) key.get(offset + 3)) << 24;
            case 3:
                k1 ^= ((long) key.get(offset + 2)) << 16;
            case 2:
                k1 ^= ((long) key.get(offset + 1)) << 8;
            case 1:
                k1 ^= ((long) key.get(offset));
                k1 *= c1;
                k1 = rotl64(k1, 31);
                k1 *= c2;
                h1 ^= k1;
        };

        // ----------
        // finalization

        h1 ^= length;
        h2 ^= length;

        h1 += h2;
        h2 += h1;

        h1 = fmix(h1);
        h2 = fmix(h2);

        h1 += h2;
        h2 += h1;

        return new LongLong(h1, h2);
    }

    /**
     * Generates 128 bit hash from byte array with default seed value.
     * 
     * @param data
     *            byte array to hash.
     * @param length
     *            length of the array to hash.
     * 
     * @return 128 bit hash of the given string.
     */
    public static LongLong hash128(final byte[] data, int length) {
        return hash128(ByteBuffer.wrap(data), 0, length, 0xe17a1465);
    }

    /**
     * Generates 128 bit hash from a string.
     * 
     * @param text
     *            string to hash.
     * 
     * @return 128 bit hash of the given string.
     */
    public static LongLong hash128(final String text) {
        final byte[] bytes = text.getBytes();
        return hash128(bytes, bytes.length);
    }

    /**
     * Generates 128 bits hash from an array of strings.
     * 
     * @param strings
     *            string to hash.
     * 
     * @return 128 bit hash of the given array of strings.
     */
    public static LongLong hash128(final String... strings) {
        return hash128(append(strings));
    }

    /**
     * Generates 128 bit hash from a substring.
     * 
     * @param text
     *            string to hash.
     * @param from
     *            starting index.
     * @param length
     *            length of the substring to hash.
     * 
     * @return 128 bit hash of the given array.
     */
    public static LongLong hash128(final String text, int from, int length) {
        return hash128(text.substring(from, from + length));
    }

    private static String append(String... strings) {
        StringBuilder buf = new StringBuilder();
        for (String s : strings) {
            buf.append(s);
        }
        return buf.toString();
    }

    private static long getblock(ByteBuffer key, int offset, int index) {
        int i_8 = index << 3;
        return (((long) key.get(offset + i_8 + 0) & 0xff)
                + (((long) key.get(offset + i_8 + 1) & 0xff) << 8)
                + (((long) key.get(offset + i_8 + 2) & 0xff) << 16)
                + (((long) key.get(offset + i_8 + 3) & 0xff) << 24)
                + (((long) key.get(offset + i_8 + 4) & 0xff) << 32)
                + (((long) key.get(offset + i_8 + 5) & 0xff) << 40)
                + (((long) key.get(offset + i_8 + 6) & 0xff) << 48) + (((long) key.get(offset
                + i_8 + 7) & 0xff) << 56));
    }

    private static long rotl64(long v, int n) {
        return ((v << n) | (v >>> (64 - n)));
    }

    private static long fmix(long k) {
        k ^= k >>> 33;
        k *= 0xff51afd7ed558ccdL;
        k ^= k >>> 33;
        k *= 0xc4ceb9fe1a85ec53L;
        k ^= k >>> 33;

        return k;
    }

}
