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

import java.nio.ByteBuffer;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;

/**
 * 
 * @author lpellegr
 */
public class Base64UUID {

    public static String encode(UUID uuid) {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[16]);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return Base64.encodeBase64URLSafeString(buffer.array());
    }

    public static UUID decode(String base64uuid) {
        if (base64uuid.length() != 22) {
            throw new IllegalArgumentException(
                    "Not a valid Base64 encoded UUID");
        }
        ByteBuffer buffer = ByteBuffer.wrap(Base64.decodeBase64(base64uuid));
        if (buffer.capacity() != 16) {
            throw new IllegalArgumentException(
                    "Not a valid Base64 encoded UUID");
        }
        return new UUID(buffer.getLong(), buffer.getLong());
    }

}