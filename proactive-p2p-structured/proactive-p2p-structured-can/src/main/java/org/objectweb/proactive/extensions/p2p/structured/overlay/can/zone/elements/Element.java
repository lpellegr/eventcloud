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
package org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements;

import java.io.Serializable;

import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;

/**
 * An element represents one component from a {@link Coordinate}.
 * 
 * @author lpellegr
 */
public abstract class Element<T extends Comparable<T>> implements
        Comparable<Element<T>>, Serializable {

    private static final long serialVersionUID = 1L;

    protected final T value;

    public Element(T value) {
        this.value = value;
    }

    /**
     * Computes and returns a new {@link Element} with a value being the middle
     * of the current element and the specified element <code>elt</code>.
     * 
     * @param elt
     *            the element to compute with.
     * 
     * @return a new {@link Element} with a value being the middle of the
     *         current element the specified element <code>elt</code>.
     */
    public abstract Element<T> middle(Element<T> elt);

    /**
     * Returns a boolean indicating if the current element is between
     * respectively the specified elements <code>e1</code> and <code>e2</code>.
     * 
     * @param e1
     *            the first bound.
     * 
     * @param e2
     *            the second bound.
     * 
     * @return <code>true</code> whether <code>e1<0 and this in [e1;e2[</code>
     *         or <code>e1 > e2 and this in [e2;e1[</code>, <code>false</code>
     *         otherwise.
     */
    public boolean isBetween(Element<T> e1, Element<T> e2) {
        if (e1.compareTo(e2) < 0) {
            return (this.compareTo(e1) >= 0) && (this.compareTo(e2) < 0);
        } else if (e1.compareTo(e2) > 0) {
            return (this.compareTo(e2) >= 0) && (this.compareTo(e1) < 0);
        }
        return false;
    }

    /**
     * Computes and returns a new {@link Element} which is the middle of the
     * specified elements {@code e1} and {@code e2}.
     * 
     * @param e1
     *            the lower bound.
     * @param e2
     *            the upper bound.
     * @param <T>
     *            the value type contained by the element.
     * 
     * @return a new {@link Element} which is the middle of the specified
     *         elements {@code e1} and {@code e2}.
     * 
     * @see Element#middle(Element)
     */
    public static <T extends Comparable<T>> Element<T> middle(Element<T> e1,
                                                              Element<T> e2) {
        return e1.middle(e2);
    }

    /**
     * Returns the maximum among the specified coordinate elements.
     * 
     * @param elt1
     *            first element.
     * @param elt2
     *            second element.
     * @param <T>
     *            the value type contained by the element.
     * 
     * @return the maximum among the specified coordinate elements using
     *         {@link Element#compareTo(Element)}.
     */
    public static <T extends Comparable<T>> Element<T> max(Element<T> elt1,
                                                           Element<T> elt2) {
        return elt1.compareTo(elt2) > 0
                ? elt1 : elt2;
    }

    /**
     * Returns the minimum among the specified coordinate elements.
     * 
     * @param elt1
     *            first element.
     * @param elt2
     *            second element.
     * @param <T>
     *            the value type contained by the element.
     * 
     * @return the minimum among the specified coordinate elements using
     *         {@link Element#compareTo(Element)}.
     */
    public static <T extends Comparable<T>> Element<T> min(Element<T> elt1,
                                                           Element<T> elt2) {
        return elt1.compareTo(elt2) < 0
                ? elt1 : elt2;
    }

    /**
     * Returns the representative value of this element.
     * 
     * @return the representative value of this element.
     */
    public T getValue() {
        return this.value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Element<T> e) {
        return this.value.compareTo(e.getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        return obj != null && this.getClass().equals(obj.getClass())
                && this.compareTo((Element<T>) obj) == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.value.toString();
    }

}
