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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.pubsub.Subscription;

/**
 * Custom buffer used by {@link PublishSubscribeOperationsDelayer} to avoid
 * instanceof tests and multiple iterations.
 * 
 * @author lpellegr
 */
public class CustomBuffer implements Collection<Object> {

    private final QuadrupleList quadruples;

    private final List<Subscription> subscriptions;

    private final Map<ExtendedCompoundEvent, ExtendedCompoundEvent> extendedCompoundEvents;

    public CustomBuffer(int bufsize) {
        this.quadruples = new QuadrupleList(bufsize);
        this.subscriptions = new ArrayList<Subscription>(bufsize);
        this.extendedCompoundEvents =
                new HashMap<ExtendedCompoundEvent, ExtendedCompoundEvent>(
                        bufsize);
    }

    public void add(Quadruple q) {
        this.quadruples.add(q);
    }

    public void add(ExtendedCompoundEvent e) {
        ExtendedCompoundEvent previousValue =
                this.extendedCompoundEvents.put(e, e);

        if (previousValue != null) {
            e.addQuadrupleIndexesUsedForIndexing(previousValue.quadrupleIndexesUsedForIndexing);
        }
    }

    public void add(Subscription s) {
        this.subscriptions.add(s);
    }

    public QuadrupleList getQuadruples() {
        return this.quadruples;
    }

    public List<Subscription> getSubscriptions() {
        return this.subscriptions;
    }

    public Collection<ExtendedCompoundEvent> getExtendedCompoundEvents() {
        return this.extendedCompoundEvents.values();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        int result = this.quadruples.size() + this.subscriptions.size();

        if (this.extendedCompoundEvents != null) {
            result += this.extendedCompoundEvents.size();
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Object> iterator() {
        throw new UnsupportedOperationException();
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
    public boolean add(Object e) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
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
    public boolean addAll(Collection<? extends Object> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
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
        this.quadruples.clear();
        this.subscriptions.clear();

        if (this.extendedCompoundEvents != null) {
            this.extendedCompoundEvents.clear();
        }
    }

}
