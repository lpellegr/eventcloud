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
package org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.Serializable;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;
import org.objectweb.proactive.extensions.p2p.structured.utils.HomogenousPair;
import org.objectweb.proactive.extensions.p2p.structured.utils.converters.MakeDeepCopy;

import com.google.common.math.DoubleMath;

/**
 * A zone is a {@code D} dimensional space (e.g. a rectangle in 2D) which is
 * completely logical and managed by a {@link CanOverlay}. The constant
 * {@code D} is defined by {@link P2PStructuredProperties#CAN_NB_DIMENSIONS}.
 * The logical space is maintained by two coordinates which are named the lower
 * and upper bounds. The lower bound represents the bottom left corner whereas
 * the upper bound delineates the top right corner of the {@code D} dimensional
 * space.
 * 
 * @param <E>
 *            the {@link Element}s type manipulated.
 * 
 * @author lpellegr
 */
public abstract class Zone<E extends Element> implements Serializable {

    private static final long serialVersionUID = 140L;

    protected final Coordinate<E> lowerBound;

    protected final Coordinate<E> upperBound;

    /**
     * Constructs a new zone by using the specified {@code lowerBound} and
     * {@code upperBound}.
     * 
     * @param lowerBound
     *            the lower bound coordinate.
     * @param upperBound
     *            the upper bound coordinate.
     */
    protected Zone(Coordinate<E> lowerBound, Coordinate<E> upperBound) {
        this.lowerBound = checkNotNull(lowerBound);
        this.upperBound = checkNotNull(upperBound);
    }

    /**
     * Indicates whether the specified {@code zone} can be merged with the
     * current zone that abuts on the given {@code neighborDimension}.
     * 
     * @param zone
     *            the zone to check with.
     * 
     * @param neighborDimension
     *            the dimension on which the the given zone neighbor the current
     *            one.
     * 
     * @return {@code true} if the specified zone can merged with the current
     *         one, {@code false} otherwise.
     */
    @SuppressWarnings("unchecked")
    public boolean canMerge(Zone<E> zone, byte neighborDimension) {
        Zone<E> currentZoneCopy = null;
        try {
            currentZoneCopy = (Zone<E>) MakeDeepCopy.makeDeepCopy(this);

            currentZoneCopy.lowerBound.setElement(
                    neighborDimension, Element.min(
                            this.lowerBound.getElement(neighborDimension),
                            zone.getLowerBound(neighborDimension)));

            currentZoneCopy.upperBound.setElement(
                    neighborDimension, Element.max(
                            this.upperBound.getElement(neighborDimension),
                            zone.getUpperBound(neighborDimension)));

            return DoubleMath.fuzzyEquals(
                    currentZoneCopy.getArea(), this.getArea() + zone.getArea(),
                    0.00001);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns the span of the zone.
     * 
     * @return the span of the zone.
     */
    protected abstract double getArea();

    /**
     * Checks if the specified {@code coordinate} is in the zone managed.
     * 
     * @param coordinate
     *            the coordinate to check.
     * 
     * @return {@code true} if the coordinate is in the zone managed,
     *         {@code false} otherwise.
     */
    public boolean contains(Coordinate<E> coordinate) {
        for (byte dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
            if (this.contains(dim, coordinate.getElement(dim)) != 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Indicates whether the zone contains the specified {@code element} on the
     * specified {@code dimension} or not.
     * <p>
     * An element value set to {@code null} is supposed to be contained by any
     * zone on any dimension. This semantic is used to construct systems in
     * which queries can reach several peers.
     * 
     * @param dimension
     *            the dimension.
     * 
     * @param element
     *            the coordinate to check.
     * 
     * @return {@code 0} if the specified coordinate is contained by the zone,
     *         {@code -1} if the coordinate is taller than the lower bound of
     *         the zone and {@code 1} if the coordinate is greater or equal to
     *         the upper bound of the zone.
     */
    public byte contains(byte dimension, E element) {
        if (element == null) {
            return 0;
        }

        if (element.compareTo(this.upperBound.getElement(dimension)) >= 0) {
            return 1;
        } else if (element.compareTo(this.lowerBound.getElement(dimension)) < 0) {
            return -1;
        }

        return 0;
    }

    /**
     * Returns a boolean indicating whether the given {@code zone} overlaps the
     * current zone along the given axis.
     * 
     * @param zone
     *            the zone to compare with.
     * 
     * @param dimension
     *            the dimension used to perform the check operation.
     * 
     * @return {@code true} if the specified zone overlaps the current zone,
     *         {@code false} otherwise.
     */
    public boolean overlaps(Zone<E> zone, byte dimension) {
        E a = this.lowerBound.getElement(dimension);
        E b = this.upperBound.getElement(dimension);
        E c = zone.getLowerBound(dimension);
        E d = zone.getUpperBound(dimension);

        return (((a.compareTo(c) >= 0) && (a.compareTo(d) < 0))
                || ((b.compareTo(c) > 0) && (b.compareTo(d) <= 0))
                || ((c.compareTo(a) >= 0) && (c.compareTo(b) < 0)) || ((d.compareTo(a) > 0) && (d.compareTo(b) <= 0)));
    }

    /**
     * Returns a boolean indicating whether the given {@code zone} overlaps the
     * current zone. The specified {@code zone} must overlap on all dimensions.
     * 
     * @param zone
     *            the zone to compare with.
     * 
     * @return {@code true} if the specified zone overlaps the current zone on
     *         all dimensions, {@code false} otherwise.
     */
    public boolean overlaps(Zone<E> zone) {
        for (byte i = 0; i < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); i++) {
            if (!this.overlaps(zone, i)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Checks whether the given {@code zone} abuts the current one in the given
     * {@code dimension} and {@code direction}.
     * 
     * @param zone
     *            the zone to compare with.
     * 
     * @param dimension
     *            the dimension on which the check is performed.
     * 
     * @param direction
     *            indicates the direction on which the check is performed:
     *            {@code false} is the inferior direction and {@code true} is
     *            the superior direction.
     * 
     * @return a boolean indicating if the specified {@code zone} abuts the
     *         current zone.
     */
    public boolean abuts(Zone<E> zone, byte dimension, boolean direction) {
        return (direction && (this.lowerBound.getElement(dimension).compareTo(
                zone.getUpperBound(dimension)) == 0))
                || (!direction && (this.upperBound.getElement(dimension)
                        .compareTo(zone.getLowerBound(dimension)) == 0));
    }

    /**
     * Returns the dimension on which the given {@code zone} neighbors the
     * current one. The result is the dimension number or {@code -1} if the
     * specified zone does not neighbor the current zone.
     * <p>
     * In a d-dimensional space, two zones are neighbors if their edges overlap
     * in exactly {@code d-1} dimensions and abut in exactly {@code 1}
     * dimension.
     * 
     * @param zone
     *            the zone to compare with.
     * 
     * @return the dimension on which the given {@code zone} neighbors the
     *         current one.
     */
    public byte neighbors(Zone<E> zone) {
        byte overlaps = 0;
        byte abuts = 0;
        byte abutsDimension = -1;

        for (byte dimension = 0; dimension < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dimension++) {
            if (this.overlaps(zone, dimension)) {
                overlaps++;
            } else {
                if (this.abuts(zone, dimension, true)
                        || this.abuts(zone, dimension, false)) {
                    abutsDimension = dimension;
                    abuts++;
                } else {
                    return -1;
                }
            }
        }

        if ((abuts != 1)
                || (overlaps != P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue() - 1)) {
            return -1;
        } else {
            return abutsDimension;
        }
    }

    /**
     * Returns two zones representing the original one split into two following
     * the specified {@code dimension}.
     * 
     * @param dimension
     *            the dimension to split on.
     * 
     * @return two new zone standing for the original one split into two
     *         following the specified {@code dimension}.
     */
    public abstract HomogenousPair<? extends Zone<E>> split(byte dimension);

    @SuppressWarnings("unchecked")
    protected Coordinate<E>[] splitCoordinates(byte dimension) {
        E middle =
                Element.middle(
                        this.lowerBound.getElement(dimension),
                        this.upperBound.getElement(dimension));

        try {
            Coordinate<E> lowerBoundCopy = this.lowerBound.clone();
            Coordinate<E> upperBoundCopy = this.upperBound.clone();

            lowerBoundCopy.setElement(dimension, middle);
            upperBoundCopy.setElement(dimension, middle);

            // result={a, b, c, d} where C1={a, b} and C2={c, d}
            return new Coordinate[] {
                    this.lowerBound, upperBoundCopy, lowerBoundCopy,
                    this.upperBound};
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns a new zone which is the merge between the current zone and the
     * specified one.
     * 
     * @param zone
     *            the zone to merge with.
     * 
     * @return a new zone which is the merge between the current zone and the
     *         specified one.
     */
    public abstract Zone<E> merge(Zone<E> zone);

    protected HomogenousPair<Coordinate<E>> mergeCoordinates(Zone<E> zone) {
        byte d = this.neighbors(zone);

        try {
            Coordinate<E> lowerBoundCopy = this.lowerBound.clone();
            Coordinate<E> upperBoundCopy = this.upperBound.clone();

            lowerBoundCopy.setElement(d, Element.min(
                    this.lowerBound.getElement(d), zone.getLowerBound(d)));
            upperBoundCopy.setElement(d, Element.max(
                    this.upperBound.getElement(d), zone.getUpperBound(d)));

            return HomogenousPair.createHomogenous(
                    lowerBoundCopy, upperBoundCopy);
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException(e);
        }
    }

    public Coordinate<E> getUpperBound() {
        return this.upperBound;
    }

    public Coordinate<E> getLowerBound() {
        return this.lowerBound;
    }

    public E getLowerBound(byte dimension) {
        return this.lowerBound.getElement(dimension);
    }

    public E getUpperBound(byte dimension) {
        return this.upperBound.getElement(dimension);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return 31 * (31 + this.lowerBound.hashCode())
                + this.upperBound.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Zone
                && this.lowerBound.equals(((Zone<?>) obj).lowerBound)
                && this.upperBound.equals(((Zone<?>) obj).upperBound);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        result.append(this.lowerBound);
        result.append(" to ");
        result.append(this.upperBound);

        return result.toString();
    }

}
