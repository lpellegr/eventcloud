package org.objectweb.proactive.extensions.p2p.structured.router;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.concurrent.ConcurrentMap;

import org.objectweb.proactive.extensions.p2p.structured.messages.RequestResponseMessage;
import org.objectweb.proactive.extensions.p2p.structured.validator.ConstraintsValidator;

import com.google.common.collect.MapMaker;

/**
 * This class stores all {@link Router}s instances in the current JVM. The class
 * is a singleton in a given JVM. A router is different from an another
 * depending from the message to route, so the RouterStore stores an instance of
 * a router for each new type of messages created.
 * 
 * @author lpellegr
 */
public class RouterStore {

	private ConcurrentMap<String, Router<? extends RequestResponseMessage<?>, ?>> routers;
	
    private static class LazyInitializer {
    	public static RouterStore instance = new RouterStore();
    }
    
    private RouterStore() {
    	this.routers = 
    		new MapMaker()
    		.concurrencyLevel(8)
    		.softValues()
//    		.maximumSize(100)
    		.makeMap();
    }

	/**
	 * Stores a {@link Router} instance.
	 * 
	 * @param msgClass
	 *            the type of message to which the router is associated.
	 * @param validatorClass
	 *            the validator class used by the router.
	 * 
	 * @return the instance which is already contained by the store or
	 *         {@code null} if there were no existing instance for the specified
	 *         {@code msgClass} and {@code validatorClass}.
	 */
    public Router<? extends RequestResponseMessage<?>, ?> store(
    					Class<?> msgClass, 
						Class<?> validatorClass,
						Router<?, ?> instance) {
    	return this.routers.putIfAbsent(
    				buildKey(msgClass, validatorClass), instance);
    }

    /**
     * Returns an instance of a {@link Router} previously stored for the
     * specified type of message or <code>null</code> if there is no instance
     * available for the specified type of message.
     * 
	 * @param msgClass
	 *            the type of message to which the router is associated.
	 * @param validatorClass
	 *            the validator class used by the router.
     * 
     * @return an instance of a {@link Router} previously stored for the
     *         specified type of message or <code>null</code> if there is no
     *         instance available for the specified type of message.
     */
    public Router<? extends RequestResponseMessage<?>, ?> get(
    						Class<?> msgClass, 
    		  				Class<?> validatorClass) {
        return this.routers.get(
        			buildKey(msgClass, validatorClass));
    }

    private static String buildKey(Class<?> msgClass, 
    							   Class<?> validatorClass) {
    	checkNotNull(msgClass);
    	checkNotNull(validatorClass);

    	if (msgClass.getCanonicalName() == null
    			|| validatorClass.getCanonicalName() == null) {
    		return null;
    	} else {
    		return msgClass.getCanonicalName().concat(
					validatorClass.getCanonicalName());
    	}
    }
    /**
     * Indicates if there is an instance of a {@link Router} available for the
     * specified type of message.
     * 
	 * @param msgClass
	 *            the type of message to which the router is associated.
	 * @param validatorClass
	 *            the validator class used by the router.
	 *            
     * @return <code>true</code> if there an instance of {@link Router}
     *         available, <code>false</code> otherwise.
     */
    public boolean contains(Class<? extends RequestResponseMessage<?>> msgClass, 
    						Class<? extends ConstraintsValidator<?>> validatorClass) {
        return this.routers.get(
        			buildKey(msgClass, validatorClass)) != null;
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
