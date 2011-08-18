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
package org.objectweb.proactive.extensions.p2p.structured.utils.converter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

/**
 * This class converts an object using a regular object stream.
 * 
 * @author lpellegr
 */
public class ByteToObjectConverter {

    private ByteToObjectConverter() {
        
    }
    
    /**
     * Converts to an object using a regular object stream.
     * 
     * @param byteArray
     *            the byte array to convert.
     * 
     * @return the deserialized object.
     * 
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object convert(byte[] byteArray) throws IOException,
            ClassNotFoundException {
        InputStream is = new ByteArrayInputStream(byteArray);
        ObjectInputStream objectInputStream = null;

        try {
            objectInputStream = new ObjectInputStream(is);
            return objectInputStream.readObject();
        } finally {
            if (objectInputStream != null) {
                objectInputStream.close();
            }
            is.close();
        }
    }

}
