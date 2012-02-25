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
package org.objectweb.proactive.extensions.p2p.structured.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Class used to retrieve an enum type from an ordinal representation for any
 * enum that implements {@link EnumConverter}.
 * 
 * @author Heinz M. Kabutz
 * 
 * @param <V>
 */
public class ReverseEnumMap<V extends Enum<V> & EnumConverter<V>> {

    private final Map<Short, V> map = new HashMap<Short, V>();

    public ReverseEnumMap(Class<V> valueType) {
        for (V v : valueType.getEnumConstants()) {
            map.put(v.convert(), v);
        }
    }

    public V get(short num) {
        return map.get(num);
    }

}
