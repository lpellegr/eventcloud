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
package fr.inria.eventcloud.api.properties;

import fr.inria.eventcloud.api.Rdfable;

/**
 * ElaProperty stands for Event Level Agreement Property. It is used to store a
 * property name and it associated value.
 * 
 * @author lpellegr
 */
public abstract class ElaProperty implements Rdfable {

    protected String name;

    protected String value;

    protected ElaProperty() {
    }

    public ElaProperty(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Returns the property name.
     * 
     * @return the property name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the value associated to the property.
     * 
     * @return the value associated to the property.
     */
    public String getValue() {
        return this.value;
    }

}
