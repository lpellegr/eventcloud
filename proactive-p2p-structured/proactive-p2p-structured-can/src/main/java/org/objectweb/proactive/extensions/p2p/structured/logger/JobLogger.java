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
package org.objectweb.proactive.extensions.p2p.structured.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.objectweb.proactive.extensions.p2p.structured.messages.MessageId;

/**
 * Class used to write information in a log file. Used to check if there is any
 * duplicate messages in the broadcast request routing, and to log other metrics
 * such as reception time of the message.
 * 
 * @author acraciun
 */
public class JobLogger {

    /** The full path toward the directory where the log files will be found */
    private static final String LOG_DIRECTORY =
            System.getProperty("java.io.tmpdir") + File.separator
                    + "broadcast_logs" + File.separator;
    /** If true, trigger the code to monitor messages when messages are received */
    private static final boolean BCAST_DEBUG = false;
    /**
     * Determine if the logs should be written on the console as well. Warning :
     * logging to the console might slow down the execution
     */
    private static final boolean LOG_TO_CONSOLE = false;
    /** The separator between two lines in the log files */
    private static final String LINE_SEPARATOR =
            System.getProperty("line.separator");
    /** The format of the dates that will be written in the log files */
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss:SSS");

    /** Collection of already known loggers during the execution */
    private static Hashtable<String, Logger> m_loggers =
            new Hashtable<String, Logger>();
    /** Number of peers in the created network */
    private static int nbPeers;
    /** Prefix of the logs' filenames */
    private static String prefix;

    public static String getLogDirectory() {
        return LOG_DIRECTORY;
    }

    public static boolean isBcastDebugEnabled() {
        return BCAST_DEBUG;
    }

    public static boolean getLogToConsole() {
        return LOG_TO_CONSOLE;
    }

    public static String getLineSeparator() {
        return LINE_SEPARATOR;
    }

    public static SimpleDateFormat getDateFormat() {
        return DATE_FORMAT;
    }

    public static String getPrefix() {
        return prefix;
    }

    /**
     * Retrieves a job logger with the given name.
     * 
     * @param jobName
     * 
     * @return job logger instance.
     */
    private static synchronized Logger getJobLogger(String jobName) {
        if (!LOG_TO_CONSOLE) {
            Logger.getRootLogger().removeAllAppenders();
        }
        Logger logger = m_loggers.get(jobName);
        if (logger == null) {
            Layout layout = new PatternLayout("%m");
            logger = Logger.getLogger(jobName);
            m_loggers.put(jobName, logger);
            logger.setLevel(Level.INFO);
            try {
                File file = new File(LOG_DIRECTORY);
                file.mkdirs();
                UUID id = UUID.randomUUID();
                file =
                        new File(LOG_DIRECTORY + jobName + id.toString()
                                + ".log");
                FileAppender appender =
                        new FileAppender(layout, file.getAbsolutePath(), false);
                appender.setImmediateFlush(true);
                appender.activateOptions();
                logger.removeAllAppenders();
                logger.addAppender(appender);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return logger;
    }

    /**
     * Sets the number of peers currently in the network (useful for file names)
     * 
     * @param newNbPeers
     */
    public static void setNbPeers(int newNbPeers) {
        nbPeers = newNbPeers;
        prefix = nbPeers + "P_";
    }

    /**
     * Logs an exception.
     * 
     * @param jobName
     * @param e
     */
    public static synchronized void logException(String jobName, Exception e) {
        Logger l = getJobLogger(jobName);
        l.info(e.getMessage(), e);
    }

    /**
     * Logs a message.
     * 
     * @param jobName
     * @param message
     */
    public static synchronized void logMessage(String jobName, String message) {
        Logger l = getJobLogger(jobName);
        l.info(message);
    }

    /**
     * Timestamps the beginning of a broadcast execution.
     * 
     * @param name
     * @param id
     */
    public static synchronized void recordTime(String name, MessageId id) {
        Date startingDate = new Date();
        String timestamp = DATE_FORMAT.format(startingDate);
        try {
            BufferedWriter logFile =
                    new BufferedWriter(new FileWriter(LOG_DIRECTORY
                            + id.toString() + prefix + name + ".logs"));
            logFile.write(timestamp);
            logFile.newLine();
            logFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Lists the hosts where the broadcast was received.
     * 
     * @param name
     * @param id
     * 
     * @return the hosts visited.
     */
    public static synchronized Set<String> retrieveHostsVisited(String name,
                                                                String id) {
        HashSet<String> hostnames = new HashSet<String>();
        File directory = new File(LOG_DIRECTORY);
        String[] files = directory.list();
        for (String file : files) {
            try {
                FileReader f = new FileReader(JobLogger.LOG_DIRECTORY + file);
                f.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (file.endsWith(".log") && file.contains(name)
                    && file.contains(id)) {
                hostnames.add(file);
            }
        }
        return hostnames;
    }
}
