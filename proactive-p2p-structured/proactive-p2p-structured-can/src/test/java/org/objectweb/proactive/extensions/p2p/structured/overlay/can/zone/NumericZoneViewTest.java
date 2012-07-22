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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.CoordinateFactory;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.DoubleCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.DoubleElement;
import org.objectweb.proactive.extensions.p2p.structured.utils.HomogenousPair;

/**
 * Tests cases for {@link NumericZoneView}.
 * 
 * @author lpellegr
 */
public class NumericZoneViewTest {

    private DoubleCoordinate a;

    private DoubleCoordinate b;

    private NumericZoneView view;

    @Before
    public void setUp() {
        this.a = CoordinateFactory.createDoubleCoordinate(0.0);
        this.b = CoordinateFactory.createDoubleCoordinate(1.0);
        this.view = new NumericZoneView(this.a, this.b);
    }

    @Test
    public void testDistance() {
        Assert.assertEquals(
                this.view.distance(CoordinateFactory.createDoubleCoordinate(1.0)),
                this.view.distance(CoordinateFactory.createDoubleCoordinate(0.0)),
                0.001);

        Assert.assertEquals(
                0.0,
                this.view.distance(CoordinateFactory.createDoubleCoordinate(0.5)),
                0.001);

        Assert.assertTrue(this.view.distance(CoordinateFactory.createDoubleCoordinate(0.75)) < this.view.distance(CoordinateFactory.createDoubleCoordinate(1.0)));
    }

    @Test
    public void testGetArea() {
        HomogenousPair<ZoneView<DoubleCoordinate, DoubleElement, Double>> views =
                this.view.split(CanOverlay.getRandomDimension());

        Assert.assertEquals(
                ((NumericZoneView) views.getFirst()).getArea(),
                ((NumericZoneView) views.getSecond()).getArea(), 0.001);
    }

}
