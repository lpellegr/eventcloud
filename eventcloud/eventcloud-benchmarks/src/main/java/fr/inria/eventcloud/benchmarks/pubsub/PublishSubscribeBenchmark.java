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
import fr.inria.eventcloud.overlay.can.SemanticCoordinateFactory;
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

    @Parameter(names = {"-gcma", "--gcma-descriptor"}, description = "Path to the GCMA descriptor to use for deploying the benchmark entities on several machines")
    public String gcmaDescriptor = null;

    @Parameter(names = {"--disable-inter-ces-shuffling"}, description = "Indicates whether the shuffling of the generated set of compounds events should be disabled or not")
    public boolean disableInterCompoundEventsShuffling = false;

    @Parameter(names = {"--disable-intra-ces-shuffling"}, description = "Indicates whether the shuffling of the quadruples inside the generated compound events must be disabled or not")
    public boolean disableIntraCompoundEventsShuffling = false;

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

    public static void main(String[] args) {
        // WARNING: setting property values here set them only for the JVM where
        // this class is executed not on all JVMs associated to peers. This
        // should be done through the GCMA configuration file

        // alphabetic lower case interval
        P2PStructuredProperties.CAN_LOWER_BOUND.setValue(0x61);
        P2PStructuredProperties.CAN_UPPER_BOUND.setValue(0x7A);

        PublishSubscribeBenchmark benchmark = new PublishSubscribeBenchmark();

        JCommander jCommander = new JCommander(benchmark);

        try {
            jCommander.parse(args);

            if (benchmark.help) {
                jCommander.usage();
                System.exit(0);
            }
        } catch (ParameterException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }

        benchmark.execute();

        System.exit(0);
    }

    private void logParameterValues() {
        log.info("Benchmark starting with the following parameters:");
        log.info("  compoundEventSize -> {}", this.nbQuadruplesPerCompoundEvent);
        log.info("  dryRuns -> {}", this.discardFirstRuns);
        log.info("  gcmaDescriptor -> {}", this.gcmaDescriptor);
        log.info("  inMemoryDatastore -> {}", this.inMemoryDatastore);
        log.info("  listenerType -> {}", this.listenerType);
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
                new MicroBenchmark(this.nbRuns, new MicroBenchmarkRun() {
                    @Override
                    public void run(StatsRecorder recorder) {
                        try {
                            PublishSubscribeBenchmark.this.execute(recorder);

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

                            recorder.reportValue(
                                    MicroBenchmark.DEFAULT_CATEGORY_NAME,
                                    maxEndToEndMeasurement);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
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
        statsBuffer.append(nbQuadsPerPeer.getMean()).append("\n\n");

        double endToEndSum = 0;
        double pointToPointSum = 0;
        double outputSum = 0;

        for (int i = 0; i < this.subscriptions.size(); i++) {
            SubscriptionId subscriptionId = this.subscriptions.get(i).getId();

            statsBuffer.append("Benchmark results for subscription ");
            statsBuffer.append(subscriptionId.toString());
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

    public void execute(StatsRecorder recorder) throws EventCloudIdNotManaged,
            TimeoutException, ProActiveException, IOException {

        this.listeners.clear();
        this.subscriptions.clear();

        // clears results collected during previous run
        if (this.endToEndMeasurementsExitTime != null) {
            this.endToEndMeasurementsExitTime.clear();
        }

        this.outputMeasurements.clear();
        this.pointToPointExitMeasurements.clear();
        this.pointToPointEntryMeasurements.clear();

        // creates collector
        BenchmarkStatsCollector collector =
                PAActiveObject.newActive(
                        BenchmarkStatsCollector.class, new Object[] {
                                this.nbPublishers, this.nbSubscribers,
                                this.nbSubscriptionsPerSubscriber});
        String collectorURL =
                PAActiveObject.registerByName(
                        collector, BENCHMARK_STATS_COLLECTOR_NAME);

        GcmDeploymentNodeProvider nodeProvider =
                this.createNodeProviderIfRequired();

        EventCloudDeploymentDescriptor descriptor =
                this.createDeploymentDescriptor(nodeProvider);

        // creates eventcloud
        EventCloudDeployer deployer =
                new EventCloudDeployer(new EventCloudDescription(), descriptor);
        deployer.deploy(1, this.nbPeers);

        SemanticZone[] zones = this.retrievePeerZones(deployer);

        SyntheticSubscription[] subscriptions =
                this.getOrCreateSubscriptions(zones);

        Event[] events = this.createEvents(deployer, zones, subscriptions);

        if (!this.disableInterCompoundEventsShuffling) {
            org.objectweb.proactive.extensions.p2p.structured.utils.Arrays.shuffle(events);
        }

        EventCloudsRegistry registry =
                this.deployRegistry(deployer, nodeProvider);

        String registryURL = null;
        try {
            registryURL = registry.register("registry");
        } catch (ProActiveException e) {
            throw new IllegalStateException(e);
        }

        EventCloudId eventCloudId = deployer.getEventCloudDescription().getId();

        // creates proxies
        List<CustomPublishProxy> publishProxies =
                this.createPublishProxies(
                        registryURL, eventCloudId, nodeProvider);
        List<SubscribeApi> subscribeProxies =
                this.createSubscribeProxies(
                        registryURL, eventCloudId, nodeProvider);

        log.info("The subscriptions used by the subscribers are the following:");
        for (SyntheticSubscription subscription : subscriptions) {
            log.info("  {}", subscription.content);
        }

        // subscribes
        for (int i = 0; i < subscriptions.length; i++) {
            final SubscribeApi subscribeProxy =
                    subscribeProxies.get(i / this.nbSubscriptionsPerSubscriber);

            Subscription subscription =
                    new Subscription(subscriptions[i].content);

            this.subscriptions.add(subscription);
            this.listeners.put(subscription.getId(), this.subscribe(
                    collector, subscribeProxy, subscription, this.listenerType,
                    subscriptions[i].nbEventsExpected));
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

        this.endToEndMeasurementEntryTime = System.currentTimeMillis();

        // triggers publications (publish events)
        for (CustomPublishProxy proxy : publishProxies) {
            proxy.publish();
        }

        // timeout after 1 hour
        collector.waitForAllPublisherReports(3600000);

        this.pointToPointEntryMeasurements =
                collector.getPointToPointEntryMeasurements();

        // timeout after 1 hour
        collector.waitForAllSubscriberReports(3600000);

        // the end to end termination time is the time at which each
        // subscriber has notified the collector about its termination
        this.endToEndMeasurementsExitTime =
                collector.getEndToEndTerminationTimes();

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
                Thread.sleep(EventCloudProperties.PUBLISH_SUBSCRIBE_OPERATIONS_DELAYER_TIMEOUT.getValue() * 2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

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
                buf.append('\n');
            }
        }

        System.out.println("  Quadruples distribution on peers: "
                + buf.toString());

        recorder.reportValue(
                NB_QUADRUPLES_PER_PEER_CATEGORY, totalNumberOfQuadruples
                        / this.nbPeers);

        deployer.undeploy();

        if (nodeProvider != null) {
            nodeProvider.terminate();
        }

        registry.unregister();
        ComponentUtils.terminateComponent(registry);

        PAActiveObject.unregister(collectorURL);
    }

    private Event[] createEvents(EventCloudDeployer deployer,
                                 SemanticZone[] zones,
                                 SyntheticSubscription[] subscriptions) {
        if (this.events == null) {
            if (!this.useDifferentSubscriptions) {
                this.events =
                        this.computeGenerationsAndSelectBest(
                                deployer, zones,
                                subscriptions[0].fixedPredicates,
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

                        if (zones[k].contains(SemanticCoordinateFactory.newSemanticCoordinate(q))) {
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
            GetIdAndZoneResponseOperation<SemanticElement> response =
                    CanOperations.getIdAndZoneResponseOperation(peers.get(i));
            SemanticZone zone = (SemanticZone) response.getPeerZone();
            zones[i] = zone;
        }

        return zones;
    }

    private EventCloudsRegistry deployRegistry(EventCloudDeployer deployer,
                                               GcmDeploymentNodeProvider nodeProvider) {
        EventCloudsRegistry registry;

        if (this.gcmaDescriptor == null) {
            registry = EventCloudsRegistryFactory.newEventCloudsRegistry();
        } else {
            registry =
                    EventCloudsRegistryFactory.newEventCloudsRegistry(nodeProvider);
        }
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
                        eventIndex,
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
                                zones, i, this.nbQuadruplesPerCompoundEvent, 10);
            }
        }

        return events;
    }

    private static interface EventProvider {

        Event get(SemanticZone[] zones, int eventIndex, int nbQuadruplesPerCE,
                  int rdfTermSize);

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
                                                          NodeProvider nodeProvider)
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

    private List<SubscribeApi> createSubscribeProxies(String registryUrl,
                                                      EventCloudId id,
                                                      NodeProvider nodeProvider)
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
                            zone.getUpperBound((byte) 2), -1, 10);
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
