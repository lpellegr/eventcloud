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
package fr.inria.eventcloud.benchmarks.radix10_conversion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RiotReader;
import org.objectweb.proactive.extensions.p2p.structured.utils.ApfloatUtils;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.converters.FileConverter;
import com.google.common.base.Stopwatch;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Quad;

import fr.inria.eventcloud.overlay.can.SemanticElement;

/**
 * A simple benchmark to test the influence of the precision on execution time.
 * 
 * @author lpellegr
 */
public class Radix10ConversionBenchmark {

    @Parameter(names = {"-if", "--input-file"}, description = "TriG input file to use", converter = FileConverter.class, required = true)
    private File trigResource;

    @Parameter(names = {"-r", "-run", "--run"}, splitter = NoSplitter.class, description = "The different run to perform. The argument is in the form of \"nbQuadsToRead,precision\"", converter = RunConverter.class, required = true)
    private List<Run> runs = new ArrayList<Run>();

    @Parameter(names = {"-stats", "--statistics", "-rdf-stats", "--rdf-stats"}, description = "Enables RDF statistics")
    private boolean enableRdfStats = false;

    @Parameter(names = {"-h", "--help"}, help = true)
    private boolean help;

    /**
     * Usage example once the jar with dependencies is generated:
     * 
     * {@code  java -server -Xms4G -Xmx4G -cp $PWD/target/eventcloud-benchmarks-1.4.0-SNAPSHOT-jar-with-dependencies.jar:$PWD/target/eventcloud-benchmarks-1.4.0-SNAPSHOT.jar fr.inria.eventcloud.benchmarks.radix10_conversion.Radix10ConversionBenchmark --input-file $HOME/Desktop/twitter.trig --statistics -run 1000000,7 -run 1000000,14 -run 1000000,340 -run 10000000,7 -run 10000000,14 -run 10000000,340}
     */
    public static void main(String[] args) {
        Radix10ConversionBenchmark benchmark = new Radix10ConversionBenchmark();

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

        // it seems this is compulsory due to the Jena iterator that can not be
        // closed
        System.exit(0);
    }

    public void execute() {
        for (Run run : this.runs) {
            this.test(run);
        }
    }

    private final void test(Run run) {
        System.out.println("Testing precision with NB_QUADS_TO_READ="
                + run.getNbQuadsToRead() + " and PRECISION="
                + run.getPrecision());

        FileInputStream fis = null;
        Iterator<Quad> it = null;
        try {
            fis = new FileInputStream(this.trigResource);

            it = RiotReader.createIteratorQuads(fis, Lang.TRIG, null);

            SummaryStatistics statsWithoutPrefixRemoval =
                    new SummaryStatistics();
            SummaryStatistics statsWithPrefixRemoval = new SummaryStatistics();

            Stopwatch stopwatch = Stopwatch.createUnstarted();

            int i = 1;

            while (it.hasNext()) {
                if (i >= run.getNbQuadsToRead()) {
                    break;
                }

                Quad quad = it.next();

                if (this.enableRdfStats) {
                    // compute stats without prefix removal
                    statsWithoutPrefixRemoval.addValue(size(quad.getGraph()));
                    statsWithoutPrefixRemoval.addValue(size(quad.getSubject()));
                    statsWithoutPrefixRemoval.addValue(size(quad.getPredicate()));
                    statsWithoutPrefixRemoval.addValue(size(quad.getObject()));
                }

                String g = SemanticElement.removePrefix(quad.getGraph());
                String s = SemanticElement.removePrefix(quad.getSubject());
                String p = SemanticElement.removePrefix(quad.getPredicate());
                String o = SemanticElement.removePrefix(quad.getObject());

                if (this.enableRdfStats) {
                    // compute stats with prefix removal
                    statsWithPrefixRemoval.addValue(g.length());
                    statsWithPrefixRemoval.addValue(s.length());
                    statsWithPrefixRemoval.addValue(p.length());
                    statsWithPrefixRemoval.addValue(o.length());
                }

                long precision = run.getPrecision();

                stopwatch.start();
                ApfloatUtils.toFloatRadix10(g, precision);
                ApfloatUtils.toFloatRadix10(s, precision);
                ApfloatUtils.toFloatRadix10(p, precision);
                ApfloatUtils.toFloatRadix10(o, precision);
                stopwatch.stop();

                i++;
            }

            if (this.enableRdfStats) {
                System.out.println("  RDF term min size before prefix removal is "
                        + statsWithoutPrefixRemoval.getMin());
                System.out.println("  RDF term max size before prefix removal is "
                        + statsWithoutPrefixRemoval.getMax());
                System.out.println("  RDF term average size before prefix removal is "
                        + statsWithoutPrefixRemoval.getMean());

                System.out.println("  RDF term min size after prefix removal is "
                        + statsWithPrefixRemoval.getMin());
                System.out.println("  RDF term max size after prefix removal is "
                        + statsWithPrefixRemoval.getMax());
                System.out.println("  RDF term average size after prefix removal is "
                        + statsWithPrefixRemoval.getMean());
            }

            System.out.println("Time to perform radix 10 conversion for " + i
                    + " with precision set to " + run.getPrecision() + " is "
                    + stopwatch.toString() + " --> "
                    + stopwatch.elapsed(TimeUnit.MILLISECONDS) + " ms");
            System.out.println();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static final int size(Node node) {
        return node.toString().length();
    }

}
