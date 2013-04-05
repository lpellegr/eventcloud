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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeoutException;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.deployment.NodeProvider;
import org.objectweb.proactive.extensions.p2p.structured.deployment.gcmdeployment.GcmDeploymentNodeProvider;
import org.objectweb.proactive.extensions.p2p.structured.operations.CanOperations;
import org.objectweb.proactive.extensions.p2p.structured.operations.GenericResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.GetIdAndZoneResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.providers.InjectionConstraintsProvider;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;
import org.objectweb.proactive.extensions.p2p.structured.utils.ComponentUtils;
import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.Category;
import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.MicroBenchmark;
import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.MicroBenchmarkRun;
import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.StatsRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.common.base.Supplier;

import fr.inria.eventcloud.EventCloudDescription;
import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.EventCloudId;
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
import fr.inria.eventcloud.benchmarks.pubsub.proxies.CustomProxyFactory;
import fr.inria.eventcloud.benchmarks.pubsub.proxies.CustomPublishProxy;
import fr.inria.eventcloud.benchmarks.pubsub.suppliers.CompoundEventSupplier;
import fr.inria.eventcloud.benchmarks.pubsub.suppliers.QuadrupleSupplier;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.deployment.EventCloudDeployer;
import fr.inria.eventcloud.deployment.EventCloudDeploymentDescriptor;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.factories.EventCloudsRegistryFactory;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.operations.can.CountQuadruplesOperation;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.overlay.can.SemanticElement;
import fr.inria.eventcloud.overlay.can.SemanticZone;
import fr.inria.eventcloud.providers.SemanticInMemoryOverlayProvider;
import fr.inria.eventcloud.providers.SemanticPersistentOverlayProvider;

/**
 * Simple application to evaluate the publish/subscribe algorithm on a single
 * machine or in a distributed setup. Times are measured in milliseconds with
 * {@link System#currentTimeMillis()}.
 * 
 * @author lpellegr
 */
public class PublishSubscribeBenchmark {

    private static final Logger log =
            LoggerFactory.getLogger(PublishSubscribeBenchmark.class);

    private static final String NB_QUADRUPLES_PER_PEER_CATEGORY =
            "quadsPerPeer";

    // parameters

    @Parameter(names = {"-np", "--nb-publications"}, description = "The number of events to publish")
    public static int nbPublications = 1000;

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
     * event is received on the subscriber (used to count the throughput in terms 
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

    private Supplier<? extends Event> supplier;

    public static void main(String[] args) {
        // printable ascii codepoints interval [32,126]

        // use only characters [a-z] otherwise invalid IRI may be generated
        P2PStructuredProperties.CAN_LOWER_BOUND.setValue(97);
        P2PStructuredProperties.CAN_UPPER_BOUND.setValue(122);

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

    private void logParameterValues() {
        log.info("Benchmark starting with the following parameters:");
        log.info("  compoundEventSize -> {}", this.nbQuadruplesPerCompoundEvent);
        log.info("  discardFirstRuns -> {}", this.discardFirstRuns);
        log.info("  gcmaDescriptor -> {}", this.gcmaDescriptor);
        log.info("  inMemoryDatastore -> {}", this.inMemoryDatastore);
        log.info("  listenerType -> {}", this.listenerType);
        log.info("  nbPeers -> {}", this.nbPeers);
        log.info("  nbPublications -> {}", nbPublications);
        log.info("  nbPublishers -> {}", this.nbPublishers);
        log.info("  nbRuns -> {}", this.nbRuns);
        log.info("  nbSubscribers -> {}", this.nbSubscribers);
        log.info("  publishQuadruples -> {}", this.publishIndependentQuadruples);
        log.info("  rewritingLevel -> {}", this.rewritingLevel);
        log.info(
                "  uniformDataDistribution -> {}", this.uniformDataDistribution);
        log.info(
                "  waitBetweenPublications -> {}", this.waitBetweenPublications);
    }

    public StatsRecorder execute() {
        this.logParameterValues();

        if (this.rewritingLevel < 0) {
            throw new IllegalStateException("Illegal rewriting level: "
                    + this.rewritingLevel);
        }

        this.supplier =
                this.publishIndependentQuadruples
                        ? new QuadrupleSupplier() : new CompoundEventSupplier(
                                this.nbQuadruplesPerCompoundEvent,
                                this.rewritingLevel);

        this.outputMeasurements =
                new HashMap<SubscriptionId, SimpleMeasurement>(
                        this.nbSubscribers);

        this.pointToPointEntryMeasurements =
                new HashMap<String, Long>(nbPublications);

        this.pointToPointExitMeasurements =
                new HashMap<SubscriptionId, CumulatedMeasurement>(
                        nbPublications);

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
                                POINT_TO_POINT_MEASUREMENT_CATEGORY,
                                NB_QUADRUPLES_PER_PEER_CATEGORY}, this.nbRuns,
                        new MicroBenchmarkRun() {

                            @Override
                            public void run(StatsRecorder recorder) {
                                try {
                                    PublishSubscribeBenchmark.this.execute(
                                            events, recorder);

                                    recorder.reportValue(
                                            END_TO_END_MEASUREMENT_CATEGORY,
                                            PublishSubscribeBenchmark.this.endToEndMeasurement.getElapsedTime());

                                    SimpleMeasurement outputMeasurement =
                                            PublishSubscribeBenchmark.this.outputMeasurements.values()
                                                    .iterator()
                                                    .next();

                                    recorder.reportValue(
                                            OUTPUT_MEASUREMENT_CATEGORY,
                                            outputMeasurement.getElapsedTime());

                                    recorder.reportValue(
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

        Category nbQuadsPerPeer =
                microBenchmark.getStatsRecorder().getCategory(
                        NB_QUADRUPLES_PER_PEER_CATEGORY);
        Category endToEnd =
                microBenchmark.getStatsRecorder().getCategory(
                        END_TO_END_MEASUREMENT_CATEGORY);
        Category output =
                microBenchmark.getStatsRecorder().getCategory(
                        OUTPUT_MEASUREMENT_CATEGORY);
        Category pointToPoint =
                microBenchmark.getStatsRecorder().getCategory(
                        POINT_TO_POINT_MEASUREMENT_CATEGORY);

        System.out.println("Average number of quadruples per peer is "
                + nbQuadsPerPeer.getMean());

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

    public void execute(Event[] events, StatsRecorder recorder)
            throws EventCloudIdNotManaged, TimeoutException, ProActiveException {
        // clears results collected during previous run
        this.outputMeasurements.clear();
        this.pointToPointExitMeasurements.clear();
        this.pointToPointEntryMeasurements.clear();

        // creates collector
        BenchmarkStatsCollector collector =
                PAActiveObject.newActive(
                        BenchmarkStatsCollector.class, new Object[] {
                                this.nbPublishers, this.nbSubscribers});
        String collectorURL =
                PAActiveObject.registerByName(
                        collector, "benchmark-stats-collector");

        GcmDeploymentNodeProvider nodeProvider =
                this.createNodeProviderIfRequired();

        EventCloudDeploymentDescriptor descriptor =
                this.createDeploymentDescriptor(nodeProvider);

        // creates eventcloud
        EventCloudDeployer deployer =
                new EventCloudDeployer(new EventCloudDescription(), descriptor);
        deployer.deploy(1, this.nbPeers);

        if (this.uniformDataDistribution && this.rewritingLevel > 0) {
            events =
                    this.createEventsForUniformDistributionAndRewritingSteps(deployer);
        } else if (this.uniformDataDistribution && this.rewritingLevel == 0) {
            events = this.createEventsForUniformDistribution(deployer);
        }

        String registryURL = this.deployRegistry(deployer, nodeProvider);

        EventCloudId id = deployer.getEventCloudDescription().getId();

        // creates proxies
        List<CustomPublishProxy> publishProxies =
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

        // init custom publish proxies
        for (CustomPublishProxy proxy : publishProxies) {
            proxy.init(collectorURL, this.waitBetweenPublications);
        }

        // assign events to publish proxies
        final int segment = (nbPublications / this.nbPublishers);

        for (int i = 0; i < this.nbPublishers; i++) {
            final CustomPublishProxy publishProxy = publishProxies.get(i);

            int start = i * segment;
            int end = (i + 1) * segment - 1;

            PublishSubscribeBenchmark.this.assignSetOfevents(
                    publishProxy, events, start, end);

        }

        int rest = nbPublications % this.nbPublishers;
        if (rest != 0) {
            int start = this.nbPublishers * segment;
            this.assignSetOfevents(publishProxies.get(0), events, start, start
                    + rest - 1);
        }

        this.endToEndMeasurement.setEntryTime();

        // triggers publications (publish events)
        for (CustomPublishProxy proxy : publishProxies) {
            proxy.publish();
        }

        // timeout after 10 hours
        collector.waitForAllPublisherReports(36000000);

        this.pointToPointEntryMeasurements =
                collector.getPointToPointEntryMeasurements();

        // timeout after 10 hours
        collector.waitForAllSubscriberReports(36000000);

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

        // count number of quadruples per peer
        int totalNumberOfQuadruples = 0;

        if (EventCloudProperties.isSbce3PubSubAlgorithmUsed()) {
            // waits a little because some quadruples may not have been yet
            // stored on peers due to the fact that the matching between a CE
            // and a subscription is performed on one peer and the storage of
            // quadruples on multiple peers. This issue does not occur with
            // SBCE1 due to the reconstruction step that ensures all quadruples
            // are stored before to deliver the final notification. However,
            // this issue may occur with all the algorithms when a signal or
            // binding notification listener is used.
            try {
                Thread.sleep(EventCloudProperties.PUBLISH_COMPOUND_EVENT_DELAYER_TIMEOUT.getValue() * 2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        List<Peer> peers = deployer.getRandomSemanticTracker().getPeers();

        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < peers.size(); i++) {
            @SuppressWarnings("unchecked")
            GenericResponseOperation<Integer> response =
                    (GenericResponseOperation<Integer>) PAFuture.getFutureValue(peers.get(
                            i)
                            .receive(new CountQuadruplesOperation()));

            totalNumberOfQuadruples += response.getValue();

            buf.append(response.getValue());
            if (i < peers.size() - 1) {
                buf.append(" ");
            } else {
                buf.append(", sum=");
                buf.append(totalNumberOfQuadruples);
                buf.append('\n');
            }
        }

        System.out.println("  Quadruples distribution on peer: "
                + buf.toString());

        recorder.reportValue(
                NB_QUADRUPLES_PER_PEER_CATEGORY, totalNumberOfQuadruples
                        / this.nbPeers);

        deployer.undeploy();
        ComponentUtils.terminateComponent(registryURL);

        if (nodeProvider != null) {
            nodeProvider.terminate();
        }
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

    private Event[] createEventsForUniformDistribution(EventCloudDeployer deployer) {

        return this.createEvents(deployer, new EventProvider() {
            @Override
            public Event get(SemanticZone[] zones, int nbQuadruplesPerCE,
                             int rdfTermSize) {
                return EventGenerator.randomCompoundEvent(
                        zones, nbQuadruplesPerCE, rdfTermSize);
            }
        });
    }

    private Event[] createEventsForUniformDistributionAndRewritingSteps(EventCloudDeployer deployer) {
        return this.createEvents(deployer, new EventProvider() {
            @Override
            public Event get(SemanticZone[] zones, int nbQuadruplesPerCE,
                             int rdfTermSize) {
                return EventGenerator.randomCompoundEventForRewriting(
                        zones, nbQuadruplesPerCE, rdfTermSize,
                        PublishSubscribeBenchmark.this.rewritingLevel);
            }
        });
    }

    private Event[] createEvents(EventCloudDeployer deployer,
                                 EventProvider provider) {
        EventGenerator.reset();

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
                throw new UnsupportedOperationException();
            } else {
                events[i] =
                        provider.get(
                                zones, this.nbQuadruplesPerCompoundEvent, 10);
            }
        }

        return events;
    }

    private static interface EventProvider {

        Event get(SemanticZone[] zones, int nbQuadruplesPerCE, int rdfTermSize);

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

    private List<CustomPublishProxy> createPublishProxies(String registryUrl,
                                                          EventCloudId id,
                                                          NodeProvider nodeProvider,
                                                          int nbPublishProxies)
            throws EventCloudIdNotManaged {
        List<CustomPublishProxy> result =
                new ArrayList<CustomPublishProxy>(nbPublishProxies);

        for (int i = 0; i < nbPublishProxies; i++) {
            CustomPublishProxy publishProxy;

            if (nodeProvider == null) {
                publishProxy =
                        CustomProxyFactory.newCustomPublishProxy(
                                registryUrl, id);
            } else {
                publishProxy =
                        CustomProxyFactory.newCustomPublishProxy(
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

                if (this.uniformDataDistribution) {
                    buf.append("?p");
                    buf.append(i);
                } else {
                    buf.append("<urn:p");
                    buf.append(i);
                    buf.append(">");
                }

                buf.append(" ?o");
                buf.append(i);
                buf.append(" . ");
            }
            buf.append("} }");

            subscription = buf.toString();
        }

        return subscription;
    }

    private void assignSetOfevents(CustomPublishProxy publishProxy,
                                   Event[] events, int start, int end) {
        publishProxy.assignEvents(Arrays.copyOfRange(events, start, end + 1));
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
