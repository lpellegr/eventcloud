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
package fr.inria.eventcloud.benchmarks.pubsub;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.Body;
import org.objectweb.proactive.InitActive;
import org.objectweb.proactive.RunActive;
import org.objectweb.proactive.annotation.multiactivity.Compatible;
import org.objectweb.proactive.annotation.multiactivity.DefineGroups;
import org.objectweb.proactive.annotation.multiactivity.DefineRules;
import org.objectweb.proactive.annotation.multiactivity.Group;
import org.objectweb.proactive.annotation.multiactivity.MemberOf;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.MutableInteger;
import org.objectweb.proactive.multiactivity.MultiActiveService;
import org.objectweb.proactive.multiactivity.execution.RequestExecutor;

import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.benchmarks.pubsub.measurements.CumulatedMeasurement;
import fr.inria.eventcloud.benchmarks.pubsub.measurements.SimpleMeasurement;

/**
 * Class in charge of collecting benchmark statistics. Also, it allows to create
 * a synchronization point to wait for the specified number of reports from
 * publishers and susbcribers.
 * 
 * @author lpellegr
 */
@DefineGroups({
        @Group(name = "notify", selfCompatible = false),
        @Group(name = "wait", selfCompatible = false)})
@DefineRules({@Compatible(value = {"notify", "wait"})})
public class BenchmarkStatsCollector implements InitActive, RunActive {

    private final int nbPublishers;

    private final int nbSubscribers;

    private MutableInteger nbReportsReceivedByPublishers = new MutableInteger();

    private MutableInteger nbReportsReceivedBySubscribers =
            new MutableInteger();

    private RequestExecutor requestExecutor;

    private boolean publisherWakeUp = false;

    private boolean subscriberWakeUp = false;

    // measurements

    private long endToEndTerminationTime;

    private Map<SubscriptionId, SimpleMeasurement> outputMeasurements;

    private Map<String, Long> pointToPointEntryMeasurements;

    private Map<SubscriptionId, CumulatedMeasurement> pointToPointExitMeasurements;

    public BenchmarkStatsCollector() {
        this.nbPublishers = 0;
        this.nbSubscribers = 0;
    }

    public BenchmarkStatsCollector(int nbPublishers, int nbSubscribers) {
        this.nbPublishers = nbPublishers;
        this.nbSubscribers = nbSubscribers;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initActivity(Body body) {
        this.outputMeasurements =
                new HashMap<SubscriptionId, SimpleMeasurement>(
                        this.nbSubscribers);

        this.pointToPointEntryMeasurements = new HashMap<String, Long>();

        this.pointToPointExitMeasurements =
                new HashMap<SubscriptionId, CumulatedMeasurement>();
    }

    @MemberOf("notify")
    public long reportEndToEndTermination(long endTime) {
        if (endTime > this.endToEndTerminationTime) {
            this.endToEndTerminationTime = endTime;
        }

        return this.endToEndTerminationTime;
    }

    @MemberOf("notify")
    public boolean reportMeasurements(Map<String, Long> pointToPointEntryMeasurements) {
        this.pointToPointEntryMeasurements.putAll(pointToPointEntryMeasurements);

        this.nbReportsReceivedByPublishers.add(1);

        if (this.nbReportsReceivedByPublishers.getValue() == this.nbPublishers) {
            this.publisherWakeUp = true;

            synchronized (this.nbReportsReceivedByPublishers) {
                this.nbReportsReceivedByPublishers.notifyAll();
            }

            this.requestExecutor.decrementExtraActiveRequestCount(1);
        }

        return true;
    }

    @MemberOf("notify")
    public boolean reportMeasurements(SubscriptionId subscriptionId,
                                      SimpleMeasurement outputMeasurement,
                                      CumulatedMeasurement pointToPointExitMeasurement) {
        boolean result = true;

        result &=
                this.reportOutputMeasurement(subscriptionId, outputMeasurement);
        result &=
                this.reportPointToPointExitMeasurement(
                        subscriptionId, pointToPointExitMeasurement);

        if (result) {
            this.nbReportsReceivedBySubscribers.add(1);

            if (this.nbReportsReceivedBySubscribers.getValue() == this.nbSubscribers) {
                this.subscriberWakeUp = true;

                synchronized (this.nbReportsReceivedBySubscribers) {
                    this.nbReportsReceivedBySubscribers.notifyAll();
                }

                this.requestExecutor.decrementExtraActiveRequestCount(1);
            }
        }

        return result;
    }

    private boolean reportOutputMeasurement(SubscriptionId subscriptionId,
                                            SimpleMeasurement outputMeasurement) {
        return this.outputMeasurements.put(subscriptionId, outputMeasurement) == null;
    }

    private boolean reportPointToPointExitMeasurement(SubscriptionId subscriptionId,
                                                      CumulatedMeasurement pointToPointExitMeasurement) {
        return this.pointToPointExitMeasurements.put(
                subscriptionId, pointToPointExitMeasurement) == null;
    }

    public long getEndToEndTerminationTime() {
        return this.endToEndTerminationTime;
    }

    public SimpleMeasurement getOutputMeasurement(SubscriptionId subscriptionId) {
        return this.outputMeasurements.get(subscriptionId);
    }

    public CumulatedMeasurement getPointToPointExitMeasurement(SubscriptionId subscriptionId) {
        return this.pointToPointExitMeasurements.get(subscriptionId);
    }

    public Map<SubscriptionId, SimpleMeasurement> getOutputMeasurements() {
        return this.outputMeasurements;
    }

    public Map<String, Long> getPointToPointEntryMeasurements() {
        return this.pointToPointEntryMeasurements;
    }

    public Map<SubscriptionId, CumulatedMeasurement> getPointToPointExitMeasurements() {
        return this.pointToPointExitMeasurements;
    }

    @MemberOf("wait")
    public void waitForAllPublisherReports(int timeout) throws TimeoutException {

        if (this.nbReportsReceivedByPublishers.getValue() < this.nbPublishers) {
            synchronized (this.nbReportsReceivedByPublishers) {
                while (this.nbReportsReceivedByPublishers.getValue() < this.nbPublishers) {
                    try {
                        this.nbReportsReceivedByPublishers.wait(timeout);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (!this.publisherWakeUp) {
                        break;
                    }
                }
            }

            this.requestExecutor.decrementExtraActiveRequestCount(1);
        }

        if (!this.publisherWakeUp) {
            throw new TimeoutException("Received only "
                    + this.nbReportsReceivedByPublishers.getValue()
                    + " publisher report(s) whereas " + this.nbPublishers
                    + " were expected.");
        }
    }

    @MemberOf("wait")
    public void waitForAllSubscriberReports(int timeout)
            throws TimeoutException {

        if (this.nbReportsReceivedBySubscribers.getValue() < this.nbSubscribers) {
            synchronized (this.nbReportsReceivedBySubscribers) {
                while (this.nbReportsReceivedBySubscribers.getValue() < this.nbSubscribers) {
                    try {
                        this.nbReportsReceivedBySubscribers.wait(timeout);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    if (!this.subscriberWakeUp) {
                        break;
                    }
                }
            }

            this.requestExecutor.decrementExtraActiveRequestCount(1);
        }

        if (!this.subscriberWakeUp) {
            throw new TimeoutException("Received only "
                    + this.nbReportsReceivedBySubscribers.getValue()
                    + " subscriber report(s) whereas " + this.nbSubscribers
                    + " were expected.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void runActivity(Body body) {
        MultiActiveService service = new MultiActiveService(body);
        this.requestExecutor =
                ((RequestExecutor) service.getServingController());
        service.multiActiveServing();
    }

    /*
     * Simple scenario to test the wait/notify mechanism.
     */
    public static void main(String[] args)
            throws ActiveObjectCreationException, NodeException,
            TimeoutException, IOException {
        BenchmarkStatsCollector collector =
                PAActiveObject.newActive(
                        BenchmarkStatsCollector.class, new Object[] {2, 2});

        final String url = PAActiveObject.getUrl(collector);

        // simulate publishers
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(3000);

                    System.out.println("Publishers report measurements");

                    BenchmarkStatsCollector collector =
                            PAActiveObject.lookupActive(
                                    BenchmarkStatsCollector.class, url);

                    System.out.println("Publisher sends report");
                    collector.reportMeasurements(new HashMap<String, Long>());

                    System.out.println("Publisher sends report");
                    collector.reportMeasurements(new HashMap<String, Long>());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // simulate subscribers
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Thread.sleep(3000);

                    System.out.println("Subscribers report measurements");

                    BenchmarkStatsCollector collector =
                            PAActiveObject.lookupActive(
                                    BenchmarkStatsCollector.class, url);

                    System.out.println("Subscriber sends report");
                    collector.reportMeasurements(
                            new SubscriptionId(), new SimpleMeasurement(),
                            new CumulatedMeasurement());

                    System.out.println("Subscriber sends report");
                    collector.reportMeasurements(
                            new SubscriptionId(), new SimpleMeasurement(),
                            new CumulatedMeasurement());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        collector.waitForAllPublisherReports(10000);
        System.out.println("All publisher reports received");

        collector.waitForAllSubscriberReports(10000);
        System.out.println("All subscriber reports received");

        System.out.println("All reports received");
    }

}
