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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package org.objectweb.proactive.extensions.p2p.structured.utils;

import java.io.IOException;
import java.io.Serializable;

import org.objectweb.proactive.core.util.converter.ByteToObjectConverter;
import org.objectweb.proactive.core.util.converter.ObjectToByteConverter;

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
public final class SerializedValue<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private byte[] bytes;

    public transient T value;

    public SerializedValue(T value) {
        try {
            this.bytes = ObjectToByteConverter.ObjectStream.convert(value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public synchronized T getValue() {
        if (this.value == null) {
            try {
                this.value =
                        (T) ByteToObjectConverter.ObjectStream.convert(this.bytes);
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

}