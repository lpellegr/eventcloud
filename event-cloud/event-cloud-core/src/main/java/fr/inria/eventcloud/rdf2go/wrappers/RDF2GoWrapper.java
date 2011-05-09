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
package fr.inria.eventcloud.rdf2go.wrappers;

import java.io.Serializable;

/**
 * Defines contract that all RDF2Go wrappers must verify.
 * 
 * @author lpellegr
 */
public interface RDF2GoWrapper<T> extends Serializable {

    /**
     * Converts the current wrapper object to a concrete RDF2Go object.
     * 
     * @return a concrete RDF2Go object.
     */
    public abstract T toRDF2Go();

}
