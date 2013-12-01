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

import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.StatsRecorder;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import fr.inria.eventcloud.api.listeners.NotificationListenerType;
import fr.inria.eventcloud.benchmarks.pubsub.PublishSubscribeBenchmark;
import fr.inria.eventcloud.benchmarks.pubsub.SubscriptionType;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.delayers.Delayer;

/**
 * Benchmark aiming to assess the effect of using {@link Delayer}. Currently it
 * checks the impact for publish/subscribe only.
 * 
 * @author lpellegr
 */
public class DelayersBenchmark {

    @Parameter(names = {"-nr", "--nb-runs"}, description = "Number of runs")
    private int nbRuns = 5;

    @Parameter(names = {"-ct", "--commit-timeout"}, description = "Commit timeout value in ms")
    private int commitTimeout =
            EventCloudProperties.PUBLISH_SUBSCRIBE_OPERATIONS_DELAYER_TIMEOUT.getValue();

    @Parameter(names = {"-bs", "--buffer-size"}, description = "Buffer size")
    private int bufferSize =
            EventCloudProperties.PUBLISH_SUBSCRIBE_OPERATIONS_DELAYER_BUFFER_SIZE.getValue();

    @Parameter(names = {"-qs", "--quadruple-size"}, description = "The size of each quadruple")
    private int quadrupleSize = 10;

    @Parameter(names = {"-ces", "--compound-event-size"}, description = "Number of quadruples per ce")
    private int compoundEventSize = 5;

    @Parameter(names = {"-np", "--number-of-publications"}, description = "Number of publications")
    private int nbPublications = 3000;

    @Parameter(names = {"-h", "--help"}, help = true)
    private boolean help;

    public static void main(String[] args) throws IOException,
            ClassNotFoundException {
        DelayersBenchmark benchmark = new DelayersBenchmark();

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

    public void execute() throws IOException, ClassNotFoundException {
        EventCloudProperties.PUBLISH_SUBSCRIBE_OPERATIONS_DELAYER_TIMEOUT.setValue(this.commitTimeout);
        EventCloudProperties.PUBLISH_SUBSCRIBE_OPERATIONS_DELAYER_BUFFER_SIZE.setValue(this.bufferSize);

        PublishSubscribeBenchmark pubSubBenchmark =
                new PublishSubscribeBenchmark(
                        this.nbRuns, 2, 1, 1, 1, this.nbPublications,
                        this.compoundEventSize, 1, false, 0, 4,
                        SubscriptionType.PATH_QUERY_FIXED_PREDICATE, false,
                        NotificationListenerType.COMPOUND_EVENT, false, 1,
                        false, false, false);

        StatsRecorder stats = pubSubBenchmark.execute();

        double endToEndAverageThroughput =
                this.nbPublications
                        / (stats.getCategory("endToEndMeasurement0").getMean() / 1000);
        double pointToPointAverageThroughput =
                stats.getCategory("pointToPointMeasurement0").getMean()
                        / this.nbPublications;
        double outputAverageThroughput =
                this.nbPublications
                        / (stats.getCategory("outputMeasurement0").getMean() / 1000);

        double nbQuadsPerPeer = stats.getCategory("quadsPerPeer").getValue(0);

        System.out.println("buffer_size=" + this.bufferSize);
        System.out.println("commit_timeout=" + this.commitTimeout);
        System.out.println("endtoend_average_throughput="
                + endToEndAverageThroughput);
        System.out.println("average_latency=" + pointToPointAverageThroughput);
        System.out.println("consumer_average_throughput="
                + outputAverageThroughput);
        System.out.println("nb_quads=" + nbQuadsPerPeer);
    }
}
