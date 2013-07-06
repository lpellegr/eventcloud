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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.junit.Assert;
import org.objectweb.proactive.extensions.p2p.structured.deployment.CanDeploymentDescriptor;
import org.objectweb.proactive.extensions.p2p.structured.deployment.JunitByClassCanNetworkDeployer;
import org.objectweb.proactive.extensions.p2p.structured.logger.JobLogger;
import org.objectweb.proactive.extensions.p2p.structured.logger.LogReader;
import org.objectweb.proactive.extensions.p2p.structured.messages.Request;
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
 * This class aims to build the necessary random CAN, to choose the initiator of
 * the broadcast, and to perform different broadcasts in the local experiment
 * implemented in @link{BroadcastBenchmark}.
 * 
 * @author jrochas
 */
public class BroadcastInfrastructure extends JunitByClassCanNetworkDeployer {

    private static final String LOG_EFFICIENT = "EfficientBroadcast";
    private static final String LOG_FLOODING = "FloodingBroadcast";
    private static final String LOG_OPTIMAL = "OptimalBroadcast";

    private static int nbPeersReachedFlooding;
    private static int nbPeersReachedEfficient;
    private static int nbPeersReachedOptimal;

    protected int nbPeers;
    protected String logDirectory;
    protected Coordinate<StringElement> constraint;

    protected Proxy proxy;

    public BroadcastInfrastructure(int nbPeers, String logDirectory,
            Coordinate<StringElement> constraint, boolean fractalCAN,
            boolean uniformCAN) {
        super(
                uniformCAN
                        ? new CanDeploymentDescriptor<StringElement>(
                                new SerializableProvider<StringCanOverlay>() {
                                    private static final long serialVersionUID =
                                            140L;

                                    @Override
                                    public StringCanOverlay get() {
                                        return new StringCanOverlay();
                                    }
                                }).setInjectionConstraintsProvider(InjectionConstraintsProvider.newUniformInjectionConstraintsProvider())

                        : fractalCAN
                                ?

                                new CanDeploymentDescriptor<StringElement>(
                                        new SerializableProvider<StringCanOverlay>() {
                                            private static final long serialVersionUID =
                                                    140L;

                                            @Override
                                            public StringCanOverlay get() {
                                                return new StringCanOverlay();
                                            }
                                        }).setInjectionConstraintsProvider(InjectionConstraintsProvider.newFractalInjectionConstraintsProvider())

                                : new CanDeploymentDescriptor<StringElement>(
                                        new SerializableProvider<StringCanOverlay>() {
                                            private static final long serialVersionUID =
                                                    140L;

                                            @Override
                                            public StringCanOverlay get() {
                                                return new StringCanOverlay();
                                            }
                                        }), 1, nbPeers);

        this.nbPeers = nbPeers;
        this.logDirectory = logDirectory;
        this.constraint = constraint;
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
     * 
     * @throws InterruptedException
     */
    public void measureFloodingBroadcast() throws InterruptedException {

        // Sending the broadcast via an AnycastRequest (= flood)
        Request<Coordinate<StringElement>> request;
        if (this.constraint == null) {
            request =
                    new AnycastRequest<StringElement>(
                            new DefaultAnycastConstraintsValidator<StringElement>(
                                    CoordinateFactory.newStringCoordinate()));
        } else {
            request =
                    new AnycastRequest<StringElement>(
                            new DefaultAnycastConstraintsValidator<StringElement>(
                                    this.constraint));
        }
        this.printRequestSize(request);

        // Timestamp the beginning of the broadcast
        JobLogger.recordTime("FloodingBroadcast", request.getId());
        this.proxy.sendv(request);

        // The previous call is asynchronous, so
        // it is preferable to wait a little bit
        Thread.sleep(1000 * this.nbPeers / 2);

        // This is going to log all the interesting metrics for a benchmark
        // in the LOG_FLOODING at the root of the logs directory (see JobLogger)
        nbPeersReachedFlooding =
                LogReader.logResults(
                        LOG_FLOODING, this.nbPeers, request.getId().toString()
                                + JobLogger.getPrefix() + LOG_FLOODING, "0", "");
    }

    /**
     * Log the metrics extracted from an EfficientBroadcast run in a file.
     * 
     * @throws InterruptedException
     */
    public void measureEfficientBroadcast() throws InterruptedException {

        // Sending the broadcast via an EfficientBroadcastRequest
        Request<Coordinate<StringElement>> request;
        if (this.constraint == null) {
            request =
                    new EfficientBroadcastRequest<StringElement>(
                            new DefaultAnycastConstraintsValidator<StringElement>(
                                    CoordinateFactory.newStringCoordinate()));
        } else {
            request =
                    new EfficientBroadcastRequest<StringElement>(
                            new DefaultAnycastConstraintsValidator<StringElement>(
                                    this.constraint));
        }
        this.printRequestSize(request);

        // Timestamp the beginning of the broadcast
        JobLogger.recordTime("EfficientBroadcast", request.getId());
        this.proxy.sendv(request);

        // The previous call is asynchronous, so
        // it is preferable to wait a little bit
        Thread.sleep(1000 * this.nbPeers / 2);

        // This is going to log all the interesting metrics for a benchmark
        // in the LOG_EFFICIENT at the root of the logs directory (see
        // JobLogger)
        nbPeersReachedEfficient =
                LogReader.logResults(
                        LOG_EFFICIENT, this.nbPeers, request.getId().toString()
                                + JobLogger.getPrefix() + LOG_EFFICIENT, "0",
                        "");
    }

    /**
     * Log the metrics extracted from an OptimalBroadcast run in a file.
     * 
     * @throws InterruptedException
     */
    public void measureOptimalBroadcast() throws InterruptedException {

        // Sending the broadcast via an EfficientBroadcastRequest
        Request<Coordinate<StringElement>> request;
        if (this.constraint == null) {
            request =
                    new OptimalBroadcastRequest<StringElement>(
                            new DefaultAnycastConstraintsValidator<StringElement>(
                                    CoordinateFactory.newStringCoordinate()));
        } else {
            request =
                    new OptimalBroadcastRequest<StringElement>(
                            new DefaultAnycastConstraintsValidator<StringElement>(
                                    this.constraint));

        }
        this.printRequestSize(request);

        // Timestamp the beginning of the broadcast
        JobLogger.recordTime("OptimalBroadcast", request.getId());
        this.proxy.sendv(request);

        // The previous call is asynchronous, so
        // it is preferable to wait a little bit
        Thread.sleep(1000 * this.nbPeers / 2);

        // This is going to log all the interesting metrics for a benchmark
        // in the LOG_OPTIMAL at the root of the logs directory (see JobLogger)
        nbPeersReachedOptimal =
                LogReader.logResults(LOG_OPTIMAL, this.nbPeers, request.getId()
                        .toString()
                        + JobLogger.getPrefix() + LOG_OPTIMAL, "0", "");
    }

    /**
     * Displays the request size in bytes. Request size is different for the
     * three kinds of request experimented.
     * 
     * @param request
     */
    public void printRequestSize(Request<Coordinate<StringElement>> request) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream oos;
        try {
            oos = new ObjectOutputStream(bout);
            oos.writeObject(request);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("The size of an " + request.getClass() + " is : "
                + bout.size() + " bytes.");
    }

    /**
     * Checks if the number of peers reached by the multicast is the same for
     * all the experimented broadcast.
     */
    public void assertNbPeersReachedEquals() {
        Assert.assertTrue(nbPeersReachedFlooding != 0
                && nbPeersReachedEfficient != 0 && nbPeersReachedOptimal != 0
                && nbPeersReachedFlooding == nbPeersReachedOptimal
                && nbPeersReachedEfficient == nbPeersReachedOptimal);
    }

    /**
     * Checks if all the peers received the message.
     */
    public void assertAllPeersReached() {
        Assert.assertTrue(nbPeersReachedFlooding == this.nbPeers
                && nbPeersReachedEfficient == this.nbPeers
                && nbPeersReachedOptimal == this.nbPeers);
    }

}
