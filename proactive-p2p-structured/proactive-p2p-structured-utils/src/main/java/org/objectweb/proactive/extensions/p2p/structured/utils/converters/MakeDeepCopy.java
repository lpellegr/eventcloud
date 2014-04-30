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
package org.objectweb.proactive.extensions.p2p.structured.utils.converters;

import java.io.IOException;

/**
 * This class is used to perform a deep copy of an object by using a regular
 * object stream.
 * 
 * @author lpellegr
 */
public final class MakeDeepCopy {

    private MakeDeepCopy() {

    }

    /**
     * Performs a deep copy of an object by using a regular object stream.
     * 
     * @param obj
     *            the object to be deep copied.
     * 
     * @return the copy.
     * 
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static Object makeDeepCopy(Object obj) throws IOException,
            ClassNotFoundException {
        byte[] array = ObjectToByteConverter.convert(obj);
        return ByteToObjectConverter.convert(array);
    }

}
