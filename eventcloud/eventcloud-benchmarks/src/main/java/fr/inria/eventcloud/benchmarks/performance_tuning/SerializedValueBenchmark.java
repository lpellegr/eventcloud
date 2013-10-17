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
package fr.inria.eventcloud.benchmarks.performance_tuning;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.objectweb.proactive.extensions.p2p.structured.utils.SerializedValue;
import org.objectweb.proactive.extensions.p2p.structured.utils.converters.MakeDeepCopy;
import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.MicroBenchmark;
import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.MicroBenchmarkServiceAdapter;
import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.StatsRecorder;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.hp.hpl.jena.graph.NodeFactory;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.generators.StringGenerator;

/**
 * Benchmark used to evaluate the benefits of using {@link SerializedValue}s in
 * a P2P network.
 * 
 * @author lpellegr
 */
public class SerializedValueBenchmark {

    @Parameter(names = {"-nr", "--nb-runs"}, description = "Number of runs")
    private int nbRuns = 10;

    @Parameter(names = {"-nh", "--nb-hops"}, description = "Number of hops simulated")
    private int nbHops = 40000;

    @Parameter(names = {"-qs", "--quadruple-size"}, description = "The size of each quadruple")
    private int quadrupleSize = 128;

    @Parameter(names = {"-ces", "--compound-event-size"}, description = "Number of quadruples per ce")
    private int compoundEventSize = 10;

    @Parameter(names = {"-twce", "--test-with-compound-events"}, description = "Indicates if we should use compound events or quadruples")
    private boolean evaluateCompoundEvent = false;

    @Parameter(names = {"-h", "--help"}, help = true)
    private boolean help;

    private static String CATEGORY_WITH_SV = "with-serialized-value";

    private static String CATEGORY_WITHOUT_SV = "without-serialized-value";

    public static void main(String[] args) throws IOException,
            ClassNotFoundException {
        SerializedValueBenchmark benchmark = new SerializedValueBenchmark();

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
    }

    public void execute() throws IOException, ClassNotFoundException {
        MicroBenchmark microBenchmark =
                new MicroBenchmark(
                        this.nbRuns, new MicroBenchmarkServiceAdapter() {
                            @Override
                            public void run(StatsRecorder recorder) {
                                long executionTime = 0;

                                if (!SerializedValueBenchmark.this.evaluateCompoundEvent) {
                                    Quadruple q =
                                            SerializedValueBenchmark.this.createQuadruple();

                                    // evaluate serialization with serialized
                                    // value and a quadruple
                                    Message<Quadruple> quadrupleMessageWithSerializedValue =
                                            new MessageWithSerializedValue<Quadruple>(
                                                    q);
                                    executionTime =
                                            this.makeDeepCopy(quadrupleMessageWithSerializedValue);
                                    recorder.reportValue(
                                            CATEGORY_WITH_SV, executionTime);

                                    // evaluate serialization with serialized
                                    // value and a quadruple
                                    Message<Quadruple> quadrupleMessageWithoutSerializedValue =
                                            new MessageWithoutSerializedValue<Quadruple>(
                                                    q);
                                    executionTime =
                                            this.makeDeepCopy(quadrupleMessageWithoutSerializedValue);
                                    recorder.reportValue(
                                            CATEGORY_WITHOUT_SV, executionTime);
                                } else {
                                    CompoundEvent ce =
                                            SerializedValueBenchmark.this.createCompoundEvent();

                                    // evaluate serialization with serialized
                                    // value and a compound event
                                    Message<CompoundEvent> ceMessageWithSerializedValue =
                                            new MessageWithSerializedValue<CompoundEvent>(
                                                    ce);
                                    executionTime =
                                            this.makeDeepCopy(ceMessageWithSerializedValue);
                                    recorder.reportValue(
                                            CATEGORY_WITH_SV, executionTime);

                                    // evaluate serialization with serialized
                                    // value and a quadruple
                                    Message<CompoundEvent> ceMessageWithoutSerializedValue =
                                            new MessageWithoutSerializedValue<CompoundEvent>(
                                                    ce);
                                    executionTime =
                                            this.makeDeepCopy(ceMessageWithoutSerializedValue);
                                    recorder.reportValue(
                                            CATEGORY_WITHOUT_SV, executionTime);
                                }
                            }

                            public long makeDeepCopy(Message<?> message) {
                                Message<?> newMessage = null;

                                Stopwatch stopwatch = Stopwatch.createStarted();

                                for (int i = 0; i < SerializedValueBenchmark.this.nbHops; i++) {
                                    try {
                                        newMessage =
                                                (Message<?>) MakeDeepCopy.makeDeepCopy(message);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }

                                }
                                newMessage.getData();

                                stopwatch.stop();

                                return stopwatch.elapsed(TimeUnit.MILLISECONDS);
                            }

                        });
        // microBenchmark.showProgress();
        microBenchmark.execute();

        System.out.println(this.quadrupleSize
                + " \t "
                + microBenchmark.getStatsRecorder().getCategory(
                        CATEGORY_WITHOUT_SV).getMean()
                + " \t "
                + microBenchmark.getStatsRecorder().getCategory(
                        CATEGORY_WITH_SV).getMean());
    }

    private Quadruple createQuadruple() {
        int chunkSize = this.quadrupleSize / 4;
        int extra = this.quadrupleSize % 4;

        String[] terms = new String[4];

        for (int i = 0; i < terms.length; i++) {
            terms[i] =
                    StringGenerator.randomAlphabetic(1)
                            + StringGenerator.randomAlphanumeric(chunkSize - 1);
        }

        if (extra > 0) {
            terms[3] = terms[3] + StringGenerator.randomAlphanumeric(extra);
        }

        return new Quadruple(
                NodeFactory.createURI(terms[0]),
                NodeFactory.createURI(terms[1]),
                NodeFactory.createURI(terms[2]),
                NodeFactory.createURI(terms[3]));
    }

    private CompoundEvent createCompoundEvent() {
        Builder<Quadruple> builder = ImmutableList.builder();

        for (int i = 0; i < this.compoundEventSize; i++) {
            builder.add(this.createQuadruple());
        }

        return new CompoundEvent(builder.build());
    }

}
