package org.objectweb.proactive.extensions.p2p.structured.exceptions;

import org.objectweb.proactive.extensions.p2p.structured.api.messages.Request;
import org.objectweb.proactive.extensions.p2p.structured.overlay.QueryManager;

/**
 * An exception thrown when an error occurs while post-processing a
 * {@link Request} from a {@link QueryManager}.
 * 
 * @author lpellegr
 */
public class PostProcessException extends DispatchException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a <code>PostProcessException</code> with no specified detail
     * message.
     */
    public PostProcessException() {
        super();
    }

    /**
     * Constructs a <code>PostProcessException</code> with the specified detail
     * message.
     * 
     * @param message
     *            the detail message.
     */
    public PostProcessException(String message) {
        super(message);
    }

    /**
     * Constructs a <code>PostProcessException</code> with the specified detail
     * message and nested exception.
     * 
     * @param message
     *            the detail message.
     * @param cause
     *            the nested exception.
     */
    public PostProcessException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a <code>PostProcessException</code> with the specified detail
     * message and nested exception.
     * 
     * @param cause
     *            the nested exception.
     */
    public PostProcessException(Throwable cause) {
        super(cause);
    }

}
