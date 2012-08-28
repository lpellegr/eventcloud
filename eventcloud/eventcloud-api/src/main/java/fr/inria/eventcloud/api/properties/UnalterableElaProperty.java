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
package fr.inria.eventcloud.api.properties;

import java.util.ArrayList;
import java.util.List;

import fr.inria.eventcloud.api.Quadruple;

/**
 * An UnalterableElaProperty is an {@link ElaProperty} which is not modifiable.
 * 
 * @author lpellegr
 */
public final class UnalterableElaProperty extends ElaProperty {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a not modifable ELA property with the specified name and
     * value.
     * 
     * @param name
     *            the property name.
     * @param value
     *            the property value.
     */
    public UnalterableElaProperty(String name, String value) {
        super(name, value);
    }

    /**
     * Constructs a not modifiable ELA property from a collection of quadruples.
     * The specified collection of quadruples is assumed to contain only the
     * information about a {@link UnalterableElaProperty}. If it is not the
     * case, an {@link IllegalArgumentException} will be thrown.
     * 
     * @param quads
     *            the collection of quadruples containing the information about
     *            the not modifiable ELA property.
     */
    public UnalterableElaProperty(List<Quadruple> quads) {
        super();
        // TODO implement the translation but for that we have to define the
        // event-cloud namespace
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Quadruple> toQuadruples() {
        List<Quadruple> quads = new ArrayList<Quadruple>();
        // TODO: implement the translation to quadruples
        // quads.add(new Quadruple("eventCloud", "hasUnalterableElaProperty",
        // "UUID", graph));
        // quads.add(new Quadruple("UUID", "name",
        // Node.createLiteral(super.name), graph));
        // quads.add(new Quadruple("UUID", "value",
        // Node.createLiteral(super.value), graph));
        return quads;
    }

}
