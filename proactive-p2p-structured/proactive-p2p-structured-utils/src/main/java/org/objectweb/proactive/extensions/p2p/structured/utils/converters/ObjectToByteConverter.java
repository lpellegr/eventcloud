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
package org.objectweb.proactive.extensions.p2p.structured.utils.converters;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.eclipse.jetty.util.ByteArrayOutputStream2;

/**
 * This class is used to convert an object to a byte array using a regular
 * object stream.
 * 
 * @author lpellegr
 */
public class ObjectToByteConverter {

    private ObjectToByteConverter() {

    }

    /**
     * Convert an object to a byte array using a regular object stream.
     * 
     * @param obj
     *            the object to convert.
     * 
     * @return the object converted to a byte array
     * 
     * @throws IOException
     */
    public static byte[] convert(Object obj) throws IOException {
        final ByteArrayOutputStream2 byteArrayOutputStream =
                new ByteArrayOutputStream2();
        ObjectOutputStream objectOutputStream =
                new ObjectOutputStream(byteArrayOutputStream);

        try {
            objectOutputStream.writeObject(obj);
            objectOutputStream.flush();
            return byteArrayOutputStream.getBuf();
        } finally {
            if (objectOutputStream != null) {
                objectOutputStream.close();
            }
            byteArrayOutputStream.close();
        }
    }

}
