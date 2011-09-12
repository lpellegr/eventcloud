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
import java.util.Arrays;

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

    public LongLong(long[] longLongValue) {
        if (longLongValue.length != 2) {
            throw new IllegalArgumentException(
                    "Expected 128 bits value but was: "
                            + (2 << (4 + longLongValue.length)) + " bits");
        }

        this.value = longLongValue;
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
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < this.value.length; i++) {
            result.append(this.value[i]);
            if (i < this.value.length - 1) {
                result.append("#");
            }
        }
        return result.toString();
    }

    public static LongLong fromString(String longLongValue) {
        String[] values = longLongValue.split("#");
        long[] result = new long[values.length];

        if (values.length != 2) {
            throw new IllegalArgumentException(longLongValue
                    + " is not a LongLong");
        }

        for (int i = 0; i < values.length; i++) {
            result[i] = Long.parseLong(values[i]);
        }

        return new LongLong(result);
    }

}
