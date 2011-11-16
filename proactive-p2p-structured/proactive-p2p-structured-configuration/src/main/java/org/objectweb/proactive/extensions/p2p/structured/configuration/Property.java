/**
 * Copyright (c) 2011 INRIA.
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
package org.objectweb.proactive.extensions.p2p.structured.configuration;

/**
 * 
 * @author lpellegr
 */
public abstract class Property<T> {

    protected final String name;

    protected final T defaultValue;

    protected T value;

    protected Property(String name, T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.value = defaultValue;
    }

    /**
     * Returns the key associated to this property.
     * 
     * @return the key associated to this property.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the default value of this property.
     * 
     * @return the default value of this property.
     */
    public T getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * Returns the value of this property.
     * 
     * @return the value of this property.
     */
    public T getValue() {
        return this.value;
    }

    /**
     * Sets the value of this property.
     * 
     * @param value
     *            new value of the property.
     */
    public void setValue(T value) {
        this.value = value;
    }

    /**
     * Sets the value of this property from the specified String value.
     * 
     * @param value
     *            new value of the property.
     */
    public abstract void setValueAsString(String value);

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.name + "=" + this.value;
    }

}
