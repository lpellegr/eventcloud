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
 * A property is a typed Java property. This abstraction must be used instead of
 * {@link System#getProperty(String)} and
 * {@link System#setProperty(String, String)}.
 * 
 * @author lpellegr
 */
public abstract class Property {

    public enum PropertyType {
        STRING, INTEGER, BYTE, BOOLEAN, DOUBLE;
    }

    protected final String name;

    protected final PropertyType type;

    protected volatile String defaultValue;

    protected Property(String name, PropertyType type) {
        this.name = name;
        this.type = type;
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
     * Returns this property type.
     * 
     * @return the type of this property.
     */
    public PropertyType getType() {
        return this.type;
    }

    /**
     * Returns the value of this property.
     * 
     * @return the value of this property.
     */
    public String getValueAsString() {
        return System.getProperty(this.name, this.defaultValue);
    }

    /**
     * Set the value of this property.
     * 
     * @param value
     *            new value of the property.
     */
    public void setValue(String value) {
        System.setProperty(this.name, value);
    }

    /**
     * Sets the default value of this property.
     * 
     * @param value
     *            new value of the property.
     */
    protected void setDefaultValue(String value) {
        this.defaultValue = value;
    }

    /**
     * Sets the default value of this property.
     * 
     * @return the default value of this property.
     */
    protected String getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * Returns the string to be passed on the command line.
     * 
     * The property surrounded by '-D' and '='.
     * 
     * @return the string to be passed on the command line.
     */
    public String getCmdLine() {
        return "-D" + this.name + '=';
    }

    /**
     * Check if the value is valid for this property
     * 
     * @param value
     *            a property value
     * @return true if and only if the value is valid for this property type.
     */
    public abstract boolean isValid(String value);

    @Override
    public String toString() {
        return this.name + "=" + getValueAsString();
    }

}
