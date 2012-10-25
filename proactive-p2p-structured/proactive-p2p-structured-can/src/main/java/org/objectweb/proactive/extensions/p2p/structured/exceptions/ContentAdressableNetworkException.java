/**
 * Copyright (c) 2011-2012 INRIA.
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

import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;

/**
 * An exception thrown when an error occurs from a {@link StructuredOverlay} of
 * type CAN.
 * 
 * @author lpellegr
 */
public class ContentAdressableNetworkException extends StructuredP2PException {

    private static final long serialVersionUID = 130L;

    /**
     * Constructs a {@code ContentAdressableNetworkException} with no specified
     * detail message.
     */
    public ContentAdressableNetworkException() {
        super();
    }

    /**
     * Constructs a {@code ContentAdressableNetworkException} with the specified
     * detail message.
     * 
     * @param message
     *            the detail message.
     */
    public ContentAdressableNetworkException(String message) {
        super(message);
    }

    /**
     * Constructs a {@code ContentAdressableNetworkException} with the specified
     * detail message and nested exception.
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
     * Constructs a {@code ContentAdressableNetworkException} with the specified
     * detail message and nested exception.
     * 
     * @param cause
     *            the nested exception.
     */
    public ContentAdressableNetworkException(Throwable cause) {
        super(cause);
    }

}
