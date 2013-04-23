/**
 * Copyright (c) 2011-2013 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package org.objectweb.proactive.extensions.p2p.structured.exceptions;

/**
 * An exception thrown when a problem occurs at the structured p2p framework
 * level.
 * 
 * @author lpellegr
 */
public class StructuredP2PException extends Exception {

    private static final long serialVersionUID = 150L;

    /**
     * Constructs a {@code StructuredP2PException} with no specified detail
     * message.
     */
    public StructuredP2PException() {
        super();
    }

    /**
     * Constructs a {@code StructuredP2PException} with the specified detail
     * message.
     * 
     * @param message
     *            the detail message.
     */
    public StructuredP2PException(String message) {
        super(message);
    }

    /**
     * Constructs a {@code StructuredP2PException} with the specified detail
     * message and nested exception.
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
     * Constructs a {@code StructuredP2PException} with the specified detail
     * message and nested exception.
     * 
     * @param cause
     *            the nested exception.
     */
    public StructuredP2PException(Throwable cause) {
        super(cause);
    }

}
