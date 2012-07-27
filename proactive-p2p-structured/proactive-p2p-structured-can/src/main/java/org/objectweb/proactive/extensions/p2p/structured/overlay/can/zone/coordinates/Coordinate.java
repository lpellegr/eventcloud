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
package org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;
import org.objectweb.proactive.extensions.p2p.structured.utils.converters.MakeDeepCopy;

/**
 * Represents a set of elements (i.e. a set of values for each component of the
 * coordinate) used to determine the position of a point in a {@link Zone}.
 * 
 * @author lpellegr
 * 
 * @param <E>
 *            the {@link Element}s type contained by the coordinate.
 */
public abstract class Coordinate<E extends Element> implements Cloneable,
        Comparable<Coordinate<E>>, Iterable<E>, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The set of elements constituting the coordinate.
     */
    private final E[] values;

    /**
     * Constructs a new coordinate with the specified {@code elements} as
     * coordinate elements.
     * 
     * @param elements
     *            the elements composing the coordinate.
     */
    public Coordinate(E... elements) {
        if (elements.length != P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue()) {
            throw new AssertionError("The number of coordinate elements ("
                    + elements.length
                    + ") is not equals to the number of dimensions ("
                    + P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue()
                    + ")");
        }

        this.values = elements;
    }

    /**
     * Returns the {@link Element} at the given <code>index</code>.
     * 
     * @param index
     *            the index of the element to return (i.e. the coordinate value
     *            on the given dimension).
     * 
     * @return the {@link Element} at the given <code>index</code>.
     */
    public E getElement(byte index) {
        return this.values[index];
    }

    /**
     * Returns the elements composing the coordinate.
     * 
     * @return the elements composing the coordinate.
     */
    public E[] getElements() {
        return this.values;
    }

    /**
     * Returns the number of coordinate elements contained by this coordinate.
     * 
     * @return the number of coordinate elements contained by this coordinate.
     */
    public int size() {
        return this.values.length;
    }

    /**
     * Sets the specified index to the given value <code>elt</code>.
     * 
     * @param index
     *            the element index to edit (i.e. the dimension).
     * 
     * @param elt
     *            the new element to set.
     */
    public void setElement(int index, E elt) {
        this.values[index] = elt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer("(");

        for (int i = 0; i < this.values.length; i++) {
            result.append(this.values[i]);
            if (i != this.values.length - 1) {
                result.append(',');
            }
        }
        result.append(')');

        return result.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<E> iterator() {
        return Arrays.asList(this.values).iterator();
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public Coordinate<E> clone() throws CloneNotSupportedException {
        try {
            return (Coordinate<E>) MakeDeepCopy.makeDeepCopy(this);
        } catch (IOException e) {
            throw new AssertionError(e);
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Coordinate<E> coord) {
        if (this.size() != coord.size()) {
            return -1;
        }

        for (byte i = 0; i < this.size(); i++) {
            if (!this.values[i].equals(coord.getElement(i))) {
                return -1;
            }
        }

        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Arrays.hashCode(this.values);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        return obj instanceof Coordinate
                && this.compareTo((Coordinate<E>) obj) == 0;
    }

}
