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

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

/**
 * Defines a unique identifier (based on a {@link UUID}) with a toString method
 * which returns a base64 encoded value.
 * 
 * @author lpellegr
 */
public class UniqueId implements Serializable {

    private static final long serialVersionUID = 140L;

    protected final UUID value;

    /**
     * Creates a unique identifier.
     */
    public UniqueId() {
        this(UUID.randomUUID());
    }

    protected UniqueId(UUID value) {
        this.value = value;
    }

    /**
     * Parses the string argument as a UniqueId.
     * 
     * @param uniqueId
     *            a <code>String</code> containing the <code>UniqueId</code>
     *            representation to be parsed.
     * 
     * @return the <code>UniqueId</code> represented by the argument.
     */
    public static final UniqueId parseUniqueId(String uniqueId) {
        return new UniqueId(decode(uniqueId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof UniqueId
                && this.value.equals(((UniqueId) obj).value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return encode(this.value);
    }

    public static String encode(UUID uuid) {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());

        return DatatypeConverter.printHexBinary(buffer.array());
    }

    public static UUID decode(String uuid) {
        ByteBuffer buffer =
                ByteBuffer.wrap(DatatypeConverter.parseHexBinary(uuid));

        return new UUID(buffer.getLong(), buffer.getLong());
    }

}
