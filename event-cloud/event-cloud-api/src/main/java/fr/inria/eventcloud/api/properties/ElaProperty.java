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

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Quadruple;

/**
 * ElaProperty stands for Event Level Agreement Property. It is used to store a
 * property name and it associated value.
 * 
 * @author lpellegr
 */
public abstract class ElaProperty {

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

    /**
     * Translates an ELA property to a collection of quadruples.
     * 
     * @param graph
     *            the context/graph value to use.
     * 
     * @return a collection of quadruples that contain the information about the
     *         ELA property that has been translated.
     */
    public abstract Collection<Quadruple> toQuadruples(Node graph);

}
