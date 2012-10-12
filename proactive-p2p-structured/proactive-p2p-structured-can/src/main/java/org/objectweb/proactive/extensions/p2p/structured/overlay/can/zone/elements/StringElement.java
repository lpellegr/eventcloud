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
package org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements;

import org.apfloat.Apfloat;
import org.apfloat.Apint;
import org.apfloat.ApintMath;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.utils.ApfloatUtils;
import org.objectweb.proactive.extensions.p2p.structured.utils.StringRepresentation;
import org.objectweb.proactive.extensions.p2p.structured.utils.UnicodeUtils;

/**
 * Embodies a String coordinate element.
 * 
 * @author lpellegr
 */
public class StringElement extends Element {

    private static final long serialVersionUID = 1L;

    protected final String value;

    /**
     * Constructs a new StringElement from specified string {@code value}.
     * 
     * @param value
     *            the value as string.
     */
    public StringElement(String value) {
        this.value = value;
    }

    protected StringElement(Apfloat apfloat) {
        this.value = ApfloatUtils.toString(apfloat);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringElement middle(Element elt) {
        Apfloat e1 = ApfloatUtils.toFloatRadix10(this.value);
        Apfloat e2 = ApfloatUtils.toFloatRadix10(((StringElement) elt).value);

        return this.newStringElement(e1.add(e2).divide(new Apfloat(2)));
    }

    protected StringElement newStringElement(Apfloat apfloat) {
        return new StringElement(apfloat);
    }

    public double normalize(double lowerBound, double upperBound) {
        if (lowerBound < 0) {
            throw new IllegalArgumentException("Lower bound must be positive: "
                    + lowerBound);
        }

        if (upperBound <= lowerBound) {
            throw new IllegalArgumentException(
                    "Upper bound must be greater than lower bound");
        }

        Apfloat scale =
                new Apfloat((upperBound - lowerBound)
                        / P2PStructuredProperties.CAN_UPPER_BOUND.getValue());

        return ApfloatUtils.toFloatRadix10(this.value).multiply(scale).add(
                new Apfloat(lowerBound)).doubleValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringElement clone() throws CloneNotSupportedException {
        return (StringElement) super.clone();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Element elt) {
        return UnicodeUtils.compareUtf32(
                this.value, ((StringElement) elt).value);
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
        return that instanceof StringElement
                && this.compareTo((StringElement) that) == 0;
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

    public static void main(String[] args) {
        // StringBuilder bd = new StringBuilder(1000);
        // for (int i = 0; i < 1000; i++) {
        // bd.append(i + 4000);
        // }
        //
        // long t2 = System.nanoTime();
        // toFloatRadix10BasicMethod(bd.toString(), RADIX);
        // long t3 = System.nanoTime();
        // System.out.println("StringElement.main() toFloatRadixHorner took :  "
        // + ((t3 - t2) / 1000 / 1000));
        //
        // long t0 = System.nanoTime();
        // toFloatRadix10Horner(bd.toString());
        // long t1 = System.nanoTime();
        // System.out.println("StringElement.main() toFloatRadix took : "
        // + ((t1 - t0) / 1000 / 1000));

        Apint radix = new Apint(65536);
        int exponent = 2;

        Apint pow = ApintMath.pow(radix, exponent);
        Apfloat result = new Apfloat(1, Apfloat.DEFAULT).divide(pow);

        System.out.println(result);
    }

}
