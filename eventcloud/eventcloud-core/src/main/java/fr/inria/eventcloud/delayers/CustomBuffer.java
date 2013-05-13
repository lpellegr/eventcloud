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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.pubsub.Subscription;

/**
 * Custom buffer used by {@link PublishSubscribeOperationsDelayer} to avoid
 * instanceof tests and multiple iterations.
 * 
 * @author lpellegr
 */
public class CustomBuffer implements Collection<Object> {

    private final Set<Quadruple> quadruples;

    private final List<Subscription> subscriptions;

    private final Set<ExtendedCompoundEvent> compoundEvents;

    public CustomBuffer(int bufsize) {
        this.quadruples = new HashSet<Quadruple>(bufsize);
        this.subscriptions = new ArrayList<Subscription>(bufsize);

        if (EventCloudProperties.isSbce3PubSubAlgorithmUsed()) {
            this.compoundEvents = new HashSet<ExtendedCompoundEvent>(bufsize);
        } else {
            this.compoundEvents = null;
        }
    }

    public void add(Quadruple q) {
        this.quadruples.add(q);
    }

    public void add(ExtendedCompoundEvent e) {
        this.compoundEvents.add(e);
    }

    public void add(Subscription s) {
        this.subscriptions.add(s);
    }

    /**
     * 
     * 
     * @return the quadruples
     */
    public Set<Quadruple> getQuadruples() {
        return this.quadruples;
    }

    /**
     * 
     * 
     * @return the subscriptions
     */
    public List<Subscription> getSubscriptions() {
        return this.subscriptions;
    }

    /**
     * 
     * 
     * @return the compoundEvents
     */
    public Set<ExtendedCompoundEvent> getExtendedCompoundEvents() {
        return this.compoundEvents;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        int result = this.quadruples.size() + this.subscriptions.size();

        if (this.compoundEvents != null) {
            result += this.compoundEvents.size();
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

        if (this.compoundEvents != null) {
            this.compoundEvents.clear();
        }
    }

}
