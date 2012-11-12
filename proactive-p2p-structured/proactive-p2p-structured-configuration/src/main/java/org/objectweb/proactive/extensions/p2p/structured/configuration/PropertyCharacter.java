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
 * A {@link Character} property.
 * 
 * @author lpellegr
 */
public class PropertyCharacter extends Property<Character> {

    public PropertyCharacter(String name, Character defaultValue) {
        super(name, defaultValue);
    }

    public PropertyCharacter(String name, Character defaultValue,
            Validator<Character> validator) {
        super(name, defaultValue, validator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Character parse(String value) {
        if (value.length() > 1) {
            throw new IllegalArgumentException();
        }

        return value.charAt(0);
    }

}
