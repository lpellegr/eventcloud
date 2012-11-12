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
 * An AlterableElaProperty is an {@link ElaProperty} which is modifiable.
 * 
 * @author lpellegr
 */
public final class AlterableElaProperty extends ElaProperty {

    private static final long serialVersionUID = 130L;

    /**
     * Constructs a modifable ELA property with the specified name and value.
     * 
     * @param name
     *            the property name.
     * @param value
     *            the property value.
     */
    public AlterableElaProperty(String name, String value) {
        super(name, value);
    }

    /**
     * Constructs a modifiable ELA property from a collection of quadruples. The
     * specified collection of quadruples is assumed to contain only the
     * information about an {@link AlterableElaProperty}. If it is not the case,
     * an {@link IllegalArgumentException} will be thrown.
     * 
     * @param quads
     *            the collection of quadruples containing the information about
     *            the modifiable ELA property.
     */
    public AlterableElaProperty(List<Quadruple> quads) {
        super();
        // TODO implement the translation but for that we have to define the
        // EventCloud namespace
    }

    /**
     * Updates the value associated to the property.
     * 
     * @param value
     *            the new value to set.
     */
    public void setValue(String value) {
        super.value = value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Quadruple> toQuadruples() {
        List<Quadruple> quads = new ArrayList<Quadruple>();
        // TODO: implement translation to quadruples
        // quads.add(new Quadruple("eventCloud", "hasAlterableElaProperty",
        // "UUID", graph));
        // quads.add(new Quadruple("UUID", "name",
        // Node.createLiteral(super.name), graph));
        // quads.add(new Quadruple("UUID", "value",
        // Node.createLiteral(super.value), graph));
        return quads;
    }

}
