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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package org.objectweb.proactive.extensions.p2p.structured.configuration;

import org.objectweb.proactive.core.ProActiveRuntimeException;

/**
 * An Integer property.
 * 
 * @author lpellegr
 */
public class PropertyInteger extends Property {

    public PropertyInteger(String name) {
        super(name, PropertyType.INTEGER);
    }

    public PropertyInteger(String name, int defaultValue) {
        this(name);
        this.setDefaultValue(Integer.valueOf(defaultValue).toString());
    }

    /**
     * Returns the value of this property.
     * 
     * @return the value of this property.
     */
    public int getValue() {
        String str = super.getValueAsString();
        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw new ProActiveRuntimeException("Invalid value for property "
                    + super.name + " must be an integer", e);
        }
    }

    /**
     * Updates the value of this property.
     * 
     * @param value
     *            the new value.
     */
    public void setValue(int value) {
        super.setValue(Integer.valueOf(value).toString());
    }

    @Override
    public boolean isValid(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
