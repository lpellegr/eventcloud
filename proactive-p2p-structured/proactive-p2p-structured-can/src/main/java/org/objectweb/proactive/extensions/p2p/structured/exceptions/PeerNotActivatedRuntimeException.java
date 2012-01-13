/**
 * Copyright (c) 2011 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
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
     * Constructs a {@code PeerNotActivatedRuntimeException} with no specified
     * detail message.
     */
    public PeerNotActivatedRuntimeException() {
        super();
    }

    /**
     * Constructs a {@code PeerNotActivatedRuntimeException} with the specified
     * detail message.
     * 
     * @param message
     *            the detail message.
     */
    public PeerNotActivatedRuntimeException(String message) {
        super(message);
    }

    /**
     * Constructs a {@code PeerNotActivatedRuntimeException} with the specified
     * detail message and nested exception.
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
     * Constructs a {@code PeerNotActivatedRuntimeException} with the specified
     * detail message and nested exception.
     * 
     * @param cause
     *            the nested exception.
     */
    public PeerNotActivatedRuntimeException(Throwable cause) {
        super(cause);
    }

}
