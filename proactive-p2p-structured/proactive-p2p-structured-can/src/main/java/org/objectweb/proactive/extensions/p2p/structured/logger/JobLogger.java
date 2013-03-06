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
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;

/**
 * Class used to write information in a log file. Used to check if there is any
 * duplicate messages in the broadcast request routing, and to log other metrics
 * such as reception time of the message.
 * 
 * @author acraciun
 */
public class JobLogger {

    /** Collection of loggers created during the execution */
    private static Hashtable<String, Logger> m_loggers =
            new Hashtable<String, Logger>();

    // These values can be set in a test that uses the JobLogger
    private static int nbPeers;
    public static String logDirectory = System.getProperty("java.io.tmpdir")
            + File.separator + "broadcast_logs" + File.separator;
    public static final boolean bcastDebugMode = true;
    public static String PREFIX;

    public static final String RETURN = System.getProperty("line.separator");
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss:SSS");

    /**
     * Retrieves a job logger with the given name.
     * 
     * @param jobName
     * 
     * @return job logger instance.
     */
    private static synchronized Logger getJobLogger(String jobName) {
        Logger logger = m_loggers.get(jobName);
        if (logger == null) {
            Layout layout = new PatternLayout("%m");
            logger = Logger.getLogger(jobName);
            m_loggers.put(jobName, logger);
            logger.setLevel(Level.INFO);
            try {
                File file = new File(logDirectory);
                file.mkdirs();
                UUID id = UUID.randomUUID();
                file =
                        new File(logDirectory + jobName + id.toString()
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
        PREFIX = P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue() + "D_"
                        + nbPeers + "P_";
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
    public static synchronized void recordTime(String name, UUID id) {
        Date startingDate = new Date();
        String timestamp = DATE_FORMAT.format(startingDate);
        try {
            BufferedWriter logFile =
                    new BufferedWriter(new FileWriter(logDirectory
                            + id.toString() + PREFIX + name + ".logs"));
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
        File directory = new File(logDirectory);
        String[] files = directory.list();
        for (String file : files) {
            try {
                FileReader f = new FileReader(JobLogger.logDirectory + file);
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
