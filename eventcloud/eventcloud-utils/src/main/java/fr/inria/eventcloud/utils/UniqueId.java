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

    private static final long serialVersionUID = 160L;

    protected final UUID value;

    /**
     * Cached hashCode value (class is immutable)
     */
    private transient int hashCode;

    /**
     * Cached toString value (class is immutable)
     */
    private transient String toString;

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
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        return other instanceof UniqueId
                && this.value.equals(((UniqueId) other).value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        if (this.hashCode == 0) {
            this.hashCode = this.value.hashCode();
        }

        return this.hashCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        if (this.toString == null) {
            this.toString = encode(this.value);
        }

        return this.toString;
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

    public static void main(String[] args) {
        System.out.println(new UniqueId());
    }

}
