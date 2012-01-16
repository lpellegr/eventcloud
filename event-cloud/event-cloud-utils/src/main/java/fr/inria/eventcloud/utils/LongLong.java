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
package fr.inria.eventcloud.utils;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;

/**
 * This class is used to wrap a 128 bits value (which is an array composed of 2
 * long in Java).
 * 
 * @see MurmurHash#hash128(String)
 * 
 * @author lpellegr
 */
public class LongLong implements Comparable<LongLong>, Serializable {

    private static final long serialVersionUID = 1L;

    private final long[] value;

    /**
     * Constructs a LongLong from the two specified long values.
     * 
     * @param mostSigBits
     *            The most significant bits of the {@code LongLong}.
     * 
     * @param leastSigBits
     *            The least significant bits of the {@code LongLong}.
     */
    public LongLong(long mostSigBits, long leastSigBits) {
        this.value = new long[2];
        this.value[0] = mostSigBits;
        this.value[1] = leastSigBits;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(LongLong l) {
        if (this.value[0] < l.value[0]) {
            return -1;
        } else if (this.value[0] > l.value[0]) {
            return 1;
        } else {
            if (this.value[1] < l.value[1]) {
                return -1;
            } else if (this.value[1] > l.value[1]) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof LongLong
                && Arrays.equals(this.value, ((LongLong) obj).value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(this.value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(this.value[0]);
        buffer.putLong(this.value[1]);

        return Base64.encodeBase64URLSafeString(buffer.array());
    }

    public static LongLong fromString(String base64uuid) {
        if (base64uuid.length() != 22) {
            throw new IllegalArgumentException(
                    "Not a valid Base64 encoded LongLong");
        }
        ByteBuffer buffer = ByteBuffer.wrap(Base64.decodeBase64(base64uuid));
        if (buffer.capacity() != 16) {
            throw new IllegalArgumentException(
                    "Not a valid Base64 encoded LongLong");
        }

        return new LongLong(buffer.getLong(), buffer.getLong());
    }

}