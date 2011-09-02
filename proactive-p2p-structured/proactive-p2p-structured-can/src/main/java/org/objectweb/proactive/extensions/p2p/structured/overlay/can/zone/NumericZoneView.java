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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.DoubleCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.DoubleElement;

/**
 * A numeric zone view is a zone view maintaining coordinates as
 * {@link DoubleElement}s.
 * 
 * @author lpellegr
 */
public class NumericZoneView extends
        ZoneView<DoubleCoordinate, DoubleElement, Double> {

    private static final long serialVersionUID = 1L;

    private transient DoubleCoordinate center;

    public NumericZoneView(DoubleCoordinate lowerBound,
            DoubleCoordinate upperBound) {
        super(lowerBound, upperBound);
    }

    /**
     * Computes the distance from the center of this zone view to the given
     * {@code coordinate}.
     * 
     * @param coordinate
     *            the point to use in order to compute the distance.
     * @return the distance from the center of this zone view to the given
     *         point.
     */
    public double distance(DoubleCoordinate coordinate) {
        double distance = 0;
        double projection;

        for (byte dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
            projection =
                    coordinate.getElement(dim).getValue()
                            - this.getCenter().getElement(dim).getValue();
            projection *= projection;
            distance += projection;
        }

        return Math.sqrt(distance);
    }

    /**
     * Computes the center of the current zone view and sets the value to a
     * transient field to avoid to compute it several times.
     */
    private void findCenter() {
        DoubleElement[] middleElts =
                new DoubleElement[P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue()];

        for (byte i = 0; i < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); i++) {
            middleElts[i] =
                    (DoubleElement) super.lowerBound.getElement(i).middle(
                            super.upperBound.getElement(i));
        }

        this.center = new DoubleCoordinate(middleElts);
    }

    /**
     * Returns the span of the zone.
     * 
     * @return the span of the zone.
     */
    public double getArea() {
        double area = 1;

        // find the percentage of the space for each dimension
        for (byte i = 0; i < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); i++) {
            area *=
                    super.upperBound.getElement(i).getValue()
                            - super.lowerBound.getElement(i).getValue();
        }

        return area;
    }

    /**
     * Returns the center of the current zone view.
     * 
     * @return the center of the current zone view.
     */
    public DoubleCoordinate getCenter() {
        if (this.center == null) {
            this.findCenter();
        }
        return this.center;
    }

}
