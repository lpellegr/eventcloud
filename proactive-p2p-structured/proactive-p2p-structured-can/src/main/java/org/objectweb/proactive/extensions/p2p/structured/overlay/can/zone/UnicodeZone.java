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

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;
import org.objectweb.proactive.extensions.p2p.structured.utils.HomogenousPair;

/**
 * UnicodeZone is a zone whose the bounds are unicode characters. The lower
 * bound is defined by {@link P2PStructuredProperties#CAN_LOWER_BOUND} and the
 * upper bound by {@link P2PStructuredProperties#CAN_UPPER_BOUND}.
 * 
 * @param <E>
 *            the {@link Element}s type manipulated.
 * 
 * @author lpellegr
 */
public abstract class UnicodeZone<E extends StringElement> extends Zone<E> {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new zone by using the specified {@code lowerBound} and
     * {@code upperBound}.
     * 
     * @param lowerBound
     *            the lower bound coordinate.
     * @param upperBound
     *            the upper bound coordinate.
     */
    public UnicodeZone(Coordinate<E> lowerBound, Coordinate<E> upperBound) {
        super(lowerBound, upperBound);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HomogenousPair<UnicodeZone<E>> split(byte dimension) {
        Coordinate<E>[] coords = super.splitCoordinates(dimension);

        return HomogenousPair.createHomogenous(this.newZone(
                coords[0], coords[1]), this.newZone(coords[2], coords[3]));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Zone<E> merge(Zone<E> zone) {
        HomogenousPair<Coordinate<E>> coords = super.mergeCoordinates(zone);

        return this.newZone(coords.getFirst(), coords.getSecond());
    }

    protected abstract UnicodeZone<E> newZone(Coordinate<E> lowerBound,
                                              Coordinate<E> upperBound);

    /**
     * {@inheritDoc}
     */
    @Override
    protected double getArea() {
        double area = 1;

        // find the percentage of the space for each dimension
        for (byte i = 0; i < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); i++) {
            area *=
                    super.upperBound.getElement(i).normalize(0, 1)
                            - super.lowerBound.getElement(i).normalize(0, 1);
        }

        return area;
    }

}
