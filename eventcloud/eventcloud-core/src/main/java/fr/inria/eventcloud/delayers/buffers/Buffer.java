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
package fr.inria.eventcloud.delayers.buffers;

import java.util.Iterator;

import fr.inria.eventcloud.overlay.SemanticCanOverlay;

/**
 * Defines methods required by a delayer to manipulate a buffer.
 * 
 * @author lpellegr
 */
public abstract class Buffer<T> implements Iterable<T> {

    protected final SemanticCanOverlay overlay;

    public Buffer(SemanticCanOverlay overlay) {
        super();
        this.overlay = overlay;
    }

    /**
     * Adds a new element to the buffer.
     * 
     * @param value
     *            the element value to add to the buffer.
     */
    public abstract void add(T value);

    /**
     * Removes all elements from the buffer.
     */
    public abstract void clear();

    /**
     * Returns a boolean that indicates whether the buffer is empty or not.
     * 
     * @return {@code true} if the buffer is empty, {@code false} otherwise.
     */
    public abstract boolean isEmpty();

    @Override
    public abstract Iterator<T> iterator();

    /**
     * Persists the content of the buffer but does not clear elements from the
     * buffer.
     */
    public abstract void persist();

    /**
     * Returns the number of elements inside the buffer.
     * 
     * @return the number of elements inside the buffer.
     */
    public abstract int size();

}
