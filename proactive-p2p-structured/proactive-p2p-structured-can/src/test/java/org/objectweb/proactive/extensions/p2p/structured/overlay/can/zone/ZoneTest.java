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
package org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone;

import static org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties.CAN_LOWER_BOUND;
import static org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties.CAN_UPPER_BOUND;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.points.Point;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.points.PointFactory;
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
                PointFactory.newStringCoordinate(CAN_LOWER_BOUND.getValueAsString()));

        Assert.assertEquals(
                zone.getUpperBound(),
                PointFactory.newStringCoordinate(CAN_UPPER_BOUND.getValueAsString()));
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
                        PointFactory.newStringCoordinate("a"),
                        PointFactory.newStringCoordinate("z"));

        byte dimension = CanOverlay.getRandomDimension();

        HomogenousPair<UnicodeZone<StringCoordinate>> newZones =
                z.split(dimension);

        UnicodeZone<StringCoordinate> z1 = newZones.getFirst();
        UnicodeZone<StringCoordinate> z2 = newZones.getSecond();

        Zone<StringCoordinate> mergedZone = z1.merge(z2, dimension);

        Assert.assertEquals(z, mergedZone);
    }

    @Test
    public void testWhetherZonesAreAdjacentInTwoDimensions_1() {
        StringZone z1 =
                new StringZone(
                        new Point<StringCoordinate>(
                                new StringCoordinate("a"),
                                new StringCoordinate("a")),
                        new Point<StringCoordinate>(
                                new StringCoordinate("d"),
                                new StringCoordinate("d")));

        StringZone z2 =
                new StringZone(
                        new Point<StringCoordinate>(
                                new StringCoordinate("c"),
                                new StringCoordinate("e")),
                        new Point<StringCoordinate>(
                                new StringCoordinate("z"),
                                new StringCoordinate("z")));

        Assert.assertFalse(z1.neighbors(z2));
    }

    @Test
    public void testWhetherZonesAreAdjacentInTwoDimensions_2() {
        StringZone z1 =
                new StringZone(
                        new Point<StringCoordinate>(
                                new StringCoordinate("a"),
                                new StringCoordinate("a")),
                        new Point<StringCoordinate>(
                                new StringCoordinate("d"),
                                new StringCoordinate("d")));

        StringZone z2 =
                new StringZone(
                        new Point<StringCoordinate>(
                                new StringCoordinate("c"),
                                new StringCoordinate("d")),
                        new Point<StringCoordinate>(
                                new StringCoordinate("cbb"),
                                new StringCoordinate("z")));

        Assert.assertTrue(z1.neighbors(z2));
    }

    @Test
    public void testWhetherZonesAreAdjacentInTwoDimensions_3() {
        StringZone z1 =
                new StringZone(
                        new Point<StringCoordinate>(
                                new StringCoordinate("a"),
                                new StringCoordinate("a")),
                        new Point<StringCoordinate>(
                                new StringCoordinate("d"),
                                new StringCoordinate("d")));

        StringZone z2 =
                new StringZone(
                        new Point<StringCoordinate>(
                                new StringCoordinate("e"),
                                new StringCoordinate("b")),
                        new Point<StringCoordinate>(
                                new StringCoordinate("t"),
                                new StringCoordinate("d")));

        Assert.assertFalse(z1.neighbors(z2));
    }

    @Test
    public void testWhetherZonesAreAdjacentInThreeDimensions_1() {
        StringZone z1 =
                new StringZone(
                        new Point<StringCoordinate>(
                                new StringCoordinate("a"),
                                new StringCoordinate("a"),
                                new StringCoordinate("a")),
                        new Point<StringCoordinate>(
                                new StringCoordinate("d"),
                                new StringCoordinate("d"),
                                new StringCoordinate("d")));

        StringZone z2 =
                new StringZone(
                        new Point<StringCoordinate>(
                                new StringCoordinate("b"),
                                new StringCoordinate("b"),
                                new StringCoordinate("t")),
                        new Point<StringCoordinate>(
                                new StringCoordinate("c"),
                                new StringCoordinate("c"),
                                new StringCoordinate("z")));

        Assert.assertFalse(z1.neighbors(z2));
    }

    @Test
    public void testWhetherZonesAreAdjacentInThreeDimensions_2() {
        StringZone z1 =
                new StringZone(
                        new Point<StringCoordinate>(
                                new StringCoordinate("a"),
                                new StringCoordinate("a"),
                                new StringCoordinate("a")),
                        new Point<StringCoordinate>(
                                new StringCoordinate("d"),
                                new StringCoordinate("d"),
                                new StringCoordinate("d")));

        StringZone z2 =
                new StringZone(
                        new Point<StringCoordinate>(
                                new StringCoordinate("b"),
                                new StringCoordinate("b"),
                                new StringCoordinate("d")),
                        new Point<StringCoordinate>(
                                new StringCoordinate("c"),
                                new StringCoordinate("c"),
                                new StringCoordinate("z")));

        Assert.assertTrue(z1.neighbors(z2));
    }

    @Test
    public void testWhetherZonesAreAdjacentInThreeDimensions_3() {
        StringZone z1 =
                new StringZone(
                        new Point<StringCoordinate>(
                                new StringCoordinate("a"),
                                new StringCoordinate("a"),
                                new StringCoordinate("a")),
                        new Point<StringCoordinate>(
                                new StringCoordinate("d"),
                                new StringCoordinate("d"),
                                new StringCoordinate("d")));

        StringZone z2 =
                new StringZone(
                        new Point<StringCoordinate>(
                                new StringCoordinate("b"),
                                new StringCoordinate("b"),
                                new StringCoordinate("0")),
                        new Point<StringCoordinate>(
                                new StringCoordinate("c"),
                                new StringCoordinate("c"),
                                new StringCoordinate("a")));

        Assert.assertTrue(z1.neighbors(z2));
    }

}
