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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;
import org.objectweb.proactive.extensions.p2p.structured.utils.LoggerUtils;
import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.Category;
import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.MicroBenchmark;
import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.MicroBenchmarkRun;
import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.StatsRecorder;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.common.base.Supplier;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import fr.inria.eventcloud.EventCloudDescription;
import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.PublishApi;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.SubscribeApi;
import fr.inria.eventcloud.api.Subscription;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.BindingNotificationListener;
import fr.inria.eventcloud.api.listeners.CompoundEventNotificationListener;
import fr.inria.eventcloud.api.listeners.NotificationListener;
import fr.inria.eventcloud.api.listeners.NotificationListenerType;
import fr.inria.eventcloud.api.listeners.SignalNotificationListener;
import fr.inria.eventcloud.deployment.EventCloudDeployer;
import fr.inria.eventcloud.deployment.EventCloudDeploymentDescriptor;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.factories.EventCloudsRegistryFactory;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.providers.SemanticInMemoryOverlayProvider;
import fr.inria.eventcloud.providers.SemanticPersistentOverlayProvider;

/**
 * Simple application to evaluate the publish/subscribe algorithm on a single
 * machine.
 * 
 * @author lpellegr
 */
public class PublishSubscribeBenchmark {

    // parameters

    @Parameter(names = {"-np", "--nb-publications"}, description = "The number of events to publish")
    private static int nbPublications = 10;

    @Parameter(names = {"-ces", "--compound-event-size"}, description = "The number of quadruples contained by each CE")
    private int compoundEventSize = 10;

    @Parameter(names = {"-nr", "--nb-runs"}, description = "The number of runs to perform", required = true)
    private int nbRuns = 1;

    @Parameter(names = {"-p", "--nb-peers"}, description = "The number of peers to inject into the P2P network")
    private int nbPeers = 1;

    @Parameter(names = {"--nb-publishers"}, description = "The number of publishers, each sharing the publication pool")
    private int nbPublishers = 1;

    @Parameter(names = {"--nb-subscribers"}, description = "The number of subscribers")
    private int nbSubscribers = 1;

    @Parameter(names = {"--publish-quadruples"}, description = "Indicates whether events must be emitted as quadruples (default CEs)")
    private boolean publishIndependentQuadruples = false;

    @Parameter(names = {"-lt", "--listener-type"}, description = "The listener type used by all the subscribers for subscribing", converter = ListenerTypeConverter.class)
    private NotificationListenerType listenerType =
            NotificationListenerType.COMPOUND_EVENT;

    @Parameter(names = {"-imds", "--in-memory-datastore"}, description = "Specifies whether datastores on peers have to be persisted on disk or not")
    private boolean inMemoryDatastore = false;

    @Parameter(names = {"-h", "--help"}, description = "Print help", help = true)
    private boolean help;

    // measurements

    /*
     * Measure the time taken to receive all the events by considering the time 
     * when the publications start to be published and the time when all the 
     * notifications are received on all the subscribers
     */
    private final SimpleMeasurement endToEndMeasurement =
            new SimpleMeasurement();

    /*
     * Measure the time to receive all the events by considering the time when
     * the first event is received on the subscriber and the time when the last
     * event is received on the subscriber (used to compte the throughput in terms 
     * of notifications per second on the subscriber side)
     */
    private static Map<SubscriptionId, SimpleMeasurement> outputMeasurements =
            new HashMap<SubscriptionId, SimpleMeasurement>();

    /*
     * Measures the time to receive each event independently (i.e. average latency)
     */
    private static Map<Node, Long> pointToPointEntryMeasurements =
            new ConcurrentHashMap<Node, Long>();

    private static Map<SubscriptionId, CumulatedMeasurement> pointToPointExitMeasurements =
            new ConcurrentHashMap<SubscriptionId, CumulatedMeasurement>();

    // internal

    private static Map<SubscriptionId, AtomicInteger> nbEventsReceivedBySubscriber =
            new HashMap<SubscriptionId, AtomicInteger>();

    private Map<SubscriptionId, NotificationListener<?>> listeners =
            new HashMap<SubscriptionId, NotificationListener<?>>();

    private ExecutorService threadPool =
            Executors.newFixedThreadPool(this.nbPublishers);

    private Supplier<? extends Event> supplier;

    private boolean usingCompoundEventSupplier;

    public static void main(String[] args) {
        LoggerUtils.disableLoggers();

        PublishSubscribeBenchmark benchmark = new PublishSubscribeBenchmark();

        JCommander jCommander = new JCommander(benchmark);

        try {
            jCommander.parse(args);

            if (benchmark.help) {
                jCommander.usage();
                System.exit(0);
            }
        } catch (ParameterException e) {
            jCommander.usage();
            System.exit(1);
        }

        benchmark.execute();

        System.exit(0);
    }

    public void execute() {
        this.supplier =
                this.publishIndependentQuadruples
                        ? new QuadrupleSupplier() : new CompoundEventSupplier(
                                this.compoundEventSize);

        this.usingCompoundEventSupplier =
                this.supplier instanceof CompoundEventSupplier;

        // pre-generates events so that the data are the same for all the runs
        // and the time is not included into the benchmark execution time
        final Event[] events = new Event[nbPublications];

        for (int i = 0; i < nbPublications; i++) {
            events[i] = this.supplier.get();
        }

        // creates and runs micro benchmark
        MicroBenchmark microBenchmark =
                new MicroBenchmark(3, this.nbRuns, new MicroBenchmarkRun() {

                    @Override
                    public void run(StatsRecorder recorder) {
                        PublishSubscribeBenchmark.this.execute(events);

                        recorder.reportTime(
                                0,
                                PublishSubscribeBenchmark.this.endToEndMeasurement.getElapsedTime());

                        if (PublishSubscribeBenchmark.this.listenerType == NotificationListenerType.COMPOUND_EVENT) {
                            SimpleMeasurement outputMeasurement =
                                    outputMeasurements.values()
                                            .iterator()
                                            .next();

                            recorder.reportTime(
                                    1, outputMeasurement.getElapsedTime());

                            recorder.reportTime(
                                    2,
                                    pointToPointExitMeasurements.values()
                                            .iterator()
                                            .next()
                                            .getElapsedTime(
                                                    pointToPointEntryMeasurements));
                        }
                    }

                });
        microBenchmark.showProgress();
        microBenchmark.execute();

        // for (Peer p : deployer.getRandomSemanticTracker(id).getPeers()) {
        // log.info(p.dump());
        // }

        System.out.println();
        System.out.println(this.nbRuns + " run(s)");

        Category endToEnd = microBenchmark.getStatsRecorder().getCategory(0);
        Category output = microBenchmark.getStatsRecorder().getCategory(1);
        Category pointToPoint =
                microBenchmark.getStatsRecorder().getCategory(2);

        System.out.println("End-to-End measurement, average="
                + endToEnd.getMean() + ", median=" + endToEnd.getMedian()
                + ", average throughput="
                + (nbPublications / (endToEnd.getMean() / 1000)));

        System.out.println("Point-to-Point measurement, average="
                + pointToPoint.getMean() + ", median="
                + pointToPoint.getMedian() + ", average latency="
                + (pointToPoint.getMean() / nbPublications));

        System.out.println("Output measurement, average=" + output.getMean()
                + ", median=" + output.getMedian() + ", average throughput="
                + (nbPublications / (output.getMean() / 1000)));
    }

    public void execute(final Event[] events) {
        SerializableProvider<? extends SemanticCanOverlay> overlayProvider;

        if (this.inMemoryDatastore) {
            overlayProvider = new SemanticInMemoryOverlayProvider();
        } else {
            overlayProvider = new SemanticPersistentOverlayProvider();
        }

        EventCloudDeployer deployer =
                new EventCloudDeployer(
                        new EventCloudDescription(),
                        new EventCloudDeploymentDescriptor(overlayProvider));
        deployer.deploy(1, this.nbPeers);

        EventCloudsRegistry registry =
                EventCloudsRegistryFactory.newEventCloudsRegistry();
        registry.register(deployer);
        String registryURL = null;
        try {
            registryURL = registry.register("registry");
        } catch (ProActiveException e) {
            e.printStackTrace();
        }

        EventCloudId id = deployer.getEventCloudDescription().getId();

        List<PublishApi> publishProxies = null;
        List<SubscribeApi> subscribeProxies = null;
        try {
            publishProxies =
                    this.createPublishProxies(
                            registryURL, id, this.nbPublishers);
            subscribeProxies =
                    this.createSubscribeProxies(
                            registryURL, id, this.nbSubscribers);
        } catch (EventCloudIdNotManaged e) {
            e.printStackTrace();
        }

        nbEventsReceivedBySubscriber.clear();
        outputMeasurements.clear();
        pointToPointExitMeasurements.clear();
        pointToPointEntryMeasurements.clear();

        for (int i = 0; i < this.nbSubscribers; i++) {
            final SubscribeApi subscribeProxy = subscribeProxies.get(i);

            Subscription subscription =
                    new Subscription(
                            "SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { ?s ?p ?o } }");

            nbEventsReceivedBySubscriber.put(
                    subscription.getId(), new AtomicInteger());

            outputMeasurements.put(
                    subscription.getId(), new SimpleMeasurement());
            pointToPointExitMeasurements.put(
                    subscription.getId(), new CumulatedMeasurement(
                            nbPublications));

            this.subscribe(subscribeProxy, subscription, this.listenerType);
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final int segment = (nbPublications / this.nbPublishers);

        this.endToEndMeasurement.setEntryTime();

        for (int i = 0; i < this.nbPublishers; i++) {
            final PublishApi publishProxy = publishProxies.get(i);
            final int j = i;

            this.threadPool.submit(new Runnable() {
                @Override
                public void run() {
                    int start = j * segment;
                    int end = (j + 1) * segment - 1;

                    PublishSubscribeBenchmark.this.publish(
                            publishProxy, events, start, end);
                }
            });
        }

        int rest = nbPublications % this.nbPublishers;
        if (rest != 0) {
            int start = this.nbPublishers * segment;
            PublishSubscribeBenchmark.this.publish(
                    publishProxies.get(0), events, start, start + rest - 1);
        }

        // waits to receive the number of published events
        synchronized (nbEventsReceivedBySubscriber) {
            while (!this.allEventsReceived()) {
                try {
                    nbEventsReceivedBySubscriber.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        this.endToEndMeasurement.setExitTime();

        // deployer.undeploy();
    }

    private List<PublishApi> createPublishProxies(String registryUrl,
                                                  EventCloudId id,
                                                  int nbPublishProxies)
            throws EventCloudIdNotManaged {
        List<PublishApi> result = new ArrayList<PublishApi>(nbPublishProxies);

        for (int i = 0; i < nbPublishProxies; i++) {
            result.add(ProxyFactory.newPublishProxy(registryUrl, id));
        }

        return result;
    }

    private List<SubscribeApi> createSubscribeProxies(String registryUrl,
                                                      EventCloudId id,
                                                      int nbSubscribeProxies)
            throws EventCloudIdNotManaged {
        List<SubscribeApi> result =
                new ArrayList<SubscribeApi>(nbSubscribeProxies);

        for (int i = 0; i < nbSubscribeProxies; i++) {
            result.add(ProxyFactory.newSubscribeProxy(registryUrl, id));
        }

        return result;
    }

    private void publish(PublishApi publishProxy, Event[] events, int start,
                         int end) {
        for (int i = start; i <= end; i++) {
            this.publish(publishProxy, events[i]);
        }
    }

    private void publish(PublishApi publishProxy, Event event) {
        if (this.usingCompoundEventSupplier) {
            CompoundEvent ce = (CompoundEvent) event;

            pointToPointEntryMeasurements.put(ce.getGraph(), System.nanoTime());

            publishProxy.publish(ce);
        } else {
            publishProxy.publish((Quadruple) event);
        }
    }

    private void subscribe(SubscribeApi subscribeProxy,
                           Subscription subscription,
                           NotificationListenerType listenerType) {

        NotificationListener<?> listener = null;

        switch (listenerType) {
            case BINDING:
                listener = new CustomBindingListener();
                subscribeProxy.subscribe(
                        subscription, (CustomBindingListener) listener);
                break;
            case COMPOUND_EVENT:
                listener = new CustomEventListener();
                subscribeProxy.subscribe(
                        subscription, (CustomEventListener) listener);
                break;
            case SIGNAL:
                listener = new CustomSignalListener();
                subscribeProxy.subscribe(
                        subscription, (CustomSignalListener) listener);
                break;
        }

        this.listeners.put(subscription.getId(), listener);
    }

    private boolean allEventsReceived() {
        for (AtomicInteger counter : nbEventsReceivedBySubscriber.values()) {
            if (counter.get() < nbPublications - 1) {
                return false;
            }
        }

        return true;
    }

    private static final class CustomBindingListener extends
            BindingNotificationListener {
        private static final long serialVersionUID = 140L;

        @Override
        public void onNotification(SubscriptionId id, Binding solution) {
            handleNewEvent(id);
        }
    }

    private static final class CustomEventListener extends
            CompoundEventNotificationListener {
        private static final long serialVersionUID = 140L;

        @Override
        public void onNotification(SubscriptionId id, CompoundEvent solution) {
            handleNewEvent(id);
            pointToPointExitMeasurements.get(id).reportReception(
                    solution.getGraph());
        }
    }

    private static final class CustomSignalListener extends
            SignalNotificationListener {
        private static final long serialVersionUID = 140L;

        @Override
        public void onNotification(SubscriptionId id) {
            handleNewEvent(id);
        }
    }

    private static void handleNewEvent(SubscriptionId id) {
        synchronized (nbEventsReceivedBySubscriber) {
            long nbEventsReceived =
                    nbEventsReceivedBySubscriber.get(id).incrementAndGet();

            SimpleMeasurement outputMeasurement = outputMeasurements.get(id);

            if (nbEventsReceived == 1) {
                outputMeasurement.setEntryTime();
            }

            if (nbEventsReceived == nbPublications) {
                outputMeasurement.setExitTime();
                nbEventsReceivedBySubscriber.notifyAll();
            }
        }
    }

    private static interface Measurement extends Serializable {

        long getElapsedTime();

    }

    private static class CumulatedMeasurement implements Serializable {

        private static final long serialVersionUID = 140L;

        private Map<Node, Long> times;

        public CumulatedMeasurement(int nbPublicationsExpected) {
            this.times = new HashMap<Node, Long>(nbPublicationsExpected);
        }

        public void reportReception(Node eventId) {
            this.times.put(eventId, System.nanoTime());
        }

        public long getElapsedTime(Map<Node, Long> pointToPointEntryMeasurements) {
            long sum = 0;

            for (Entry<Node, Long> entry : this.times.entrySet()) {
                sum +=
                        entry.getValue()
                                - pointToPointEntryMeasurements.get(entry.getKey());
            }

            return sum / 1000000;
        }

    }

    private static class SimpleMeasurement implements Measurement {

        private static final long serialVersionUID = 140L;

        private long entryTime;

        private long exitTime;

        /**
         * {@inheritDoc}
         */
        @Override
        public long getElapsedTime() {
            // returns elapsed time in ms
            return (this.exitTime - this.entryTime) / 1000000;
        }

        public void setEntryTime() {
            this.entryTime = System.nanoTime();
        }

        public void setExitTime() {
            this.exitTime = System.nanoTime();
        }

    }

}
