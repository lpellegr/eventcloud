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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Utility class that provides some metrics about a type of broadcast algorithm.
 * 
 * @author jrochas
 */
public class LogReader {

    private static final String SEPARATOR = "\t";

    private static String filePath;
    private static String loggerName;
    private static int nbPeers;

    public static String getSeparator() {
        return SEPARATOR;
    }

    private static void setAttributes(String _loggerName, int _nbPeers,
                                      String _filePath) {
        loggerName = _loggerName;
        nbPeers = _nbPeers;
        filePath = _filePath;
    }

    /**
     * Read the log file and count how many duplicated the broadcast generated
     */
    private static int scanNprintResults(String name, String runNumber,
                                         String id) {
        Date startingDate = null;
        int nbDuplicates = 0;
        int nbPeersReached = 0;
        int nbPeersUsedAsRouters = 0;
        List<Date> firstReceptionTimestamps = new LinkedList<Date>();
        List<Date> receptionTimestamps = new LinkedList<Date>();
        appendAllLogs(name, id);
        try {
            String line;
            Date receptionDate;
            Date firstReceptionDate;
            BufferedReader buff =
                    new BufferedReader(new FileReader(filePath + ".logs"));
            try {
                if ((line = buff.readLine()) != null) {
                    try {
                        startingDate = JobLogger.getDateFormat().parse(line);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                // The other lines contain the metrics
                while ((line = buff.readLine()) != null) {
                    try {
                        // Intend to remove one slight bug while logging
                        if (line.startsWith(" ")) {
                            line = line.substring(1, line.length());
                        }
                        // Getting the type of the message by parsing its first
                        // character
                        // 0 : it is a first received message
                        // 1 : it is a duplicate
                        if (line.length() > 0) {
                            String[] lineArray = line.split(" ");
                            String type = lineArray[0];
                            String timestamp =
                                    lineArray[1] + " " + lineArray[2];
                            int suspectedDuplicate = Integer.parseInt(type);
                            if (suspectedDuplicate == 1) {
                                nbDuplicates++;
                            }
                            if (suspectedDuplicate == 0) {
                                nbPeersReached++;
                                // Getting the timestamp of the first reception
                                firstReceptionDate =
                                        JobLogger.getDateFormat().parse(
                                                timestamp);
                                firstReceptionTimestamps.add(firstReceptionDate);
                            }
                            if (suspectedDuplicate == -1) {
                                nbPeersUsedAsRouters++;
                            }
                            // Getting the timestamp of the reception
                            receptionDate =
                                    JobLogger.getDateFormat().parse(timestamp);
                            receptionTimestamps.add(receptionDate);
                        }
                    } catch (NumberFormatException e) {
                        break;
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                buff.close();
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
        // Finding the timestamp of the last broadcast message received
        // (first reception -> reception delay)
        Date maxFirstReceptionDate = startingDate;
        for (Date date : firstReceptionTimestamps) {
            if (date.after(maxFirstReceptionDate)) {
                maxFirstReceptionDate = date;
            }
        }
        // Finding the timestamp of the last message received
        // (all receptions -> wave delay)
        Date maxReceptionDate = startingDate;
        for (Date date : receptionTimestamps) {
            if (date.after(maxReceptionDate)) {
                maxReceptionDate = date;
            }
        }
        // Deducing the broadcast delays
        long receptionDelay =
                maxFirstReceptionDate.getTime() - startingDate.getTime();
        long waveDelay = maxReceptionDate.getTime() - startingDate.getTime();
        logRunData(
                nbPeersUsedAsRouters, nbDuplicates, nbPeersReached,
                receptionDelay, waveDelay, runNumber);
        return nbPeersReached;
    }

    /**
     * Gather all the logs produced by each machine reached by the broadcast.
     */
    private static void appendAllLogs(String name, String id) {
        BufferedReader source = null;
        BufferedWriter destination = null;
        String[] path = filePath.split("/");
        StringBuilder completeFileName = new StringBuilder();
        for (int i = 0; i < path.length - 1; i++) {
            completeFileName.append(path[i]);
            completeFileName.append("/");
        }
        Set<String> hostnames = JobLogger.retrieveHostsVisited(name, id);
        try {

            for (String hostname : hostnames) {
                if (hostname != null && !hostname.equals("")
                        && !hostname.equals(" ")) {
                    String line;
                    source =
                            new BufferedReader(new FileReader(completeFileName
                                    + hostname));
                    destination =
                            new BufferedWriter(new FileWriter(filePath
                                    + ".logs", true));
                    try {
                        line = source.readLine();
                        while (line != null) {
                            destination.write(line);
                            destination.newLine();
                            line = source.readLine();
                        }
                    } finally {
                        source.close();
                        destination.close();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes the extracted metrics in a file.
     * 
     * @param nbDuplicates
     * @param nbPeersReached
     * @param receptionDelay
     * @param waveDelay
     * @param runNumber
     */
    private static void logRunData(int nbPeersUsedAsRouter, int nbDuplicates,
                                   int nbPeersReached, long receptionDelay,
                                   long waveDelay, String runNumber) {
        try {
            BufferedWriter dataWriter =
                    new BufferedWriter(new FileWriter(
                            JobLogger.getLogDirectory() + JobLogger.getPrefix()
                                    + loggerName + "_" + runNumber + ".data"));
            dataWriter.write("peers" + SEPARATOR + nbPeersReached);
            dataWriter.newLine();
            dataWriter.write("duplicates" + SEPARATOR + nbDuplicates);
            dataWriter.newLine();
            dataWriter.write("reception" + SEPARATOR + receptionDelay);
            dataWriter.newLine();
            dataWriter.write("wave" + SEPARATOR + waveDelay);
            dataWriter.newLine();
            dataWriter.close();
            System.out.println("Broadcast algorithm used : \t\t\t\t"
                    + loggerName);
            System.out.println("Number of peers in the network : \t\t\t"
                    + nbPeers);
            System.out.println("Number of peers reached by the broadcast : \t\t"
                    + nbPeersReached);
            System.out.println("Number of duplicates listed : \t\t\t\t"
                    + nbDuplicates);
            System.out.println("Number of peers that have only been used as routers : \t"
                    + nbPeersUsedAsRouter);
            System.out.println("Time needed to reach all the peers (ms) : \t\t"
                    + receptionDelay);
            System.out.println("Time needed for the broadcast wave to die (ms) : \t"
                    + waveDelay);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Logs the results of a broadcast execution.
     * 
     * @param loggerName
     * @param nbPeers
     * @param filename
     * @param runNumber
     * @param id
     * 
     * @return the number of peers reached.
     */
    public static int logResults(String loggerName, int nbPeers,
                                 String filename, String runNumber, String id) {
        setAttributes(loggerName, nbPeers, JobLogger.getLogDirectory()
                + filename);
        int nbPeersReached = scanNprintResults(loggerName, runNumber, id);
        return nbPeersReached;
    }
}
