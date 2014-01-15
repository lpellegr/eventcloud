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
import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.datastore.QuadrupleIterator;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.messages.request.AddQuadrupleRequest;
import fr.inria.eventcloud.overlay.can.SemanticCoordinate;
import fr.inria.eventcloud.utils.RDFReader;

/**
 * Benchmark class used to assess load balancing doping function.
 * 
 * @author lpellegr
 */
public class LoadBalancingDopingFunctionBenchmark {

    @Parameter(names = {"-if", "--input-file"}, description = "Path to input file (in TriG format) containing quadruples to load", converter = FileConverter.class, required = true)
    private File inputFile = null;

    @Parameter(names = {"-adf", "--apply-doping-function"}, description = "Apply doping function or not")
    private boolean applyDopingFunction = true;

    @Parameter(names = {"-nr", "--nb-runs"}, description = "Number of times the test is performed")
    private int nbRuns = 5;

    @Parameter(names = {"-np", "--nb-publications"}, description = "The number of events to publish", required = true)
    private int nbPublications = -1;

    @Parameter(names = {"-dfr", "--discard-first-runs"}, description = "Indicates the number of first runs to discard")
    private int discardFirstRuns = 2;

    @Parameter(names = {"-h", "--help"}, help = true)
    private boolean help;

    private Action action;

    public static void main(String[] args) {
        Logger root =
                (Logger) LoggerFactory.getLogger(AddQuadrupleRequest.class);
        root.setLevel(Level.OFF);

        LoadBalancingDopingFunctionBenchmark benchmark =
                new LoadBalancingDopingFunctionBenchmark();

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
        if (this.applyDopingFunction) {
            this.action = new Action() {
                @Override
                public String perform(Node value) {
                    return SemanticCoordinate.applyDopingFunction(value);
                }
            };
        } else {
            this.action = new Action() {
                @Override
                public String perform(Node value) {
                    return value.toString();
                }
            };
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

                            private String result;

                            @Override
                            public void run(StatsRecorder recorder)
                                    throws ProActiveException,
                                    EventCloudIdNotManaged {
                                Stopwatch stopwatch = Stopwatch.createStarted();

                                for (Quadruple q : quadruples) {
                                    this.result =
                                            LoadBalancingDopingFunctionBenchmark.this.action.perform(q.getGraph());
                                    this.result +=
                                            LoadBalancingDopingFunctionBenchmark.this.action.perform(q.getSubject());
                                    this.result +=
                                            LoadBalancingDopingFunctionBenchmark.this.action.perform(q.getPredicate());
                                    this.result +=
                                            LoadBalancingDopingFunctionBenchmark.this.action.perform(q.getObject());
                                }

                                stopwatch.stop();

                                recorder.reportValue(
                                        "overall",
                                        stopwatch.elapsed(TimeUnit.MILLISECONDS));
                            }

                            /**
                             * {@inheritDoc}
                             */
                            @Override
                            public void teardown() throws Exception {
                                super.teardown();
                                System.out.println(this.result);
                            }
                        });
        microBenchmark.discardFirstRuns(this.discardFirstRuns);
        microBenchmark.showProgress();
        microBenchmark.execute();

        System.out.println(microBenchmark.getStatsRecorder().getCategory(
                "overall").getMean());
    }

    private List<Quadruple> loadQuadruples() {
        final List<Quadruple> quadruples =
                new ArrayList<Quadruple>(this.nbPublications);

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

        return quadruples;
    }

    public static interface Action {

        String perform(Node value);

    }

}
