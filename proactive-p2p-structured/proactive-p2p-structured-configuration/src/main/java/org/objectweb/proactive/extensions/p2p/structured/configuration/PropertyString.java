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
 * A String property.
 * 
 * @author lpellegr
 */
public class PropertyString extends Property {

    public PropertyString(String name) {
        super(name, PropertyType.STRING);
    }

    public PropertyString(String name, String defaultValue) {
        this(name);
        this.setDefaultValue(defaultValue);
    }

    /**
     * Returns the value of this property.
     * 
     * @return the value of this property.
     */
    public String getValue() {
        return super.getValueAsString();
    }

    /**
     * Updates the value of this property.
     * 
     * @param value
     *            the new value.
     */
    public void setValue(String value) {
        super.setValue(value);
    }

    @Override
    public boolean isValid(String value) {
        return value != null;
    }

}
