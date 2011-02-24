package org.objectweb.proactive.extensions.p2p.structured.exceptions;

import org.objectweb.proactive.extensions.p2p.structured.messages.request.AbstractRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.RequestReplyManager;

/**
 * An exception thrown when an error occurs while dispatching a {@link AbstractRequest}
 * from a {@link RequestReplyManager}.
 * 
 * @author lpellegr
 */
public class DispatchException extends StructuredP2PException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a <code>DispatchException</code> with no specified detail
     * message.
     */
    public DispatchException() {
        super();
    }

    /**
     * Constructs a <code>DispatchException</code> with the specified detail
     * message.
     * 
     * @param message
     *            the detail message.
     */
    public DispatchException(String message) {
        super(message);
    }

    /**
     * Constructs a <code>DispatchException</code> with the specified detail
     * message and nested exception.
     * 
     * @param message
     *            the detail message.
     * @param cause
     *            the nested exception.
     */
    public DispatchException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a <code>DispatchException</code> with the specified detail
     * message and nested exception.
     * 
     * @param cause
     *            the nested exception.
     */
    public DispatchException(Throwable cause) {
        super(cause);
    }

}
