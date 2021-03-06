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
package fr.inria.eventcloud.overlay.can;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.UnicodeZone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.points.Point;

/**
 * Semantic version of a {@link Zone}.
 * 
 * @author lpellegr
 */
public final class SemanticZone extends UnicodeZone<SemanticCoordinate> {

    private static final long serialVersionUID = 160L;

    public SemanticZone() {
        super(
                SemanticPointFactory.newSemanticPoint(P2PStructuredProperties.CAN_LOWER_BOUND.getValueAsString()),
                SemanticPointFactory.newSemanticPoint(P2PStructuredProperties.CAN_UPPER_BOUND.getValueAsString()));
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
    public SemanticZone(Point<SemanticCoordinate> lowerBound,
            Point<SemanticCoordinate> upperBound) {
        super(lowerBound, upperBound);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected UnicodeZone<SemanticCoordinate> newZone(Point<SemanticCoordinate> lowerBound,
                                                      Point<SemanticCoordinate> upperBound) {
        return new SemanticZone(lowerBound, upperBound);
    }

}
