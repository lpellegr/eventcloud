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
package org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.points;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.utils.converters.MakeDeepCopy;

/**
 * Represents a set of coordinates (i.e. a set of values for each element of the
 * point) used to determine the position of a point in a {@link Zone}.
 * 
 * @param <E>
 *            the {@link Coordinate}s type contained by the point.
 * 
 * @author lpellegr
 */
public final class Point<E extends Coordinate> implements Cloneable,
        Comparable<Point<E>>, Iterable<E>, Serializable {

    private static final long serialVersionUID = 160L;

    /**
     * The set of coordinates that characterize the point.
     */
    private final E[] coordinates;

    /**
     * Constructs a new point with the specified {@code coordinates}.
     * 
     * @param coordinates
     *            the coordinates that characterize the point.
     */
    @SafeVarargs
    public Point(E... coordinates) {
        this.coordinates = coordinates;
    }

    /**
     * Returns the {@link Coordinate} at the given <code>index</code>.
     * 
     * @param index
     *            the index of the coordinate to return (i.e. the coordinate on
     *            the given dimension).
     * 
     * @return the {@link Coordinate} at the given <code>index</code>.
     */
    public E getCoordinate(byte index) {
        return this.coordinates[index];
    }

    /**
     * Returns the coordinates that characterize the point.
     * 
     * @return the coordinates that characterize the point.
     */
    public E[] getCoordinates() {
        return this.coordinates;
    }

    /**
     * Returns the number of coordinates contained by this point.
     * 
     * @return the number of coordinates contained by this point.
     */
    public int size() {
        return this.coordinates.length;
    }

    /**
     * Replaces the coordinate at the specified index with the new coordinate
     * that is given.
     * 
     * @param index
     *            the coordinate index to edit (i.e. the dimension).
     * @param coordinate
     *            the new coordinate to set.
     */
    public void setCoordinate(int index, E coordinate) {
        this.coordinates[index] = coordinate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer("(");

        for (int i = 0; i < this.coordinates.length; i++) {
            result.append(this.coordinates[i]);
            if (i != this.coordinates.length - 1) {
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
        return Arrays.asList(this.coordinates).iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Point<E> clone() throws CloneNotSupportedException {
        try {
            return (Point<E>) MakeDeepCopy.makeDeepCopy(this);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Point<E> coord) {
        if (this.size() != coord.size()) {
            return -1;
        }

        for (byte i = 0; i < this.size(); i++) {
            if (!this.coordinates[i].equals(coord.getCoordinate(i))) {
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
        return Arrays.hashCode(this.coordinates);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        return obj instanceof Point && this.compareTo((Point<E>) obj) == 0;
    }

}
