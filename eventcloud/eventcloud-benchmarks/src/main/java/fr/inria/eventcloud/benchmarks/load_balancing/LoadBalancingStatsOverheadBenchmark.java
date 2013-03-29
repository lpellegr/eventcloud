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

import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.MicroBenchmark;
import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.MicroBenchmarkRun;
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
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.datastore.QuadrupleIterator;
import fr.inria.eventcloud.deployment.EventCloudDeployer;
import fr.inria.eventcloud.deployment.EventCloudDeploymentDescriptor;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.factories.EventCloudsRegistryFactory;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.messages.request.can.AddQuadrupleRequest;
import fr.inria.eventcloud.parsers.RdfParser;

/**
 * Load-balancing benchmark to evaluate the impact of the stats computed in
 * background on the insertion time.
 * 
 * @author lpellegr
 */
public class LoadBalancingStatsOverheadBenchmark {

    @Parameter(names = {"-if", "--input-file"}, description = "Read the quadruple to publish from a file", converter = FileConverter.class)
    private File inputFile = null;

    @Parameter(names = {"-nr", "--nb-runs"}, description = "Number of times the test is performed", required = true)
    private int nbRuns = 1;

    @Parameter(names = {"-np", "--nb-publications"}, description = "The number of events to publish", required = true)
    private int nbPublications = -1;

    @Parameter(names = {"-nc", "--nb-characters-per-rdfterm"}, description = "The number of characters per RDF term")
    private int nbCharacters = 10;

    @Parameter(names = {"-dfr", "--discard-first-runs"}, description = "Indicates the number of first runs to discard")
    private int discardFirstRuns = 1;

    @Parameter(names = {"-srt", "--stats-recording-type"}, description = "Indicates which type of stats recording is performed", converter = StatsRecorderClassConverter.class)
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

        MicroBenchmark microBenchmark =
                new MicroBenchmark(this.nbRuns, new MicroBenchmarkRun() {

                    @Override
                    public void run(StatsRecorder recorder)
                            throws ProActiveException, EventCloudIdNotManaged {
                        EventCloudDeployer deployer =
                                new EventCloudDeployer(
                                        new EventCloudDescription(),
                                        new EventCloudDeploymentDescriptor());
                        deployer.deploy(1, 1);

                        EventCloudsRegistry registry =
                                EventCloudsRegistryFactory.newEventCloudsRegistry();
                        registry.register(deployer);

                        String registryURL = registry.register("registry");

                        EventCloudId id =
                                deployer.getEventCloudDescription().getId();

                        final PutGetApi putgetProxy =
                                ProxyFactory.newPutGetProxy(registryURL, id);

                        Stopwatch stopwatch = new Stopwatch();

                        for (int i = 0; i < LoadBalancingStatsOverheadBenchmark.this.nbPublications; i++) {
                            stopwatch.start();
                            putgetProxy.add(quadruples.get(i));
                            stopwatch.stop();
                        }

                        recorder.reportTime(
                                MicroBenchmark.DEFAULT_CATEGORY_NAME,
                                stopwatch.elapsed(TimeUnit.MILLISECONDS));
                    }

                });
        microBenchmark.discardFirstRuns(this.discardFirstRuns);
        microBenchmark.showProgress();
        microBenchmark.execute();

        System.out.println(microBenchmark.getStatsRecorder().getCategory(
                MicroBenchmark.DEFAULT_CATEGORY_NAME).getMean());
    }

    private List<Quadruple> loadQuadruples() {
        final List<Quadruple> quadruples =
                new ArrayList<Quadruple>(this.nbPublications);

        if (this.inputFile != null) {
            try {
                QuadrupleIterator it =
                        RdfParser.parse(
                                new FileInputStream(this.inputFile),
                                SerializationFormat.NQuads, false);
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

}
