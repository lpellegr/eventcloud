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
package org.objectweb.proactive.extensions.p2p.structured.overlay.can;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.messages.MessageId;
import org.objectweb.proactive.extensions.p2p.structured.messages.RequestResponseManager;

/**
 * A request manager specific to CAN peers in order to maintain some information
 * about the routing.
 * 
 * @author lpellegr
 */
public class CanRequestResponseManager extends RequestResponseManager {

    private static final long serialVersionUID = 160L;

    /*
     * Used to maintain the list of requests which have already been 
     * received when AnycastRouter is used.
     */
    // TODO: old entries must be garbage collected to avoid memory leak
    private Set<MessageId> requestsAlreadyReceived;

    public CanRequestResponseManager() {
        super(P2PStructuredProperties.MAO_SOFT_LIMIT_PEERS.getValue());

        this.requestsAlreadyReceived =
                Collections.newSetFromMap(new ConcurrentHashMap<MessageId, Boolean>(
                        16, 0.75f,
                        P2PStructuredProperties.MAO_SOFT_LIMIT_PEERS.getValue()));
    }

    /**
     * Indicates whether the current peer has already received the request
     * identified by the specified identifier.
     * 
     * @param requestId
     *            the identifier to check.
     * 
     * @return {@code true} if the current peer has already received the
     *         request, {@code false} otherwise.
     */
    public boolean hasReceivedRequest(MessageId requestId) {
        return this.requestsAlreadyReceived.contains(requestId);
    }

    /**
     * Adds the request to the list of requests already received by the peer,
     * identified by the specified identifier, and returns whether the request
     * has already been received or not.
     * 
     * @param requestId
     *            the identifier to check.
     * 
     * @return {@code false} if the current peer has already received the
     *         request, {@code true} otherwise.
     */
    public boolean receiveRequest(MessageId requestId) {
        return this.requestsAlreadyReceived.add(requestId);
    }

    public int getNbRequestsTraced() {
        return this.requestsAlreadyReceived.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {
        super.clear();
        this.requestsAlreadyReceived.clear();
    }

}
