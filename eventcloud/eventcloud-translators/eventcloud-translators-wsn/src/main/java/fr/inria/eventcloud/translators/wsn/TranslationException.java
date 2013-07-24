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
package fr.inria.eventcloud.translators.wsn;

/**
 * Exception thrown when a problem occurs during the translation.
 * 
 * @author lpellegr
 */
public class TranslationException extends Exception {

    private static final long serialVersionUID = 160L;

    /**
     * Constructs a {@code TranslationException} with no specified detail
     * message.
     */
    public TranslationException() {
        super();
    }

    /**
     * Constructs a {@code TranslationException} with the specified detail
     * message.
     * 
     * @param message
     *            the detail message.
     */
    public TranslationException(String message) {
        super(message);
    }

    /**
     * Constructs a {@code TranslationException} with the specified detail
     * message and nested exception.
     * 
     * @param message
     *            the detail message.
     * @param cause
     *            the nested exception.
     */
    public TranslationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a {@code TranslationException} with the specified detail
     * message and nested exception.
     * 
     * @param cause
     *            the nested exception.
     */
    public TranslationException(Throwable cause) {
        super(cause);
    }

}
