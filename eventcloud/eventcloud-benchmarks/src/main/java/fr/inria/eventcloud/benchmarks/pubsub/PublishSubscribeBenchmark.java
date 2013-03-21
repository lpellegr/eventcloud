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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.p2p.structured.deployment.NodeProvider;
import org.objectweb.proactive.extensions.p2p.structured.deployment.gcmdeployment.GcmDeploymentNodeProvider;
import org.objectweb.proactive.extensions.p2p.structured.operations.CanOperations;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.GetIdAndZoneResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.providers.InjectionConstraintsProvider;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;
import org.objectweb.proactive.extensions.p2p.structured.utils.ComponentUtils;
import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.Category;
import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.MicroBenchmark;
import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.MicroBenchmarkRun;
import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.StatsRecorder;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.common.base.Supplier;

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
import fr.inria.eventcloud.benchmarks.pubsub.listeners.CustomBindingListener;
import fr.inria.eventcloud.benchmarks.pubsub.listeners.CustomCompoundEventListener;
import fr.inria.eventcloud.benchmarks.pubsub.listeners.CustomSignalListener;
import fr.inria.eventcloud.benchmarks.pubsub.measurements.CumulatedMeasurement;
import fr.inria.eventcloud.benchmarks.pubsub.measurements.SimpleMeasurement;
import fr.inria.eventcloud.benchmarks.pubsub.suppliers.CompoundEventSupplier;
import fr.inria.eventcloud.benchmarks.pubsub.suppliers.QuadrupleSupplier;
import fr.inria.eventcloud.deployment.EventCloudDeployer;
import fr.inria.eventcloud.deployment.EventCloudDeploymentDescriptor;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.factories.EventCloudsRegistryFactory;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.overlay.can.SemanticElement;
import fr.inria.eventcloud.overlay.can.SemanticZone;
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
    public int nbQuadruplesPerCompoundEvent = 10;

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

    @Parameter(names = {"-udd", "--uniform-data-distribution"}, description = "Generates data so that they are distributed uniformly among the available peers")
    public boolean uniformDataDistribution = false;

    @Parameter(names = {"-gcma", "--gcma-descriptor"}, description = "Path to the GCMA descriptor to use for deploying the benchmark entities on several machines")
    public String gcmaDescriptor = null;

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
    private Map<SubscriptionId, SimpleMeasurement> outputMeasurements;

    /*
     * Measures the time to receive each event independently (i.e. average latency)
     */
    private Map<String, Long> pointToPointEntryMeasurements;
    private Map<SubscriptionId, CumulatedMeasurement> pointToPointExitMeasurements;

    // internal

    private Map<SubscriptionId, NotificationListener<?>> listeners =
            new HashMap<SubscriptionId, NotificationListener<?>>();

    private ExecutorService threadPool =
            Executors.newFixedThreadPool(this.nbPublishers);

    private Supplier<? extends Event> supplier;

    private boolean usingCompoundEventSupplier;

    public static void main(String[] args) {
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
                                this.nbQuadruplesPerCompoundEvent,
                                this.rewritingLevel);

        this.usingCompoundEventSupplier =
                this.supplier instanceof CompoundEventSupplier;

        this.outputMeasurements =
                new HashMap<SubscriptionId, SimpleMeasurement>(
                        this.nbSubscribers);

        this.pointToPointEntryMeasurements =
                new ConcurrentHashMap<String, Long>(this.nbSubscribers
                        * nbPublications);

        this.pointToPointExitMeasurements =
                new HashMap<SubscriptionId, CumulatedMeasurement>(
                        this.pointToPointEntryMeasurements.size());

        // pre-generates events so that the data are the same for all the
        // runs and the time is not included into the benchmark execution time
        final Event[] events = new Event[nbPublications];

        if (!this.uniformDataDistribution) {
            for (int i = 0; i < nbPublications; i++) {
                events[i] = this.supplier.get();
            }
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
                                try {
                                    PublishSubscribeBenchmark.this.execute(events);

                                    recorder.reportTime(
                                            END_TO_END_MEASUREMENT_CATEGORY,
                                            PublishSubscribeBenchmark.this.endToEndMeasurement.getElapsedTime());

                                    SimpleMeasurement outputMeasurement =
                                            PublishSubscribeBenchmark.this.outputMeasurements.values()
                                                    .iterator()
                                                    .next();

                                    recorder.reportTime(
                                            OUTPUT_MEASUREMENT_CATEGORY,
                                            outputMeasurement.getElapsedTime());

                                    recorder.reportTime(
                                            POINT_TO_POINT_MEASUREMENT_CATEGORY,
                                            PublishSubscribeBenchmark.this.pointToPointExitMeasurements.values()
                                                    .iterator()
                                                    .next()
                                                    .getElapsedTime(
                                                            PublishSubscribeBenchmark.this.pointToPointEntryMeasurements));
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }

                        });
        microBenchmark.discardFirstRuns(this.discardFirstRuns);
        microBenchmark.showProgress();
        microBenchmark.execute();

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

    public void execute(Event[] events) throws ActiveObjectCreationException,
            NodeException, EventCloudIdNotManaged, TimeoutException {
        // clears results collected during previous run
        this.outputMeasurements.clear();
        this.pointToPointExitMeasurements.clear();
        this.pointToPointEntryMeasurements.clear();

        // creates collector
        BenchmarkStatsCollector collector =
                PAActiveObject.newActive(
                        BenchmarkStatsCollector.class,
                        new Object[] {this.nbSubscribers});

        GcmDeploymentNodeProvider nodeProvider =
                this.createNodeProviderIfRequired();

        EventCloudDeploymentDescriptor descriptor =
                this.createDeploymentDescriptor(nodeProvider);

        // creates eventcloud
        EventCloudDeployer deployer =
                new EventCloudDeployer(new EventCloudDescription(), descriptor);
        deployer.deploy(1, this.nbPeers);

        if (this.uniformDataDistribution) {
            events = this.updateEventsForUniformDistribution(deployer);
        }

        String registryURL = this.deployRegistry(deployer, nodeProvider);

        EventCloudId id = deployer.getEventCloudDescription().getId();

        // creates proxies
        List<PublishApi> publishProxies =
                this.createPublishProxies(
                        registryURL, id, nodeProvider, this.nbPublishers);
        List<SubscribeApi> subscribeProxies =
                this.createSubscribeProxies(
                        registryURL, id, nodeProvider, this.nbSubscribers);

        // subscribes
        for (int i = 0; i < this.nbSubscribers; i++) {
            final SubscribeApi subscribeProxy = subscribeProxies.get(i);

            Subscription subscription =
                    new Subscription(this.createSubscription());

            this.subscribe(
                    collector, subscribeProxy, subscription, this.listenerType);
        }

        // waits that subscriptions are indexed as the process is asynchronous
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // publishes
        final int segment = (nbPublications / this.nbPublishers);
        this.endToEndMeasurement.setEntryTime();

        for (int i = 0; i < this.nbPublishers; i++) {
            final PublishApi publishProxy = publishProxies.get(i);
            final int j = i;
            final Event[] pevents = events;

            this.threadPool.submit(new Runnable() {
                @Override
                public void run() {
                    int start = j * segment;
                    int end = (j + 1) * segment - 1;

                    PublishSubscribeBenchmark.this.publish(
                            publishProxy, pevents, start, end);
                }
            });
        }

        int rest = nbPublications % this.nbPublishers;
        if (rest != 0) {
            int start = this.nbPublishers * segment;
            PublishSubscribeBenchmark.this.publish(
                    publishProxies.get(0), events, start, start + rest - 1);
        }

        collector.waitForAllSubscriberReports(0);

        // the end to end termination time is the time at which the last
        // subscriber has notified the collector about its termination
        this.endToEndMeasurement.setExitTime(collector.getEndToEndTerminationTime());

        // collects output measurements
        for (Entry<SubscriptionId, SimpleMeasurement> entry : collector.getOutputMeasurements()
                .entrySet()) {
            this.outputMeasurements.put(entry.getKey(), entry.getValue());
        }

        // collects point-to-point exit measurements
        for (Entry<SubscriptionId, CumulatedMeasurement> entry : collector.getPointToPointExitMeasurements()
                .entrySet()) {
            this.pointToPointExitMeasurements.put(
                    entry.getKey(), entry.getValue());
        }

        deployer.undeploy();
        ComponentUtils.terminateComponent(registryURL);
    }

    private String deployRegistry(EventCloudDeployer deployer,
                                  GcmDeploymentNodeProvider nodeProvider) {
        EventCloudsRegistry registry;
        if (this.gcmaDescriptor == null) {
            registry = EventCloudsRegistryFactory.newEventCloudsRegistry();
        } else {
            registry =
                    EventCloudsRegistryFactory.newEventCloudsRegistry(nodeProvider);
        }
        registry.register(deployer);

        String registryURL = null;
        try {
            registryURL = registry.register("registry");
        } catch (ProActiveException e) {
            e.printStackTrace();
        }
        return registryURL;
    }

    private Event[] updateEventsForUniformDistribution(EventCloudDeployer deployer) {
        Event[] events = new Event[nbPublications];
        SemanticZone[] zones = new SemanticZone[this.nbPeers];

        int i = 0;
        for (Peer peer : deployer.getRandomSemanticTracker().getPeers()) {
            GetIdAndZoneResponseOperation<SemanticElement> response =
                    CanOperations.getIdAndZoneResponseOperation(peer);
            SemanticZone zone = (SemanticZone) response.getPeerZone();
            zones[i] = zone;
            i++;
        }

        for (i = 0; i < nbPublications; i++) {
            if (this.publishIndependentQuadruples) {
                events[i] =
                        EventGenerator.randomQuadruple(
                                zones[i % this.nbPeers], 10);
            } else {
                events[i] =
                        EventGenerator.randomCompoundEvent(
                                zones[i % this.nbPeers],
                                this.nbQuadruplesPerCompoundEvent, 10);
            }
        }

        return events;
    }

    private EventCloudDeploymentDescriptor createDeploymentDescriptor(GcmDeploymentNodeProvider nodeProvider) {
        EventCloudDeploymentDescriptor descriptor =
                new EventCloudDeploymentDescriptor(
                        this.createOverlayProvider(this.inMemoryDatastore));
        descriptor.setInjectionConstraintsProvider(InjectionConstraintsProvider.newUniformInjectionConstraintsProvider());

        if (this.gcmaDescriptor != null) {
            descriptor.setNodeProvider(nodeProvider);
        }
        return descriptor;
    }

    private GcmDeploymentNodeProvider createNodeProviderIfRequired() {
        GcmDeploymentNodeProvider nodeProvider = null;

        if (this.gcmaDescriptor != null) {
            nodeProvider = new GcmDeploymentNodeProvider(this.gcmaDescriptor);
            nodeProvider.start();
        }

        return nodeProvider;
    }

    private SerializableProvider<? extends SemanticCanOverlay> createOverlayProvider(boolean inMemory) {
        if (this.inMemoryDatastore) {
            return new SemanticInMemoryOverlayProvider();
        } else {
            return new SemanticPersistentOverlayProvider();
        }
    }

    private List<PublishApi> createPublishProxies(String registryUrl,
                                                  EventCloudId id,
                                                  NodeProvider nodeProvider,
                                                  int nbPublishProxies)
            throws EventCloudIdNotManaged {
        List<PublishApi> result = new ArrayList<PublishApi>(nbPublishProxies);

        for (int i = 0; i < nbPublishProxies; i++) {
            PublishApi publishProxy;

            if (nodeProvider == null) {
                publishProxy = ProxyFactory.newPublishProxy(registryUrl, id);
            } else {
                publishProxy =
                        ProxyFactory.newPublishProxy(
                                nodeProvider, registryUrl, id);
            }

            result.add(publishProxy);
        }

        return result;
    }

    private List<SubscribeApi> createSubscribeProxies(String registryUrl,
                                                      EventCloudId id,
                                                      NodeProvider nodeProvider,
                                                      int nbSubscribeProxies)
            throws EventCloudIdNotManaged {
        List<SubscribeApi> result =
                new ArrayList<SubscribeApi>(nbSubscribeProxies);

        for (int i = 0; i < nbSubscribeProxies; i++) {
            SubscribeApi subscribeProxy;

            if (nodeProvider == null) {
                subscribeProxy =
                        ProxyFactory.newSubscribeProxy(registryUrl, id);
            } else {
                subscribeProxy =
                        ProxyFactory.newSubscribeProxy(
                                nodeProvider, registryUrl, id);
            }

            result.add(subscribeProxy);
        }

        return result;
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

    private void publish(PublishApi publishProxy, Event[] events, int start,
                         int end) {
        for (int i = start; i <= end; i++) {
            this.publish(publishProxy, events[i]);
        }
    }

    private void publish(PublishApi publishProxy, Event event) {
        if (this.usingCompoundEventSupplier) {
            CompoundEvent ce = (CompoundEvent) event;

            this.pointToPointEntryMeasurements.put(
                    ce.getGraph().getURI(), System.currentTimeMillis());

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

    private void subscribe(BenchmarkStatsCollector collector,
                           SubscribeApi subscribeProxy,
                           Subscription subscription,
                           NotificationListenerType listenerType) {

        NotificationListener<?> listener = null;

        switch (listenerType) {
            case BINDING:
                listener = new CustomBindingListener(collector, nbPublications);
                subscribeProxy.subscribe(
                        subscription, (BindingNotificationListener) listener);
                break;
            case COMPOUND_EVENT:
                listener =
                        new CustomCompoundEventListener(
                                collector, nbPublications);
                subscribeProxy.subscribe(
                        subscription,
                        (CompoundEventNotificationListener) listener);
                break;
            case SIGNAL:
                listener = new CustomSignalListener(collector, nbPublications);
                subscribeProxy.subscribe(
                        subscription, (SignalNotificationListener) listener);
                break;
        }

        this.listeners.put(subscription.getId(), listener);
    }

}
