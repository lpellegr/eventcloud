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
package org.objectweb.proactive.extensions.p2p.structured.messages.can.benchmarks;

import java.io.File;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.logger.JobLogger;

/**
 * This class runs a local experiment to compare different algorithms that can
 * be used when a message needs to be disseminated across all peers. The results
 * are given in files in the directory specified.
 * 
 * The number of peers can be given through the command line parameters.
 * Otherwise, the default parameters are going to be used.
 * 
 * @author jrochas
 */
public class BroadcastsBenchmark {
	
	/** Number of peers in the network (can be changed through
	 * first main method parameter) */
	private static int nbPeers = 25;
	/** Number of dimensions of the CAN (can be changed through
	 * second main method parameter) */
	private static int nbDimensions = 4;

	public static void main(String[] args) {

		if (args.length > 1) {
			try {
				nbPeers = Integer.parseInt(args[0]);
				nbDimensions = Integer.parseInt(args[1]);
			}
			catch (NumberFormatException e) {
				System.out.println("Aborting - Require arguments : " +
						"number of peers in the network + number of dimensions " +
						"of the CAN [-fractal], or require no arguments at all (default values : 25 4)" +
						" + [-fractal] if the CAN must be built according to a fractal approach.");
				System.exit(0);
			}
		}
		File directory = new File(JobLogger.logDirectory);
		if (directory.exists()) {
			File[] files = directory.listFiles();
			for (File file : files) {
				file.delete();
			}
		}

		// Set the number of dimensions
		P2PStructuredProperties.CAN_NB_DIMENSIONS.setValue((byte) nbDimensions);
		// Set the number of peers for the log filenames
		JobLogger.setNbPeers(nbPeers);

		try {
			boolean fractalCAN = false;
			if ((args.length > 0 && args[0].equals("-fractal")) || 
					(args.length > 2 && args[2].equals("-fractal"))) {
				fractalCAN = true;
				JobLogger.logMessage(BroadcastsBenchmark.class.getName(), 
						"********** Building fractal CAN **********");
			}
			else {
				JobLogger.logMessage(BroadcastsBenchmark.class.getName(),
						"********** Building random CAN **********");
			}
			// Building the CAN
			BroadcastInfrastructure broadcastInfrastructure =
					new BroadcastInfrastructure(nbPeers, JobLogger.logDirectory, fractalCAN);
			broadcastInfrastructure.initialize();

			// Running a FloodingBroadcast
			broadcastInfrastructure.measureFloodingBroadcast();
			Thread.sleep(1000);

			// Running an EfficientBroadcast
			broadcastInfrastructure.measureEfficientBroadcast();
			Thread.sleep(1000);

			// Running an OptimalBroadcast
			broadcastInfrastructure.measureOptimalBroadcast();
			Thread.sleep(1000);

			broadcastInfrastructure.terminate();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.exit(0);
	}

}
