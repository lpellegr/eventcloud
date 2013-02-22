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
package fr.inria.eventcloud.overlay.can;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;
import org.objectweb.proactive.extensions.p2p.structured.utils.LoggerUtils;
import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.MicroBenchmark;
import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.MicroBenchmarkRun;
import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.StatsRecorder;

import fr.inria.eventcloud.datastore.stats.CentroidStatsRecorder;
import fr.inria.eventcloud.datastore.stats.MeanStatsRecorder;

/**
 * Tests for static load-balancing.
 * 
 * @author lpellegr
 */
public class StaticLoadBalancingTest {

    @Rule
    public Timeout globalTimeout = new Timeout(300000); // 5 min max per
                                                        // method tested

    /*
     * Two peers, load-balancing and stats recording disabled. Short RDF terms length.
     */
    @Test
    public void testStaticLoadBalancing1() {
        new StaticLoadBalancingTestBuilder(1000, 10).build().execute();
    }

    /*
     * Two peers, load-balancing disabled but stats recording enabled. MeanStatsRecorder. Short RDF terms length.
     */
    @Test
    public void testStaticLoadBalancing2() {
        new StaticLoadBalancingTestBuilder(1000, 10).enableStatsRecording(
                MeanStatsRecorder.class).build().execute();
    }

    /*
     * Two peers, load-balancing and stats recording disabled. Normal RDF term length.
     */
    @Test
    public void testStaticLoadBalancing3() {
        new StaticLoadBalancingTestBuilder(1000, 100).build().execute();
    }

    /*
     * Two peers, load-balancing disabled but stats recording enabled. MeanStatsRecorder. Normal RDF term length.
     */
    @Test
    public void testStaticLoadBalancing4() {
        new StaticLoadBalancingTestBuilder(1000, 100).enableStatsRecording(
                MeanStatsRecorder.class).build().execute();
    }

    /*
     * Two peers, load-balancing enabled. MeanStatsRecorder. Short RDF terms length.
     */
    @Test
    public void testStaticLoadBalancing5() {
        new StaticLoadBalancingTestBuilder(1000, 10).enableLoadBalancing(
                MeanStatsRecorder.class)
                .setNbPeersToInject(1)
                .build()
                .execute();
    }

    /*
     * Two peers, load-balancing enabled. MeanStatsRecorder. Short RDF terms length. Simulate compound events.
     */
    @Test
    public void testStaticLoadBalancing6() {
        new StaticLoadBalancingTestBuilder(1000, 10).enableLoadBalancing(
                MeanStatsRecorder.class)
                .simulateCompoundEvents(10)
                .setNbPeersToInject(1)
                .build()
                .execute();
    }

    /*
     * Two peers, load-balancing enabled. CentroidStatsRecorder -> 2/3 short and 1/3 normal RDF terms length.
     */
    @Test
    public void testStaticLoadBalancing7() {
        new StaticLoadBalancingTestBuilder(900, 10).enableLoadBalancing(
                CentroidStatsRecorder.class)
                .insertSkewedData(true)
                .setNbPeersToInject(1)
                .build()
                .execute();
    }

    /*
     * 10 peers, load-balancing enabled. MeanStatsRecorder. Inject 10 peers and performs 100 lookups.
     */
    @Test
    public void testStaticLoadBalancing8() {
        new StaticLoadBalancingTestBuilder(1000, 10).enableLoadBalancing(
                MeanStatsRecorder.class)
                .setNbPeersToInject(10)
                .setNbLookupAfterJoinOperations(100)
                .build()
                .execute();
    }

    /*
     * 10 peers, load-balancing enabled. MeanStatsRecorder. Vessel compound events with useless data (optional).
     */
    @Test
    public void testStaticLoadBalancing9() {
        new StaticLoadBalancingTestBuilder("/vessel.trig").enableLoadBalancing(
                MeanStatsRecorder.class)
                .setNbPeersToInject(10)
                .build()
                .execute();
    }

    /*
     * 10 peers, load-balancing enabled. MeanStatsRecorder. Vessel compound events without useless data (optional).
     */
    @Test
    public void testStaticLoadBalancing10() {
        new StaticLoadBalancingTestBuilder("/vessel-clean.trig").enableLoadBalancing(
                MeanStatsRecorder.class)
                .setNbPeersToInject(10)
                .build()
                .execute();
    }

    public static void main(String[] args) {
        // To be run with the following JVM options
        // -server -Xms4G -Xmx4G
        LoggerUtils.disableLoggers();

        int nbRuns = 10;

        if (args.length == 1) {
            try {
                nbRuns = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                // ignore and use the default number of runs
            }
        }

        MicroBenchmark microBenchmark =
                new MicroBenchmark(nbRuns, new MicroBenchmarkRun() {
                    @Override
                    public void run(StatsRecorder recorder) throws Exception {
                        StaticLoadBalancingTestBuilder.Test test =
                                new StaticLoadBalancingTestBuilder(1000, 10).enableStatsRecording(
                                        MeanStatsRecorder.class)
                                        .build();

                        test.execute();
                        recorder.reportTime(0, test.getExecutionTime());
                    }
                });
        microBenchmark.showProgress();
        microBenchmark.execute();

        System.out.println("Average time for " + nbRuns + " runs is "
                + microBenchmark.getStatsRecorder().getCategory(0).getMean());
    }

}
