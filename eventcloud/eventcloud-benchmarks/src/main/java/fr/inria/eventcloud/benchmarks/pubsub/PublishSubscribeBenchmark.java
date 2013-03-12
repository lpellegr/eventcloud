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
import fr.inria.eventcloud.api.PublishSubscribeConstants;
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
 * machine. Times are measured in milliseconds with
 * {@link System#currentTimeMillis()}.
 * 
 * @author lpellegr
 */
public class PublishSubscribeBenchmark {

    // parameters

    @Parameter(names = {"-np", "--nb-publications"}, description = "The number of events to publish")
    public static int nbPublications = 10;

    @Parameter(names = {"-ces", "--compound-event-size"}, description = "The number of quadruples contained by each CE")
    public int compoundEventSize = 10;

    @Parameter(names = {"-nr", "--nb-runs"}, description = "The number of runs to perform", required = true)
    public int nbRuns = 1;

    @Parameter(names = {"-p", "--nb-peers"}, description = "The number of peers to inject into the P2P network")
    public int nbPeers = 1;

    @Parameter(names = {"--nb-publishers"}, description = "The number of publishers, each sharing the publication pool")
    public int nbPublishers = 1;

    @Parameter(names = {"--nb-subscribers"}, description = "The number of subscribers")
    public int nbSubscribers = 1;

    @Parameter(names = {"-dfr", "--discard-first-runs"}, description = "Indicates the number of first runs to discard")
    public int discardFirstRuns = 1;

    @Parameter(names = {"--wait-between-publications"}, description = "The time to wait (in ms) between each publication from a publisher")
    public int waitBetweenPublications = 0;

    // a rewriting level of 0 means no rewrite
    @Parameter(names = {"--rewriting-level", "-rl"}, description = "Indicates the number of rewrites to force before delivering a notification")
    public int rewritingLevel = 0;

    @Parameter(names = {"--publish-quadruples"}, description = "Indicates whether events must be emitted as quadruples (default CEs)")
    public boolean publishIndependentQuadruples = false;

    @Parameter(names = {"-lt", "--listener-type"}, description = "The listener type used by all the subscribers for subscribing", converter = ListenerTypeConverter.class)
    public NotificationListenerType listenerType =
            NotificationListenerType.COMPOUND_EVENT;

    @Parameter(names = {"-imds", "--in-memory-datastore"}, description = "Specifies whether datastores on peers have to be persisted on disk or not")
    public boolean inMemoryDatastore = false;

    @Parameter(names = {"-h", "--help"}, description = "Print help", help = true)
    public boolean help;

    // category names

    public static final String END_TO_END_MEASUREMENT_CATEGORY =
            MicroBenchmark.DEFAULT_CATEGORY_NAME;

    public static final String OUTPUT_MEASUREMENT_CATEGORY =
            "outputMeasurement";

    public static final String POINT_TO_POINT_MEASUREMENT_CATEGORY =
            "pointToPointMeasurement";

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
    private static Map<SubscriptionId, SimpleMeasurement> outputMeasurements;

    /*
     * Measures the time to receive each event independently (i.e. average latency)
     */
    private static Map<Node, Long> pointToPointEntryMeasurements;

    private static Map<SubscriptionId, CumulatedMeasurement> pointToPointExitMeasurements;

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
        // LoggerUtils.disableLoggers();

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

    public StatsRecorder execute() {
        if (this.rewritingLevel < 0) {
            throw new IllegalStateException("Illegal rewriting level: "
                    + this.rewritingLevel);
        }

        this.supplier =
                this.publishIndependentQuadruples
                        ? new QuadrupleSupplier() : new CompoundEventSupplier(
                                this.compoundEventSize, this.rewritingLevel);

        this.usingCompoundEventSupplier =
                this.supplier instanceof CompoundEventSupplier;

        outputMeasurements =
                new ConcurrentHashMap<SubscriptionId, SimpleMeasurement>(
                        this.nbSubscribers);

        pointToPointEntryMeasurements =
                new ConcurrentHashMap<Node, Long>(this.nbSubscribers
                        * nbPublications);

        pointToPointExitMeasurements =
                new ConcurrentHashMap<SubscriptionId, CumulatedMeasurement>(
                        pointToPointEntryMeasurements.size());

        // pre-generates events so that the data are the same for all the runs
        // and the time is not included into the benchmark execution time
        final Event[] events = new Event[nbPublications];

        for (int i = 0; i < nbPublications; i++) {
            events[i] = this.supplier.get();
        }

        // creates and runs micro benchmark
        MicroBenchmark microBenchmark =
                new MicroBenchmark(
                        new String[] {
                                END_TO_END_MEASUREMENT_CATEGORY,
                                OUTPUT_MEASUREMENT_CATEGORY,
                                POINT_TO_POINT_MEASUREMENT_CATEGORY},
                        this.nbRuns, new MicroBenchmarkRun() {

                            @Override
                            public void run(StatsRecorder recorder) {
                                PublishSubscribeBenchmark.this.execute(events);

                                recorder.reportTime(
                                        END_TO_END_MEASUREMENT_CATEGORY,
                                        PublishSubscribeBenchmark.this.endToEndMeasurement.getElapsedTime());

                                SimpleMeasurement outputMeasurement =
                                        outputMeasurements.values()
                                                .iterator()
                                                .next();

                                recorder.reportTime(
                                        OUTPUT_MEASUREMENT_CATEGORY,
                                        outputMeasurement.getElapsedTime());

                                recorder.reportTime(
                                        POINT_TO_POINT_MEASUREMENT_CATEGORY,
                                        pointToPointExitMeasurements.values()
                                                .iterator()
                                                .next()
                                                .getElapsedTime(
                                                        pointToPointEntryMeasurements));
                            }

                        });
        microBenchmark.discardFirstRuns(this.discardFirstRuns);
        microBenchmark.showProgress();
        microBenchmark.execute();

        // for (Peer p : deployer.getRandomSemanticTracker(id).getPeers()) {
        // log.info(p.dump());
        // }

        System.out.println();
        System.out.println(this.nbRuns + " run(s)");

        Category endToEnd =
                microBenchmark.getStatsRecorder().getCategory(
                        END_TO_END_MEASUREMENT_CATEGORY);
        Category output =
                microBenchmark.getStatsRecorder().getCategory(
                        OUTPUT_MEASUREMENT_CATEGORY);
        Category pointToPoint =
                microBenchmark.getStatsRecorder().getCategory(
                        POINT_TO_POINT_MEASUREMENT_CATEGORY);

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

        return microBenchmark.getStatsRecorder();
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
                    new Subscription(this.createSubscription());

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

    private String createSubscription() {
        String subscription;

        if (this.rewritingLevel == 0) {
            subscription = "SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { ?s ?p ?o } }";
        } else {
            StringBuilder buf = new StringBuilder();

            buf.append("SELECT ?g ");
            for (int i = 1; i <= this.rewritingLevel + 1; i++) {
                buf.append("?o");
                buf.append(i);
                buf.append(' ');
            }

            buf.append("WHERE { GRAPH ?g { ");
            for (int i = 1; i <= this.rewritingLevel + 1; i++) {
                if (i == 1) {
                    buf.append("?s1 ");
                } else {
                    buf.append("?o");
                    buf.append(i - 1);
                    buf.append(' ');
                }

                buf.append("<urn:p");
                buf.append(i);
                buf.append("> ?o");
                buf.append(i);
                buf.append(" . ");
            }
            buf.append("} }");

            subscription = buf.toString();
        }

        return subscription;
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

            pointToPointEntryMeasurements.put(
                    ce.getGraph(), System.currentTimeMillis());

            publishProxy.publish(ce);
        } else {
            publishProxy.publish((Quadruple) event);
        }

        if (this.waitBetweenPublications > 0) {
            try {
                Thread.sleep(this.waitBetweenPublications);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
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
            if (counter.get() < nbPublications) {
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
            pointToPointExitMeasurements.get(id)
                    .reportReception(
                            Quadruple.removeMetaInformation(solution.get(PublishSubscribeConstants.GRAPH_VAR)));
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
        public void onNotification(SubscriptionId id, String eventId) {
            handleNewEvent(id);
            pointToPointExitMeasurements.get(id).reportReception(
                    Node.createURI(Quadruple.removeMetaInformation(eventId)));
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

    public static int getNbPublications() {
        return nbPublications;
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
            this.times.put(eventId, System.currentTimeMillis());
        }

        public long getElapsedTime(Map<Node, Long> pointToPointEntryMeasurements) {
            long sum = 0;

            synchronized (this.times) {
                for (Entry<Node, Long> entry : this.times.entrySet()) {
                    sum +=
                            entry.getValue()
                                    - pointToPointEntryMeasurements.get(entry.getKey());
                }
            }

            return sum;
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
            return this.exitTime - this.entryTime;
        }

        public void setEntryTime() {
            this.entryTime = System.currentTimeMillis();
        }

        public void setExitTime() {
            this.exitTime = System.currentTimeMillis();
        }

    }

}
