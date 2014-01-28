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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeoutException;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.p2p.structured.deployment.NodeProvider;
import org.objectweb.proactive.extensions.p2p.structured.deployment.gcmdeployment.GcmDeploymentNodeProvider;
import org.objectweb.proactive.extensions.p2p.structured.deployment.local.LocalNodeProvider;
import org.objectweb.proactive.extensions.p2p.structured.operations.GenericResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.OverlayId;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.MicroBenchmark;
import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.MicroBenchmarkService;
import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.StatsRecorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.FileConverter;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.EventCloudDescription;
import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.benchmarks.load_balancing.converters.LoadBalancingStrategyConverter;
import fr.inria.eventcloud.benchmarks.load_balancing.overlay.CustomSemanticOverlayProvider;
import fr.inria.eventcloud.benchmarks.load_balancing.proxies.CustomProxyFactory;
import fr.inria.eventcloud.benchmarks.load_balancing.proxies.CustomPublishProxy;
import fr.inria.eventcloud.benchmarks.pubsub.operations.ClearOperation;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.datastore.QuadrupleIterator;
import fr.inria.eventcloud.deployment.EventCloudComponentsManager;
import fr.inria.eventcloud.deployment.EventCloudDeployer;
import fr.inria.eventcloud.deployment.EventCloudDeploymentDescriptor;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.factories.EventCloudComponentsManagerFactory;
import fr.inria.eventcloud.factories.EventCloudsRegistryFactory;
import fr.inria.eventcloud.load_balancing.LoadBalancingStrategy;
import fr.inria.eventcloud.load_balancing.configuration.LoadBalancingConfiguration;
import fr.inria.eventcloud.load_balancing.criteria.Criterion;
import fr.inria.eventcloud.load_balancing.criteria.QuadrupleCountCriterion;
import fr.inria.eventcloud.operations.can.CountQuadruplesOperation;
import fr.inria.eventcloud.utils.RDFReader;

/**
 * Simple application to assess load balancing. The network is initialized with
 * one peer only. New ones are added thanks to the load balancing strategy that
 * is applied.
 * 
 * @author lpellegr
 */
public class LoadBalancingBenchmark {

    private static final Logger log =
            LoggerFactory.getLogger(LoadBalancingBenchmark.class);

    private static final String BENCHMARK_STATS_COLLECTOR_NAME =
            "benchmark-stats-collector";

    // parameters

    @Parameter(names = {"-if", "--input-file"}, description = "File containing quadruples using TriG syntax", converter = FileConverter.class, required = true)
    public File inputFile;

    @Parameter(names = {"-nr", "--nb-runs"}, description = "The number of runs to perform")
    public int nbRuns = 1;

    @Parameter(names = {"-dr", "--dry-runs"}, description = "Indicates the number of first runs to discard")
    public int discardFirstRuns = 0;

    @Parameter(names = {"-imds", "--in-memory-datastore"}, description = "Specifies whether datastores on peers have to be persisted on disk or not")
    public boolean inMemoryDatastore = false;

    @Parameter(names = {"-gcma", "--gcma-descriptor"}, description = "Path to the GCMA descriptor to use for deploying the benchmark entities on several machines")
    public String gcmaDescriptor = null;

    @Parameter(names = {"-p", "--nb-peers"}, description = "The maximum number of peers to inject into the P2P network")
    public int nbPeers = 1;

    @Parameter(names = {"-s", "--strategy"}, description = "Load balancing strategy to apply", converter = LoadBalancingStrategyConverter.class)
    public LoadBalancingStrategy strategy;

    @Parameter(names = {"-h", "--help"}, description = "Print help", help = true)
    public boolean help;

    private EventCloudComponentsManager componentsManager;

    private int nbQuadruplesPublished;

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

    public StatsRecorder execute() {
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

                    private Event[] events;

                    @Override
                    public void setup() throws Exception {
                        log.info(
                                "Loading events from {}",
                                LoadBalancingBenchmark.this.inputFile);
                        this.events =
                                LoadBalancingBenchmark.this.loadEvents(LoadBalancingBenchmark.this.inputFile);

                        log.info(
                                "{} compound events loaded", this.events.length);

                        this.collector =
                                PAActiveObject.newActive(
                                        BenchmarkStatsCollector.class,
                                        new Object[] {LoadBalancingBenchmark.this.nbPeers - 1});
                        this.collectorURL =
                                PAActiveObject.registerByName(
                                        this.collector,
                                        BENCHMARK_STATS_COLLECTOR_NAME);

                        this.nodeProvider =
                                LoadBalancingBenchmark.this.createNodeProvider();

                        LoadBalancingBenchmark.this.componentsManager =
                                EventCloudComponentsManagerFactory.newComponentsManager(
                                        this.nodeProvider, 1,
                                        LoadBalancingBenchmark.this.nbPeers, 1,
                                        1, 0);

                        LoadBalancingBenchmark.this.componentsManager.start();

                        EventCloudDeploymentDescriptor descriptor =
                                this.createDeploymentDescriptor(
                                        this.nodeProvider, this.collectorURL);

                        this.deployer =
                                new EventCloudDeployer(
                                        new EventCloudDescription(),
                                        descriptor,
                                        LoadBalancingBenchmark.this.componentsManager);
                        this.deployer.deploy(1, 1);

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
                    }

                    private EventCloudDeploymentDescriptor createDeploymentDescriptor(NodeProvider nodeProvider,
                                                                                      String benchmarkStatsCollectorURL) {
                        Criterion[] criteria = new Criterion[1];
                        criteria[0] = new QuadrupleCountCriterion();

                        LoadBalancingConfiguration configuration = null;

                        if (LoadBalancingBenchmark.this.strategy != null) {
                            configuration =
                                    new LoadBalancingConfiguration(
                                            criteria,
                                            LoadBalancingBenchmark.this.componentsManager,
                                            LoadBalancingBenchmark.this.strategy);
                        }

                        EventCloudDeploymentDescriptor descriptor =
                                new EventCloudDeploymentDescriptor(
                                        new CustomSemanticOverlayProvider(
                                                configuration,
                                                this.collectorURL,
                                                LoadBalancingBenchmark.this.nbQuadruplesPublished,
                                                LoadBalancingBenchmark.this.inMemoryDatastore));

                        return descriptor;
                    }

                    @Override
                    public void run(StatsRecorder recorder)
                            throws TimeoutException {
                        log.info("Assigning events");
                        this.publishProxies.assignEvents(this.events);

                        log.info("Publishing events to trigger load balancing");
                        this.publishProxies.publish();

                        // while
                        // (!LoadBalancingBenchmark.this.componentsManager.isPeerComponentPoolEmpty())
                        // {
                        // timeout after 1 hour
                        this.collector.waitCondition(3600000);
                        // }

                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        log.info("Distribution on peers is:");

                        Map<OverlayId, Integer> results =
                                new HashMap<OverlayId, Integer>();

                        for (Peer peer : this.collector.getPeers()) {
                            @SuppressWarnings("unchecked")
                            GenericResponseOperation<Integer> result =
                                    (GenericResponseOperation<Integer>) PAFuture.getFutureValue(peer.receive(new CountQuadruplesOperation(
                                            false)));

                            results.put(peer.getId(), result.getValue());
                        }

                        // Proxy proxy =
                        // org.objectweb.proactive.extensions.p2p.structured.factories.ProxyFactory.newProxy(this.deployer.getTrackers());
                        // CountQuadrupleResponse response =
                        // (CountQuadrupleResponse)
                        // PAFuture.getFutureValue(proxy.send(new
                        // CountQuadrupleRequest()));
                        //
                        // Map<OverlayId, Long> results = response.getResult();

                        DescriptiveStatistics stats =
                                new DescriptiveStatistics();

                        int count = 0;
                        for (Entry<OverlayId, Integer> entry : results.entrySet()) {
                            log.info("{}  {}", entry.getKey(), entry.getValue());
                            count += entry.getValue();
                            stats.addValue(entry.getValue());
                        }

                        log.info(
                                "{} peers manage a total of {} quadruples, standard deviation is {}, variability (stddev/average * 100) is {}%",
                                results.size(),
                                count,
                                stats.getStandardDeviation(),
                                (stats.getStandardDeviation() / stats.getMean()) * 100);

                        System.exit(1);
                    }

                    @Override
                    public void clear() throws Exception {
                        log.info("Clearing previously recorded information before to start benchmark");

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

        return microBenchmark.getStatsRecorder();
    }

    private Event[] loadEvents(File file) {
        QuadrupleIterator iterator;

        Multimap<Node, Quadruple> mmap = ArrayListMultimap.create();

        try {
            iterator =
                    RDFReader.pipe(new BufferedInputStream(new FileInputStream(
                            file)), SerializationFormat.TriG);

            Quadruple q;

            while (iterator.hasNext()) {
                q = iterator.next();
                mmap.put(q.getGraph(), q);
                this.nbQuadruplesPublished++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Collection<Collection<Quadruple>> compoundEvents =
                mmap.asMap().values();
        Event[] result = new Event[compoundEvents.size()];

        int i = 0;
        for (Collection<Quadruple> ce : compoundEvents) {
            result[i] = new CompoundEvent(ce);
            i++;
        }

        // meta quadruple added
        this.nbQuadruplesPublished += compoundEvents.size();

        return result;
    }

    private void logParameterValues() {
        log.info("Benchmark starting with the following parameters:");
        log.info("  inputFile -> {}", this.inputFile);
        log.info("  nbRuns -> {}", this.nbRuns);
        log.info("  dryRuns -> {}", this.discardFirstRuns);
        log.info("  gcmaDescriptor -> {}", this.gcmaDescriptor);
        log.info("  inMemoryDatastore -> {}", this.inMemoryDatastore);
        log.info("  nbPeers -> {}", this.nbPeers);
        log.info("  strategy -> {}", this.strategy);

        if (this.strategy == LoadBalancingStrategy.RELATIVE) {
            log.info(
                    "  gossip protocol -> {}",
                    EventCloudProperties.LOAD_BALANCING_GOSSIP_STRATEGY.getValue()
                            .getSimpleName());
        }
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

    private NodeProvider createNodeProvider() {
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
