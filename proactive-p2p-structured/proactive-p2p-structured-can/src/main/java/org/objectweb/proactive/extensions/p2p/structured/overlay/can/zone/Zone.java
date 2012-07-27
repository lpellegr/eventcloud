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

import static org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.CoordinateFactory.createDoubleCoordinate;
import static org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.CoordinateFactory.createStringCoordinate;

import java.io.IOException;
import java.io.Serializable;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.DoubleCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.DoubleElement;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;
import org.objectweb.proactive.extensions.p2p.structured.utils.HomogenousPair;
import org.objectweb.proactive.extensions.p2p.structured.utils.converters.MakeDeepCopy;

import com.google.common.math.DoubleMath;

/**
 * A zone defines a space (rectangle) which is completely logical and managed by
 * a {@link CanOverlay}. The coordinates of this rectangle are maintained in a
 * {@link ZoneView}. By default a zone contain two views. The first one is a
 * {@link NumericZoneView} which uses respectively {@code 0.0} and {@code 1.0}
 * as the lower and upper bound. The second is an {@link UnicodeZoneView} which
 * uses respectively {@link P2PStructuredProperties#CAN_LOWER_BOUND} and
 * {@link P2PStructuredProperties#CAN_UPPER_BOUND} as the lower and upper bound.
 * <p>
 * The former is used to compute a distance or an area whereas the latter is
 * used to index the data by using the lexicographic order.
 * <p>
 * <strong>By default all operations are delegated to the
 * {@link UnicodeZoneView}.</strong>
 * 
 * @author lpellegr
 */
public class Zone implements Serializable {

    private static final long serialVersionUID = 1L;

    private final UnicodeZoneView unicodeView;

    private final NumericZoneView numView;

    /**
     * Creates a new Zone with the specified {@code unicodeView} and
     * {@code numView}.
     * 
     * @param unicodeView
     *            the {@link UnicodeZoneView} to set.
     * @param numView
     *            the {@link NumericZoneView} to set.
     */
    public Zone(UnicodeZoneView unicodeView, NumericZoneView numView) {
        this.unicodeView = unicodeView;
        this.numView = numView;
    }

    /**
     * Constructs a new zone by initializing the views with their default
     * values.
     */
    public Zone() {
        this(
                new UnicodeZoneView(
                        createStringCoordinate(Character.toString(P2PStructuredProperties.CAN_LOWER_BOUND.getValue())),
                        createStringCoordinate(Character.toString(P2PStructuredProperties.CAN_UPPER_BOUND.getValue()))),
                new NumericZoneView(
                        createDoubleCoordinate(0.0),
                        createDoubleCoordinate(1.0)));
    }

    /**
     * Returns a new {@link HomogenousPair} containing two zone resulting from
     * the split of the current one on the specified {@code dimension}.
     * 
     * @param dimension
     *            the dimension to split on.
     * 
     * @return two zone resulting from the split of the current one on the
     *         specified {@code dimension}.
     */
    public HomogenousPair<Zone> split(byte dimension) {
        HomogenousPair<ZoneView<StringCoordinate, StringElement>> newUnicodeViews =
                this.unicodeView.split(dimension);
        HomogenousPair<ZoneView<DoubleCoordinate, DoubleElement>> newNumViews =
                this.numView.split(dimension);

        return HomogenousPair.createHomogenous(new Zone(
                (UnicodeZoneView) newUnicodeViews.getFirst(),
                (NumericZoneView) newNumViews.getFirst()), new Zone(
                (UnicodeZoneView) newUnicodeViews.getSecond(),
                (NumericZoneView) newNumViews.getSecond()));
    }

    /**
     * Indicates whether the specified {@code zone} can be merged with the
     * current zone that abuts on the given {@code neighborDimension}.
     * 
     * @param zone
     *            the zone to check with.
     * 
     * @param neighborDimension
     *            the dimension on which the the given zone neighbor the current
     *            one.
     * 
     * @return {@code true} if the specified zone can merged with the current
     *         one, {@code false} otherwise.
     */
    public boolean canMerge(Zone zone, byte neighborDimension) {
        NumericZoneView newNumView = null;
        try {
            newNumView =
                    (NumericZoneView) MakeDeepCopy.makeDeepCopy(this.numView);

            newNumView.lowerBound.setElement(neighborDimension, Element.min(
                    this.numView.lowerBound.getElement(neighborDimension),
                    zone.getNumericView().getLowerBound(neighborDimension)));

            newNumView.upperBound.setElement(neighborDimension, Element.max(
                    this.numView.upperBound.getElement(neighborDimension),
                    zone.getNumericView().getUpperBound(neighborDimension)));

            return DoubleMath.fuzzyEquals(
                    newNumView.getArea(), this.numView.getArea()
                            + zone.getNumericView().getArea(), 0.00001);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public Zone merge(Zone zone) {
        return new Zone(
                (UnicodeZoneView) this.unicodeView.merge(zone.getUnicodeView()),
                (NumericZoneView) this.numView.merge(zone.getNumericView()));
    }

    public UnicodeZoneView getUnicodeView() {
        return this.unicodeView;
    }

    public NumericZoneView getNumericView() {
        return this.numView;
    }

    public boolean overlaps(ZoneView<StringCoordinate, StringElement> view,
                            byte dimension) {
        return this.unicodeView.overlaps(view, dimension);
    }

    public boolean overlaps(ZoneView<StringCoordinate, StringElement> view) {
        return this.unicodeView.overlaps(view);
    }

    public boolean abuts(ZoneView<StringCoordinate, StringElement> view,
                         byte dimension, boolean direction) {
        return this.unicodeView.abuts(view, dimension, direction);
    }

    public int neighbors(Zone zone) {
        return this.unicodeView.neighbors(zone.getUnicodeView());
    }

    public StringCoordinate getUpperBound() {
        return this.unicodeView.getUpperBound();
    }

    public StringCoordinate getLowerBound() {
        return this.unicodeView.getLowerBound();
    }

    public StringElement getLowerBound(byte dimension) {
        return this.unicodeView.getLowerBound(dimension);
    }

    public StringElement getUpperBound(byte dimension) {
        return this.unicodeView.getUpperBound(dimension);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.unicodeView.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Zone
                && this.unicodeView.equals(((Zone) obj).getUnicodeView())
                && this.numView.equals(((Zone) obj).getNumericView());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return 31 * (31 + this.numView.hashCode())
                + this.unicodeView.hashCode();
    }

}
