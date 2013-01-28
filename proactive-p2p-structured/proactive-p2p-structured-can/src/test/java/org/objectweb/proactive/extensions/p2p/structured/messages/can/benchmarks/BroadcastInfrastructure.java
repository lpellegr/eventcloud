package org.objectweb.proactive.extensions.p2p.structured.messages.can.benchmarks;

import java.io.IOException;

import org.junit.Assert;
import org.objectweb.proactive.extensions.p2p.structured.deployment.CanDeploymentDescriptor;
import org.objectweb.proactive.extensions.p2p.structured.deployment.JunitByClassCanNetworkDeployer;
import org.objectweb.proactive.extensions.p2p.structured.logger.JobLogger;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.Request;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.EfficientBroadcastRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.OptimalBroadcastRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.StringCanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.CoordinateFactory;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;
import org.objectweb.proactive.extensions.p2p.structured.providers.InjectionConstraintsProvider;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;
import org.objectweb.proactive.extensions.p2p.structured.proxies.Proxies;
import org.objectweb.proactive.extensions.p2p.structured.proxies.Proxy;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.DefaultAnycastConstraintsValidator;

/**
 * This class aims to build the necessary random CAN, to choose the 
 * initiator of the broadcast, and to perform different broadcasts in
 * the local experiment implemented in @link{BroadcastBenchmark}.
 * @author jrochas
 */
public class BroadcastInfrastructure extends JunitByClassCanNetworkDeployer {

	private static final String LOG_EFFICIENT = "EfficientBroadcast";
	private static final String LOG_FLOODING = "FloodingBroadcast";
	private static final String LOG_OPTIMAL = "OptimalBroadcast";
	
	//private static final Coordinate<StringElement> constraint = 
			//new Coordinate<StringElement>(new StringElement("j"), null, null);

	protected int nbPeers;
	protected String logDirectory;

	protected Proxy proxy;

	public BroadcastInfrastructure(int nbPeers, String logDirectory) {
		super(
				new CanDeploymentDescriptor<StringElement>(
						new SerializableProvider<StringCanOverlay>() {
							private static final long serialVersionUID = 130L;
							@Override
							public StringCanOverlay get() {
								return new StringCanOverlay();
							}
						}).setInjectionConstraintsProvider(InjectionConstraintsProvider.newFractalInjectionConstraintsProvider()),
						1, nbPeers);
		this.nbPeers = nbPeers;
		this.logDirectory = logDirectory;
	}

	public void setProxy(Proxy proxy) {
		this.proxy = proxy;
	}

	/**
	 * Picks a random peer in the CAN to perform the broadcast.
	 */
	public void initialize() {
		super.setUp();
		this.proxy = Proxies.newProxy(super.getRandomTracker());
	}

	/**
	 * Shuts down the initiator of the broadcast.
	 */
	public void terminate() {
		super.tearDown();
		try {
			this.proxy.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Log the metrics extracted from a FloodingBroadcast run in a file.
	 * @throws InterruptedException
	 */
	public void measureFloodingBroadcast() throws InterruptedException {

		// Sending the broadcast via an AnycastRequest (= flood)
		Request<Coordinate<StringElement>> request = new AnycastRequest<StringElement>(
				new DefaultAnycastConstraintsValidator<StringElement>(
						CoordinateFactory.newStringCoordinate()));
						//constraint));

		// Timestamp the beginning of the broadcast
		JobLogger.recordTime("FloodingBroadcast", request.getId());
		this.proxy.sendv(request);

		// The previous call is asynchronous, so
		// it is preferable to wait a little bit
		Thread.sleep(2000);

		// This is going to log all the interesting metrics for a benchmark
		// in the LOG_FLOODING at the root of the logs directory (see JobLogger)
		int nbPeerReached = JobLogger.logResults(LOG_FLOODING, 
				this.nbPeers, request.getId().toString() + JobLogger.PREFIX + LOG_FLOODING, "1", "");

		// Checking the correctness of the run
		Assert.assertEquals(this.nbPeers, nbPeerReached);
	}
	
	/**
	 * Log the metrics extracted from an EfficientBroadcast run in a file.
	 * @throws InterruptedException
	 */
	public void measureEfficientBroadcast() throws InterruptedException {

		// Sending the broadcast via an EfficientBroadcastRequest
		Request<Coordinate<StringElement>> request = new EfficientBroadcastRequest<StringElement>(
				new DefaultAnycastConstraintsValidator<StringElement>(
						CoordinateFactory.newStringCoordinate()));
						//constraint));
		
		// Timestamp the beginning of the broadcast
		JobLogger.recordTime("EfficientBroadcast", request.getId());
		this.proxy.sendv(request);

		// The previous call is asynchronous, so
		// it is preferable to wait a little bit
		Thread.sleep(2000);

		// This is going to log all the interesting metrics for a benchmark
		// in the LOG_EFFICIENT at the root of the logs directory (see JobLogger)
		int nbPeerReached = JobLogger.logResults(LOG_EFFICIENT, 
				this.nbPeers, request.getId().toString() + JobLogger.PREFIX + LOG_EFFICIENT, "1", "");

		// Checking the correctness of the run
		Assert.assertEquals(this.nbPeers, nbPeerReached);
	}

	/**
	 * Log the metrics extracted from an OptimalBroadcast run in a file.
	 * @throws InterruptedException
	 */
	public void measureOptimalBroadcast() throws InterruptedException {

		// Sending the broadcast via an EfficientBroadcastRequest
		Request<Coordinate<StringElement>> request = new OptimalBroadcastRequest<StringElement>(
				new DefaultAnycastConstraintsValidator<StringElement>(
						CoordinateFactory.newStringCoordinate()));
						//constraint));
						
		// Timestamp the beginning of the broadcast
		JobLogger.recordTime("OptimalBroadcast", request.getId());
		this.proxy.sendv(request);

		// The previous call is asynchronous, so
		// it is preferable to wait a little bit
		Thread.sleep(2000);

		// This is going to log all the interesting metrics for a benchmark
		// in the LOG_OPTIMAL at the root of the logs directory (see JobLogger)
		int nbPeerReached = JobLogger.logResults(LOG_OPTIMAL, 
				this.nbPeers, request.getId().toString() + JobLogger.PREFIX + LOG_OPTIMAL, "1", "");

		// Checking the correctness of the run
		Assert.assertEquals(this.nbPeers, nbPeerReached);
	}


}
