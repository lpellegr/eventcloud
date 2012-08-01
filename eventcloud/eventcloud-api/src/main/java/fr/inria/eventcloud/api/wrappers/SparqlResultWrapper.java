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
package fr.inria.eventcloud.api.wrappers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * SparqlResultWrapper provides some useful methods to make some Jena Sparql
 * Result serializable (e.g. ResultSet or Model).
 * 
 * @author lpellegr
 */
public abstract class SparqlResultWrapper<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    protected transient T object;

    public SparqlResultWrapper(T obj) {
        this.object = obj;
    }

    public T toJena() {
        return this.object;
    }

    protected abstract void internalWriteObject(ObjectOutputStream out)
            throws IOException;

    protected abstract void internalReadObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException;

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        this.internalWriteObject(out);
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        this.internalReadObject(in);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.object.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof SparqlResultWrapper
                && this.object.equals(((SparqlResultWrapper<?>) obj).object);
    }

}
