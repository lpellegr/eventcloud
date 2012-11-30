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

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.extensions.p2p.structured.deployment.CanDeploymentDescriptor;
import org.objectweb.proactive.extensions.p2p.structured.deployment.JunitByClassCanNetworkDeployer;
import org.objectweb.proactive.extensions.p2p.structured.logger.JobLogger;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.EfficientBroadcastRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.StringCanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.CoordinateFactory;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;
import org.objectweb.proactive.extensions.p2p.structured.providers.InjectionConstraintsProvider;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;
import org.objectweb.proactive.extensions.p2p.structured.proxies.Proxies;
import org.objectweb.proactive.extensions.p2p.structured.proxies.Proxy;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.DefaultAnycastConstraintsValidator;

/**
 * This class is suppose to provide measurements of the dissemination of
 * a message across all peers with an efficient algorithm inspired by
 * the "Multicast using Content-Addressable Networks" article. 
 * 
 * Note - Inspired from : 
 * org.objectweb.proactive.extensions.p2p.structured.messages.can.AnycastLookupRequestTest
 * 
 * @author jrochas
 */
public class EfficientBroadcast extends JunitByClassCanNetworkDeployer {

	private static final String LOG_FILENAME = JobLogger.PREFIX + "EfficientBroadcast.log"; 

	private Proxy proxy;

	public EfficientBroadcast() {
		super(
				new CanDeploymentDescriptor<StringElement>(
						new SerializableProvider<StringCanOverlay>() {
							private static final long serialVersionUID = 130L;
							@Override
							public StringCanOverlay get() {
								return new StringCanOverlay();
							}
						}).setInjectionConstraintsProvider(InjectionConstraintsProvider.newFractalInjectionConstraintsProvider()),
				1, JobLogger.NB_PEERS);
	}
	
	public void setProxy(Proxy proxy) {
		this.proxy = proxy;
	}

	@Override
	public void setUp() {
		super.setUp();
		this.proxy = Proxies.newProxy(super.getRandomTracker());
	}

	/**
	 * Log the metrics of an EfficientBroadcast run in LOG_FILENAME.
	 * @throws InterruptedException
	 */
	@Test
	public void measureEfficientBroadcast() throws InterruptedException {

		// Timestamp the beginning of the broadcast
		JobLogger.recordTime(LOG_FILENAME);

		// Sending the broadcast via an EfficientBroadcastRequest
		this.proxy.sendv(new EfficientBroadcastRequest<StringElement>(
				new DefaultAnycastConstraintsValidator<StringElement>(
						CoordinateFactory.newStringCoordinate())));

		// The previous call is asynchronous, so
		// it is preferable to wait a little bit
		Thread.sleep(2000);

		// This is going to log all the interesting metrics for a benchmark
		// in the LOG_FILENAME at the root of the logs directory (see JobLogger)
		int nbPeerReached = JobLogger.logResults(this.getClass().getSimpleName(), 
				JobLogger.NB_PEERS, LOG_FILENAME);

		// Checking the correctness of the run
		Assert.assertEquals(JobLogger.NB_PEERS, nbPeerReached);
	}

	@Override
	public void tearDown() {
		super.tearDown();
		try {
			this.proxy.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
