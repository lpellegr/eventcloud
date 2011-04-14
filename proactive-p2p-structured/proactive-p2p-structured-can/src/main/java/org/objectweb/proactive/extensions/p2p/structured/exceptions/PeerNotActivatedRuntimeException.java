package org.objectweb.proactive.extensions.p2p.structured.exceptions;

import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

/**
 * An exception thrown when a {@link Peer} tries to join a peer which is not
 * activated (i.e. a peer which has created or joined no network).
 * 
 * @author lpellegr
 */
public class PeerNotActivatedRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a {@code PeerNotActivatedRuntimeException} with no
	 * specified detail message.
	 */
	public PeerNotActivatedRuntimeException() {
		super();
	}

	/**
	 * Constructs a {@code PeerNotActivatedRuntimeException} with the
	 * specified detail message.
	 * 
	 * @param message
	 *            the detail message.
	 */
	public PeerNotActivatedRuntimeException(String message) {
		super(message);
	}

	/**
	 * Constructs a {@code PeerNotActivatedRuntimeException} with the
	 * specified detail message and nested exception.
	 * 
	 * @param message
	 *            the detail message.
	 * @param cause
	 *            the nested exception.
	 */
	public PeerNotActivatedRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a {@code PeerNotActivatedRuntimeException} with the
	 * specified detail message and nested exception.
	 * 
	 * @param cause
	 *            the nested exception.
	 */
	public PeerNotActivatedRuntimeException(Throwable cause) {
		super(cause);
	}

}
