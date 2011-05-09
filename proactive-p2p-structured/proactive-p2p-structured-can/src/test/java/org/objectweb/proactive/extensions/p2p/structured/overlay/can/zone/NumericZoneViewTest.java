package org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.NumericZoneView;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.ZoneView;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.CoordinateFactory;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.DoubleCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.DoubleElement;
import org.objectweb.proactive.extensions.p2p.structured.util.Pair;

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
        this.view = new NumericZoneView(a, b);
    }

    @Test
    public void testDistance() {
        Assert.assertEquals(
                this.view.distance(CoordinateFactory.createDoubleCoordinate(1.0)),
                this.view.distance(CoordinateFactory.createDoubleCoordinate(0.0)));

        Assert.assertEquals(
                0.0,
                this.view.distance(CoordinateFactory.createDoubleCoordinate(0.5)));

        Assert.assertTrue(this.view.distance(CoordinateFactory.createDoubleCoordinate(0.75)) < this.view.distance(CoordinateFactory.createDoubleCoordinate(1.0)));
    }

    @Test
    public void testGetArea() {
        Pair<ZoneView<DoubleCoordinate, DoubleElement, Double>> views =
                this.view.split(CanOverlay.getRandomDimension());

        Assert.assertEquals(
                ((NumericZoneView) views.getFirst()).getArea(),
                ((NumericZoneView) views.getSecond()).getArea());
    }

}
