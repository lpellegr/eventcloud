package org.objectweb.proactive.extensions.p2p.structured.messages.request;

import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.messages.RequestResponseMessage;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.Response;
import org.objectweb.proactive.extensions.p2p.structured.validator.ConstraintsValidator;

/**
 * A <code>Request</code> is an abstraction of a query that can be sent
 * on a structured peer-to-peer network in order to find some data by a key
 * which is an object of type <code>K</code>. In response an object of type
 * {@link Response} is returned.
 * <p>
 * A request is performed step by step by using one-way mechanism. As a
 * consequence of it, we can't say when the response will be returned. Suppose
 * that the peer A is sending a query in order to reach the peer B managing the
 * key <code>keyToReach</code>. The first step consists in setting the
 * <code>keyToReach</code> to <code>keyToFound</code>. After that the query is
 * sent step by step until the peer managing the <code>keyToReach</code> is
 * found. When it is found, the <code>keyToReach</code> change for
 * <code>keyFromSender</code>. At this time the response is routed to the sender in
 * the opposite direction without necessarily using the same path. This last
 * point depends on the concrete type of query which can implement the desired
 * behavior.
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
     * Constructs a new query message with the specified <code>keyToReach</code>.
     * 
     * @param validator
     * 			the constraints validator used for routing decisions.
     */
    public Request(ConstraintsValidator<K> validator) {
        super(UUID.randomUUID(), validator);
    }

    /**
     * Constructs a new query message with the specified <code>uuid</code>,
     * <code>keyToReach</code> and <code>dispatchTimestamp</code>.
     * 
     * @param uuid
     *            the universally unique identifier associated to the query.
     * @param validator
     * 			the constraints validator used for routing decisions.
     * @param dispatchTimestamp
     *            the dispatch timestamp of the query.
     */
    public Request(UUID uuid, ConstraintsValidator<K> validator, long dispatchTimestamp) {
        super(uuid, validator);
        this.dispatchTimestamp = dispatchTimestamp;
    }

    /**
     * Creates an {@link Response} in accordance to the type of
     * the current {@link Request}.
     * 
     * @return an {@link Response} in accordance to the type of
     * the current {@link Request}.
     */
    public abstract Response<?> createResponse();

    /**
     * {@inheritDoc}
     */
    public long getDispatchTimestamp() {
        return this.dispatchTimestamp;
    }

}
