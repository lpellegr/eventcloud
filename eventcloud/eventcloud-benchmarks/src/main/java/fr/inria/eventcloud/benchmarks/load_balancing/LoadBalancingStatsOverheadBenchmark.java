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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.p2p.structured.operations.BooleanResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.CallableOperation;
import org.objectweb.proactive.extensions.p2p.structured.operations.ResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.utils.ComponentUtils;
import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.MicroBenchmark;
import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.MicroBenchmarkServiceAdapter;
import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.StatsRecorder;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.FileConverter;
import com.google.common.base.Stopwatch;

import fr.inria.eventcloud.EventCloudDescription;
import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.PutGetApi;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.api.generators.QuadrupleGenerator;
import fr.inria.eventcloud.benchmarks.pubsub.operations.ClearOperation;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.datastore.QuadrupleIterator;
import fr.inria.eventcloud.deployment.EventCloudDeployer;
import fr.inria.eventcloud.deployment.EventCloudDeploymentDescriptor;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.factories.EventCloudsRegistryFactory;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.messages.request.AddQuadrupleRequest;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.providers.SemanticOverlayProvider;
import fr.inria.eventcloud.utils.RDFReader;

/**
 * Load balancing benchmark to evaluate the impact of the stats computed in
 * background on the insertion time.
 * 
 * @author lpellegr
 */
public class LoadBalancingStatsOverheadBenchmark {

    @Parameter(names = {"-if", "--input-file"}, description = "Path to input file (in TriG format) containing quadruples to load", converter = FileConverter.class)
    private File inputFile = null;

    @Parameter(names = {"-nr", "--nb-runs"}, description = "Number of times the test is performed")
    private int nbRuns = 4;

    @Parameter(names = {"-np", "--nb-publications"}, description = "The number of events to publish", required = true)
    private int nbPublications = -1;

    @Parameter(names = {"-nc", "--nb-characters-per-rdfterm"}, description = "The number of characters per RDF term")
    private int nbCharacters = 10;

    @Parameter(names = {"-dfr", "--discard-first-runs"}, description = "Indicates the number of first runs to discard")
    private int discardFirstRuns = 1;

    @Parameter(names = {"-imds", "--in-memory-datastore"}, description = "Specifies whether datastores on peers have to be persisted on disk or not")
    public boolean inMemoryDatastore = false;

    @Parameter(names = {"-srt", "--stats-recording-type"}, description = "Indicates stats recording to apply (mean or centroid)", converter = StatsRecorderClassConverter.class)
    private Class<? extends StatsRecorder> statsRecorderClass = null;

    @Parameter(names = {"-h", "--help"}, help = true)
    private boolean help;

    public static void main(String[] args) {
        Logger root =
                (Logger) LoggerFactory.getLogger(AddQuadrupleRequest.class);
        root.setLevel(Level.OFF);

        LoadBalancingStatsOverheadBenchmark benchmark =
                new LoadBalancingStatsOverheadBenchmark();

        JCommander jCommander = new JCommander(benchmark);

        try {
            jCommander.parse(args);

            if (benchmark.help) {
                jCommander.usage();
                System.exit(0);
            }
        } catch (ParameterException e) {
            e.printStackTrace();
            jCommander.usage();
            System.exit(1);
        }

        benchmark.execute();

        System.exit(0);
    }

    public void execute() {
        if (this.statsRecorderClass != null) {
            EventCloudProperties.RECORD_STATS_MISC_DATASTORE.setValue(true);
            EventCloudProperties.STATS_RECORDER_CLASS.setValue(this.statsRecorderClass);
        }

        final List<Quadruple> quadruples = this.loadQuadruples();

        if (quadruples.size() < this.nbPublications) {
            throw new IllegalStateException("Not enough quadruples loaded: "
                    + quadruples.size() + " but " + this.nbPublications
                    + " required");
        }

        MicroBenchmark microBenchmark =
                new MicroBenchmark(
                        this.nbRuns, new MicroBenchmarkServiceAdapter() {

                            private EventCloudDeployer deployer;

                            private EventCloudsRegistry registry;

                            private PutGetApi putget;

                            @Override
                            public void setup() throws Exception {
                                this.deployer =
                                        new EventCloudDeployer(
                                                new EventCloudDescription(),
                                                new EventCloudDeploymentDescriptor(
                                                        new SemanticOverlayProvider(
                                                                LoadBalancingStatsOverheadBenchmark.this.inMemoryDatastore)));
                                this.deployer.deploy(1, 1);

                                this.registry =
                                        EventCloudsRegistryFactory.newEventCloudsRegistry();
                                this.registry.register(this.deployer);

                                String registryURL =
                                        this.registry.register("registry");

                                EventCloudId id =
                                        this.deployer.getEventCloudDescription()
                                                .getId();

                                this.putget =
                                        ProxyFactory.newPutGetProxy(
                                                registryURL, id);
                            }

                            @Override
                            public void clear() throws Exception {
                                this.deployer.getRandomTracker()
                                        .getRandomPeer()
                                        .receive(new ClearOperation());
                            }

                            @Override
                            public void run(StatsRecorder recorder)
                                    throws ProActiveException,
                                    EventCloudIdNotManaged {
                                Stopwatch benchmarkStopwatch =
                                        Stopwatch.createUnstarted();
                                Stopwatch perQuadStopwatch =
                                        Stopwatch.createUnstarted();

                                benchmarkStopwatch.start();

                                for (int i = 0; i < LoadBalancingStatsOverheadBenchmark.this.nbPublications; i++) {
                                    perQuadStopwatch.start();
                                    this.putget.add(quadruples.get(i));
                                    perQuadStopwatch.stop();
                                }

                                // ensures that all quadruples have been handled
                                // by the stats recorder
                                PAFuture.waitFor(this.deployer.getRandomTracker()
                                        .getRandomPeer()
                                        .receive(
                                                new SyncStatsRecorderOperation()));

                                benchmarkStopwatch.stop();

                                recorder.reportValue(
                                        "benchmarkStopwatch",
                                        benchmarkStopwatch.elapsed(TimeUnit.MILLISECONDS));
                                recorder.reportValue(
                                        "perQuadStopwatch",
                                        perQuadStopwatch.elapsed(TimeUnit.MILLISECONDS));
                            }

                            @Override
                            public void teardown() throws Exception {
                                ComponentUtils.terminateComponent(this.putget);
                                this.registry.unregister();
                                this.deployer.undeploy();
                            }

                        });
        microBenchmark.discardFirstRuns(this.discardFirstRuns);
        microBenchmark.showProgress();
        microBenchmark.execute();

        System.out.println(microBenchmark.getStatsRecorder().getCategory(
                "benchmarkStopwatch").getMean());
    }

    private List<Quadruple> loadQuadruples() {
        final List<Quadruple> quadruples =
                new ArrayList<Quadruple>(this.nbPublications);

        if (this.inputFile != null) {
            try {
                QuadrupleIterator it =
                        RDFReader.pipe(
                                new FileInputStream(this.inputFile),
                                SerializationFormat.TriG, false, true);
                int c = 0;
                while (it.hasNext()) {
                    quadruples.add(it.next());
                    c++;

                    if (this.nbPublications > 0 && c == this.nbPublications) {
                        break;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            for (int i = 0; i < this.nbPublications; i++) {
                quadruples.add(QuadrupleGenerator.randomWithoutLiteral(this.nbCharacters));
            }
        }

        return quadruples;
    }

    private static final class SyncStatsRecorderOperation extends
            CallableOperation {

        private static final long serialVersionUID = 160L;

        @Override
        public ResponseOperation handle(StructuredOverlay overlay) {
            ((SemanticCanOverlay) overlay).getMiscDatastore()
                    .getStatsRecorder()
                    .sync();
            return BooleanResponseOperation.getPositiveInstance();
        }

    }

}
