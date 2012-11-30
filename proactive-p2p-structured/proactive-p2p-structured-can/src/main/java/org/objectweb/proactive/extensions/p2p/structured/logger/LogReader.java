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

/**
 * Utility class that provides some metrics
 * about a type of broadcast algorithm.
 * @author jrochas
 *
 */
public class LogReader {

	private String filePath;
	private String loggerName;
	private int nbPeers;

	public LogReader(String filename) {
		this.filePath = filename;
	}

	public void setAttributes(String loggerName, int nbPeers) {
		this.loggerName = loggerName;
		this.nbPeers = nbPeers;
	}

	/**
	 * Read the log file and count how many duplicated the broadcast generated
	 */
	public int scanNprintResults() {
		Date startingDate = null; 
		int nbDuplicates = 0;
		int nbPeersReached = 0;
		List<Date> firstReceptionTimestamps = new LinkedList<Date>();
		List<Date> receptionTimestamps = new LinkedList<Date>();
		appendAllLogs();
		try {
			String line;
			Date receptionDate;
			Date firstReceptionDate;
			BufferedReader buff = new BufferedReader(new FileReader(this.filePath));
			// The first line is the timestamp of the broadcast starting
			line = buff.readLine();
			if (line != null) { 
				try {
					startingDate = JobLogger.DATE_FORMAT.parse(line);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			try {
				// The other lines contain the metrics
				while ((line = buff.readLine()) != null) {
					try {
						// Intend to remove one slight bug while logging
						if (line.startsWith(" ")) {
							line = line.substring(1, line.length());
						}
						// Getting the type of the message by parsing its first character
						// 0 : it is a first received message
						// 1 : it is a duplicate
						if (line.length() > 0) {
							String type = line.substring(0, 1);
							String timestamp = line.substring(1, line.length());
							int suspectedDuplicate = Integer.parseInt(type);
							if (suspectedDuplicate == 1) {
								nbDuplicates++;
							}
							if (suspectedDuplicate == 0) {
								nbPeersReached++;
								// Getting the timestamp of the first reception
								firstReceptionDate = JobLogger.DATE_FORMAT.parse(timestamp);
								firstReceptionTimestamps.add(firstReceptionDate);
							}
							// Getting the timestamp of the reception
							receptionDate = JobLogger.DATE_FORMAT.parse(timestamp);
							receptionTimestamps.add(receptionDate);
						}
					}
					catch (NumberFormatException e) {
						break;
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			} 
			finally {
				buff.close();
			}
		} 
		catch (IOException ioe) {
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
		long receptionDelay = maxFirstReceptionDate.getTime() - startingDate.getTime();
		long waveDelay = maxReceptionDate.getTime() - startingDate.getTime();
		System.out.println("Broadcast algorithm used : \t\t\t\t" + this.loggerName);
		System.out.println("Number of peers in the network : \t\t\t" + this.nbPeers); 
		System.out.println("Number of peers reached by the broadcast : \t\t" + nbPeersReached);
		System.out.println("Number of duplicates listed : \t\t\t\t" + nbDuplicates);
		System.out.println("Time needed to reach all the peers (ms) : \t\t" + receptionDelay);
		System.out.println("Time needed for the broadcast wave to die (ms) : \t" + waveDelay);
		return nbPeersReached;
	}

	/**
	 * Gather all the logs produced by each machine reached by the broadcast.
	 */
	public void appendAllLogs() {
		BufferedReader source = null;
		BufferedWriter destination = null;
		String[] path = this.filePath.split("/");
		String simpleFileName = path[path.length - 1].substring(JobLogger.PREFIX.length(), path[path.length - 1].length() - ".log".length()) + "_";
		StringBuilder completeFileName = new StringBuilder();
		for (int i = 0 ; i < path.length - 1 ; i++) {
			completeFileName.append(path[i]);
			completeFileName.append("/");
		}
		completeFileName.append(simpleFileName);
		for (String hostname : JobLogger.retrieveHostsVisited()) {
			try {
				String line;
				source = new BufferedReader(new FileReader(completeFileName + hostname + ".log"));
				destination = new BufferedWriter(new FileWriter(this.filePath, true));
				destination.newLine();
				try {
					while ((line = source.readLine()) != null) {
						if (line != null) { 
							destination.write(line);
							destination.newLine();
						}
					}
				}
				finally {
					source.close();
					destination.close();
				}
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
