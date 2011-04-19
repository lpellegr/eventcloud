package org.objectweb.proactive.extensions.p2p.structured.util;

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
            throw new AssertionError("index value is " + index);
        }

        return index == 0
                ? this.first : this.second;
    }

}
