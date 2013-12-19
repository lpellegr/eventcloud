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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

import fr.inria.eventcloud.api.listeners.NotificationListenerType;
import fr.inria.eventcloud.benchmarks.pubsub.PublishSubscribeBenchmark;
import fr.inria.eventcloud.benchmarks.pubsub.SubscriptionType;
import fr.inria.eventcloud.delayers.Delayer;

/**
 * Benchmark aiming to assess the effect of using {@link Delayer}. Currently it
 * checks the impact for publish/subscribe only.
 * 
 * @author lpellegr
 */
public class FindBestPublicationRate {

    private static final Logger log =
            LoggerFactory.getLogger(FindBestPublicationRate.class);

    @Parameter(names = {"-nr", "--nb-runs"}, description = "Number of runs")
    private int nbRuns = 2;

    @Parameter(names = {"-rts", "--rdf-term-size"}, description = "The size of each RDF term generated")
    private int rdfTermSize = 10;

    @Parameter(names = {"-ces", "--compound-event-size"}, description = "Number of quadruples per ce")
    private int compoundEventSize = 5;

    @Parameter(names = {"-npmin", "--nb-publications-min"}, description = "Number of publications to publish initially")
    private int nbPublicationsMin = 3000;

    @Parameter(names = {"-npmax", "--nb-publications-max"}, description = "Maximum number of publications to publish")
    private int nbPublicationsMax = 30000;

    @Parameter(names = {"-p", "--nb-peers"}, description = "The number of peers to inject into the P2P network")
    public int nbPeers = 1;

    @Parameter(names = {"--nb-publishers"}, description = "The number of publishers, each sharing the publication pool")
    public int nbPublishers = 1;

    @Parameter(names = {"--nb-subscribers"}, description = "The number of subscribers")
    public int nbSubscribers = 1;

    @Parameter(names = {"-gcma", "--gcma-descriptor"}, description = "Path to the GCMA descriptor to use for deploying the benchmark entities on several machines")
    public String gcmaDescriptor = null;

    @Parameter(names = {"-h", "--help"}, help = true)
    private boolean help;

    public static void main(String[] args) throws IOException,
            ClassNotFoundException {
        FindBestPublicationRate benchmark = new FindBestPublicationRate();

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

        benchmark.perform();

        System.exit(0);
    }

    public void perform() {
        boolean inc = true;
        int bestPublicationNumber = this.nbPublicationsMin;
        double bestThroughput =
                this.executeOneIteration(this.nbPublicationsMin);

        while (this.nbPublicationsMin != this.nbPublicationsMax) {
            int m = middle(this.nbPublicationsMin, this.nbPublicationsMax);

            log.info(
                    "Testing with m={}, {}, nbPublicationsMin={}, nbPublicationsMax={}, bestThroughput={}",
                    m, inc
                            ? "inc" : "dec", this.nbPublicationsMin,
                    this.nbPublicationsMax, bestThroughput);

            double throughput = this.executeOneIteration(m);

            if (inc) {
                if (throughput < bestThroughput) {
                    this.nbPublicationsMax = m;
                    inc = !inc;
                } else {
                    this.nbPublicationsMin = m;
                    bestPublicationNumber = m;
                    bestThroughput = throughput;
                }
            } else {
                if (throughput < bestThroughput) {
                    this.nbPublicationsMin = m;
                    inc = !inc;
                } else {
                    this.nbPublicationsMax = m;
                    bestPublicationNumber = m;
                    bestThroughput = throughput;
                }
            }

            log.info(
                    "nbPublicationsMin={}, nbPublicationsMax={}, lastThroughput={}, throughput={}",
                    this.nbPublicationsMin, this.nbPublicationsMax,
                    bestThroughput, throughput);
        }

        log.info(
                "Best throughput ({}) is when nbPublications is set to {}",
                bestThroughput, bestPublicationNumber);
    }

    private static int middle(int a, int b) {
        return (a + b) / 2;
    }

    private double executeOneIteration(int nbPublications) {
        PublishSubscribeBenchmark pubSubBenchmark =
                new PublishSubscribeBenchmark(
                        this.gcmaDescriptor, this.nbRuns, 2, this.nbPublishers,
                        this.nbPeers, this.nbSubscribers, nbPublications,
                        this.compoundEventSize, this.rdfTermSize, 1, false, 0,
                        4, SubscriptionType.PATH_QUERY_FIXED_PREDICATE, false,
                        NotificationListenerType.COMPOUND_EVENT, false, 1,
                        false, false, false);

        StatsRecorder stats = pubSubBenchmark.execute();

        return nbPublications
                / (stats.getCategory("outputMeasurement0").getMean() / 1000);
    }

}
