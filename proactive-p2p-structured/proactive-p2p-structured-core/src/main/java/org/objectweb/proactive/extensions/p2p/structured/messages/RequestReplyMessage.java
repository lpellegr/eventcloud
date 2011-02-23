package org.objectweb.proactive.extensions.p2p.structured.messages;

import java.io.Serializable;
import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.router.RouterStore;

/**
 * @author Laurent Pellegrino
 */
public abstract class RequestReplyMessage<K> implements Routable<K>, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Universally unique identifier used in order to identify the response.
     */
    private final UUID uuid;

    /**
     * The key used in order to route the query on the network.
     */
    private K keyToReach;

    /**
     * The number of hops between the source and the destination of this message.
     */
    private int hopCount = 0;

    /**
     * Constructs a new SynchronousMessage with the specified identifier and
     * keyToReach.
     * 
     * @param identifier
     *            the unique identifier associated to the message.
     *            
     * @param keyToReach
     *            the key to reach (i.e. routes until reaching the {@link Peer}
     *            managing this key).
     */
    public RequestReplyMessage(UUID identifier, K keyToReach) {
        this.uuid = identifier;
        this.keyToReach = keyToReach;
    }

    /**
     * Returns the timestamp of the creation of the message.
     * 
     * @return the timestamp of the creation of the message.
     */
    public abstract long getDispatchTimestamp();

    /**
     * Returns the universally unique identifier which identifies
     * the message.
     * 
     * @return the universally unique identifier which identifies
     * 		   the message.
     */
    public UUID getId() {
        return this.uuid;
    }
    
    /**
     * Returns the key to reach (i.e. the peer containing this key is the
     * receiver of this message).
     * 
     * @return the key to reach (i.e. the peer containing this key is the
     *         receiver of this message).
     */
    public K getKeyToReach() {
        return this.keyToReach;
    }

	/**
	 * Returns the number of hops between the source and the destination of this
	 * message.
	 * 
	 * @return the number of hops between the source and the destination of this
	 *         message.
	 */
    public int getHopCount() {
        return this.hopCount;
    }

	/**
	 * Increments the hop count (i.e. the counter counting the number of hops
	 * between the source and the destination of this message) by the specified
	 * <code>increment</code>.
	 * 
	 * @param increment
	 *            the size of the increment.
	 */
    public void incrementHopCount(int increment) {
        this.hopCount += increment;
    }
    
    @SuppressWarnings("unchecked")
    public void route(StructuredOverlay overlay) {
        Class<RequestReplyMessage<K>> clazz =
                (Class<RequestReplyMessage<K>>) this.getClass();
        RouterStore routerStore = RouterStore.getInstance();
        if (!routerStore.contains(clazz)) {
            routerStore.store(clazz, this.getRouter());
        }
        ((Router<RequestReplyMessage<K>, K>) routerStore.get(clazz)).makeDecision(overlay, this);
    }
    
    /**
     * Sets the key to reach.
     * 
     * @param keyToReach
     *            the new key to reach.
     */
    public void setKeyToReach(K keyToReach) {
        this.keyToReach = keyToReach;
    }

    /**
     * Sets a new value to the hop count counter.
     * 
     * @param value
     * 				the new value to set.
     */
    public void setHopCount(int value) {
        this.hopCount = value;
    }

}
