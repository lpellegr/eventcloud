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
package fr.inria.eventcloud.translators.wsn;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author lpellegr
 * 
 * @param <IN>
 * @param <OUT>
 */
public abstract class Translator<IN, OUT> {

    private static Logger log = LoggerFactory.getLogger(Translator.class);

    public abstract OUT translate(IN in) throws TranslationException;

    protected static void logAndThrowTranslationException(String msg)
            throws TranslationException {
        log.error(msg);
        throw new TranslationException(msg);
    }

}
