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

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;

/**
 * 
 * 
 * @author lpellegr
 */
public class QuadrupleStatsAnalyzer {

    // http://km.aifb.kit.edu/projects/btc-2012/dbpedia/data-0.nq.gz
    @Parameter(names = {"-f", "--file"}, description = "Path to the file to analyze which contains RDF data", required = true)
    private String filename;

    @Parameter(names = {"-mtl", "--measure-terms-length"}, description = "Indicates whether RDF terms length must be measured or not")
    private boolean measureTermsLength = false;

    private DescriptiveStatistics graphLength;
    private DescriptiveStatistics subjectLength;
    private DescriptiveStatistics predicateLength;
    private DescriptiveStatistics objectLength;

    private DescriptiveStatistics quadsLength;

    @Parameter(names = {"-h", "--help"}, help = true)
    private boolean help;

    public QuadrupleStatsAnalyzer() {
        this.graphLength = new DescriptiveStatistics();
        this.subjectLength = new DescriptiveStatistics();
        this.predicateLength = new DescriptiveStatistics();
        this.objectLength = new DescriptiveStatistics();

        this.quadsLength = new DescriptiveStatistics();
        // SummaryStatistics
    }

    public static void main(String[] args) {
        QuadrupleStatsAnalyzer analyzer = new QuadrupleStatsAnalyzer();

        JCommander jCommander = new JCommander(analyzer);

        try {
            jCommander.parse(args);

            if (analyzer.help) {
                jCommander.usage();
                System.exit(0);
            }
        } catch (ParameterException e) {
            jCommander.usage();
            System.exit(1);
        }

        analyzer.execute();
        analyzer.printResults();
    }

    private void printResults() {
        if (this.measureTermsLength) {
            System.out.println("Average graph length is "
                    + this.graphLength.getMean());
            System.out.println("Average subject length is "
                    + this.subjectLength.getMean());
            System.out.println("Average predicate length is "
                    + this.predicateLength.getMean());
            System.out.println("Average object length is "
                    + this.objectLength.getMean());
            System.out.println();
        }

        System.out.println(this.quadsLength.getValues().length
                + " quadruples measured");
        System.out.println("Average quadruple length is "
                + this.quadsLength.getMean());
    }

    public void execute() {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(this.filename), 8192 * 1024);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        String line = null;
        try {
            long c = 1;

            while ((line = br.readLine()) != null) {
                int quadSize = 0;
                if (this.measureTermsLength) {
                    int endOfGraph = line.indexOf(' ');
                    int endOfSubject = line.indexOf(' ', endOfGraph + 1);
                    int endOfPredicate = line.indexOf(' ', endOfSubject + 1);

                    int graphLength = endOfGraph - 2;
                    int subjectLength = endOfSubject - endOfGraph - 3;
                    int predicateLength = endOfPredicate - endOfSubject - 3;
                    int objectLength = line.length() - endOfPredicate - 5;

                    this.graphLength.addValue(graphLength);
                    this.subjectLength.addValue(subjectLength);
                    this.predicateLength.addValue(predicateLength);
                    this.objectLength.addValue(objectLength);

                    quadSize =
                            graphLength + subjectLength + predicateLength
                                    + objectLength;
                } else {
                    quadSize = line.length() - 13;
                }

                this.quadsLength.addValue(quadSize);

                if (c % 100000 == 0) {
                    System.out.println(c);
                }

                if (c == 100000000) {
                    break;
                }
                c++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
