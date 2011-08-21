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

/**
 * Some convenient methods for casting objects.
 * 
 * @author lpellegr
 */
public final class CastUtils {

    private CastUtils() {

    }

    /**
     * Converts an array of primitive bytes to objects.
     * 
     * This method returns {@code null} for a {@code null} input array.
     * 
     * @param byteArray
     *            a byte array.
     * 
     * @return a {@code Byte} array, {@code null} if null array input.
     */
    public static Byte[] toObject(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }

        Byte[] box = new Byte[byteArray.length];
        for (int i = 0; i < box.length; i++) {
            box[i] = byteArray[i];
        }
        return box;
    }

    /**
     * Converts an array of object Bytes to primitives handling {@code null}.
     * 
     * This method returns {@code null} for a {@code null} input array.
     * 
     * @param byteArray
     *            a Byte array, may be null.
     * 
     * @return a byte array, null if null array input.
     */
    public static byte[] toPrimitive(Byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }

        byte[] unbox = new byte[byteArray.length];
        for (int i = 0; i < unbox.length; i++) {
            unbox[i] = byteArray[i];
        }
        return unbox;
    }

}
