/**
 * Copyright (c) 2011-2014 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package org.objectweb.proactive.extensions.p2p.structured.configuration;

/**
 * A {@link Double} property.
 * 
 * @author lpellegr
 */
public class PropertyDouble extends Property<Double> {

    public PropertyDouble(String name, Double defaultValue) {
        super(name, defaultValue);
    }

    public PropertyDouble(String name, Double defaultValue,
            Validator<Double> validator) {
        super(name, defaultValue, validator);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Double parse(String value) {
        return Double.valueOf(value);
    }

}
