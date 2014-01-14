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
 * {@link CallableOperation} is an {@link Operation} which returns a response.
 * 
 * @author lpellegr
 */
public abstract class CallableOperation implements Operation {

    private static final long serialVersionUID = 160L;

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
     * The following methods are used to check methods compatibilities in PeerImpl
     */

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCompatibleWithRouting() {
        return false;
    }

}
