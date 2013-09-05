/**
 * Copyright (c) 2011-2013 INRIA.
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

/**
 * Defines the methods which should be implemented by enums that may be
 * serialized (c.f. http://www.javaspecialists.co.za/archive/Issue113.html).
 * 
 * @author Heinz M. Kabutz
 * 
 * @param <E>
 *            the enum type
 */
public interface EnumConverter<E extends Enum<E> & EnumConverter<E>> {

    /**
     * Returns an ordinal representation.
     * 
     * @return an ordinal representation.
     */
    short convert();

    /**
     * Converts an ordinal representation to the original enum type.
     * 
     * @param val
     *            the ordinal value associated to an enum type.
     * 
     * @return an ordinal representation to the original enum type.
     */
    E convert(short val);

}
