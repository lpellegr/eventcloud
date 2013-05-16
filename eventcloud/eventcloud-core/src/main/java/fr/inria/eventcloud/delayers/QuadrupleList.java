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
package fr.inria.eventcloud.delayers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.google.common.collect.Iterators;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.pubsub.PublishSubscribeUtils;

/**
 * List that keeps separated meta-quadruples and non meta-quadruples.
 * 
 * @author lpellegr
 */
public class QuadrupleList implements List<Quadruple> {

    private final List<Quadruple> nonMetaQuadruples;

    private final List<Quadruple> metaQuadruples;

    public QuadrupleList(int initialCapacity) {
        this.nonMetaQuadruples = new ArrayList<Quadruple>(initialCapacity);

        int estimatedSize =
                initialCapacity
                        / EventCloudProperties.AVERAGE_NB_QUADRUPLES_PER_COMPOUND_EVENT.getValue();

        if (estimatedSize == 0) {
            estimatedSize = 1;
        }

        this.metaQuadruples = new ArrayList<Quadruple>(estimatedSize);
    }

    /**
     * Returns meta-quadruples.
     * 
     * @return the metaQuadruples.
     */
    public List<Quadruple> getMetaQuadruples() {
        return this.metaQuadruples;
    }

    /**
     * Returns non meta-quadruples.
     * 
     * @return the nonMetaQuadruples.
     */
    public List<Quadruple> getNonMetaQuadruples() {
        return this.nonMetaQuadruples;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return this.nonMetaQuadruples.size() + this.metaQuadruples.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return this.metaQuadruples.isEmpty()
                && this.nonMetaQuadruples.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Object o) {
        return this.metaQuadruples.contains(o)
                || this.nonMetaQuadruples.contains(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Quadruple> iterator() {
        return Iterators.concat(
                this.metaQuadruples.iterator(),
                this.nonMetaQuadruples.iterator());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(Quadruple q) {
        if (PublishSubscribeUtils.isMetaQuadruple(q)) {
            return this.metaQuadruples.add(q);
        } else {
            return this.nonMetaQuadruples.add(q);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(Object o) {
        return this.nonMetaQuadruples.remove(o)
                || this.metaQuadruples.remove(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(Collection<? extends Quadruple> quadruples) {
        for (Quadruple q : quadruples) {
            this.add(q);
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(int index, Collection<? extends Quadruple> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeAll(Collection<?> quadruples) {
        boolean result = false;

        for (Object q : quadruples) {
            result |= this.remove(q);
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        this.metaQuadruples.clear();
        this.nonMetaQuadruples.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Quadruple get(int index) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Quadruple set(int index, Quadruple element) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(int index, Quadruple element) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Quadruple remove(int index) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int indexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int lastIndexOf(Object o) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListIterator<Quadruple> listIterator() {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListIterator<Quadruple> listIterator(int index) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Quadruple> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }

}
