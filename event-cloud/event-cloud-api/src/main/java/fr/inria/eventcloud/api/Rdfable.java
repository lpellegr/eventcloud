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
package fr.inria.eventcloud.api;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Quadruple;

/**
 * A Class implements the {@link Rdfable} interface to indicate that the content
 * of the class can be serialized into RDF data.
 * 
 * @author lpellegr
 */
public interface Rdfable {

    /**
     * Serializes the object that defines this method as a set of quadruples.
     * 
     * @return a set of quadruples.
     */
    public Collection<Quadruple> toQuadruples();

}
