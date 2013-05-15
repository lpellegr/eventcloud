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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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

    private final int nbQuadruplesPublished;

    private final int nbSubscriptionsPerSubscriber;

    private MutableInteger nbReportsReceivedByPublishers;

    private MutableInteger nbReportsReceivedBySubscribers;

    private RequestExecutor requestExecutor;

    // waiting states

    private boolean allPublisherReportsReceived = false;

    private boolean allSubscriberReportsReceived = false;

    private AtomicBoolean allPublishedQuadruplesStored = new AtomicBoolean(
            false);

    // measurements

    private Map<SubscriptionId, Long> endToEndTerminationTimes;

    private Map<SubscriptionId, SimpleMeasurement> outputMeasurements;

    private Map<String, Long> pointToPointEntryMeasurements;

    private Map<SubscriptionId, CumulatedMeasurement> pointToPointExitMeasurements;

    private ConcurrentMap<UUID, AtomicInteger> nbQuadrupleStoredPerPeer;

    public BenchmarkStatsCollector() {
        this.nbPublishers = 0;
        this.nbSubscribers = 0;
        this.nbSubscriptionsPerSubscriber = 0;
        this.nbQuadruplesPublished = 0;
    }

    public BenchmarkStatsCollector(int nbPublishers, int nbSubscribers,
            int nbSubscriptionsPerSubscriber, int nbQuadruplesExpected) {
        this.nbPublishers = nbPublishers;
        this.nbSubscribers = nbSubscribers;
        this.nbSubscriptionsPerSubscriber = nbSubscriptionsPerSubscriber;
        this.nbQuadruplesPublished = nbQuadruplesExpected;
    }

    public boolean clear() {
        // if field not null then initActivity executed
        if (this.endToEndTerminationTimes != null) {
            this.endToEndTerminationTimes.clear();
            this.outputMeasurements.clear();
            this.pointToPointEntryMeasurements.clear();
            this.pointToPointExitMeasurements.clear();
            this.nbQuadrupleStoredPerPeer.clear();

            this.nbReportsReceivedByPublishers.setValue(0);
            this.nbReportsReceivedBySubscribers.setValue(0);

            this.allPublishedQuadruplesStored.set(false);
            this.allPublisherReportsReceived = false;
            this.allSubscriberReportsReceived = false;
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initActivity(Body body) {
        this.endToEndTerminationTimes =
                new HashMap<SubscriptionId, Long>(this.nbSubscribers
                        * this.nbSubscriptionsPerSubscriber);

        this.outputMeasurements =
                new HashMap<SubscriptionId, SimpleMeasurement>(
                        this.nbSubscribers * this.nbSubscriptionsPerSubscriber);

        this.pointToPointEntryMeasurements = new HashMap<String, Long>();

        this.pointToPointExitMeasurements =
                new HashMap<SubscriptionId, CumulatedMeasurement>();

        this.nbQuadrupleStoredPerPeer =
                new ConcurrentHashMap<UUID, AtomicInteger>();

        this.nbReportsReceivedByPublishers = new MutableInteger();
        this.nbReportsReceivedBySubscribers = new MutableInteger();
    }

    @MemberOf("notify")
    public long reportEndToEndTermination(SubscriptionId subscriptionId,
                                          long endTime) {
        this.endToEndTerminationTimes.put(subscriptionId, endTime);

        return endTime;
    }

    @MemberOf("notify")
    public boolean reportMeasurements(Map<String, Long> pointToPointEntryMeasurements) {
        this.pointToPointEntryMeasurements.putAll(pointToPointEntryMeasurements);

        this.nbReportsReceivedByPublishers.add(1);

        if (this.nbReportsReceivedByPublishers.getValue() == this.nbPublishers) {
            this.allPublisherReportsReceived = true;

            synchronized (this.nbReportsReceivedByPublishers) {
                this.nbReportsReceivedByPublishers.notifyAll();
            }
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

            if (this.nbReportsReceivedBySubscribers.getValue() == this.nbSubscribers
                    * this.nbSubscriptionsPerSubscriber) {
                this.allSubscriberReportsReceived = true;

                synchronized (this.nbReportsReceivedBySubscribers) {
                    this.nbReportsReceivedBySubscribers.notifyAll();
                }
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

    @MemberOf("notify")
    public boolean reportNbQuadrupleStored(UUID peerId, int nbQuadruples) {
        AtomicInteger previousValue =
                this.nbQuadrupleStoredPerPeer.putIfAbsent(
                        peerId, new AtomicInteger(nbQuadruples));

        if (previousValue != null) {
            previousValue.addAndGet(nbQuadruples);
        }

        if (this.countTotalNumberOfQuadrupleStoredOnPeers() == this.nbQuadruplesPublished) {
            if (this.allPublishedQuadruplesStored.compareAndSet(false, true)) {
                synchronized (this.nbQuadrupleStoredPerPeer) {
                    this.nbQuadrupleStoredPerPeer.notifyAll();
                }
            }
        }

        return true;
    }

    private int countTotalNumberOfQuadrupleStoredOnPeers() {
        int result = 0;

        for (AtomicInteger v : this.nbQuadrupleStoredPerPeer.values()) {
            result += v.get();
        }

        return result;
    }

    public long getEndToEndTerminationTime(SubscriptionId subscriptionId) {
        return this.endToEndTerminationTimes.get(subscriptionId);
    }

    public SimpleMeasurement getOutputMeasurement(SubscriptionId subscriptionId) {
        return this.outputMeasurements.get(subscriptionId);
    }

    public CumulatedMeasurement getPointToPointExitMeasurement(SubscriptionId subscriptionId) {
        return this.pointToPointExitMeasurements.get(subscriptionId);
    }

    public Map<SubscriptionId, Long> getEndToEndTerminationTimes() {
        return this.endToEndTerminationTimes;
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
                    this.requestExecutor.incrementExtraActiveRequestCount(1);
                    try {
                        this.nbReportsReceivedByPublishers.wait(timeout);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        this.requestExecutor.decrementExtraActiveRequestCount(1);
                    }

                    if (!this.allPublisherReportsReceived) {
                        break;
                    }
                }
            }

        } else {
            this.allPublisherReportsReceived = true;
        }

        if (!this.allPublisherReportsReceived) {
            throw new TimeoutException("Received only "
                    + this.nbReportsReceivedByPublishers.getValue()
                    + " publisher report(s) after " + timeout + " ms whereas "
                    + this.nbPublishers + " were expected.");
        }
    }

    @MemberOf("wait")
    public void waitForAllSubscriberReports(int timeout)
            throws TimeoutException {

        if (this.nbReportsReceivedBySubscribers.getValue() < this.nbSubscribers
                * this.nbSubscriptionsPerSubscriber) {

            synchronized (this.nbReportsReceivedBySubscribers) {
                while (this.nbReportsReceivedBySubscribers.getValue() < this.nbSubscribers
                        * this.nbSubscriptionsPerSubscriber) {
                    this.requestExecutor.incrementExtraActiveRequestCount(1);
                    try {
                        this.nbReportsReceivedBySubscribers.wait(timeout);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        this.requestExecutor.decrementExtraActiveRequestCount(1);
                    }

                    if (!this.allSubscriberReportsReceived) {
                        break;
                    }
                }
            }

        } else {
            this.allSubscriberReportsReceived = true;
        }

        if (!this.allSubscriberReportsReceived) {
            throw new TimeoutException("Received only "
                    + this.nbReportsReceivedBySubscribers.getValue()
                    + " subscriber report(s) after " + timeout + " ms whereas "
                    + (this.nbSubscribers * this.nbSubscriptionsPerSubscriber)
                    + " were expected.");
        }
    }

    @MemberOf("wait")
    public void waitForStoringQuadruples(int timeout) throws TimeoutException {

        if (this.countTotalNumberOfQuadrupleStoredOnPeers() < this.nbQuadruplesPublished) {

            synchronized (this.nbQuadrupleStoredPerPeer) {
                while (this.countTotalNumberOfQuadrupleStoredOnPeers() < this.nbQuadruplesPublished) {
                    this.requestExecutor.incrementExtraActiveRequestCount(1);
                    try {
                        this.nbQuadrupleStoredPerPeer.wait(timeout);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        this.requestExecutor.decrementExtraActiveRequestCount(1);
                    }

                    if (!this.allPublishedQuadruplesStored.get()) {
                        break;
                    }
                }
            }

        } else {
            this.allPublishedQuadruplesStored.set(true);
        }

        if (!this.allPublishedQuadruplesStored.get()) {
            throw new TimeoutException("Notified about "
                    + this.countTotalNumberOfQuadrupleStoredOnPeers()
                    + " quadruple(s) stored after " + timeout + " ms whereas "
                    + this.nbQuadruplesPublished + " were expected.");
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
            TimeoutException {
        BenchmarkStatsCollector collector =
                PAActiveObject.newActive(
                        BenchmarkStatsCollector.class,
                        new Object[] {2, 2, 1, 1});

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
