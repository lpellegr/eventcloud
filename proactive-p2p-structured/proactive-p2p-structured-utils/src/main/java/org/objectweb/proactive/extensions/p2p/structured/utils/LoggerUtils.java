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
package org.objectweb.proactive.extensions.p2p.structured.utils;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;

/**
 * Some utility methods to simplify some actions on loggers.
 * 
 * @author lpellegr
 */
public class LoggerUtils {

    public static void disableLog4jLogger() {
        System.setProperty("log4j.configuration", "");
        Logger.getRootLogger().setLevel(Level.OFF);
    }

    public static void disableLogbackLogger() {
        LoggerContext context =
                (LoggerContext) LoggerFactory.getILoggerFactory();
        context.reset();
    }

    public static void disableLoggers() {
        disableLog4jLogger();
        disableLogbackLogger();
    }

}
