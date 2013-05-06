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
package org.objectweb.proactive.extensions.p2p.structured.messages.can.broadcast;

import java.io.File;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.logger.JobLogger;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;

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
public class BroadcastTest {
	
	/** Number of peers in the network (can be changed through
	 * first main method parameter) */
	private static int nbPeers = 25;
	/** Number of dimensions of the CAN (can be changed through
	 * second main method parameter) */
	private static int nbDimensions = 4;
	/** The constraint used for the second test */
	private static final Coordinate<StringElement> constraint = 
			new Coordinate<StringElement>(
					new StringElement("j"), new StringElement("j"), null);

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
		clearLogFiles();

		// Set the number of dimensions
		P2PStructuredProperties.CAN_NB_DIMENSIONS.setValue((byte) nbDimensions);
		// Set the number of peers for the log filenames
		JobLogger.setNbPeers(nbPeers);

		try {
			boolean fractalCAN = false;
			boolean uniformCAN = false;
			if ((args.length > 0 && args[0].equals("-fractal")) || 
					(args.length > 2 && args[2].equals("-fractal"))) {
				fractalCAN = true;
				System.out.println("********** Building fractal CAN **********");
			}
			if ((args.length > 0 && args[0].equals("-uniform")) || 
					(args.length > 2 && args[2].equals("-uniform"))) {
				uniformCAN = true;
				System.out.println("********** Building uniform CAN **********");
			}
			else {
				System.out.println("********** Building random CAN **********");
			}
			
			/* ********************************************** */
			/* First test: without any constraint = broadcast */
			/* ********************************************** */
			BroadcastInfrastructure broadcastInfrastructure = 
					new BroadcastInfrastructure(
							nbPeers, JobLogger.getLogDirectory(), 
							null, fractalCAN, uniformCAN);
			broadcastInfrastructure.initialize();

			// Running a FloodingBroadcast
			broadcastInfrastructure.measureFloodingBroadcast();

			// Running an EfficientBroadcast
			broadcastInfrastructure.measureEfficientBroadcast();

			// Running an OptimalBroadcast
			broadcastInfrastructure.measureOptimalBroadcast();
			
			broadcastInfrastructure.assertAllPeersReached();

			broadcastInfrastructure.terminate();
			
			clearLogFiles();
			
			/* ***************************************** */
			/* Second test: with constraints = multicast */
			/* ***************************************** */
			// The dimension of the CAN must be 3 with the constraints
			P2PStructuredProperties.CAN_NB_DIMENSIONS.setValue((byte) 3);
			broadcastInfrastructure = 
					new BroadcastInfrastructure(
							nbPeers, JobLogger.getLogDirectory(), 
							constraint, fractalCAN, uniformCAN);
			broadcastInfrastructure.initialize();

			// Running a FloodingBroadcast
			broadcastInfrastructure.measureFloodingBroadcast();

			// Running an EfficientBroadcast
			broadcastInfrastructure.measureEfficientBroadcast();

			// Running an OptimalBroadcast
			broadcastInfrastructure.measureOptimalBroadcast();
			
			broadcastInfrastructure.assertNbPeersReachedEquals();

			broadcastInfrastructure.terminate();
			
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.exit(0);
	}

	/**
	 * Removes all log files to be sure that
	 * only current logs are taken into account.
	 */
	private static void clearLogFiles() {
		File directory = new File(JobLogger.getLogDirectory());
		if (directory.exists()) {
			File[] files = directory.listFiles();
			for (File file : files) {
				file.delete();
			}
		}
	}

}
