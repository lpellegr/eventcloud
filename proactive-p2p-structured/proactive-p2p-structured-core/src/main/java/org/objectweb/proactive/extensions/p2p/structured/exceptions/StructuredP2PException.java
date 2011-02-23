package org.objectweb.proactive.extensions.p2p.structured.exceptions;

/**
 * An exception thrown when a problem occurs at the structured p2p framework
 * level.
 * 
 * @author lpellegr
 */
public class StructuredP2PException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a <code>StructuredP2PException</code> with no specified detail
     * message.
     */
    public StructuredP2PException() {
        super();
    }

    /**
     * Constructs a <code>StructuredP2PException</code> with the specified
     * detail message.
     * 
     * @param message
     *            the detail message.
     */
    public StructuredP2PException(String message) {
        super(message);
    }

    /**
     * Constructs a <code>StructuredP2PException</code> with the specified
     * detail message and nested exception.
     * 
     * @param message
     *            the detail message.
     * @param cause
     *            the nested exception.
     */
    public StructuredP2PException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a <code>StructuredP2PException</code> with the specified
     * detail message and nested exception.
     * 
     * @param cause
     *            the nested exception.
     */
    public StructuredP2PException(Throwable cause) {
        super(cause);
    }

}
