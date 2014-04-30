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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import org.objectweb.proactive.extensions.p2p.structured.utils.converters.ByteToObjectConverter;
import org.objectweb.proactive.extensions.p2p.structured.utils.converters.ObjectToByteConverter;

/**
 * SerializedValue is a class that is used to maintain a value as an array of
 * bytes. This is especially useful for conveying a value on a peer to peer
 * network when ProActive is used.
 * <p>
 * Indeed, the value associated to a data that is conveyed on a peer-to-peer
 * network is serialized and deserialized at each hop. The only means to avoid
 * it is to send the data as an array of bytes: when a class which is marked as
 * {@link Serializable} contains some fields with type bytes, the Java
 * serialization does not have to perform any action.
 * 
 * @author lpellegr
 * 
 * @param <T>
 *            the value type to serialize.
 */
public class SerializedValue<T> implements Externalizable {

    private static final long serialVersionUID = 160L;

    private byte[] bytes;

    private transient T value;

    /**
     * This constructor is required to work with efficient serialization through
     * the {@link Externalizable} interface but should never be used manually.
     * The correct manner to create an instance of this class is to use
     * {@link #create(Object)}.
     */
    public SerializedValue() {
    }

    protected SerializedValue(T value) {
        try {
            this.bytes = ObjectToByteConverter.convert(value);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Creates a new serialized value from the specified {@code value}.
     * 
     * @param value
     *            the value to serialize and to keep as a byte array.
     * 
     * @return an instance of {@link SerializedValue} for the given value.
     */
    public static <T> SerializedValue<T> create(T value) {
        return new SerializedValue<T>(value);
    }

    @SuppressWarnings("unchecked")
    public synchronized T getValue() {
        if (this.value == null) {
            try {
                this.value = (T) ByteToObjectConverter.convert(this.bytes);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return this.value;
    }

    public byte[] getBytes() {
        return this.bytes;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        int length = in.readInt();
        byte[] content = new byte[length];
        in.readFully(content);
        this.bytes = content;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeInt(this.bytes.length);
        out.write(this.bytes);
    }

}
