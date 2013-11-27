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
package fr.inria.eventcloud.benchmarks.load_balancing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeoutException;

import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.p2p.structured.deployment.NodeProvider;
import org.objectweb.proactive.extensions.p2p.structured.deployment.gcmdeployment.GcmDeploymentNodeProvider;
import org.objectweb.proactive.extensions.p2p.structured.deployment.local.LocalNodeProvider;
import org.objectweb.proactive.extensions.p2p.structured.operations.CanOperations;
import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.GetIdAndZoneResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayId;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.providers.InjectionConstraintsProvider;
import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.MicroBenchmark;
import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.MicroBenchmarkService;
import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.StatsRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.FileConverter;

import fr.inria.eventcloud.EventCloudDescription;
import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.benchmarks.load_balancing.overlay.CustomSemanticOverlayProvider;
import fr.inria.eventcloud.benchmarks.load_balancing.proxies.CustomProxyFactory;
import fr.inria.eventcloud.benchmarks.load_balancing.proxies.CustomPublishProxy;
import fr.inria.eventcloud.benchmarks.pubsub.EventGenerator;
import fr.inria.eventcloud.benchmarks.pubsub.operations.ClearOperation;
import fr.inria.eventcloud.deployment.EventCloudComponentsManager;
import fr.inria.eventcloud.deployment.EventCloudDeployer;
import fr.inria.eventcloud.deployment.EventCloudDeploymentDescriptor;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.factories.EventCloudComponentsManagerFactory;
import fr.inria.eventcloud.factories.EventCloudsRegistryFactory;
import fr.inria.eventcloud.load_balancing.configuration.ThresholdLoadBalancingConfiguration;
import fr.inria.eventcloud.overlay.can.SemanticCoordinate;
import fr.inria.eventcloud.overlay.can.SemanticZone;

/**
 * Simple application to assess load balancing
 * 
 * @author lpellegr
 */
public class LoadBalancingBenchmark {

    private static final Logger log =
            LoggerFactory.getLogger(LoadBalancingBenchmark.class);

    private static final String BENCHMARK_STATS_COLLECTOR_NAME =
            "benchmark-stats-collector";

    // parameters

    @Parameter(names = {"-np", "--nb-publications"}, description = "The number of events to publish")
    public int nbPublications = 100;

    @Parameter(names = {"-ces", "--compound-event-size"}, description = "The number of quadruples contained by each CE")
    public int nbQuadruplesPerCompoundEvent = 10;

    @Parameter(names = {"-if", "--input-file"}, description = "Path to file containing quadruples to load", converter = FileConverter.class)
    public File inputFile;

    @Parameter(names = {"-nr", "--nb-runs"}, description = "The number of runs to perform", required = true)
    public int nbRuns = 1;

    @Parameter(names = {"-k", "--maximum-nb-quads-per-peer"}, description = "The maximum number of quads per peer", required = true)
    public int maximumNbQuadsPerPeer;

    @Parameter(names = {"-dr", "--dry-runs"}, description = "Indicates the number of first runs to discard")
    public int discardFirstRuns = 1;

    @Parameter(names = {"-imds", "--in-memory-datastore"}, description = "Specifies whether datastores on peers have to be persisted on disk or not")
    public boolean inMemoryDatastore = false;

    @Parameter(names = {"-h", "--help"}, description = "Print help", help = true)
    public boolean help;

    @Parameter(names = {"-gcma", "--gcma-descriptor"}, description = "Path to the GCMA descriptor to use for deploying the benchmark entities on several machines")
    public String gcmaDescriptor = null;

    private EventCloudComponentsManager ecComponentsManager;

    public int nbCompoundEvents;

    public int nbPeers;

    public static void main(String[] args) {
        LoadBalancingBenchmark benchmark = new LoadBalancingBenchmark();

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
        log.info("  nbPublications -> {}", this.nbPublications);
        log.info("  inputFile -> {}", this.inputFile);
        log.info("  dryRuns -> {}", this.discardFirstRuns);
        log.info("  inMemoryDatastore -> {}", this.inMemoryDatastore);
        log.info("  nbPeers -> {}", this.nbPeers);
        log.info("  nbRuns -> {}", this.nbRuns);
    }

    public StatsRecorder execute() {
        this.nbPeers =
                1 + (this.computeNumberOfQuadruplesExpected() / this.maximumNbQuadsPerPeer);

        this.logParameterValues();

        // creates and runs micro benchmark
        MicroBenchmark microBenchmark =
                new MicroBenchmark(this.nbRuns, new MicroBenchmarkService() {

                    private EventCloudsRegistry registry;

                    private BenchmarkStatsCollector collector;

                    private String collectorURL;

                    private NodeProvider nodeProvider;

                    private EventCloudDeployer deployer;

                    private CustomPublishProxy publishProxies;

                    private CompoundEvent[] events;

                    @Override
                    public void setup() throws Exception {
                        this.collector =
                                PAActiveObject.newActive(
                                        BenchmarkStatsCollector.class,
                                        new Object[] {
                                                LoadBalancingBenchmark.this.computeNumberOfQuadruplesExpected(),
                                                LoadBalancingBenchmark.this.maximumNbQuadsPerPeer});
                        this.collectorURL =
                                PAActiveObject.registerByName(
                                        this.collector,
                                        BENCHMARK_STATS_COLLECTOR_NAME);

                        this.nodeProvider =
                                LoadBalancingBenchmark.this.createNodeProviderIfRequired();

                        LoadBalancingBenchmark.this.ecComponentsManager =
                                EventCloudComponentsManagerFactory.newComponentsManager(
                                        this.nodeProvider, 1,
                                        LoadBalancingBenchmark.this.nbPeers, 1,
                                        1, 0);

                        LoadBalancingBenchmark.this.ecComponentsManager.start();

                        EventCloudDeploymentDescriptor descriptor =
                                this.createDeploymentDescriptor(
                                        this.nodeProvider, this.collectorURL);

                        this.deployer =
                                new EventCloudDeployer(
                                        new EventCloudDescription(),
                                        descriptor,
                                        LoadBalancingBenchmark.this.ecComponentsManager);
                        this.deployer.deploy(1, 1);

                        SemanticZone[] zones =
                                LoadBalancingBenchmark.this.retrievePeerZones(this.deployer);

                        this.events =
                                new CompoundEvent[LoadBalancingBenchmark.this.nbPublications];
                        for (int i = 0; i < this.events.length; i++) {
                            this.events[i] =
                                    EventGenerator.randomCompoundEvent(
                                            zones,
                                            LoadBalancingBenchmark.this.nbQuadruplesPerCompoundEvent,
                                            10, true);
                        }

                        this.registry =
                                LoadBalancingBenchmark.this.deployRegistry(
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

                        this.publishProxies =
                                LoadBalancingBenchmark.this.createPublishProxies(
                                        this.nodeProvider, registryURL,
                                        eventCloudId);

                        // //
                        // log.info("Clearing recorded information before to start benchmark");
                        // // clear();
                        //
                        // Thread.sleep(360000000);
                    }

                    private EventCloudDeploymentDescriptor createDeploymentDescriptor(NodeProvider nodeProvider,
                                                                                      String benchmarkStatsCollectorURL) {
                        EventCloudDeploymentDescriptor descriptor =
                                new EventCloudDeploymentDescriptor(
                                        new CustomSemanticOverlayProvider(
                                                new ThresholdLoadBalancingConfiguration(
                                                        LoadBalancingBenchmark.this.ecComponentsManager,
                                                        LoadBalancingBenchmark.this.maximumNbQuadsPerPeer),
                                                this.collectorURL,
                                                LoadBalancingBenchmark.this.computeNumberOfQuadruplesExpected(),
                                                LoadBalancingBenchmark.this.inMemoryDatastore));
                        descriptor.setInjectionConstraintsProvider(InjectionConstraintsProvider.newUniformInjectionConstraintsProvider());

                        return descriptor;
                    }

                    @Override
                    public void run(StatsRecorder recorder)
                            throws TimeoutException {
                        log.info("Assign events");
                        this.publishProxies.assignEvents(this.events);

                        log.info("Publishing generated events to trigger load balancing");
                        this.publishProxies.publish();

                        // timeout after 1 hour
                        this.collector.wait(3600000);

                        log.info("Distribution on peers is:");

                        Map<OverlayId, Integer> results =
                                this.collector.getResults();

                        int count = 0;
                        for (Entry<OverlayId, Integer> entry : results.entrySet()) {
                            log.info("{}  {}", entry.getKey(), entry.getValue());
                            count += entry.getValue();
                        }

                        log.info("Sum of data managed by peers gives {}", count);
                        log.info(
                                "Number of peers used is {} whereas {} only should be used in an ideal situation",
                                results.size(),
                                LoadBalancingBenchmark.this.computeNumberOfQuadruplesExpected()
                                        / LoadBalancingBenchmark.this.maximumNbQuadsPerPeer);

                        // grep "Assign" LoadBalancingBenchmark
                        // grep "Join" LoadBalancingBenchmark | grep
                        // "has required"

                        // TODO remove once clear is performed, we have to
                        // perform multiple runs!
                        System.exit(1);
                    }

                    @Override
                    public void clear() throws Exception {
                        List<ResponseOperation> futures =
                                new ArrayList<ResponseOperation>();

                        for (Peer p : this.deployer.getRandomTracker()
                                .getPeers()) {
                            futures.add(p.receive(new ClearOperation()));
                        }

                        PAFuture.waitForAll(futures);

                        this.collector.clear();

                    }

                    @Override
                    public void teardown() throws Exception {
                        LoadBalancingBenchmark.this.undeploy(
                                this.nodeProvider, this.deployer,
                                this.registry, this.collectorURL);
                    }

                });
        microBenchmark.discardFirstRuns(this.discardFirstRuns);
        microBenchmark.showProgress();
        microBenchmark.execute();

        // System.out.println(this.createBenchmarkReport(microBenchmark));

        return microBenchmark.getStatsRecorder();
    }

    private int computeNumberOfQuadruplesExpected() {
        return (this.nbQuadruplesPerCompoundEvent + 1) * this.nbPublications;
    }

    private SemanticZone[] retrievePeerZones(EventCloudDeployer deployer) {
        List<Peer> peers = deployer.getRandomSemanticTracker().getPeers();
        SemanticZone[] zones = new SemanticZone[peers.size()];

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

    private NodeProvider createNodeProviderIfRequired() {
        if (this.gcmaDescriptor != null) {
            return new GcmDeploymentNodeProvider(this.gcmaDescriptor);
        }

        return new LocalNodeProvider();
    }

    private CustomPublishProxy createPublishProxies(NodeProvider nodeProvider,
                                                    String registryUrl,
                                                    EventCloudId id)
            throws EventCloudIdNotManaged {

        CustomPublishProxy publishProxy;

        if (nodeProvider == null) {
            publishProxy =
                    CustomProxyFactory.newCustomPublishProxy(registryUrl, id);
        } else {
            publishProxy =
                    CustomProxyFactory.newCustomPublishProxy(
                            nodeProvider, registryUrl, id);
        }

        return publishProxy;
    }

}
