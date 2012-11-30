package org.objectweb.proactive.extensions.p2p.structured.logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;

/**
 * Class used to write information in a log file. Used to check if there any
 * duplicate messages in the broadcast request routing.
 * 
 * @author acraciun
 */
public class JobLogger {

	private static Hashtable<String, Logger> m_loggers = new Hashtable<String, Logger>();
	private static Set<String> hostnames = new HashSet<String>();

	public static final int NB_PEERS = 20;
	public static final String DIRECTORY = "/user/jrochas/home/Documents/jrochas/tmp/logs/";
	public static final String PREFIX = P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue() + "D_" + NB_PEERS + "P_";
	public static final boolean BCAST_DEBUG_MODE = true ;	
	public static final String RETURN = System.getProperty("line.separator");
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

	private static synchronized Logger getJobLogger(String jobName) {
		Logger logger = m_loggers.get(jobName);
		if (logger == null) {
			Layout layout = new PatternLayout("%m ");
			logger = Logger.getLogger(jobName);
			m_loggers.put(jobName, logger);
			logger.setLevel(Level.INFO);
			try {
				File file = new File(DIRECTORY);
				file.mkdirs();
				file = new File(DIRECTORY + jobName);
				FileAppender appender = new FileAppender(layout,
						file.getAbsolutePath(), false);
				logger.removeAllAppenders();
				logger.addAppender(appender);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return logger;
	}

	public static synchronized void logException(String jobName, Exception e) {
		Logger l = getJobLogger(jobName);
		l.info(e.getMessage(), e);
	}

	public static synchronized void logMessage(String jobName, String message) {
		Logger l = getJobLogger(jobName);
		l.info(message);
	}
	
	public static synchronized int logResults(String loggerName, int nbPeers, String filename) {
		LogReader reader = new LogReader(JobLogger.DIRECTORY + filename);
		reader.setAttributes(loggerName, nbPeers);
        int nbPeersReached = reader.scanNprintResults();
        return nbPeersReached;
	}
	
	public static synchronized void recordTime(String filename) {
		Date startingTime = new Date();
		String timestamp = DATE_FORMAT.format(startingTime);
    	JobLogger.logMessage(filename, timestamp);
	}
	
	public static synchronized void appendHostVisited(String hostname) {
		hostnames.add(hostname);
	}
	
	public static synchronized Set<String> retrieveHostsVisited() {
		return hostnames;
	}

}
