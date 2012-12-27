/**
 * Copyright (c) 2011-2013 INRIA.
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
 * Used to check whether an illegal value is provided for a {@link Property} or
 * not.
 * 
 * @author lpellegr
 * 
 * @param <T>
 *            property value type.
 */
public abstract class Validator<T> {

    public void checkValidity(String propertyName, T propertyValue) {
        if (!this.isLegalValue(propertyValue)) {
            throw new IllegalArgumentException(this.getErrorMessage(
                    propertyName, propertyValue));
        }
    }

    protected String getErrorMessage(String propertyName, T propertyValue) {
        return "Invalid value specified for property '" + propertyName + "': "
                + propertyValue;
    }

    public abstract boolean isLegalValue(T propertyValue);

}
