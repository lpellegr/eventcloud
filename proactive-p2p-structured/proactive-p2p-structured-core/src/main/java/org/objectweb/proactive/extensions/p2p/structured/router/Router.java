package org.objectweb.proactive.extensions.p2p.structured.router;

import org.objectweb.proactive.extensions.p2p.structured.messages.RequestReplyMessage;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.validator.ConstraintsValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A router defines how to route a {@link RequestReplyMessage}. It provides several 
 * methods to customize the routing steps.
 * 
 * @author lpellegr
 * 
 * @param <T>
 *            the type of message to route.
 * @param <K>
 *            the type of the key used to check if the constraints are
 *            validated.
 */
public abstract class Router<T extends RequestReplyMessage<K>, K> {

    private static final transient Logger logger = LoggerFactory.getLogger(Router.class);

    protected ConstraintsValidator<K> constraintsValidator;

    /**
     * Initialize a router with the specified <code>validator</code>.
     * 
     * @param validator
     *            the {@link ConstraintsValidator} to use in order to check the
     *            constraints.
     */
    public Router(ConstraintsValidator<K> validator) {
        this.constraintsValidator = validator;
    }

	/**
	 * This method is used by a router to make decision as a message enters on a
	 * peer. The decision consists in choosing which method from
	 * {@link #route(StructuredOverlay, RequestReplyMessage)} or
	 * {@link #handle(StructuredOverlay, RequestReplyMessage)} must be called. A
	 * correct implementation of <code>makeDecision</code> implies that
	 * <code>route</code> and <code>handle</code> methods are both called
	 * depending of conditions.
	 * 
	 * @param overlay
	 *            the overlay used to make the decision.
	 * @param msg
	 *            the message to forward or to handle.
	 */
    public abstract void makeDecision(StructuredOverlay overlay, T msg); 
    
    protected void handle(StructuredOverlay overlay, T msg) {
        this.preHandle(overlay, msg);
        this.performHandle(overlay, msg);
        this.postHandle(overlay, msg);
    }

    public void route(StructuredOverlay overlay, T msg) {
        this.preRoute(overlay, msg);
        this.performRoute(overlay, msg);
        this.postRoute(overlay, msg);
    }

    /**
     * This method is called just before
     * {@link Router#performHandle(StructuredOverlay, RequestReplyMessage)}. You
     * can override it for custom action.
     */
    protected void preHandle(StructuredOverlay overlay, T msg) {
        logger.trace("preHandle()");
    }

    /**
     * This method is called just before
     * {@link Router#performRoute(StructuredOverlay, RequestReplyMessage)}. You
     * can override it for custom action.
     */
    protected void preRoute(StructuredOverlay overlay, T msg) {
        logger.trace("preRoute()");
    }

    /**
     * This method is called just after
     * {@link Router#performHandle(StructuredOverlay, RequestReplyMessage)}. You
     * can override for custom action.
     */
    protected void postHandle(StructuredOverlay overlay, T msg) {
        logger.trace("postHandle()");
    }

    /**
     * This method is called just after
     * {@link Router#postRoute(StructuredOverlay, RequestReplyMessage)}. You can
     * override it for custom action.
     */
    protected void postRoute(StructuredOverlay overlay, T msg) {
        logger.trace("postRoute()");
    }

    protected void onDestinationReached(StructuredOverlay overlay, T msg) {
        logger.trace("onDestinationReached()");
    }

    /**
     * Handles the specified <code>msg</msg> on the given <code>overlay</code>.
     * 
     * @param overlay
     *            the {@link StructuredOverlay} used to handle the message.
     * @param msg
     *            the {@link RequestReplyMessage} to handle.
     */
    protected abstract void performHandle(StructuredOverlay overlay, T msg);

    /**
     * Makes decision to route the message to an another peer or to handle it by
     * calling {@link #handle(StructuredOverlay, RequestReplyMessage)}. The
     * message must be handled if the constraints are validated by using the
     * {@link ConstraintsValidator}.
     * 
     * @param overlay
     *            the {@link StructuredOverlay} to use in order to make the
     *            decision.
     * @param msg
     *            the {@link RequestReplyMessage} which have to be routed.
     */
    protected abstract void performRoute(StructuredOverlay overlay, T msg);

	/**
	 * Indicates if the specified {@code overlay} validates the constraints
	 * which are denoted by {@code key}.
	 * 
	 * @param overlay
	 *            the overlay on which the constraints are checked.
	 * @param key
	 *            the constraints to check.
	 * 
	 * @return <code>true</code> if the constraints are validated,
	 *         <code>false</code> otherwise.
	 */
    protected boolean validatesKeyConstraints(StructuredOverlay overlay, K key) {
        return this.constraintsValidator.validatesKeyConstraints(overlay, key);
    }

}
