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

import static org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties.CAN_LOWER_BOUND;
import static org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties.CAN_UPPER_BOUND;
import static org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.CoordinateFactory.createDoubleCoordinate;
import static org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.CoordinateFactory.createStringCoordinate;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.utils.HomogenousPair;

/**
 * Test cases for class {@link Zone}.
 * 
 * @author lpellegr
 */
public class ZoneTest {

    @Test
    public void testZoneCreation() {
        Zone zone = new Zone();

        Assert.assertEquals(
                zone.getLowerBound(),
                createStringCoordinate(CAN_LOWER_BOUND.getValue()));

        Assert.assertEquals(
                zone.getUpperBound(),
                createStringCoordinate(CAN_UPPER_BOUND.getValue()));
    }

    @Test
    public void testSplitAndMerge() {
        Zone z =
                new Zone(new UnicodeZoneView(
                        createStringCoordinate("a"),
                        createStringCoordinate("z")), new NumericZoneView(
                        createDoubleCoordinate(0.0),
                        createDoubleCoordinate(1.0)));

        HomogenousPair<Zone> newZones =
                z.split(CanOverlay.getRandomDimension());
        Assert.assertEquals(z, newZones.getFirst().merge(newZones.getSecond()));
    }

}
