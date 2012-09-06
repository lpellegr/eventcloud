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

import static org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties.CAN_LOWER_BOUND;
import static org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties.CAN_UPPER_BOUND;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.CoordinateFactory;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;
import org.objectweb.proactive.extensions.p2p.structured.utils.HomogenousPair;

/**
 * Test cases for class {@link Zone}.
 * 
 * @author lpellegr
 */
public class ZoneTest {

    @Test
    public void testZoneCreation() {
        StringZone zone = new StringZone();

        Assert.assertEquals(
                zone.getLowerBound(),
                CoordinateFactory.newStringCoordinate(CAN_LOWER_BOUND.getValue()));

        Assert.assertEquals(
                zone.getUpperBound(),
                CoordinateFactory.newStringCoordinate(CAN_UPPER_BOUND.getValue()));
    }

    @Test
    public void testZoneEquality() {
        StringZone a = new StringZone();
        StringZone b = new StringZone();

        Assert.assertEquals(a, b);
    }

    @Test
    public void testSplitAndMerge() {
        StringZone z =
                new StringZone(
                        CoordinateFactory.newStringCoordinate("a"),
                        CoordinateFactory.newStringCoordinate("z"));

        HomogenousPair<UnicodeZone<StringElement>> newZones =
                z.split(CanOverlay.getRandomDimension());

        Zone<StringElement> mergedZone =
                newZones.getFirst().merge(newZones.getSecond());

        Assert.assertEquals(z, mergedZone);
    }

}
