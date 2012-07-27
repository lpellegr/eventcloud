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

import java.math.BigInteger;

import org.apfloat.Apfloat;
import org.apfloat.ApfloatMath;
import org.apfloat.Apint;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.utils.UnicodeUtil;

/**
 * Embodies a String coordinate element.
 * 
 * @author lpellegr
 */
public class StringElement extends Element {

    private static final long serialVersionUID = 1L;

    private static final long PRECISION = Apfloat.INFINITE;

    private static final BigInteger RADIX =
            BigInteger.valueOf(((int) P2PStructuredProperties.CAN_UPPER_BOUND.getValue()) + 1);

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
        this.apfloat = new Apfloat(toIntegerRadix10(value, RADIX), PRECISION);
    }

    private StringElement(Apfloat apfloat) {
        this.apfloat = apfloat;
    }

    private static BigInteger toIntegerRadix10(String value, BigInteger radix) {
        return toIntegerRadix10(
                UnicodeUtil.fromStringToCodePoints(value), radix);
    }

    private static BigInteger toIntegerRadix10(int[] digits, BigInteger radix) {
        BigInteger result = BigInteger.ZERO;

        for (int i = 0; i < digits.length; i++) {
            result =
                    result.add(radix.pow(i).multiply(
                            BigInteger.valueOf(digits[digits.length - i - 1])));
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringElement middle(Element elt) {
        return new StringElement(
                this.apfloat.add(((StringElement) elt).apfloat).divide(
                        new Apfloat(2, PRECISION)));
    }

    /**
     * Compares the elements by using the lexicographic order on their String
     * representation.
     * 
     * @param elt
     *            the element to compare with.
     * 
     * @return a negative integer, zero, or a positive integer as this object is
     *         less than, equal to, or greater than the specified object.
     */
    public int compareLexicographicallyTo(StringElement elt) {
        return this.getUnicodeRepresentation().compareTo(
                elt.getUnicodeRepresentation());
    }

    /**
     * Returns a boolean indicating if the string representation of the current
     * element is between respectively the specified string representations of
     * the elements {@code e1} and {@code e2}.
     * 
     * @param e1
     *            the first bound.
     * @param e2
     *            the second bound.
     * 
     * @return {@code true} if {@code e1 < 0} and this in {@code [e1;e2[} or
     *         {@code e1 > e2} and this in {@code [e2;e1[}, {@code false}
     *         otherwise.
     */
    public boolean isLexicographicallyBetween(StringElement e1, StringElement e2) {
        if (e1.compareLexicographicallyTo(e2) < 0) {
            return (this.compareLexicographicallyTo(e1) >= 0)
                    && (this.compareLexicographicallyTo(e2) < 0);
        } else if (e1.compareTo(e2) > 0) {
            return (this.compareLexicographicallyTo(e2) >= 0)
                    && (this.compareLexicographicallyTo(e1) < 0);
        }
        return false;
    }

    public synchronized String getUnicodeRepresentation() {
        if (this.unicodeRepresentation == null) {
            Apint apint = this.apfloat.truncate();
            Apint quotient = apint;

            // handle integer part
            StringBuilder integerPart = new StringBuilder();
            quotient = this.divideQuotientRecursively(integerPart, quotient);
            integerPart.reverse();

            // handle fractional part
            long shift = this.apfloat.size() - this.apfloat.scale();
            apint =
                    ApfloatMath.scale(ApfloatMath.modf(this.apfloat)[1], shift)
                            .truncate();
            quotient = apint;

            StringBuilder fractionalPart = new StringBuilder();
            this.divideQuotientRecursively(fractionalPart, quotient);
            fractionalPart.reverse();

            StringBuilder result = integerPart.append(fractionalPart);
            if (result.length() == 0) {
                result.append((char) Character.MIN_CODE_POINT);
            }

            this.unicodeRepresentation = result.toString();
        }

        return this.unicodeRepresentation;
    }

    private Apint divideQuotientRecursively(StringBuilder result, Apint quotient) {
        Apint radix = new Apint(RADIX);

        while (quotient.compareTo(Apint.ZERO) != 0) {
            int remainder = quotient.mod(radix).intValue();
            result.append((char) remainder);
            quotient = quotient.divide(radix);
        }

        return quotient;
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

        if (coordinateRepresentation.equals("codepoints")) {
            result.append(UnicodeUtil.makePrintable(this.getUnicodeRepresentation()));
        } else if (coordinateRepresentation.equals("decimal")) {
            result.append(this.apfloat.toString(true));
        } else if (coordinateRepresentation.equals("default")) {
            result.append(this.getUnicodeRepresentation());
        } else {
            throw new IllegalStateException("Unknown value for property "
                    + P2PStructuredProperties.CAN_COORDINATE_DISPLAY.getName());
        }

        result.append('{');
        result.append(this.getUnicodeRepresentation().length());
        result.append('}');

        return result.toString();
    }

}
