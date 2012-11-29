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
package org.objectweb.proactive.extensions.p2p.structured.configuration;

/**
 * A {@link String} property.
 * 
 * @author lpellegr
 */
public class PropertyString extends Property<String> {

    public PropertyString(String name, String defaultValue) {
        super(name, defaultValue);
    }

    public PropertyString(String name, String defaultValue,
            Validator<String> validator) {
        super(name, defaultValue, validator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String parse(String value) {
        return value;
    }

}
