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
package org.objectweb.proactive.extensions.p2p.structured.operations;

import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;

/**
 * {@link CallableOperation} is an {@link Operation} which returns a response.
 * 
 * @author lpellegr
 */
public abstract class CallableOperation implements Operation {

    private static final long serialVersionUID = 1L;

    /**
     * Handles the operation by using the specified {@code overlay}
     * 
     * @param overlay
     *            the overlay receiving the operation.
     * 
     * @return a response associated to the operation handled.
     */
    public abstract ResponseOperation handle(StructuredOverlay overlay);

    /*
     * The following methods are used to check the method compatibilities in PeerImpl
     */

    /**
     * Indicates whether the operation is used to transfer some information
     * between {@link Peer}s during a join operation.
     * 
     * @return {@code true} if the operation is used to transfer some
     *         information between {@link Peer}s during a join operation,
     *         {@code false} otherwise.
     */
    public boolean isJoinOperation() {
        return false;
    }

    /**
     * Indicates whether the operation is used to transfer some information
     * between {@link Peer}s during a leave operation.
     * 
     * @return {@code true} if the operation is used to transfer some
     *         information between {@link Peer}s during a leave operation,
     *         {@code false} otherwise.
     */
    public boolean isLeaveOperation() {
        return false;
    }

    public boolean isCompatible(CallableOperation op) {
        /* Truth table
        /* ----------------------
        /* First request   Second Request   Compatible?
        /* isJoinOrLeave   !isJoinOrLeave   no
        /* isJoinOrLeave   isJoinOrLeave    no
        /* !isJoinOrLeave  !isJoinOrLeave   yes
        /* !isJoinOrLeave  isJoinOrLeave    no
        */
        return !this.isJoinOrLeaveOperation() && !op.isJoinOrLeaveOperation();
    }

    private boolean isJoinOrLeaveOperation() {
        return this.isJoinOperation() || this.isLeaveOperation();
    }

}
