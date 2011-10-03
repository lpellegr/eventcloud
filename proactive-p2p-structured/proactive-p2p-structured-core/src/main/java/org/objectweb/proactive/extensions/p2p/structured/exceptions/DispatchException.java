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

import org.objectweb.proactive.extensions.p2p.structured.messages.request.Request;
import org.objectweb.proactive.extensions.p2p.structured.overlay.RequestResponseManager;

/**
 * An exception thrown when an error occurs while dispatching a {@link Request}
 * from a {@link RequestResponseManager}.
 * 
 * @author lpellegr
 */
public class DispatchException extends StructuredP2PException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a {@code DispatchException} with no specified detail message.
     */
    public DispatchException() {
        super();
    }

    /**
     * Constructs a {@code DispatchException} with the specified detail message.
     * 
     * @param message
     *            the detail message.
     */
    public DispatchException(String message) {
        super(message);
    }

    /**
     * Constructs a {@code DispatchException} with the specified detail message
     * and nested exception.
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
     * Constructs a {@code DispatchException} with the specified detail message
     * and nested exception.
     * 
     * @param cause
     *            the nested exception.
     */
    public DispatchException(Throwable cause) {
        super(cause);
    }

}
