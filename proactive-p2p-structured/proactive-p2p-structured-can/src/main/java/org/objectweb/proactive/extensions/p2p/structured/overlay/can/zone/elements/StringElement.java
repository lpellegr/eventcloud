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

import java.io.IOException;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.utils.UnicodeUtil;

/**
 * Embodies a String coordinate element.
 * 
 * @author lpellegr
 */
public class StringElement extends Element<DecimalBigInt> {

    private static final long serialVersionUID = 1L;

    private transient String stringValue;

    /**
     * Constructs a new element with the specified <code>value</code>.
     * 
     * @param value
     *            the value as string.
     */
    public StringElement(String value) {
        super(DecimalBigInt.create(
                UnicodeUtil.fromStringToCodePoints(value), value.length() - 1,
                P2PStructuredProperties.CAN_UPPER_BOUND.getValue()));

        this.stringValue = value;
    }

    public StringElement(DecimalBigInt value) {
        super(value);
        this.stringValue = createStringRepresentation(value);
    }

    private String createStringRepresentation(DecimalBigInt value) {
        StringBuilder result = new StringBuilder();

        for (Integer codePoint : super.value.getDigits()) {
            result.append(Character.toChars(codePoint));
        }

        return result.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StringElement middle(Element<DecimalBigInt> elt) {
        return new StringElement(this.value.plus(elt.value).divideByTwo());
    }

    /**
     * Returns the string representation of this element.
     * 
     * @return the string representation of this element.
     */
    public String getStringValue() {
        return this.stringValue;
    }

    /**
     * Compares the elements by using the lexicographic order on their String
     * representation.
     * 
     * @param e
     *            the element to compare with.
     * 
     * @return a negative integer, zero, or a positive integer as this object is
     *         less than, equal to, or greater than the specified object.
     */
    public int compareLexicographicallyTo(StringElement e) {
        return this.getStringValue().compareTo(
                ((StringElement) e).getStringValue());
    }

    /**
     * Returns a boolean indicating if the string representation of the current
     * element is between respectively the specified string representations of
     * the elements <code>e1</code> and <code>e2</code>.
     * 
     * @param e1
     *            the first bound.
     * 
     * @param e2
     *            the second bound.
     * 
     * @return <code>true</code> whether <code>e1<0 and this in [e1;e2[</code>
     *         or <code>e1 > e2 and this in [e2;e1[</code>, <code>false</code>
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

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String coordinateRepresentation =
                P2PStructuredProperties.CAN_COORDINATE_DISPLAY.getValue();

        StringBuilder result = new StringBuilder();

        if (coordinateRepresentation.equals("codepoints")) {
            result.append(UnicodeUtil.makePrintable(super.value.getDigits()));
        } else if (coordinateRepresentation.equals("decimal")) {
            result.append(super.value.toString());
        } else if (coordinateRepresentation.equals("default")) {
            result.append(this.stringValue);
        } else {
            throw new IllegalStateException("Unknown value for property "
                    + P2PStructuredProperties.CAN_COORDINATE_DISPLAY.getName());
        }

        result.append('{');
        result.append(super.value.getDecimalSeparatorIndex());
        result.append('}');

        return result.toString();
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        // always perform the default de-serialization first
        in.defaultReadObject();
        // populates transient fields
        this.stringValue = createStringRepresentation(super.value);
    }

}
