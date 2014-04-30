/**
 * Copyright (c) 2011-2014 INRIA.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class to ease implementation of translators.
 * 
 * @author lpellegr
 * 
 * @param <IN>
 *            the class of the object to translate.
 * @param <OUT>
 *            the class of the object after translation.
 */
public abstract class Translator<IN, OUT> {

    private static Logger log = LoggerFactory.getLogger(Translator.class);

    /**
     * Translates the specified object.
     * 
     * @param in
     *            the object to translate.
     * 
     * @return the translated object.
     * 
     * @throws TranslationException
     *             if an error occurs during the translation.
     */
    public abstract OUT translate(IN in) throws TranslationException;

    protected static void logAndThrowTranslationException(String msg)
            throws TranslationException {
        log.error(msg);
        throw new TranslationException(msg);
    }

}
