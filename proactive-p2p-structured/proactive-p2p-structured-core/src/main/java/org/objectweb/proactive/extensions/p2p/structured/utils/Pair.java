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

import java.io.Serializable;

/**
 * Simple pair class.
 * 
 * @author lpellegr
 * 
 * @param <T>
 *            the values type which is contained by the pair.
 */
public class Pair<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final T first;

    private final T second;

    public Pair(T first, T second) {
        super();
        this.first = first;
        this.second = second;
    }

    public T getFirst() {
        return this.first;
    }

    public T getSecond() {
        return this.second;
    }

    public T get(int index) {
        if (index < 0 || index > 1) {
            throw new IndexOutOfBoundsException("index " + index
                    + " is not in [0,1]");
        }

        return index == 0
                ? this.first : this.second;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return 31 * (31 + this.first.hashCode()) + this.second.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        return obj instanceof Pair && this.first.equals(((Pair<T>) obj).first)
                && this.second.equals(((Pair<T>) obj).second);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Pair [first=" + this.first + ", second=" + this.second + "]";
    }

}
