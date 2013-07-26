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
package org.objectweb.proactive.extensions.p2p.structured.messages;

import org.objectweb.proactive.extensions.p2p.structured.providers.ResponseProvider;
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

    private static final long serialVersionUID = 160L;

    /**
     * Timestamp of the creation of the message.
     */
    protected long dispatchTimestamp;

    protected ResponseProvider<? extends Response<K>, K> responseProvider;

    /**
     * Constructs a new request with the specified {@code validator}. The
     * request is assumed to generate no response (no final destination or
     * response provider if specified).
     * 
     * @param validator
     */
    public Request(ConstraintsValidator<K> validator) {
        this(validator, null);
    }

    /**
     * Constructs a new request with the specified {@code validator} and
     * {@code responseProvider}.
     * 
     * @param validator
     *            the constraints validator used for routing decisions.
     * @param responseProvider
     *            the responseProvider to use when a response has to be created.
     */
    public Request(ConstraintsValidator<K> validator,
            ResponseProvider<? extends Response<K>, K> responseProvider) {
        this(validator, responseProvider, System.currentTimeMillis());
    }

    /**
     * Constructs a new request with the specified {@code uuid},
     * {@code validator}, {@code responseProvider} and {@code dispatchTimestamp}
     * .
     * 
     * @param requestId
     *            request identifier.
     * @param validator
     *            the constraints validator used for routing decisions.
     * @param responseProvider
     *            the responseProvider to use when a response has to be created.
     * @param dispatchTimestamp
     *            the dispatch timestamp of the query.
     */
    private Request(ConstraintsValidator<K> validator,
            ResponseProvider<? extends Response<K>, K> responseProvider,
            long dispatchTimestamp) {
        super(validator);

        this.dispatchTimestamp = dispatchTimestamp;
        this.responseProvider = responseProvider;
    }

    /**
     * Returns the response provider associated to this request. A {@code null}
     * value indicates that this request is not supposed to return a response.
     * 
     * @return the response provider associated to this request. A {@code null}
     *         value indicates that this request is not supposed to return a
     *         response.
     */
    public ResponseProvider<? extends Response<K>, K> getResponseProvider() {
        return this.responseProvider;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getDispatchTimestamp() {
        return this.dispatchTimestamp;
    }

}
