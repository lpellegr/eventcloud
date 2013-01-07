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
 * Tests if a peer is activated (i.e. whether it has joined a network).
 * 
 * @author lpellegr
 */
public class IsActivatedOperation extends CallableOperation {

    private static final long serialVersionUID = 140L;

    /**
     * Returns a {@link BooleanResponseOperation} containing {@code true} value
     * if the peer handling this operation is activated, {@code false}
     * otherwise.
     * 
     * @param overlay
     *            the overlay handling the operation.
     * 
     * @return a {@link BooleanResponseOperation} containing {@code true} value
     *         if the peer handling this message is activated, {@code false}
     *         otherwise.
     */
    @Override
    public BooleanResponseOperation handle(StructuredOverlay overlay) {
        return new BooleanResponseOperation(overlay.isActivated());
    }

}
