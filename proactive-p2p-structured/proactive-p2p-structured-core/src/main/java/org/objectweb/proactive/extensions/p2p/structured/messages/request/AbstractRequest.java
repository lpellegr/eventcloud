package org.objectweb.proactive.extensions.p2p.structured.messages.request;

import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.messages.RequestReplyMessage;
import org.objectweb.proactive.extensions.p2p.structured.messages.reply.AbstractReply;

/**
 * An <code>AbstractRequest</code> is an abstraction of a query that can be sent
 * on a structured peer-to-peer network in order to find some data by a key
 * which is an object of type <code>K</code>. In response an object of type
 * {@link AbstractReply} is returned with the desired data.
 * <p>
 * A request is performed step by step by using one-way mechanism. as a
 * consequence of it, we can't say when the response will be returned. Suppose
 * that the peer A is sending a query in order to reach the peer B managing the
 * key <code>keyToReach</code>. The first step consists in setting the
 * <code>keyToReach</code> to <code>keyToFound</code>. After that the query is
 * sent step by step until the peer managing the <code>keyToReach</code> is
 * found. When it is found, the <code>keyToReach</code> change for
 * <code>keyFromSender</code>. At this time the reply is routed to the sender in
 * the opposite direction without necessarily using the same path. This last
 * point depends on the concrete type of query which can implement the desired
 * behavior.
 * 
 * @author lpellegr
 * 
 * @see RequestReplyMessage
 * @see AbstractReply
 */
public abstract class AbstractRequest<K> extends RequestReplyMessage<K> {

    private static final long serialVersionUID = 1L;

    /**
     * Timestamp of the creation of the message.
     */
    private long dispatchTimestamp = System.currentTimeMillis();

    /**
     * Constructs a new query message with the specified <code>keyToReach</code>.
     * 
     * @param keyToReach
     *            the key to reach.
     */
    public AbstractRequest(K keyToReach) {
        super(UUID.randomUUID(), keyToReach);
    }

    /**
     * Constructs a new query message with the specified <code>uuid</code>,
     * <code>keyToReach</code> and <code>dispatchTimestamp</code>.
     * 
     * @param uuid
     *            the universally unique identifier associated to the query.
     * @param keyToReach
     *            the key to reach.
     * @param dispatchTimestamp
     *            the dispatch timestamp of the query.
     */
    public AbstractRequest(UUID uuid, K keyToReach, long dispatchTimestamp) {
        super(uuid, keyToReach);
        this.dispatchTimestamp = dispatchTimestamp;
    }

    /**
     * Creates an {@link AbstractReply} in accordance to the type of
     * the current {@link AbstractRequest}.
     * 
     * @return an {@link AbstractReply} in accordance to the type of
     * the current {@link AbstractRequest}.
     */
    public abstract AbstractReply<?> createResponseMessage();

    /**
     * {@inheritDoc}
     */
    public long getDispatchTimestamp() {
        return this.dispatchTimestamp;
    }

}
