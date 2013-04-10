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

import java.util.Collection;

import fr.inria.eventcloud.overlay.SemanticCanOverlay;

/**
 * Actions performed by the buffer.
 * 
 * @author lpellegr
 */
public abstract class BufferOperator<B extends Collection<?>> {

    protected final SemanticCanOverlay overlay;

    public BufferOperator(SemanticCanOverlay overlay) {
        this.overlay = overlay;
    }

    /**
     * Flushes the specified objects to the underlying datastore.
     * 
     * @param buffer
     *            the buffer containing the objects that have to be flushed.
     */
    public abstract void flushBuffer(B buffer);

    /**
     * The action to trigger once the buffer has been flushed.
     * 
     * @param buffer
     *            the buffer containing the objects that have been flushed.
     */
    public abstract void triggerAction(B buffer);

}