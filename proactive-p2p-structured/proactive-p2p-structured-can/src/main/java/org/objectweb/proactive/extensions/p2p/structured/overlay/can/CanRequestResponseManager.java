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
package org.objectweb.proactive.extensions.p2p.structured.overlay.can;

import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

import org.objectweb.proactive.extensions.p2p.structured.overlay.RequestResponseManager;

/**
 * A request manager specific to CAN peers in order to maintain some information
 * about the routing.
 * 
 * @author lpellegr
 */
public class CanRequestResponseManager extends RequestResponseManager {

    private static final long serialVersionUID = 1L;

    /*
     * Used to maintain the list of requests which have been already 
     * received when anycast requests are executed.
     */
    private ConcurrentSkipListSet<UUID> requestsAlreadyReceived =
            new ConcurrentSkipListSet<UUID>();

    public void markRequestAsReceived(UUID requestId) {
        this.requestsAlreadyReceived.add(requestId);
    }

    /**
     * Indicates if the current peer has already received the request identified
     * by the specified requestId.
     * 
     * @param requestId
     * 
     * @return {@code true} if the current peer has already received the
     *         request, {@code false} otherwise.
     */
    public boolean hasReceivedRequest(UUID requestId) {
        return this.requestsAlreadyReceived.contains(requestId);
    }

}
