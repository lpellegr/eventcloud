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
package org.objectweb.proactive.extensions.p2p.structured.operations;

import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;

/**
 * A {@link RunnableOperation} is an operation that returns no answer.
 * 
 * @author lpellegr
 */
public abstract class RunnableOperation implements Operation {

    private static final long serialVersionUID = 151L;

    /**
     * Handles the operation by using the specified {@code overlay}.
     * 
     * @param overlay
     *            the overlay receiving the operation.
     */
    public abstract void handle(StructuredOverlay overlay);

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCompatibleWithJoin() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCompatibleWithLeave() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCompatibleWithReassign() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCompatibleWithRouting() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isJoinOperation() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLeaveOperation() {
        return false;
    }

    public boolean isMutualExclusionOperation() {
        return false;
    }

    public boolean isCompatible(RunnableOperation other) {
        return this.isMutualExclusionOperation()
                && other.isMutualExclusionOperation();
    }

}
