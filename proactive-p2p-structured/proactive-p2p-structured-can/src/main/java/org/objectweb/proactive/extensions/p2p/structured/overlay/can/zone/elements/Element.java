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
public abstract class Element implements Cloneable, Comparable<Element>,
        Serializable {

    private static final long serialVersionUID = 1L;

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
    public abstract Element middle(Element elt);

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
    public boolean isBetween(Element e1, Element e2) {
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
     * 
     * @return a new {@link Element} which is the middle of the specified
     *         elements {@code e1} and {@code e2}.
     * 
     * @see Element#middle(Element)
     */
    @SuppressWarnings("unchecked")
    public static <T extends Element> T middle(T e1, T e2) {
        return (T) e1.middle(e2);
    }

    /**
     * Returns the maximum among the specified coordinate elements.
     * 
     * @param elt1
     *            first element.
     * @param elt2
     *            second element.
     * 
     * @return the maximum among the specified coordinate elements.
     */
    public static <T extends Element> T max(T elt1, T elt2) {
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
     * 
     * @return the minimum among the specified coordinate elements.
     */
    public static <T extends Element> T min(T elt1, T elt2) {
        return elt1.compareTo(elt2) < 0
                ? elt1 : elt2;
    }

}
