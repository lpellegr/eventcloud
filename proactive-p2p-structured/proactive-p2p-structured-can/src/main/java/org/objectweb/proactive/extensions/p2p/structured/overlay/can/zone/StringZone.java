/**
 * Copyright (c) 2011-2013 INRIA.
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
package org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.points.Point;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.points.PointFactory;

/**
 * A {@link Zone} semiskilled for {@link StringCoordinate}s.
 * 
 * @author lpellegr
 */
public final class StringZone extends UnicodeZone<StringCoordinate> {

    private static final long serialVersionUID = 160L;

    public StringZone() {
        super(
                PointFactory.newStringCoordinate(P2PStructuredProperties.CAN_LOWER_BOUND.getValueAsString()),
                PointFactory.newStringCoordinate(P2PStructuredProperties.CAN_UPPER_BOUND.getValueAsString()));
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
    public StringZone(Point<StringCoordinate> lowerBound,
            Point<StringCoordinate> upperBound) {
        super(lowerBound, upperBound);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected UnicodeZone<StringCoordinate> newZone(Point<StringCoordinate> lowerBound,
                                                    Point<StringCoordinate> upperBound) {
        return new StringZone(lowerBound, upperBound);
    }

}
