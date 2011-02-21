package org.objectweb.proactive.extensions.p2p.structured.exceptions;

import org.objectweb.proactive.extensions.p2p.structured.api.messages.Request;
import org.objectweb.proactive.extensions.p2p.structured.overlay.QueryManager;

/**
 * An exception thrown when an error occurs while pre-processing a
 * {@link Request} from a {@link QueryManager}.
 * 
 * @author Laurent Pellegrino
 */
public class PreProcessException extends DispatchException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a <code>PreProcessException</code> with no specified detail
     * message.
     */
    public PreProcessException() {
        super();
    }

    /**
     * Constructs a <code>PreProcessException</code> with the specified detail
     * message.
     * 
     * @param message
     *            the detail message.
     */
    public PreProcessException(String message) {
        super(message);
    }

    /**
     * Constructs a <code>PreProcessException</code> with the specified detail
     * message and nested exception.
     * 
     * @param message
     *            the detail message.
     * @param cause
     *            the nested exception.
     */
    public PreProcessException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a <code>PreProcessException</code> with the specified detail
     * message and nested exception.
     * 
     * @param cause
     *            the nested exception.
     */
    public PreProcessException(Throwable cause) {
        super(cause);
    }

}
