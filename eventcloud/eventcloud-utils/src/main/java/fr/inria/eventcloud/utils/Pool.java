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
package fr.inria.eventcloud.utils;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.google.common.base.Objects;
import com.google.common.base.Supplier;

/**
 * Simple pool that maintains resources of type T. By default the pool is empty.
 * To populate the pool with some resources, the method {@link #allocate(int)}
 * must be used, This last will generate resources from the supplier specified
 * during the creation of the pool and keep a reference to the resources
 * generated. These resources may be reclaimed later with a call to
 * {@link #borrow()} and released with the help of {@link #release(Object)}.
 * <p>
 * Calls to {@link #borrow()} when the pool is empty will trigger the generation
 * of a new resource from the specified supplier. As a consequence, it is up to
 * the user to allocate the right number of resources to prevent expensive
 * execution time during a call to {@link #borrow()}. This class is thread-safe.
 * 
 * @author lpellegr
 */
public class Pool<R> implements Iterable<R> {

    protected ConcurrentLinkedQueue<R> resources;

    private Supplier<? extends R> supplier;

    public Pool(Supplier<? extends R> supplier) {
        this.supplier = supplier;
        this.resources = new ConcurrentLinkedQueue<R>();
    }

    public void allocate(int nb) {
        for (int i = 0; i < nb; i++) {
            this.resources.add(this.supplier.get());
        }
    }

    public R borrow() {
        R resource = this.resources.poll();

        if (resource == null) {
            resource = this.supplier.get();
        }

        return resource;
    }

    public void clear() {
        this.resources.clear();
    }

    public void release(R resource) {
        this.resources.add(resource);
    }

    public boolean isEmpty() {
        return this.resources.isEmpty();
    }

    public int size() {
        return this.resources.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<R> iterator() {
        return this.resources.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Objects.toStringHelper(this).omitNullValues().add(
                "size", this.resources.size()).toString();
    }

}
