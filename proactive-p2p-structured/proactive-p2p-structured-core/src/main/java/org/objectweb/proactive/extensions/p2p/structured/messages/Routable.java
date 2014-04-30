/**
 * Copyright (c) 2011-2014 INRIA.
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
package org.objectweb.proactive.extensions.p2p.structured.messages;

import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;

/**
 * This interface is assumed to be implemented by all {@link Message} in order
 * to add router features.
 * 
 * @author lpellegr
 * 
 * @param <K>
 *            the type of the key used to check if the constraints are validated
 *            (i.e. to make decision to route).
 */
public interface Routable<K> {

    /**
     * Returns the {@link Router} to use in order to route the message.
     * 
     * @return the {@link Router} to use in order to route the message.
     */
    Router<? extends Message<K>, K> getRouter();

    /**
     * Route the {@link Message} to the correct {@link Peer}. If the current
     * peer contains the key to reach, the query is handled and a response is
     * routed to the sender.
     * 
     * @param overlay
     *            the overlay used in order to route the request.
     */
    void route(StructuredOverlay overlay);

}
