package org.objectweb.proactive.extensions.p2p.structured.exceptions;

import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;

/**
 * An exception thrown when an error occurs from a {@link StructuredOverlay} of
 * type CAN.
 * 
 * @author Laurent Pellegrino
 */
public class ContentAdressableNetworkException extends StructuredP2PException {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a <code>ContentAdressableNetworkException</code> with no
	 * specified detail message.
	 */
	public ContentAdressableNetworkException() {
		super();
	}

	/**
	 * Constructs a <code>ContentAdressableNetworkException</code> with the
	 * specified detail message.
	 * 
	 * @param message
	 *            the detail message.
	 */
	public ContentAdressableNetworkException(String message) {
		super(message);
	}

	/**
	 * Constructs a <code>ContentAdressableNetworkException</code> with the
	 * specified detail message and nested exception.
	 * 
	 * @param message
	 *            the detail message.
	 * @param cause
	 *            the nested exception.
	 */
	public ContentAdressableNetworkException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a <code>ContentAdressableNetworkException</code> with the
	 * specified detail message and nested exception.
	 * 
	 * @param cause
	 *            the nested exception.
	 */
	public ContentAdressableNetworkException(Throwable cause) {
		super(cause);
	}

}
