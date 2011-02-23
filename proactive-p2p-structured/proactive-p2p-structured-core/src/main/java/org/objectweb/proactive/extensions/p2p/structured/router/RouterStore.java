package org.objectweb.proactive.extensions.p2p.structured.router;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.proactive.extensions.p2p.structured.messages.RequestReplyMessage;

/**
 * This class stores all {@link Router}s instances in the current JVM. The class
 * is a singleton in a given JVM. A router is different from an another
 * depending from the message to route, so the RouterStore stores an instance of
 * a router for each new type of messages created.
 * 
 * @author lpellegr
 */
public class RouterStore {

    private Map<Class<? extends RequestReplyMessage<?>>, Router<?, ?>> routersInstances = 
        new HashMap<Class<? extends RequestReplyMessage<?>>, Router<?, ?>>();

    private static class LazyInitializer {
    	public static RouterStore instance = new RouterStore();
    }
    
    private RouterStore() {
        
    }

    /**
     * Stores a {@link Router} instance for the specified type of message. If an
     * instance already exists for the specified type of message, the operation
     * is ignored.
     * 
     * @param key
     *            the type of message to which the router is associated.
     * @param value
     *            the {@link Router} instance to store.
     */
    public void store(Class<? extends RequestReplyMessage<?>> key, Router<?, ?> value) {
        if (!this.routersInstances.containsKey(key)) {
            this.routersInstances.put(key, value);
        }
    }

    /**
     * Stores a {@link Router} instance for the specified type of message even
     * if an instance already exists for the specified type of message.
     * 
     * @param key
     *            the type of message to which the router is associated.
     * @param value
     *            the {@link Router} instance to store.
     */
    public void forceStore(Class<? extends RequestReplyMessage<?>> key, Router<?, ?> value) {
        this.routersInstances.put(key, value);
    }

    /**
     * Returns an instance of a {@link Router} previously stored for the
     * specified type of message or <code>null</code> if there is no instance
     * available for the specified type of message.
     * 
     * @param key
     *            the type of message to look for.
     * @return an instance of a {@link Router} previously stored for the
     *         specified type of message or <code>null</code> if there is no
     *         instance available for the specified type of message.
     */
    public Router<?, ?> get(Class<? extends RequestReplyMessage<?>> key) {
        return this.routersInstances.get(key);
    }

    /**
     * Indicates if there is an instance of a {@link Router} available for the
     * specified type of message.
     * 
     * @param key
     *            the type of message to look for.
     * @return <code>true</code> if there an instance of {@link Router}
     *         available, <code>false</code> otherwise.
     */
    public boolean contains(Class<? extends RequestReplyMessage<?>> key) {
        return routersInstances.containsKey(key);
    }

    /**
     * Returns an instance of the RouterStore.
     * 
     * @return an instance of the RouterStore.
     */
    public static RouterStore getInstance() {
       return LazyInitializer.instance;
    }
    
}
