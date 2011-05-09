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
 * A Double property.
 * 
 * @author lpellegr
 */
public class PropertyDouble extends Property {

    public PropertyDouble(String name) {
        super(name, PropertyType.DOUBLE);
    }

    public PropertyDouble(String name, double defaultValue) {
        this(name);
        this.setDefaultValue(new Double(defaultValue).toString());
    }

    /**
     * Returns the value of this property.
     * 
     * @return the value of this property.
     */
    public double getValue() {
        String str = super.getValueAsString();
        try {
            return Double.parseDouble(str);
        } catch (NumberFormatException e) {
            throw new ProActiveRuntimeException("Invalid value for property "
                    + super.name + " must be a double", e);
        }
    }

    /**
     * Updates the value of this property.
     * 
     * @param value
     *            the new value.
     */
    public void setValue(double value) {
        super.setValue(new Double(value).toString());
    }

    @Override
    public boolean isValid(String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
