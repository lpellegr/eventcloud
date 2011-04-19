package org.objectweb.proactive.extensions.p2p.structured.router;

import org.objectweb.proactive.extensions.p2p.structured.messages.RequestResponseMessage;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.validator.ConstraintsValidator;

/**
 * A router defines how to route a {@link RequestResponseMessage}.
 * 
 * @author lpellegr
 * 
 * @param <T>
 *            the message type to route.
 * @param <K>
 *            the key type used to check at each routing step whether the
 *            constraints are validated or not.
 */
public abstract class Router<T extends RequestResponseMessage<K>, K> {

    /**
     * Constructs a new Router.
     */
    public Router() {
    }

    /**
     * This method is used by a router to make decision as a message enters on a
     * peer. The decision consists in choosing which method from
     * {@link #route(StructuredOverlay, RequestResponseMessage)} or
     * {@link #handle(StructuredOverlay, RequestResponseMessage)} must be
     * called. A correct implementation of <code>makeDecision</code> implies
     * that <code>route</code> and <code>handle</code> methods are both called
     * depending of conditions.
     * 
     * @param overlay
     *            the overlay used to make the decision.
     * @param msg
     *            the message to forward or to handle.
     */
    public abstract void makeDecision(StructuredOverlay overlay, T msg);

    protected void handle(StructuredOverlay overlay, T msg) {
        this.beforeHandle(overlay, msg);
        this.doHandle(overlay, msg);
        this.afterHandle(overlay, msg);
    }

    /**
     * This method is called just before
     * {@link Router#doHandle(StructuredOverlay, RequestResponseMessage)}. You
     * can override it for custom action.
     */
    protected void beforeHandle(StructuredOverlay overlay, T msg) {
        // to be overridden
    }

    /**
     * Handles the specified {@code msg} on the given {@code overlay}.
     * 
     * @param overlay
     *            the {@link StructuredOverlay} used to handle the message.
     * @param msg
     *            the {@link RequestResponseMessage} to handle.
     */
    protected abstract void doHandle(StructuredOverlay overlay, T msg);

    /**
     * This method is called just after
     * {@link Router#doHandle(StructuredOverlay, RequestResponseMessage)}. You
     * can override for custom action.
     */
    protected void afterHandle(StructuredOverlay overlay, T msg) {
        // to be overridden
    }

    /**
     * Makes decision to route the message to an another peer or to handle it by
     * calling {@link #handle(StructuredOverlay, RequestResponseMessage)}. The
     * message must be handled if the constraints are validated by using the
     * {@link ConstraintsValidator}.
     * 
     * @param overlay
     *            the {@link StructuredOverlay} to use in order to make the
     *            decision.
     * @param msg
     *            the {@link RequestResponseMessage} which have to be routed.
     */
    protected abstract void doRoute(StructuredOverlay overlay, T msg);

    /**
     * This method is called just before
     * {@link Router#doRoute(StructuredOverlay, RequestResponseMessage)}. You
     * can override it for custom action.
     */
    protected void beforeRoute(StructuredOverlay overlay, T msg) {
        // to be overridden
    }

    public void route(StructuredOverlay overlay, T msg) {
        this.beforeRoute(overlay, msg);
        this.doRoute(overlay, msg);
        this.afterRoute(overlay, msg);
    }

    /**
     * This method is called just after
     * {@link Router#afterRoute(StructuredOverlay, RequestResponseMessage)}. You
     * can override it for custom action.
     */
    protected void afterRoute(StructuredOverlay overlay, T msg) {
        // to be overridden
    }

    /**
     * Method called when the message is on the peer which validates the
     * constraints.
     * 
     * @param overlay
     *            the overlay which handles the message.
     * 
     * @param msg
     *            the message which has reached the destination.
     */
    protected void onDestinationReached(StructuredOverlay overlay, T msg) {
        // to be overridden
    }

    /**
     * Looks at {@link #equals(Object)} for equality definition.
     */
    @Override
    public int hashCode() {
        if (this.getClass().getCanonicalName() != null) {
            return this.getClass().getCanonicalName().hashCode();
        } else {
            return System.identityHashCode(this);
        }
    }

    /**
     * We assume that two routers are equals if their are not an instance of an
     * anonymous class AND if they use the same canonical name for the router
     * class and the constraints validator.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        String currentCanonicalName = this.getClass().getCanonicalName();
        String objCanonicalName = obj.getClass().getCanonicalName();

        return currentCanonicalName != null
                && currentCanonicalName.equals(objCanonicalName);
    }

}
