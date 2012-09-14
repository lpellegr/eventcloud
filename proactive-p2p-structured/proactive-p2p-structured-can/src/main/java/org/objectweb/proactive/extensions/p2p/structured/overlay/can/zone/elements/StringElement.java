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
import org.apfloat.ApfloatMath;
import org.apfloat.Apint;
import org.apfloat.ApintMath;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.utils.UnicodeUtil;

/**
 * Embodies a String coordinate element.
 * 
 * @author lpellegr
 */
public class StringElement extends Element {

    private static final long serialVersionUID = 1L;

    private static final long PRECISION =
            P2PStructuredProperties.STRING_ELEMENT_PRECISION.getValue();

    public static final Apint RADIX = new Apint(
            (P2PStructuredProperties.CAN_UPPER_BOUND.getValue()) + 1);

    private final Apfloat apfloat;

    private transient String unicodeRepresentation;

    /**
     * Constructs a new StringElement from specified string {@code value}.
     * 
     * @param value
     *            the value as string.
     */
    public StringElement(String value) {
        // converts a string to an integer radix 10 by assuming that each
        // character is a digit radix StringElement#RADIX
        this.apfloat = toFloatRadix10(value, RADIX);
    }

    protected StringElement(Apfloat apfloat) {
        this.apfloat = apfloat;
    }

    public static Apfloat toFloatRadix10(String value) {
        return toFloatRadix10(value, RADIX);
    }

    public static Apfloat toFloatRadix10(String value, Apint radix) {
        int[] codepoints = UnicodeUtil.fromStringToCodePoints(value);

        // codepoints[0] x radix^0 = codepoints[0]
        Apfloat result = new Apfloat(codepoints[0], PRECISION);

        // the radix point is automatically set to 1 because all inserted data
        // are supposed to be between a lower and upper bound that is made of
        // one character, and hence one digit radix the upper bound value (e.g.
        // 2^16).
        for (int i = 1; i < codepoints.length; i++) {
            Apint pow = ApintMath.pow(radix, i);
            Apfloat division = new Apfloat(1, PRECISION).divide(pow);
            result =
                    result.add(new Apfloat(codepoints[i], PRECISION).multiply(division));
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringElement middle(Element elt) {
        return this.newStringElement(this.apfloat.add(
                ((StringElement) elt).apfloat).divide(new Apfloat(2)));
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

        return this.apfloat.multiply(scale)
                .add(new Apfloat(lowerBound))
                .doubleValue();
    }

    public synchronized String getUnicodeRepresentation() {
        if (this.unicodeRepresentation == null) {
            Apint apint = this.apfloat.truncate();
            Apint quotient = apint;

            // handle the integer part
            StringBuilder integerPart = new StringBuilder();
            quotient = this.divideQuotientRecursively(integerPart, quotient);
            integerPart.reverse();

            // handle the fractional part
            // as explained at http://goo.gl/OKovZ
            Apfloat fractionalPart = ApfloatMath.modf(this.apfloat)[1];

            // this algorithm may not terminate and loop infinitively if the
            // apfloat precision is set as infinite
            // TODO: detect set of digits that repeat and stop after n
            // repetition
            int loop = 0;
            while (fractionalPart.compareTo(Apfloat.ZERO) > 0) {
                // simple test to stop after PRECISION loops and thus to avoid
                // infinite loop
                if (loop > PRECISION) {
                    break;
                }

                // shift radix point to right by 1.
                fractionalPart = fractionalPart.multiply(RADIX);

                if (fractionalPart.compareTo(Apfloat.ONE) >= 0) {
                    Apint truncatedPart = fractionalPart.truncate();

                    // keep only the integer part
                    integerPart.append((char) truncatedPart.intValue());
                    // get rid of value left of radix point
                    fractionalPart = fractionalPart.subtract(truncatedPart);
                } else {
                    integerPart.append((char) 0);
                }

                loop++;
            }

            if (integerPart.length() == 0) {
                integerPart.append((char) Character.MIN_CODE_POINT);
            }

            this.unicodeRepresentation = integerPart.toString();
        }

        return this.unicodeRepresentation;
    }

    private Apint divideQuotientRecursively(StringBuilder result, Apint quotient) {
        while (quotient.compareTo(Apint.ZERO) != 0) {
            int remainder = quotient.mod(RADIX).intValue();
            result.append((char) remainder);
            quotient = quotient.divide(RADIX);
        }

        return quotient;
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
        return this.apfloat.compareTo(((StringElement) elt).apfloat);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.apfloat.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object that) {
        return that instanceof StringElement
                && this.apfloat.equals(((StringElement) that).apfloat);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String coordinateRepresentation =
                P2PStructuredProperties.CAN_COORDINATE_DISPLAY.getValue();

        StringBuilder result = new StringBuilder();

        if (coordinateRepresentation.equals("default")) {
            result.append(this.apfloat.toString(true));
        } else if (coordinateRepresentation.equals("codepoints")) {
            result.append(UnicodeUtil.makePrintable(this.getUnicodeRepresentation()));
            result.append('{');
            result.append(this.getUnicodeRepresentation().length());
            result.append('}');
        } else {
            throw new IllegalStateException("Unknown value for property "
                    + P2PStructuredProperties.CAN_COORDINATE_DISPLAY.getName());
        }

        return result.toString();
    }

    public String toString(boolean useInternalRepresentation) {
        if (useInternalRepresentation) {
            return this.apfloat.toString(true);
        } else {
            return this.toString();
        }
    }

    public static void main(String[] args) {
        Apint radix = new Apint(65536);
        int exponent = 2;

        Apint pow = ApintMath.pow(radix, exponent);
        Apfloat result = new Apfloat(1, Apfloat.DEFAULT).divide(pow);

        System.out.println(result);
    }

}
