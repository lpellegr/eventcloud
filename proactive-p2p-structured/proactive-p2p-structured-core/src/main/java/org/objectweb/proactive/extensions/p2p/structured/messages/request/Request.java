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
package org.objectweb.proactive.extensions.p2p.structured.messages.request;

import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.messages.RequestResponseMessage;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.Response;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.validator.ConstraintsValidator;

/**
 * A {@code Request} is an abstraction of a query that can be sent on a
 * structured peer-to-peer network in order to reach a desired set of peers
 * (e.g. the peers where you want to retrieve some data or more generally to
 * perform some actions) by a key which is an object of type {@code K}. In
 * response an object of type {@link Response} is returned.
 * <p>
 * A request is routed step by step by using one-way mechanism. As a consequence
 * of it, we can't say when the response will be returned. Suppose that the peer
 * A is sending a query in order to reach the peer B managing the key
 * {@code keyToReach}. The first step consists in setting the {@code keyToReach}
 * to {@code keyToFind}. After that the query is sent step by step until the
 * peer managing the {@code keyToReach} is found. When it is found, the
 * {@code keyToReach} changes for {@code keyFromSender}. At this time the
 * response is routed to the sender in the opposite direction without
 * necessarily using the same path. This last point depends on the concrete
 * request type that implements the desired behavior.
 * 
 * @author lpellegr
 * 
 * @see RequestResponseMessage
 * @see Response
 */
public abstract class Request<K> extends RequestResponseMessage<K> {

    private static final long serialVersionUID = 1L;

    /**
     * Timestamp of the creation of the message.
     */
    private long dispatchTimestamp = System.currentTimeMillis();

    /**
     * Constructs a new query message with the specified {@codekeyToReach}.
     * 
     * @param validator
     *            the constraints validator used for routing decisions.
     */
    public Request(ConstraintsValidator<K> validator) {
        super(UUID.randomUUID(), validator);
    }

    /**
     * Constructs a new query message with the specified {@code uuid},
     * {@code validator} and {@code dispatchTimestamp}.
     * 
     * @param uuid
     *            the universally unique identifier associated to the query.
     * @param validator
     *            the constraints validator used for routing decisions.
     * @param dispatchTimestamp
     *            the dispatch timestamp of the query.
     */
    public Request(UUID uuid, ConstraintsValidator<K> validator,
            long dispatchTimestamp) {
        super(uuid, validator);
        this.dispatchTimestamp = dispatchTimestamp;
    }

    /**
     * Creates an {@link Response} in accordance to the type of the current
     * {@link Request}.
     * 
     * @param overlay
     *            the overlay from which the response is created.
     * 
     * @return an {@link Response} in accordance to the type of the current
     *         {@link Request}.
     */
    public abstract Response<K> createResponse(StructuredOverlay overlay);

    /**
     * {@inheritDoc}
     */
    @Override
    public long getDispatchTimestamp() {
        return this.dispatchTimestamp;
    }

}
