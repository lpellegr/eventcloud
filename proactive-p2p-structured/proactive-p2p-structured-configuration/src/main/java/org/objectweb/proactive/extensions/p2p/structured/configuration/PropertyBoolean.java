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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Boolean property.
 * 
 * @author lpellegr
 * 
 * @version $Id: PropertyBoolean.java 5079 2010-10-11 11:17:01Z plaurent $
 */
public class PropertyBoolean extends Property {

    static final private Logger logger =
            LoggerFactory.getLogger(PropertyBoolean.class);

    public static final String TRUE = "true";

    public static final String FALSE = "false";

    public PropertyBoolean(String name) {
        super(name, PropertyType.BOOLEAN);
    }

    public PropertyBoolean(String name, boolean defaultValue) {
        this(name);
        this.setDefaultValue(Boolean.valueOf(defaultValue).toString());
    }

    /**
     * Returns the value of this property.
     * 
     * @return the value of this property.
     */
    public boolean getValue() {
        String str = super.getValueAsString();
        return Boolean.parseBoolean(str);
    }

    /**
     * Updates the value of this property.
     * 
     * @param value
     *            the new value.
     */
    public void setValue(boolean value) {
        super.setValue(Boolean.valueOf(value).toString());
    }

    /**
     * Indicates if this property is true.
     * 
     * This method can only be called with boolean property. Otherwise an
     * {@link IllegalArgumentException} is thrown.
     * 
     * If the value is illegal for a boolean property, then false is returned
     * and a warning is printed.
     * 
     * @return <code>true</code> if the property is set to true.
     */
    public boolean isTrue() {
        String val = super.getValueAsString();
        if (TRUE.equals(val)) {
            return true;
        }
        if (FALSE.equals(val)) {
            return false;
        }

        logger.warn(this.name + " is a boolean property but its value is nor "
                + TRUE + " nor " + FALSE + " " + "(" + val + "). ");
        return false;
    }

    @Override
    public boolean isValid(String value) {
        if (TRUE.equals(value) || FALSE.equals(value))
            return true;

        return false;
    }

}
