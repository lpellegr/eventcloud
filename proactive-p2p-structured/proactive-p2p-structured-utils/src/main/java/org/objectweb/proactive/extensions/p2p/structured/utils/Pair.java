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

import java.io.Serializable;

/**
 * Simple pair class. By default a pair contains heterogenous values. If you are
 * interested by using homogenous values, have a look to {@link HomogenousPair}.
 * 
 * @author lpellegr
 * 
 * @param <A>
 *            the first value type contained into the pair.
 * @param <B>
 *            the second value type contained into the pair.
 */
public class Pair<A, B> implements Serializable {

    private static final long serialVersionUID = 1L;

    protected final A first;

    protected final B second;

    protected Pair(A first, B second) {
        super();
        this.first = first;
        this.second = second;
    }

    /**
     * Returns the first value contained by the pair.
     * 
     * @return the first value contained by the pair.
     */
    public A getFirst() {
        return this.first;
    }

    /**
     * Returns the second value contained by the pair.
     * 
     * @return the second value contained by the pair.
     */
    public B getSecond() {
        return this.second;
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
    public boolean equals(Object that) {
        return that instanceof Pair
                && this.first.equals(((Pair<?, ?>) that).first)
                && this.second.equals(((Pair<?, ?>) that).second);
    }

    /**
     * Creates a new pair from the specified {@code first} and {@code second}
     * values.
     * 
     * @param first
     *            the first value contained by the pair.
     * @param second
     *            the second value contained by the pair.
     * 
     * @return a new pair that contains the specified values.
     */
    public static <T1, T2> Pair<T1, T2> create(T1 first, T2 second) {
        return new Pair<T1, T2>(first, second);
    }

}
