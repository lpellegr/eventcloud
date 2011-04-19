package org.objectweb.proactive.extensions.p2p.structured.messages;

import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;

/**
 * This interface is assumed to be implemented by all
 * {@link RequestResponseMessage} in order to add router features.
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
    public abstract Router<? extends RequestResponseMessage<K>, K> getRouter();

    /**
     * Route the {@link RequestResponseMessage} to the correct {@link Peer}. If
     * the current peer contains the key to reach, the query is handled and a
     * response is routed to the sender.
     * 
     * @param overlay
     *            the overlay used in order to route the request.
     */
    public abstract void route(StructuredOverlay overlay);

}
