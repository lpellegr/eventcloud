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
package fr.inria.eventcloud.benchmarks.load_balancing_precision;

import java.io.File;
import java.util.concurrent.Callable;

import org.apfloat.ApfloatContext;
import org.apfloat.spi.BuilderFactory;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.utils.ApfloatUtils;
import org.objectweb.proactive.extensions.p2p.structured.utils.MicroBenchmark;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.FileConverter;

import fr.inria.eventcloud.datastore.stats.CentroidStatsRecorder;
import fr.inria.eventcloud.messages.request.can.AddQuadrupleRequest;
import fr.inria.eventcloud.overlay.can.StaticLoadBalancingTestBuilder;

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
    private int nbRuns = 1;

    @Parameter(names = {"-p", "--precision"}, description = "The precision to use", required = true)
    private long precision = ApfloatUtils.DEFAULT_PRECISION;

    @Parameter(names = {"-np", "--nb-peers"}, description = "The number of peers to inject")
    private int nbPeersToInject = 10;

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
            jCommander.usage();
            System.exit(1);
        }

        benchmark.execute();

        System.exit(0);
    }

    public void execute() {
        ApfloatUtils.DEFAULT_PRECISION = this.precision;

        MicroBenchmark microBenchmark =
                new MicroBenchmark(this.nbRuns, new Callable<Long>() {
                    @Override
                    public Long call() throws Exception {
                        StaticLoadBalancingTestBuilder.Test test =
                                new StaticLoadBalancingTestBuilder(
                                        LoadBalancingPrecisionBenchmark.this.trigResource.toString()).enableLoadBalancing(
                                        CentroidStatsRecorder.class)
                                        .setNbPeersToInject(
                                                LoadBalancingPrecisionBenchmark.this.nbPeersToInject)
                                        .build();

                        test.execute();
                        return test.getExecutionTime();
                    }
                });
        microBenchmark.showProgress();
        microBenchmark.execute();

        System.out.println("Average time for " + this.nbRuns + " runs is "
                + microBenchmark.getMean());
    }

}
