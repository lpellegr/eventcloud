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
package fr.inria.eventcloud.datastore;

import java.util.Iterator;

import com.hp.hpl.jena.sparql.core.Quad;

import fr.inria.eventcloud.api.Quadruple;

/**
 * Defines a quadruple iterator to have the possibility to convert Jena
 * {@link Quad} to internal {@link Quadruple} type while iterating over an
 * iterator containing {@link Quad} elements.
 * 
 * @author lpellegr
 */
public class QuadrupleIterator implements Iterator<Quadruple> {

    private final Iterator<Quad> it;

    /**
     * Creates a quadruple iterator from the specified {@link Quad} iterator.
     * 
     * @param it
     *            the specified iterator to wrap.
     */
    public QuadrupleIterator(Iterator<Quad> it) {
        this.it = it;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        return this.it.hasNext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Quadruple next() {
        Quad q = this.it.next();
        return new Quadruple(
                q.getGraph(), q.getSubject(), q.getPredicate(), q.getObject(),
                false, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove() {
        this.it.remove();
    }

    /**
     * Returns the number of elements provided by the iterator. Warning,
     * <strong>this operation is destructive</strong>; it consumes the iterator.
     * This operation is useful for testing purposes.
     * 
     * @return the number of elements provided by the iterator
     */
    public long count() {
        long result = 0;
        while (this.it.hasNext()) {
            this.it.next();
            result++;
        }
        return result;
    }

}
