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
package org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates;

import org.apfloat.Apfloat;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.utils.ApfloatUtils;
import org.objectweb.proactive.extensions.p2p.structured.utils.StringRepresentation;
import org.objectweb.proactive.extensions.p2p.structured.utils.UnicodeUtils;

/**
 * Embodies a String coordinate.
 * 
 * @author lpellegr
 */
public class StringCoordinate extends Coordinate {

    private static final long serialVersionUID = 160L;

    protected final String value;

    /**
     * Constructs a new string coordinate from the specified string
     * {@code value}.
     * 
     * @param value
     *            the value as string.
     */
    public StringCoordinate(String value) {
        this.value = value;
    }

    protected StringCoordinate(Apfloat apfloat) {
        this.value = ApfloatUtils.toString(apfloat);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringCoordinate middle(Coordinate elt) {
        Apfloat e1 = ApfloatUtils.toFloatRadix10(this.value);
        Apfloat e2 =
                ApfloatUtils.toFloatRadix10(((StringCoordinate) elt).value);

        return this.newStringCoordinate(e1.add(e2).divide(new Apfloat(2)));
    }

    protected StringCoordinate newStringCoordinate(Apfloat apfloat) {
        return new StringCoordinate(apfloat);
    }

    public double normalize(double lowerBound, double upperBound) {
        if (upperBound <= lowerBound) {
            throw new IllegalArgumentException(
                    "Upper bound must be greater than lower bound");
        }

        double w1 = upperBound - lowerBound;
        double w2 =
                P2PStructuredProperties.CAN_UPPER_BOUND.getValue()
                        - P2PStructuredProperties.CAN_LOWER_BOUND.getValue();

        double l1 = lowerBound;
        double l2 = P2PStructuredProperties.CAN_LOWER_BOUND.getValue();

        Apfloat scale = new Apfloat(w1 / w2);

        return ApfloatUtils.toFloatRadix10(this.value)
                .add(
                        new Apfloat(
                                P2PStructuredProperties.CAN_LOWER_BOUND.getValue()))
                .multiply(scale)
                .add(new Apfloat(l1 - l2).multiply(scale))
                .doubleValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringCoordinate clone() throws CloneNotSupportedException {
        return (StringCoordinate) super.clone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Coordinate elt) {
        return UnicodeUtils.compareUtf32(
                this.value, ((StringCoordinate) elt).value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object that) {
        return that instanceof StringCoordinate
                && this.compareTo((StringCoordinate) that) == 0;
    }

    public String getValue() {
        return this.value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String representation =
                P2PStructuredProperties.CAN_ELEMENT_DISPLAY.getValue();

        if (representation.equalsIgnoreCase("decimal")) {
            return ApfloatUtils.toFloatRadix10(this.value).toString(true);
        } else {
            try {
                return StringRepresentation.getInstance(representation).apply(
                        this.value);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Invalid value for property "
                        + P2PStructuredProperties.CAN_ELEMENT_DISPLAY.getName()
                        + ": " + representation);
            }
        }
    }

    public String toString(StringRepresentation representation) {
        return representation.apply(this.value);
    }

}
