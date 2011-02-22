package org.objectweb.proactive.extensions.p2p.structured.overlay.can;

import org.objectweb.proactive.extensions.p2p.structured.exceptions.ContentAdressableNetworkException;

/**
 * An {@code ZoneException} is thrown by certain methods of the {@link Zone}
 * class.
 * 
 * @author Laurent Pellegrino
 */
public class ZoneException extends ContentAdressableNetworkException {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an {@code ZoneException} with the specified detail message. A
	 * detail message is a {@link String} that describes this particular
	 * exception.
	 * 
	 * @param message
	 *            the detail message.
	 */
	public ZoneException(String message) {
		super(message);
	}

	/**
	 * Constructs an {@code ZoneException} with the specified detail message and
	 * cause.
	 * 
	 * @param message
	 *            the detail message.
	 * @param cause
	 *            the cause.
	 */
	public ZoneException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs an {@code ZoneException} with the specified cause.
	 * 
	 * @param cause
	 *            the cause.
	 */
	public ZoneException(Throwable cause) {
		super(cause);
	}

}
