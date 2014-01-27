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

import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.StatsRecorder;
import org.objectweb.proactive.extensions.p2p.structured.utils.microbenchmarks.StatsRecorderImpl;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.FileConverter;

import fr.inria.eventcloud.datastore.stats.CentroidStatsRecorder;
import fr.inria.eventcloud.overlay.can.StaticLoadBalancingTestBuilder;
import fr.inria.eventcloud.overlay.can.StaticLoadBalancingTestBuilder.Test;

/**
 * Static load balancing benchmark. Imbalances are fixed by joins made by an
 * oracle that selects at each iteration the peer that has the largest number of
 * quadruples.
 * 
 * @author lpellegr
 */
public class StaticLoadBalancingBenchmark {

    // parameters

    @Parameter(names = {"-if", "--input-file"}, description = "File containing quadruples using TriG syntax", converter = FileConverter.class, required = true)
    public File inputFile;

    @Parameter(names = {"-p", "--nb-peers"}, description = "The maximum number of peers to inject into the P2P network", required = true)
    public int nbPeers = 1;

    @Parameter(names = {"-dslb", "--disable-static-load-balancing"}, description = "Defines whether static load balancing is enabled or not")
    public boolean disableStaticLoadBalancing = false;

    @Parameter(names = {"-h", "--help"}, description = "Print help", help = true)
    public boolean help;

    public static void main(String[] args) {
        StaticLoadBalancingBenchmark benchmark =
                new StaticLoadBalancingBenchmark();

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
        StaticLoadBalancingTestBuilder builder =
                new StaticLoadBalancingTestBuilder(this.inputFile.toString());

        builder.setNbPeersToInject(this.nbPeers - 1);

        if (!this.disableStaticLoadBalancing) {
            builder.enableLoadBalancing(CentroidStatsRecorder.class);
            builder.setNbLookupAfterJoinOperations(1);
        }

        Test test = builder.build();
        test.execute();

        StatsRecorder result = new StatsRecorderImpl(1, 0);
        result.reportValue("default", test.getExecutionTime());

        return result;
    }

}
