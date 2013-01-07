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

import org.junit.Assert;
import org.objectweb.proactive.extensions.p2p.structured.logger.JobLogger;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.Request;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.CoordinateFactory;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.DefaultAnycastConstraintsValidator;


/**
 * This class is suppose to provide measurements of the dissemination of
 * a message across all peers with a basic flooding algorithm.
 * 
 * Note - Inspired from : 
 * org.objectweb.proactive.extensions.p2p.structured.messages.can.AnycastLookupRequestTest
 * 
 * @author jrochas
 */
public class FloodingBroadcast extends AbstractBroadcast {

    private static final String LOG_FILENAME = "FloodingBroadcast";

    public FloodingBroadcast(int nbPeers, String logDirectory) {
        super(nbPeers, logDirectory);
    }
    
    public void measureFloodingBroadcast() throws InterruptedException {
    	
    	// Sending the broadcast via an AnycastRequest (= flood)
    	Request<Coordinate<StringElement>> request = new AnycastRequest<StringElement>(
				new DefaultAnycastConstraintsValidator<StringElement>(
						CoordinateFactory.newStringCoordinate()));
    	// Timestamp the beginning of the broadcast
    	JobLogger.recordTime("FloodingBroadcast", request.getId());
        this.proxy.sendv(request);

        // The previous call is asynchronous, so
     	// it is preferable to wait a little bit
        Thread.sleep(2000);
        
        // This is going to log all the interesting metrics for a benchmark
     	// in the LOG_FILENAME at the root of the logs directory (see JobLogger)
        int nbPeerReached = JobLogger.logResults(this.getClass().getSimpleName(), 
        		this.nbPeers, request.getId().toString() + JobLogger.PREFIX + LOG_FILENAME, "1", "");
        
        // Checking the correctness of the run
     	Assert.assertEquals(this.nbPeers, nbPeerReached);
    }
}
