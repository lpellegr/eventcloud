/**
 * Copyright (c) 2011-2014 INRIA.
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

import org.apfloat.ApfloatContext;
import org.apfloat.spi.BuilderFactory;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
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

import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.datastore.stats.BasicStatsRecorder;
import fr.inria.eventcloud.messages.request.AddQuadrupleRequest;
import fr.inria.eventcloud.overlay.can.StaticLoadBalancingTestBuilder;
import fr.inria.eventcloud.overlay.can.StaticLoadBalancingTestBuilder.Test;

/**
 * A simple benchmark to test the influence of the precision on the
 * load-balancing of RDF data on peers.
 * 
 * @author lpellegr
 */
public class LoadBalancingPrecisionBenchmark {

    @Parameter(names = {"-if", "--input-file"}, description = "TriG input file to use", converter = FileConverter.class, required = true)
    private File trigResource;

    @Parameter(names = {"-nr", "--nb-runs"}, description = "Number of times the test is performed", required = true)
    private int nbRuns = 4;

    @Parameter(names = {"-p", "--precision"}, description = "The precision to use", required = true)
    private int precision =
            P2PStructuredProperties.CAN_COORDINATES_PRECISION.getValue();

    @Parameter(names = {"-np", "--nb-peers"}, description = "The number of peers to inject")
    private int nbPeersToInject = 16;

    @Parameter(names = {"-dslb", "--disable-static-load-balancing"}, description = "Indicates whether static load balancing must be disabled or not")
    private boolean disableStaticLoadBalancing = false;

    @Parameter(names = {"-h", "--help"}, help = true)
    private boolean help;

    public static void main(String[] args) {
        Logger root =
                (Logger) LoggerFactory.getLogger(AddQuadrupleRequest.class);
        root.setLevel(Level.OFF);

        // TODO: find why it is not called automatically through
        // AbstractComponent#initComponentActivity or why it must be defined
        // before a call to the previous method?
        try {
            // sets the default builder factory for the Apfloat library
            ApfloatContext.getContext()
                    .setBuilderFactory(
                            (BuilderFactory) P2PStructuredProperties.APFLOAT_DEFAULT_BUILDER_FACTORY.getValue()
                                    .newInstance());
        } catch (IllegalAccessException iae) {
            throw new IllegalStateException(iae);
        } catch (InstantiationException ie) {
            throw new IllegalStateException(ie);
        }

        LoadBalancingPrecisionBenchmark benchmark =
                new LoadBalancingPrecisionBenchmark();

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
        P2PStructuredProperties.CAN_COORDINATES_PRECISION.setValue(this.precision);

        MicroBenchmark microBenchmark =
                new MicroBenchmark(
                        this.nbRuns, new MicroBenchmarkServiceAdapter() {
                            @Override
                            public void run(StatsRecorder recorder) {
                                StaticLoadBalancingTestBuilder builder =
                                        new StaticLoadBalancingTestBuilder(
                                                LoadBalancingPrecisionBenchmark.this.trigResource.toString()).setNbPeersToInject(LoadBalancingPrecisionBenchmark.this.nbPeersToInject);

                                @SuppressWarnings("unchecked")
                                Class<? extends fr.inria.eventcloud.datastore.stats.StatsRecorder> statsRecordingClass =
                                        (Class<? extends fr.inria.eventcloud.datastore.stats.StatsRecorder>) EventCloudProperties.STATS_RECORDER_CLASS.getValue();

                                if (LoadBalancingPrecisionBenchmark.this.disableStaticLoadBalancing) {
                                    builder.enableStatsRecording(BasicStatsRecorder.class);
                                } else {
                                    builder.enableLoadBalancing(statsRecordingClass);
                                }

                                Test test = builder.build();
                                test.execute();
                                recorder.reportValue(
                                        MicroBenchmark.DEFAULT_CATEGORY_NAME,
                                        test.getExecutionTime());
                            }
                        });
        microBenchmark.showProgress();
        microBenchmark.execute();

        System.out.println("Average time for "
                + this.nbRuns
                + " runs is "
                + microBenchmark.getStatsRecorder().getCategory(
                        MicroBenchmark.DEFAULT_CATEGORY_NAME).getMean());
    }
}
