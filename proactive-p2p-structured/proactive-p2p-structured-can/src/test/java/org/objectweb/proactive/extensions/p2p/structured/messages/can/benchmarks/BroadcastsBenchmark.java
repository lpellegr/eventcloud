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
package org.objectweb.proactive.extensions.p2p.structured.messages.can.benchmarks;

/**
 * This class aims to compare different algorithm that
 * can be used when a message needs to be disseminated
 * accross all peers (i.e. with no particular constraints).
 * 
 * @author jrochas
 */
public class BroadcastsBenchmark {

	public static void main (String[] args) throws InterruptedException {

		try {
			// Running an EfficientBroadcast
			FloodingBroadcast floodingBcast = new FloodingBroadcast();
			floodingBcast.setUp();
			floodingBcast.measureFloodingBroadcast();
			floodingBcast.tearDown();

			Thread.sleep(2000);
			
			// Running an EfficientBroadcast
			EfficientBroadcast efficientBcast = new EfficientBroadcast();
			efficientBcast.setUp();
			efficientBcast.measureEfficientBroadcast();
			efficientBcast.tearDown();
			
			Thread.sleep(2000);
			
			// Running an OptimalBroadcast
			OptimalBroadcast optimalBcast = new OptimalBroadcast();
			optimalBcast.setUp();
			optimalBcast.measureOptimalBroadcast();
			optimalBcast.tearDown();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.exit(0);
	}

}
