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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeoutException;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.deployment.NodeProvider;
import org.objectweb.proactive.extensions.p2p.structured.deployment.gcmdeployment.GcmDeploymentNodeProvider;
import org.objectweb.proactive.extensions.p2p.structured.deployment.local.LocalNodeProvider;
import org.objectweb.proactive.extensions.p2p.structured.operations.CanOperations;
import org.objectweb.proactive.extensions.p2p.structured.operations.GenericResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.GetIdAndZoneResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.providers.InjectionConstraintsProvider;
import org.objectweb.proactive.extensions.p2p.structured.providers.SerializableProvider;
import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.Category;
import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.MicroBenchmark;
import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.MicroBenchmarkService;
import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.StatsRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.common.base.Supplier;
import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.EventCloudDescription;
import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.SubscribeApi;
import fr.inria.eventcloud.api.Subscription;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.BindingNotificationListener;
import fr.inria.eventcloud.api.listeners.CompoundEventNotificationListener;
import fr.inria.eventcloud.api.listeners.NotificationListener;
import fr.inria.eventcloud.api.listeners.NotificationListenerType;
import fr.inria.eventcloud.api.listeners.SignalNotificationListener;
import fr.inria.eventcloud.benchmarks.pubsub.converters.ListenerTypeConverter;
import fr.inria.eventcloud.benchmarks.pubsub.converters.SubscriptionTypeConverter;
import fr.inria.eventcloud.benchmarks.pubsub.listeners.CustomBindingListener;
import fr.inria.eventcloud.benchmarks.pubsub.listeners.CustomCompoundEventListener;
import fr.inria.eventcloud.benchmarks.pubsub.listeners.CustomSignalListener;
import fr.inria.eventcloud.benchmarks.pubsub.measurements.CumulatedMeasurement;
import fr.inria.eventcloud.benchmarks.pubsub.measurements.SimpleMeasurement;
import fr.inria.eventcloud.benchmarks.pubsub.messages.RetrieveStorageEndTimesRequest;
import fr.inria.eventcloud.benchmarks.pubsub.messages.RetrieveStorageEndTimesResponse;
import fr.inria.eventcloud.benchmarks.pubsub.operations.ClearOperation;
import fr.inria.eventcloud.benchmarks.pubsub.overlay.CustomSemanticOverlayProvider;
import fr.inria.eventcloud.benchmarks.pubsub.proxies.CustomProxyFactory;
import fr.inria.eventcloud.benchmarks.pubsub.proxies.CustomPublishProxy;
import fr.inria.eventcloud.benchmarks.pubsub.suppliers.CompoundEventSupplier;
import fr.inria.eventcloud.benchmarks.pubsub.suppliers.QuadrupleSupplier;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.deployment.EventCloudComponentsManager;
import fr.inria.eventcloud.deployment.EventCloudDeployer;
import fr.inria.eventcloud.deployment.EventCloudDeploymentDescriptor;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.factories.EventCloudComponentsManagerFactory;
import fr.inria.eventcloud.factories.EventCloudsRegistryFactory;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.operations.can.CountQuadruplesOperation;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.overlay.can.SemanticCoordinate;
import fr.inria.eventcloud.overlay.can.SemanticPointFactory;
import fr.inria.eventcloud.overlay.can.SemanticZone;
import fr.inria.eventcloud.proxies.PutGetProxy;
import fr.inria.eventcloud.proxies.SubscribeProxy;

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

    private static final String PUBLICATIONS_STORAGE_TIME =
            "publicationsStorageTime";

    private static final String SUBSCRIPTIONS_STORAGE_TIME =
            "subscriptionsStorageTime";

    private static final String BENCHMARK_STATS_COLLECTOR_NAME =
            "benchmark-stats-collector";

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

    @Parameter(names = {"--nb-subscriptions-per-subscriber", "-nsps"}, description = "The number of subscriptions per subscriber")
    public int nbSubscriptionsPerSubscriber = 1;

    @Parameter(names = {"-dr", "--dry-runs"}, description = "Indicates the number of first runs to discard")
    public int discardFirstRuns = 1;

    @Parameter(names = {"--wait-between-publications"}, description = "The time to wait (in ms) between each publication from a publisher")
    public int waitBetweenPublications = 0;

    // a rewriting level of 0 means no rewrite
    @Parameter(names = {"-rl", "--rewriting-level"}, description = "Indicates the number of rewrites to force before delivering a notification")
    public int rewritingLevel = 0;

    @Parameter(names = {"-st", "--subscription-type"}, description = "Indicates the type of the subscription used by the subscribers to subscribe", converter = SubscriptionTypeConverter.class)
    public SubscriptionType subscriptionType = SubscriptionType.ACCEPT_ALL;

    // when set to true each subscriber will receive and wait for
    // nbPublications/nbSubscribers, otherwise each one will receive and wait
    // for nbPublications. It means that in the later case we publish
    // nbPublications * nbSubscribers events
    @Parameter(names = {"-ds", "--different-subscriptions"}, description = "Indicates whether different subscriptions matching different data should be used when several subscribers are defined")
    public boolean useDifferentSubscriptions = false;

    @Parameter(names = {"--publish-quadruples"}, description = "Indicates whether events must be emitted as quadruples (default CEs)")
    public boolean publishIndependentQuadruples = false;

    @Parameter(names = {"--listener-type", "-lt"}, description = "The listener type used by all the subscribers for subscribing", converter = ListenerTypeConverter.class)
    public NotificationListenerType listenerType =
            NotificationListenerType.COMPOUND_EVENT;

    @Parameter(names = {"-imds", "--in-memory-datastore"}, description = "Specifies whether datastores on peers have to be persisted on disk or not")
    public boolean inMemoryDatastore = false;

    @Parameter(names = {"-negr", "--nb-event-generation-rounds"}, description = "When combined with uniform data distribution, the specified number of event sets are generated and only event set with the best standard deviation is kept")
    public int nbEventGenerationRounds = 1;

    @Parameter(names = {"-rts", "--rdf-term-size"}, description = "The size of each RDF term generated")
    private int rdfTermSize = 10;

    @Parameter(names = {"-gcma", "--gcma-descriptor"}, description = "Path to the GCMA descriptor to use for deploying the benchmark entities on several machines")
    public String gcmaDescriptor = null;

    @Parameter(names = {"--disable-inter-ces-shuffling"}, description = "Indicates whether the shuffling of the generated set of compounds events should be disabled or not")
    public boolean disableInterCompoundEventsShuffling = false;

    @Parameter(names = {"--disable-intra-ces-shuffling"}, description = "Indicates whether the shuffling of the quadruples inside the generated compound events must be disabled or not")
    public boolean disableIntraCompoundEventsShuffling = false;

    @Parameter(names = {"-mst", "--measure-storage-time"}, description = "Measure the time elapsed between the beginning of the benchmark and when the publications/subscriptions have been stored on the peers")
    public boolean measureStorageTime = false;

    @Parameter(names = {"-h", "--help"}, description = "Print help", help = true)
    public boolean help;

    // category names

    public static final String END_TO_END_MEASUREMENT_CATEGORY =
            "endToEndMeasurement";

    public static final String OUTPUT_MEASUREMENT_CATEGORY =
            "outputMeasurement";

    public static final String POINT_TO_POINT_MEASUREMENT_CATEGORY =
            "pointToPointMeasurement";

    // measurements

    /*
     * Measure the time taken to receive all the events for a given 
     * subscription id by considering the time when the publications start to 
     * be published and the time when all the notifications are received on all 
     * the subscribers
     */
    private long endToEndMeasurementEntryTime;

    private Map<SubscriptionId, Long> endToEndMeasurementsExitTime;

    /*
     * Measure the time to receive all the events for a given subscription id 
     * by considering the time when the first event is received on the 
     * subscriber and the time when the last event is received on the 
     * subscriber (used to count the throughput in terms of notifications per 
     * second on the subscriber side)
     */
    private Map<SubscriptionId, SimpleMeasurement> outputMeasurements;

    /*
     * Measures the time to receive each event independently (i.e. average latency)
     */
    private Map<String, Long> pointToPointEntryMeasurements;

    private Map<SubscriptionId, CumulatedMeasurement> pointToPointExitMeasurements;

    // internal

    private List<Subscription> subscriptions;

    private Map<SubscriptionId, NotificationListener<?>> listeners;

    @SuppressWarnings("unused")
    private Supplier<? extends Event> supplier;

    // generated data to reuse for each run.
    // the CAN network topology is assumed to be the same at each run
    private SyntheticSubscription[] syntheticSubscriptions;

    private Event[] events;

    public PublishSubscribeBenchmark() {
        // WARNING: setting property values here set them only for the JVM where
        // this class is executed not on all JVMs associated to peers. This
        // should be done through the GCMA configuration file

        // alphabetic lower case interval
        P2PStructuredProperties.CAN_LOWER_BOUND.setValue(0x61);
        P2PStructuredProperties.CAN_UPPER_BOUND.setValue(0x7A);
    }

    public PublishSubscribeBenchmark(String gcma, int nbRuns,
            int discardFirstRuns, int nbPublishers, int nbPeers,
            int nbSubscribers, int nbPublications,
            int nbQuadruplesPerCompoundEvent, int rdfTermSize,
            int nbSubscriptionsPerSubscriber,
            boolean publishIndependentQuadruples, int waitBetweenPublications,
            int rewritingLevel, SubscriptionType subscriptionType,
            boolean useDifferentSubscriptions,
            NotificationListenerType listenerType, boolean inMemoryDatastore,
            int nbEventGenerationRounds,
            boolean disableInterCompoundEventsShuffling,
            boolean disableIntraCompoundEventsShuffling,
            boolean measureStorageTime) {
        this(nbRuns, discardFirstRuns, nbPublishers, nbPeers, nbSubscribers,
                nbPublications, nbQuadruplesPerCompoundEvent, rdfTermSize,
                nbSubscriptionsPerSubscriber, publishIndependentQuadruples,
                waitBetweenPublications, rewritingLevel, subscriptionType,
                useDifferentSubscriptions, listenerType, inMemoryDatastore,
                nbEventGenerationRounds, disableInterCompoundEventsShuffling,
                disableIntraCompoundEventsShuffling, measureStorageTime);
        this.gcmaDescriptor = gcma;
    }

    public PublishSubscribeBenchmark(int nbRuns, int discardFirstRuns,
            int nbPublishers, int nbPeers, int nbSubscribers,
            int nbPublications, int nbQuadruplesPerCompoundEvent,
            int rdfTermSize, int nbSubscriptionsPerSubscriber,
            boolean publishIndependentQuadruples, int waitBetweenPublications,
            int rewritingLevel, SubscriptionType subscriptionType,
            boolean useDifferentSubscriptions,
            NotificationListenerType listenerType, boolean inMemoryDatastore,
            int nbEventGenerationRounds,
            boolean disableInterCompoundEventsShuffling,
            boolean disableIntraCompoundEventsShuffling,
            boolean measureStorageTime) {
        this();
        this.nbRuns = nbRuns;
        this.discardFirstRuns = discardFirstRuns;
        this.nbPublishers = nbPublishers;
        this.nbPeers = nbPeers;
        this.nbSubscribers = nbSubscribers;
        this.nbQuadruplesPerCompoundEvent = nbQuadruplesPerCompoundEvent;
        this.rdfTermSize = rdfTermSize;
        this.nbSubscriptionsPerSubscriber = nbSubscriptionsPerSubscriber;
        this.publishIndependentQuadruples = publishIndependentQuadruples;
        this.waitBetweenPublications = waitBetweenPublications;
        this.rewritingLevel = rewritingLevel;
        this.subscriptionType = subscriptionType;
        this.useDifferentSubscriptions = useDifferentSubscriptions;
        this.listenerType = listenerType;
        this.inMemoryDatastore = inMemoryDatastore;
        this.nbEventGenerationRounds = nbEventGenerationRounds;
        this.disableInterCompoundEventsShuffling =
                disableInterCompoundEventsShuffling;
        this.disableIntraCompoundEventsShuffling =
                disableIntraCompoundEventsShuffling;
        this.measureStorageTime = measureStorageTime;

        PublishSubscribeBenchmark.nbPublications = nbPublications;
    }

    public static void main(String[] args) {
        PublishSubscribeBenchmark benchmark = new PublishSubscribeBenchmark();
        benchmark.parseArguments(args);
        benchmark.execute();

        System.exit(0);
    }

    public void parseArguments(String[] args) {
        JCommander jCommander = new JCommander(this);

        try {
            jCommander.parse(args);

            if (this.help) {
                jCommander.usage();
                System.exit(0);
            }
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

    }

    private void logParameterValues() {
        log.info("Benchmark starting with the following parameters:");
        log.info("  compoundEventSize -> {}", this.nbQuadruplesPerCompoundEvent);
        log.info("  dryRuns -> {}", this.discardFirstRuns);
        log.info("  gcmaDescriptor -> {}", this.gcmaDescriptor);
        log.info("  inMemoryDatastore -> {}", this.inMemoryDatastore);
        log.info("  listenerType -> {}", this.listenerType);
        log.info("  measureStorageTime -> {}", this.measureStorageTime);
        log.info(
                "  nbEventGenerationRounds -> {}", this.nbEventGenerationRounds);
        log.info("  nbPeers -> {}", this.nbPeers);
        log.info("  nbPublications -> {}", nbPublications);
        log.info("  nbPublishers -> {}", this.nbPublishers);
        log.info("  nbRuns -> {}", this.nbRuns);
        log.info("  nbSubscribers -> {}", this.nbSubscribers);
        log.info(
                "  nbSubscriptionsPerSubscriber -> {}",
                this.nbSubscriptionsPerSubscriber);
        log.info("  publishQuadruples -> {}", this.publishIndependentQuadruples);
        log.info("  rewritingLevel -> {}", this.rewritingLevel);
        log.info(
                "  disableInterCompoundEventsShuffling -> {}",
                this.disableInterCompoundEventsShuffling);
        log.info(
                "  disableIntraCompoundEventsShuffling -> {}",
                this.disableIntraCompoundEventsShuffling);
        log.info("  subscriptionType -> {}", this.subscriptionType);
        log.info(
                "  waitBetweenPublications -> {}", this.waitBetweenPublications);
    }

    public StatsRecorder execute() {
        this.logParameterValues();
        this.initInstanceFields();

        // creates and runs micro benchmark
        MicroBenchmark microBenchmark =
                new MicroBenchmark(this.nbRuns, new MicroBenchmarkService() {

                    private EventCloudsRegistry registry;

                    private BenchmarkStatsCollector collector;

                    private String collectorURL;

                    private NodeProvider nodeProvider;

                    private EventCloudDeployer deployer;

                    private PutGetProxy putgetProxy;

                    private List<CustomPublishProxy> publishProxies;

                    private List<SubscribeApi> subscribeProxies;

                    private SyntheticSubscription[] synthethicSubscriptions;

                    @Override
                    public void setup() throws Exception {
                        this.collector =
                                PAActiveObject.newActive(
                                        BenchmarkStatsCollector.class,
                                        new Object[] {
                                                PublishSubscribeBenchmark.this.nbPublishers,
                                                PublishSubscribeBenchmark.this.nbSubscribers,
                                                PublishSubscribeBenchmark.this.nbSubscriptionsPerSubscriber,
                                                (PublishSubscribeBenchmark.this.nbQuadruplesPerCompoundEvent + 1)
                                                        * nbPublications});
                        this.collectorURL =
                                PAActiveObject.registerByName(
                                        this.collector,
                                        BENCHMARK_STATS_COLLECTOR_NAME);

                        this.nodeProvider =
                                PublishSubscribeBenchmark.this.createNodeProviderIfRequired();

                        EventCloudDeploymentDescriptor descriptor =
                                PublishSubscribeBenchmark.this.createDeploymentDescriptor(
                                        this.nodeProvider, this.collectorURL);

                        EventCloudComponentsManager componentPoolManager =
                                EventCloudComponentsManagerFactory.newComponentsManager(
                                        this.nodeProvider,
                                        1,
                                        PublishSubscribeBenchmark.this.nbPeers,
                                        PublishSubscribeBenchmark.this.nbPublishers,
                                        PublishSubscribeBenchmark.this.nbSubscribers,
                                        0);

                        componentPoolManager.start();

                        this.deployer =
                                new EventCloudDeployer(
                                        new EventCloudDescription(),
                                        descriptor, componentPoolManager);
                        this.deployer.deploy(
                                1, PublishSubscribeBenchmark.this.nbPeers);

                        SemanticZone[] zones =
                                PublishSubscribeBenchmark.this.retrievePeerZones(this.deployer);

                        this.synthethicSubscriptions =
                                PublishSubscribeBenchmark.this.getOrCreateSubscriptions(zones);

                        Event[] events =
                                PublishSubscribeBenchmark.this.createEvents(
                                        this.deployer, zones,
                                        this.synthethicSubscriptions);

                        if (!PublishSubscribeBenchmark.this.disableInterCompoundEventsShuffling) {
                            org.objectweb.proactive.extensions.p2p.structured.utils.Arrays.shuffle(events);
                        }

                        this.registry =
                                PublishSubscribeBenchmark.this.deployRegistry(
                                        this.deployer, this.nodeProvider);

                        String registryURL = null;
                        try {
                            registryURL = this.registry.register("registry");
                        } catch (ProActiveException e) {
                            throw new IllegalStateException(e);
                        }

                        EventCloudId eventCloudId =
                                this.deployer.getEventCloudDescription()
                                        .getId();

                        this.putgetProxy =
                                (PutGetProxy) ProxyFactory.newPutGetProxy(
                                        registryURL, eventCloudId);

                        this.publishProxies =
                                PublishSubscribeBenchmark.this.createPublishProxies(
                                        this.nodeProvider, registryURL,
                                        eventCloudId);
                        this.subscribeProxies =
                                PublishSubscribeBenchmark.this.createSubscribeProxies(
                                        this.nodeProvider, registryURL,
                                        eventCloudId);

                        // init custom publish proxies
                        for (CustomPublishProxy proxy : this.publishProxies) {
                            proxy.init(
                                    this.collectorURL,
                                    PublishSubscribeBenchmark.this.waitBetweenPublications);
                        }

                        // assign events to publish proxies
                        final int segment =
                                (nbPublications / PublishSubscribeBenchmark.this.nbPublishers);

                        for (int i = 0; i < PublishSubscribeBenchmark.this.nbPublishers; i++) {
                            final CustomPublishProxy publishProxy =
                                    this.publishProxies.get(i);

                            int start = i * segment;
                            int end = (i + 1) * segment - 1;

                            PublishSubscribeBenchmark.this.assignSetOfevents(
                                    publishProxy,
                                    PublishSubscribeBenchmark.this.events,
                                    start, end);

                        }

                        int rest =
                                nbPublications
                                        % PublishSubscribeBenchmark.this.nbPublishers;
                        if (rest != 0) {
                            int start =
                                    PublishSubscribeBenchmark.this.nbPublishers
                                            * segment;
                            PublishSubscribeBenchmark.this.assignSetOfevents(
                                    this.publishProxies.get(0),
                                    PublishSubscribeBenchmark.this.events,
                                    start, start + rest - 1);
                        }

                        if (this.synthethicSubscriptions.length > 0) {
                            log.info("The subscriptions used by the subscriber(s) are the following:");
                            for (SyntheticSubscription subscription : this.synthethicSubscriptions) {
                                log.info("  {}", subscription.content);
                            }
                        } else {
                            log.info("No subscription registered");
                        }
                    }

                    @Override
                    public void run(StatsRecorder recorder) {
                        try {
                            this.runBenchmark(recorder);

                            long maxEndToEndMeasurement = 0;

                            for (int i = 0; i < PublishSubscribeBenchmark.this.subscriptions.size(); i++) {
                                SubscriptionId subscriptionId =
                                        PublishSubscribeBenchmark.this.subscriptions.get(
                                                i)
                                                .getId();

                                long endToEndElapsedTime =
                                        PublishSubscribeBenchmark.this.endToEndMeasurementsExitTime.get(subscriptionId)
                                                - PublishSubscribeBenchmark.this.endToEndMeasurementEntryTime;

                                if (endToEndElapsedTime > maxEndToEndMeasurement) {
                                    maxEndToEndMeasurement =
                                            endToEndElapsedTime;
                                }

                                recorder.reportValue(
                                        END_TO_END_MEASUREMENT_CATEGORY + i,
                                        endToEndElapsedTime);

                                recorder.reportValue(
                                        OUTPUT_MEASUREMENT_CATEGORY + i,
                                        PublishSubscribeBenchmark.this.outputMeasurements.get(
                                                subscriptionId)
                                                .getElapsedTime());

                                recorder.reportValue(
                                        POINT_TO_POINT_MEASUREMENT_CATEGORY + i,
                                        PublishSubscribeBenchmark.this.pointToPointExitMeasurements.get(
                                                subscriptionId)
                                                .getElapsedTime(
                                                        PublishSubscribeBenchmark.this.pointToPointEntryMeasurements));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.exit(1);
                        }
                    }

                    public void runBenchmark(StatsRecorder recorder)
                            throws TimeoutException {
                        // subscribes
                        long subscribeStartTime = System.currentTimeMillis();

                        for (int i = 0; i < this.synthethicSubscriptions.length; i++) {
                            final SubscribeApi subscribeProxy =
                                    this.subscribeProxies.get(i
                                            / PublishSubscribeBenchmark.this.nbSubscriptionsPerSubscriber);
                            Subscription subscription =
                                    new Subscription(
                                            this.synthethicSubscriptions[i].content);

                            PublishSubscribeBenchmark.this.subscriptions.add(subscription);

                            PublishSubscribeBenchmark.this.listeners.put(
                                    subscription.getId(),
                                    PublishSubscribeBenchmark.this.subscribe(
                                            this.collector,
                                            subscribeProxy,
                                            subscription,
                                            PublishSubscribeBenchmark.this.listenerType,
                                            this.synthethicSubscriptions[i].nbEventsExpected));
                        }

                        // waits that subscriptions are indexed as the process
                        // is asynchronous
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        PublishSubscribeBenchmark.this.endToEndMeasurementEntryTime =
                                System.currentTimeMillis();

                        // triggers publications (publish events)
                        for (CustomPublishProxy proxy : this.publishProxies) {
                            proxy.publish();
                        }

                        // timeout after 1 hour
                        this.collector.waitForAllPublisherReports(3600000);

                        PublishSubscribeBenchmark.this.pointToPointEntryMeasurements =
                                this.collector.getPointToPointEntryMeasurements();

                        // timeout after 1 hour
                        this.collector.waitForAllSubscriberReports(3600000);

                        if (PublishSubscribeBenchmark.this.measureStorageTime) {
                            // timeout after 1 hour
                            this.collector.waitForStoringQuadruples(3600000);
                        }

                        long globalEndToEndTerminationTime =
                                System.currentTimeMillis();

                        recorder.reportValue(
                                MicroBenchmark.DEFAULT_CATEGORY_NAME,
                                globalEndToEndTerminationTime
                                        - PublishSubscribeBenchmark.this.endToEndMeasurementEntryTime);

                        // the end to end termination time is the time at which
                        // each subscriber has notified the collector about its
                        // termination
                        PublishSubscribeBenchmark.this.endToEndMeasurementsExitTime =
                                this.collector.getEndToEndTerminationTimes();

                        // collects output measurements
                        for (Entry<SubscriptionId, SimpleMeasurement> entry : this.collector.getOutputMeasurements()
                                .entrySet()) {
                            PublishSubscribeBenchmark.this.outputMeasurements.put(
                                    entry.getKey(), entry.getValue());
                        }

                        // collects point-to-point exit measurements
                        for (Entry<SubscriptionId, CumulatedMeasurement> entry : this.collector.getPointToPointExitMeasurements()
                                .entrySet()) {
                            PublishSubscribeBenchmark.this.pointToPointExitMeasurements.put(
                                    entry.getKey(), entry.getValue());
                        }

                        if (EventCloudProperties.isSbce2PubSubAlgorithmUsed()
                                || EventCloudProperties.isSbce3PubSubAlgorithmUsed()) {
                            // waits a little because some quadruples may not
                            // have been yet stored on peers due to the fact
                            // that the matching between a CE and a subscription
                            // is performed on one peer and the storage of
                            // quadruples on multiple peers. This issue does not
                            // occur with SBCE1 due to the reconstruction step
                            // that ensures all quadruples are stored before to
                            // deliver the final notification. However, this
                            // issue may occur with all the algorithms when a
                            // signal or binding notification listener is used.
                            try {
                                Thread.sleep(EventCloudProperties.PUBLISH_SUBSCRIBE_OPERATIONS_DELAYER_TIMEOUT.getValue() * 2);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        // count number of quadruples per peer
                        int totalNumberOfQuadruples =
                                PublishSubscribeBenchmark.this.logRunStatistics(this.deployer);

                        recorder.reportValue(
                                NB_QUADRUPLES_PER_PEER_CATEGORY,
                                totalNumberOfQuadruples
                                        / PublishSubscribeBenchmark.this.nbPeers);

                        if (PublishSubscribeBenchmark.this.measureStorageTime) {
                            RetrieveStorageEndTimesResponse response =
                                    (RetrieveStorageEndTimesResponse) PAFuture.getFutureValue(this.putgetProxy.send(new RetrieveStorageEndTimesRequest()));

                            long publicationsStorageTime =
                                    response.getResult()
                                            .getPublicationsEndTime()
                                            - PublishSubscribeBenchmark.this.endToEndMeasurementEntryTime;
                            long subscriptionsStorageTime =
                                    (PublishSubscribeBenchmark.this.nbSubscribers == 0 || PublishSubscribeBenchmark.this.nbSubscriptionsPerSubscriber == 0)
                                            ? 0 : response.getResult()
                                                    .getSubscriptionsEndTime()
                                                    - subscribeStartTime;

                            log.info(
                                    "Time required to store publications: {}",
                                    publicationsStorageTime);
                            log.info(
                                    "Time required to store subscriptions: {}",
                                    subscriptionsStorageTime);

                            recorder.reportValue(
                                    PUBLICATIONS_STORAGE_TIME,
                                    publicationsStorageTime);

                            recorder.reportValue(
                                    SUBSCRIPTIONS_STORAGE_TIME,
                                    subscriptionsStorageTime);
                        }
                    }

                    @Override
                    public void clear() throws Exception {
                        PublishSubscribeBenchmark.this.listeners.clear();
                        PublishSubscribeBenchmark.this.subscriptions.clear();

                        // clears results collected during previous run
                        if (PublishSubscribeBenchmark.this.endToEndMeasurementsExitTime != null) {
                            PublishSubscribeBenchmark.this.endToEndMeasurementsExitTime.clear();
                        }

                        PublishSubscribeBenchmark.this.outputMeasurements.clear();
                        PublishSubscribeBenchmark.this.pointToPointExitMeasurements.clear();
                        PublishSubscribeBenchmark.this.pointToPointEntryMeasurements.clear();

                        List<ResponseOperation> futures =
                                new ArrayList<ResponseOperation>();

                        for (Peer p : this.deployer.getRandomTracker()
                                .getPeers()) {
                            futures.add(p.receive(new ClearOperation()));
                        }

                        PAFuture.waitForAll(futures);

                        this.collector.clear();

                        for (CustomPublishProxy cpp : this.publishProxies) {
                            cpp.clear();
                        }

                        for (SubscribeApi subscribeProxy : this.subscribeProxies) {
                            ((SubscribeProxy) subscribeProxy).clear();
                        }
                    }

                    @Override
                    public void teardown() throws Exception {
                        PublishSubscribeBenchmark.this.undeploy(
                                this.nodeProvider, this.deployer,
                                this.registry, this.collectorURL);
                    }

                });
        microBenchmark.discardFirstRuns(this.discardFirstRuns);
        microBenchmark.showProgress();
        microBenchmark.execute();

        System.out.println(this.createBenchmarkReport(microBenchmark));

        return microBenchmark.getStatsRecorder();
    }

    private void initInstanceFields() {
        if (this.rewritingLevel < 0) {
            throw new IllegalStateException("Illegal rewriting level: "
                    + this.rewritingLevel);
        }

        this.listeners =
                new HashMap<SubscriptionId, NotificationListener<?>>(
                        this.nbSubscribers);
        this.subscriptions =
                new ArrayList<Subscription>(this.nbSubscribers
                        * this.nbSubscriptionsPerSubscriber);

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
    }

    private String createBenchmarkReport(MicroBenchmark microBenchmark) {
        StringBuilder statsBuffer = new StringBuilder();

        statsBuffer.append('\n').append(this.nbRuns).append(" run(s)");
        statsBuffer.append('\n');

        Category nbQuadsPerPeer =
                microBenchmark.getStatsRecorder().getCategory(
                        NB_QUADRUPLES_PER_PEER_CATEGORY);

        statsBuffer.append("  Average number of quadruples per peer is ");
        statsBuffer.append(nbQuadsPerPeer.getMean()).append('\n');

        if (this.measureStorageTime) {
            statsBuffer.append("  Average time required to store publications is ");
            statsBuffer.append(microBenchmark.getStatsRecorder().getCategory(
                    PUBLICATIONS_STORAGE_TIME).getMean());
            statsBuffer.append('\n');
            statsBuffer.append("  Average time required to store subscriptions is ");
            statsBuffer.append(microBenchmark.getStatsRecorder().getCategory(
                    SUBSCRIPTIONS_STORAGE_TIME).getMean());
        }

        statsBuffer.append("\n\n");

        double endToEndSum = 0;
        double pointToPointSum = 0;
        double outputSum = 0;

        for (int i = 0; i < this.nbSubscribers
                * this.nbSubscriptionsPerSubscriber; i++) {
            statsBuffer.append("Benchmark results for subscription ");
            statsBuffer.append(i + 1);
            statsBuffer.append('\n');

            Category endToEndCategory =
                    microBenchmark.getStatsRecorder().getCategory(
                            END_TO_END_MEASUREMENT_CATEGORY + i);
            Category outputCategory =
                    microBenchmark.getStatsRecorder().getCategory(
                            OUTPUT_MEASUREMENT_CATEGORY + i);
            Category pointToPointCategory =
                    microBenchmark.getStatsRecorder().getCategory(
                            POINT_TO_POINT_MEASUREMENT_CATEGORY + i);

            double endToEndAverageThroughput =
                    nbPublications / (endToEndCategory.getMean() / 1000);
            double pointToPointAverageThroughput =
                    pointToPointCategory.getMean() / nbPublications;
            double outputAverageThroughput =
                    nbPublications / (outputCategory.getMean() / 1000);

            endToEndSum += endToEndAverageThroughput;
            pointToPointSum += pointToPointAverageThroughput;
            outputSum += outputAverageThroughput;

            statsBuffer.append("  End-to-End measurement, average=");
            statsBuffer.append(endToEndCategory.getMean()).append(", median=");
            statsBuffer.append(endToEndCategory.getMedian());
            statsBuffer.append(", average throughput=");
            statsBuffer.append(endToEndAverageThroughput);
            statsBuffer.append('\n');

            statsBuffer.append("  Point-to-Point measurement, average=");
            statsBuffer.append(pointToPointCategory.getMean()).append(
                    ", median=");
            statsBuffer.append(pointToPointCategory.getMedian());
            statsBuffer.append(", average latency=");
            statsBuffer.append(pointToPointAverageThroughput);
            statsBuffer.append('\n');

            statsBuffer.append("  Output measurement, average=");
            statsBuffer.append(outputCategory.getMean()).append(", median=");
            statsBuffer.append(outputCategory.getMedian());
            statsBuffer.append(", average throughput=");
            statsBuffer.append(outputAverageThroughput);
            statsBuffer.append('\n');
        }

        int nbSubscriptions = this.listeners.keySet().size();

        if (nbSubscriptions > 1) {
            statsBuffer.append('\n');
            statsBuffer.append("Average benchmark results\n");
            statsBuffer.append(" End-to-End measurement, average throughput=");
            statsBuffer.append(endToEndSum / nbSubscriptions);
            statsBuffer.append('\n');
            statsBuffer.append(" Point-to-Point measurement, average latency=");
            statsBuffer.append(pointToPointSum / nbSubscriptions);
            statsBuffer.append('\n');
            statsBuffer.append(" Output measurement, average throughput=");
            statsBuffer.append(outputSum / nbSubscriptions);
            statsBuffer.append('\n');
        }

        return statsBuffer.toString();
    }

    private void undeploy(NodeProvider nodeProvider,
                          EventCloudDeployer deployer,
                          EventCloudsRegistry registry, String collectorURL)
            throws IOException {
        registry.unregister();
        deployer.undeploy();

        deployer.getComponentPoolManager().stop();
        PAActiveObject.terminateActiveObject(
                deployer.getComponentPoolManager(), false);

        if (nodeProvider != null) {
            nodeProvider.terminate();
        }

        PAActiveObject.unregister(collectorURL);
    }

    private int logRunStatistics(EventCloudDeployer deployer) {
        int totalNumberOfQuadruples = 0;

        if (log.isDebugEnabled()) {
            List<Peer> peers = deployer.getRandomSemanticTracker().getPeers();

            log.debug("Peers dump:");
            for (Peer p : deployer.getRandomSemanticTracker().getPeers()) {
                log.debug(p.dump());
            }

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

                    if (i < peers.size() - 1) {
                        buf.append('\n');
                    }
                }
            }

            log.debug("Quadruples distribution on peers: " + buf.toString());
        }

        return totalNumberOfQuadruples;
    }

    private Event[] createEvents(EventCloudDeployer deployer,
                                 SemanticZone[] zones,
                                 SyntheticSubscription[] subscriptions) {
        if (this.events == null) {
            if (!this.useDifferentSubscriptions) {
                this.events =
                        this.computeGenerationsAndSelectBest(
                                deployer, zones, subscriptions.length == 0
                                        ? null
                                        : subscriptions[0].fixedPredicates,
                                nbPublications);

                for (int i = 0; i < subscriptions.length; i++) {
                    subscriptions[i].nbEventsExpected = nbPublications;
                }
            } else {
                Event[] events = new Event[nbPublications];

                if (nbPublications < subscriptions.length) {
                    throw new IllegalStateException(
                            "Number of publications lower than number of subscriptions. Case not managed");
                }

                int nbEventsPerSubscriber =
                        nbPublications / subscriptions.length;
                int extraEvents = nbPublications % subscriptions.length;

                for (int i = 0; i < subscriptions.length; i++) {
                    Event[] generatedEvents =
                            this.computeGenerationsAndSelectBest(
                                    deployer, zones,
                                    subscriptions[i].fixedPredicates,
                                    i < subscriptions.length - 1
                                            ? nbEventsPerSubscriber
                                            : nbEventsPerSubscriber
                                                    + extraEvents);

                    subscriptions[i].nbEventsExpected = generatedEvents.length;

                    for (int j = 0; j < generatedEvents.length; j++) {
                        events[(i * nbEventsPerSubscriber) + j] =
                                generatedEvents[j];
                    }
                }

                this.events = events;
            }
        }

        return this.events;
    }

    private Event[] computeGenerationsAndSelectBest(EventCloudDeployer deployer,
                                                    SemanticZone[] zones,
                                                    Node[] fixedPredicates,
                                                    int nbEvents) {
        List<Event[]> eventSets =
                new ArrayList<Event[]>(this.nbEventGenerationRounds);
        List<Integer[]> eventSetsDistribution =
                new ArrayList<Integer[]>(this.nbEventGenerationRounds);

        for (int i = 0; i < this.nbEventGenerationRounds; i++) {
            Event[] generatedEvents =
                    this.generateEvents(
                            deployer, zones, nbEvents, fixedPredicates);
            Integer[] distribution = new Integer[zones.length];
            for (int j = 0; j < distribution.length; j++) {
                distribution[j] = 0;
            }

            for (Event e : generatedEvents) {
                CompoundEvent ce = (CompoundEvent) e;

                for (int j = 0; j < ce.size(); j++) {
                    Quadruple q = ce.get(j);

                    boolean belongs = false;

                    for (int k = 0; k < zones.length; k++) {

                        if (zones[k].contains(SemanticPointFactory.newSemanticCoordinate(q))) {
                            distribution[k] = distribution[k] + 1;

                            belongs = true;
                        }
                    }

                    if (!belongs) {
                        throw new RuntimeException(
                                "Generated quadruple is not managed by the network: "
                                        + q);
                    }
                }
            }

            eventSets.add(generatedEvents);
            eventSetsDistribution.add(distribution);
        }

        DescriptiveStatistics stats = new DescriptiveStatistics();

        int selectedIndex = 0;
        double bestMeanDeviation = Integer.MAX_VALUE;

        for (int i = 0; i < eventSetsDistribution.size(); i++) {
            for (int j = 0; j < eventSetsDistribution.get(i).length; j++) {
                stats.addValue(eventSetsDistribution.get(i)[j]);
            }

            double mean = stats.getMean();

            // http://mathworld.wolfram.com/MeanDeviation.html
            double meanDeviation = 0;
            for (int j = 0; j < eventSetsDistribution.get(i).length; j++) {
                meanDeviation +=
                        Math.abs(eventSetsDistribution.get(i)[j] - mean);
            }
            meanDeviation /= eventSetsDistribution.get(i).length;

            if (meanDeviation < bestMeanDeviation) {
                bestMeanDeviation = meanDeviation;
                selectedIndex = i;
            }

            stats.clear();
        }

        return eventSets.get(selectedIndex);
    }

    private Event[] generateEvents(EventCloudDeployer deployer,
                                   SemanticZone[] zones, int nbEvents,
                                   Node[] fixedPredicateNodes) {
        // pre-generates events so that the data are the same for all the
        // runs and the time is not included into the benchmark execution
        // time
        Event[] generatedEvents = new Event[nbPublications];

        if (this.rewritingLevel > 0) {
            generatedEvents =
                    this.createEventsForUniformDistributionAndRewritingSteps(
                            deployer, zones, nbEvents, fixedPredicateNodes);
        } else {
            generatedEvents =
                    this.createEventsForUniformDistribution(
                            deployer, zones, nbEvents);
        }

        return generatedEvents;
    }

    private SemanticZone[] retrievePeerZones(EventCloudDeployer deployer) {
        SemanticZone[] zones = new SemanticZone[this.nbPeers];

        List<Peer> peers = deployer.getRandomSemanticTracker().getPeers();

        for (int i = 0; i < zones.length; i++) {
            GetIdAndZoneResponseOperation<SemanticCoordinate> response =
                    CanOperations.getIdAndZoneResponseOperation(peers.get(i));
            SemanticZone zone = (SemanticZone) response.getPeerZone();
            zones[i] = zone;
        }

        return zones;
    }

    private EventCloudsRegistry deployRegistry(EventCloudDeployer deployer,
                                               NodeProvider nodeProvider) {
        EventCloudsRegistry registry =
                EventCloudsRegistryFactory.newEventCloudsRegistry(nodeProvider);
        registry.register(deployer);

        return registry;
    }

    private Event[] createEventsForUniformDistribution(EventCloudDeployer deployer,
                                                       SemanticZone[] zones,
                                                       int nbEvents) {

        return this.createEvents(deployer, zones, new EventProvider() {
            @Override
            public Event get(SemanticZone[] zones, int eventIndex,
                             int nbQuadruplesPerCE, int rdfTermSize) {
                return EventGenerator.randomCompoundEvent(
                        zones,
                        nbQuadruplesPerCE,
                        rdfTermSize,
                        !PublishSubscribeBenchmark.this.disableIntraCompoundEventsShuffling);
            }
        }, nbEvents);
    }

    private Event[] createEventsForUniformDistributionAndRewritingSteps(EventCloudDeployer deployer,
                                                                        SemanticZone[] zones,
                                                                        int nbEvents,
                                                                        final Node[] fixedPredicateNodes) {
        return this.createEvents(deployer, zones, new EventProvider() {
            @Override
            public Event get(SemanticZone[] zones, int eventIndex,
                             int nbQuadruplesPerCE, int rdfTermSize) {
                return EventGenerator.randomCompoundEventForRewriting(
                        zones,
                        fixedPredicateNodes,
                        eventIndex,
                        nbQuadruplesPerCE,
                        rdfTermSize,
                        PublishSubscribeBenchmark.this.rewritingLevel,
                        !PublishSubscribeBenchmark.this.disableIntraCompoundEventsShuffling);
            }
        }, nbEvents);
    }

    private Event[] createEvents(EventCloudDeployer deployer,
                                 SemanticZone[] zones, EventProvider provider,
                                 int nbEvents) {
        EventGenerator.reset();

        Event[] events = new Event[nbEvents];

        for (int i = 0; i < nbEvents; i++) {
            if (this.publishIndependentQuadruples) {
                throw new UnsupportedOperationException();
            } else {
                events[i] =
                        provider.get(
                                zones, i, this.nbQuadruplesPerCompoundEvent,
                                this.rdfTermSize);
            }
        }

        return events;
    }

    private static interface EventProvider {

        Event get(SemanticZone[] zones, int eventIndex, int nbQuadruplesPerCE,
                  int rdfTermSize);

    }

    private EventCloudDeploymentDescriptor createDeploymentDescriptor(NodeProvider nodeProvider,
                                                                      String benchmarkStatsCollectorURL) {
        EventCloudDeploymentDescriptor descriptor =
                new EventCloudDeploymentDescriptor(this.createOverlayProvider(
                        benchmarkStatsCollectorURL, this.inMemoryDatastore));
        descriptor.setInjectionConstraintsProvider(InjectionConstraintsProvider.newUniformInjectionConstraintsProvider());

        return descriptor;
    }

    private NodeProvider createNodeProviderIfRequired() {
        if (this.gcmaDescriptor != null) {
            return new GcmDeploymentNodeProvider(this.gcmaDescriptor);
        }

        return new LocalNodeProvider();
    }

    private SerializableProvider<? extends SemanticCanOverlay> createOverlayProvider(String statsCollectorURL,
                                                                                     boolean inMemory) {
        return new CustomSemanticOverlayProvider(
                statsCollectorURL, inMemory, this.measureStorageTime);
    }

    private List<CustomPublishProxy> createPublishProxies(NodeProvider nodeProvider,
                                                          String registryUrl,
                                                          EventCloudId id)
            throws EventCloudIdNotManaged {
        List<CustomPublishProxy> result =
                new ArrayList<CustomPublishProxy>(this.nbPublishers);

        for (int i = 0; i < this.nbPublishers; i++) {
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

    private List<SubscribeApi> createSubscribeProxies(NodeProvider nodeProvider,
                                                      String registryUrl,
                                                      EventCloudId id)
            throws EventCloudIdNotManaged {
        List<SubscribeApi> result =
                new ArrayList<SubscribeApi>(this.nbSubscribers);

        for (int i = 0; i < this.nbSubscribers; i++) {
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

    private synchronized SyntheticSubscription[] getOrCreateSubscriptions(SemanticZone[] zones) {
        if (this.syntheticSubscriptions == null) {
            SyntheticSubscription[] result =
                    new SyntheticSubscription[this.nbSubscribers
                            * this.nbSubscriptionsPerSubscriber];

            Node[] predicateNodes = null;

            for (int i = 0; i < this.nbSubscribers
                    * this.nbSubscriptionsPerSubscriber; i++) {
                if (this.useDifferentSubscriptions
                        || (!this.useDifferentSubscriptions && i == 0)) {
                    predicateNodes =
                            this.generatePredicateNodes(
                                    zones, this.rewritingLevel + 1);
                }

                result[i] = this.createSubscription(predicateNodes);
            }

            this.syntheticSubscriptions = result;
        }

        return this.syntheticSubscriptions;
    }

    private Node[] generatePredicateNodes(SemanticZone[] zones, int nb) {
        Node[] result = new Node[nb];

        for (int i = 0; i < nb; i++) {
            SemanticZone zone = zones[i % zones.length];

            result[i] =
                    EventGenerator.randomNode(
                            zone.getLowerBound((byte) 2),
                            zone.getUpperBound((byte) 2), -1, this.rdfTermSize);
        }

        return result;
    }

    private SyntheticSubscription createSubscription(Node[] fixedPredicates) {
        String subscription;

        if (this.subscriptionType == SubscriptionType.ACCEPT_ALL) {
            subscription = Subscription.ACCEPT_ALL;
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

                if (this.subscriptionType == SubscriptionType.PATH_QUERY_FREE_PREDICATE) {
                    buf.append("?p");
                    buf.append(i);
                } else {
                    // fixed predicate value
                    buf.append("<");
                    buf.append(fixedPredicates[i - 1].getURI());
                    buf.append(">");
                }

                buf.append(" ?o");
                buf.append(i);
                buf.append(" . ");
            }
            buf.append("} }");

            subscription = buf.toString();
        }

        return new SyntheticSubscription(subscription, fixedPredicates);
    }

    private void assignSetOfevents(CustomPublishProxy publishProxy,
                                   Event[] events, int start, int end) {
        publishProxy.assignEvents(Arrays.copyOfRange(events, start, end + 1));
    }

    private NotificationListener<?> subscribe(BenchmarkStatsCollector collector,
                                              SubscribeApi subscribeProxy,
                                              Subscription subscription,
                                              NotificationListenerType listenerType,
                                              int nbEventsExpected) {
        NotificationListener<?> listener = null;

        switch (listenerType) {
            case BINDING:
                listener =
                        new CustomBindingListener(collector, nbEventsExpected);
                subscribeProxy.subscribe(
                        subscription, (BindingNotificationListener) listener);
                break;
            case COMPOUND_EVENT:
                listener =
                        new CustomCompoundEventListener(
                                collector, nbEventsExpected);
                subscribeProxy.subscribe(
                        subscription,
                        (CompoundEventNotificationListener) listener);
                break;
            case SIGNAL:
                listener =
                        new CustomSignalListener(collector, nbEventsExpected);
                subscribeProxy.subscribe(
                        subscription, (SignalNotificationListener) listener);
                break;
        }

        return listener;
    }

    private static class SyntheticSubscription {

        public final String content;

        public final Node[] fixedPredicates;

        public int nbEventsExpected;

        public SyntheticSubscription(String content, Node[] fixedPredicates) {
            this.content = content;
            this.fixedPredicates = fixedPredicates;
        }

    }

}
