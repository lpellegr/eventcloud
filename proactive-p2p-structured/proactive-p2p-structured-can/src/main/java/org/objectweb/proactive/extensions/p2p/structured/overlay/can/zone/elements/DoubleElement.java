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

/**
 * Embodies a double coordinate element.
 * 
 * @author lpellegr
 */
public class DoubleElement extends Element {

    private static final long serialVersionUID = 1L;

    private final Double value;

    /**
     * Constructs a new coordinate element with the specified <code>value</code>
     * .
     * 
     * @param value
     *            the String that will be parsed as a Double.
     */
    public DoubleElement(String value) {
        this.value = Double.valueOf(value);
    }

    /**
     * Constructs a new coordinate element with the specified <code>value</code>
     * .
     * 
     * @param value
     *            the Double value to set.
     */
    public DoubleElement(Double value) {
        this.value = value;
    }

    /**
     * Returns the value associated to this element.
     * 
     * @return the value associated to this element.
     */
    public Double getValue() {
        return this.value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Element middle(Element elt) {
        return new DoubleElement(
                (this.value + ((DoubleElement) elt).value) / 2.0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Element elt) {
        if (!(elt instanceof DoubleElement)) {
            throw new IllegalArgumentException();
        }

        return this.value.compareTo(((DoubleElement) elt).value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object that) {
        return that instanceof DoubleElement
                && this.value.equals(((DoubleElement) that).value);
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
    public String toString() {
        return this.value.toString();
    }

}
