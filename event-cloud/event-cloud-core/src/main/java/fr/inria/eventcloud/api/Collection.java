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
package fr.inria.eventcloud.api;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Iterator;

/**
 * This class defines a collection that is serializable. It provides the same
 * methods as {@link java.util.Collection}. This class is not assumed to play
 * the role of a stream (currently ProActive does not support it) but the role
 * of an in-memory collection.
 * 
 * @author lpellegr
 * 
 * @param <T>
 *            the type of the elements that are iterated. A element of type T
 *            must be serializable.
 */
public class Collection<T> implements java.util.Collection<T>, Serializable {

    private static final long serialVersionUID = 1L;

    private transient java.util.Collection<T> collection;

    /**
     * Creates a collection from a {@link java.util.Collection} which has a
     * concrete type which is serializable.
     * 
     * @param collection
     *            the Java collection to use in order to create the serializable
     *            collection.
     */
    public Collection(java.util.Collection<T> collection) {
        if (collection instanceof Collection) {
            throw new IllegalArgumentException(
                    "Recursive construction not allowed: you try to construct a Collection from a Collection");
        }

        this.collection = collection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return this.collection.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return this.collection.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Object o) {
        return this.collection.contains(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<T> iterator() {
        return this.collection.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] toArray() {
        return this.collection.toArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <E> E[] toArray(E[] a) {
        return this.collection.toArray(a);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(T e) {
        return this.collection.add(e);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean remove(Object o) {
        return this.collection.remove(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsAll(java.util.Collection<?> c) {
        return this.collection.containsAll(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean addAll(java.util.Collection<? extends T> c) {
        return this.collection.addAll(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeAll(java.util.Collection<?> c) {
        return this.collection.removeAll(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean retainAll(java.util.Collection<?> c) {
        return this.collection.retainAll(c);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        this.collection.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        return this.collection.equals(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.collection.hashCode();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(this.collection.getClass());
        out.writeInt(this.collection.size());
        // writes out the elements in the proper order
        for (T elt : this.collection) {
            out.writeObject(elt);
        }
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        Class<? extends Collection<T>> clazz =
                (Class<? extends Collection<T>>) in.readObject();
        try {
            this.collection = (java.util.Collection<T>) clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        int size = in.readInt();

        for (int i = 0; i < size; i++) {
            this.collection.add((T) in.readObject());
        }
    }

}
