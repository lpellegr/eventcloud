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
package fr.inria.eventcloud.overlay.can;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.UnicodeZone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;

/**
 * 
 * @author lpellegr
 */
public final class SemanticZone extends UnicodeZone<SemanticElement> {

    private static final long serialVersionUID = 1L;

    public SemanticZone() {
        super(
                SemanticCoordinateFactory.newSemanticCoordinate(P2PStructuredProperties.CAN_LOWER_BOUND.getValue()),
                SemanticCoordinateFactory.newSemanticCoordinate(P2PStructuredProperties.CAN_UPPER_BOUND.getValue()));
    }

    /**
     * Constructs a new zone by using the specified {@code lowerBound} and
     * {@code upperBound}.
     * 
     * @param lowerBound
     *            the lower bound coordinate.
     * @param upperBound
     *            the upper bound coordinate.
     */
    public SemanticZone(Coordinate<SemanticElement> lowerBound,
            Coordinate<SemanticElement> upperBound) {
        super(lowerBound, upperBound);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected UnicodeZone<SemanticElement> newZone(Coordinate<SemanticElement> lowerBound,
                                                   Coordinate<SemanticElement> upperBound) {
        return new SemanticZone(lowerBound, upperBound);
    }

}