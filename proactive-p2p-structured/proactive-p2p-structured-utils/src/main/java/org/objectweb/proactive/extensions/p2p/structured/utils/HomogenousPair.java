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

/**
 * A pair class that contains homogenous values.
 * 
 * @author lpellegr
 * 
 * @param <T>
 *            the values type contained by the pair.
 */
public class HomogenousPair<T> extends Pair<T, T> {

    private static final long serialVersionUID = 1L;

    public HomogenousPair(T first, T second) {
        super(first, second);
    }

    public T get(int index) {
        if (index < 0 || index > 1) {
            throw new IndexOutOfBoundsException("index " + index
                    + " is not in [0,1]");
        }

        return index == 0
                ? super.first : super.second;
    }

}
