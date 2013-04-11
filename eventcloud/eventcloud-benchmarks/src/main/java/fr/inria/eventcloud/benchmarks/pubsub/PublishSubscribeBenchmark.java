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
import org.objectweb.proactive.extensions.p2p.structured.utils.Pair;
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

    // -gcma /user/lpellegr/home/Desktop/GCMresources/GCMA.xml

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

    @Parameter(names = {"--subscription-type", "-st"}, description = "Indicates the type of the subscription used by the subscribers to subscribe", converter = SubscriptionTypeConverter.class)
    public SubscriptionType subscriptionType = SubscriptionType.ACCEPT_ALL;

    @Parameter(names = {"--publish-quadruples"}, description = "Indicates whether events must be emitted as quadruples (default CEs)")
    public boolean publishIndependentQuadruples = false;

    @Parameter(names = {"-lt", "--listener-type"}, description = "The listener type used by all the subscribers for subscribing", converter = ListenerTypeConverter.class)
    public NotificationListenerType listenerType =
            NotificationListenerType.COMPOUND_EVENT;

    @Parameter(names = {"-imds", "--in-memory-datastore"}, description = "Specifies whether datastores on peers have to be persisted on disk or not")
    public boolean inMemoryDatastore = false;

    @Parameter(names = {"-negr", "--nb-event-generation-rounds"}, description = "When combined with uniform data distribution, the specified number of event sets are generated and only event set with the best standard deviation is kept")
    public int nbEventGenerationRounds = 1;

    @Parameter(names = {"-gcma", "--gcma-descriptor"}, description = "Path to the GCMA descriptor to use for deploying the benchmark entities on several machines")
    public String gcmaDescriptor = null;

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

    private Map<SubscriptionId, NotificationListener<?>> listeners =
            new HashMap<SubscriptionId, NotificationListener<?>>();

    @SuppressWarnings("unused")
    private Supplier<? extends Event> supplier;

    // generated data to reuse for each run.
    // the CAN network topology is assumed to be the same at each run
    private Pair<String, Node[]> subscriptionElements;

    private Event[] events;

    public static void main(String[] args) {
        // printable ASCII codepoints interval
        P2PStructuredProperties.CAN_LOWER_BOUND.setValue(0x20);
        P2PStructuredProperties.CAN_UPPER_BOUND.setValue(0x7E);

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
        log.info("  discardFirstRuns -> {}", this.discardFirstRuns);
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
        log.info("  publishQuadruples -> {}", this.publishIndependentQuadruples);
        log.info("  rewritingLevel -> {}", this.rewritingLevel);
        log.info("  subscriptionType -> {}", this.subscriptionType);
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

        this.endToEndMeasurementsExitTime =
                new HashMap<SubscriptionId, Long>(this.nbSubscribers);

        this.outputMeasurements =
                new HashMap<SubscriptionId, SimpleMeasurement>(
                        this.nbSubscribers);

        this.pointToPointEntryMeasurements =
                new HashMap<String, Long>(nbPublications);

        this.pointToPointExitMeasurements =
                new HashMap<SubscriptionId, CumulatedMeasurement>(
                        nbPublications);

        // creates and runs micro benchmark
        MicroBenchmark microBenchmark =
                new MicroBenchmark(this.nbRuns, new MicroBenchmarkRun() {
                    @Override
                    public void run(StatsRecorder recorder) {
                        try {
                            PublishSubscribeBenchmark.this.execute(recorder);

                            long maxEndToEndMeasurement = 0;

                            for (SubscriptionId subscriptionId : PublishSubscribeBenchmark.this.listeners.keySet()) {
                                long endToEndEllapsedTime =
                                        PublishSubscribeBenchmark.this.endToEndMeasurementsExitTime.get(subscriptionId)
                                                - PublishSubscribeBenchmark.this.endToEndMeasurementEntryTime;

                                if (endToEndEllapsedTime > maxEndToEndMeasurement) {
                                    maxEndToEndMeasurement =
                                            endToEndEllapsedTime;
                                }

                                recorder.reportValue(
                                        END_TO_END_MEASUREMENT_CATEGORY
                                                + subscriptionId.toString(),
                                        endToEndEllapsedTime);

                                recorder.reportValue(
                                        OUTPUT_MEASUREMENT_CATEGORY
                                                + subscriptionId.toString(),
                                        PublishSubscribeBenchmark.this.outputMeasurements.get(
                                                subscriptionId)
                                                .getElapsedTime());

                                recorder.reportValue(
                                        POINT_TO_POINT_MEASUREMENT_CATEGORY
                                                + subscriptionId.toString(),
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

        for (SubscriptionId subscriptionId : this.listeners.keySet()) {
            statsBuffer.append("Benchmark results for subscription ");
            statsBuffer.append(subscriptionId.toString());
            statsBuffer.append('\n');

            Category endToEndCategory =
                    microBenchmark.getStatsRecorder().getCategory(
                            END_TO_END_MEASUREMENT_CATEGORY
                                    + subscriptionId.toString());
            Category outputCategory =
                    microBenchmark.getStatsRecorder().getCategory(
                            OUTPUT_MEASUREMENT_CATEGORY
                                    + subscriptionId.toString());
            Category pointToPointCategory =
                    microBenchmark.getStatsRecorder().getCategory(
                            POINT_TO_POINT_MEASUREMENT_CATEGORY
                                    + subscriptionId.toString());

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
            statsBuffer.append(", average throughput=");
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

        statsBuffer.append('\n');
        statsBuffer.append("Average benchmark results\n");
        statsBuffer.append(" End-to-End measurement, average throughput=");
        statsBuffer.append(endToEndSum / nbSubscriptions);
        statsBuffer.append('\n');
        statsBuffer.append(" Point-to-Point measurement, average throughput=");
        statsBuffer.append(pointToPointSum / nbSubscriptions);
        statsBuffer.append('\n');
        statsBuffer.append(" Output measurement, average throughput=");
        statsBuffer.append(outputSum / nbSubscriptions);
        statsBuffer.append('\n');

        System.out.println(statsBuffer.toString());

        return microBenchmark.getStatsRecorder();
    }

    public void execute(StatsRecorder recorder) throws EventCloudIdNotManaged,
            TimeoutException, ProActiveException {
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

        SemanticZone[] zones = this.retrievePeerZones(deployer);

        Pair<String, Node[]> subscriptionElements =
                this.createSubscription(zones);
        String sparqlSubscription = subscriptionElements.getFirst();
        Node[] fixedPredicateNodes = subscriptionElements.getSecond();

        Event[] events =
                this.createEvents(deployer, zones, fixedPredicateNodes);

        String registryURL = this.deployRegistry(deployer, nodeProvider);

        EventCloudId id = deployer.getEventCloudDescription().getId();

        // creates proxies
        List<CustomPublishProxy> publishProxies =
                this.createPublishProxies(
                        registryURL, id, nodeProvider, this.nbPublishers);
        List<SubscribeApi> subscribeProxies =
                this.createSubscribeProxies(
                        registryURL, id, nodeProvider, this.nbSubscribers);

        log.info(
                "The subscription used by the subscribers is {}",
                sparqlSubscription);

        // subscribes
        for (int i = 0; i < this.nbSubscribers; i++) {
            final SubscribeApi subscribeProxy = subscribeProxies.get(i);

            Subscription subscription = new Subscription(sparqlSubscription);

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

        // the end to end termination time is the time at each subscriber
        // has notified the collector about its termination
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

        // for (Peer p : deployer.getRandomSemanticTracker().getPeers()) {
        // System.out.println(p.dump());
        // }

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
        ComponentUtils.terminateComponent(registryURL);

        if (nodeProvider != null) {
            nodeProvider.terminate();
        }
    }

    private Event[] createEvents(EventCloudDeployer deployer,
                                 SemanticZone[] zones,
                                 Node[] fixedPredicateNodes) {
        if (this.events == null) {
            List<Event[]> eventSets =
                    new ArrayList<Event[]>(this.nbEventGenerationRounds);
            List<Integer[]> eventSetsDistribution =
                    new ArrayList<Integer[]>(this.nbEventGenerationRounds);

            for (int i = 0; i < this.nbEventGenerationRounds; i++) {
                Event[] generatedEvents =
                        this.generateEvents(
                                deployer, zones, fixedPredicateNodes);
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

            this.events = eventSets.get(selectedIndex);
        }

        return this.events;
    }

    private Event[] generateEvents(EventCloudDeployer deployer,
                                   SemanticZone[] zones,
                                   Node[] fixedPredicateNodes) {
        // pre-generates events so that the data are the same for all the
        // runs and the time is not included into the benchmark execution
        // time
        Event[] generatedEvents = new Event[nbPublications];

        if (this.rewritingLevel > 0) {
            generatedEvents =
                    this.createEventsForUniformDistributionAndRewritingSteps(
                            deployer, zones, fixedPredicateNodes);
        } else {
            generatedEvents =
                    this.createEventsForUniformDistribution(deployer, zones);
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

    private Event[] createEventsForUniformDistribution(EventCloudDeployer deployer,
                                                       SemanticZone[] zones) {

        return this.createEvents(deployer, zones, new EventProvider() {
            @Override
            public Event get(SemanticZone[] zones, int nbQuadruplesPerCE,
                             int rdfTermSize) {
                return EventGenerator.randomCompoundEvent(
                        zones, nbQuadruplesPerCE, rdfTermSize);
            }
        });
    }

    private Event[] createEventsForUniformDistributionAndRewritingSteps(EventCloudDeployer deployer,
                                                                        SemanticZone[] zones,
                                                                        final Node[] fixedPredicateNodes) {
        return this.createEvents(deployer, zones, new EventProvider() {
            @Override
            public Event get(SemanticZone[] zones, int nbQuadruplesPerCE,
                             int rdfTermSize) {
                return EventGenerator.randomCompoundEventForRewriting(
                        zones, nbQuadruplesPerCE, rdfTermSize,
                        PublishSubscribeBenchmark.this.rewritingLevel,
                        fixedPredicateNodes);
            }
        });
    }

    private Event[] createEvents(EventCloudDeployer deployer,
                                 SemanticZone[] zones, EventProvider provider) {
        EventGenerator.reset();

        Event[] events = new Event[nbPublications];

        for (int i = 0; i < nbPublications; i++) {
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

    private synchronized Pair<String, Node[]> createSubscription(SemanticZone[] zones) {
        if (this.subscriptionElements == null) {
            String subscription;

            Node[] fixedPredicateNodes = null;

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
                        SemanticZone zone = zones[(i - 1) % zones.length];

                        if (fixedPredicateNodes == null) {
                            fixedPredicateNodes =
                                    new Node[this.rewritingLevel + 1];
                        }

                        fixedPredicateNodes[i - 1] =
                                EventGenerator.randomNode(
                                        zone.getLowerBound((byte) 2),
                                        zone.getUpperBound((byte) 2), -1, 10);

                        buf.append("<");
                        buf.append(fixedPredicateNodes[i - 1].getURI());
                        buf.append(">");
                    }

                    buf.append(" ?o");
                    buf.append(i);
                    buf.append(" . ");
                }
                buf.append("} }");

                subscription = buf.toString();
            }

            this.subscriptionElements =
                    Pair.create(subscription, fixedPredicateNodes);
        }

        return this.subscriptionElements;
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
