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
package fr.inria.eventcloud.benchmarks;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.jena.atlas.lib.Tuple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.StreamRDF;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.sparql.core.Quad;

import fr.inria.eventcloud.overlay.can.SemanticCoordinate;

/**
 * Basic class to compute measurements about quadruples loaded from a TriG file.
 * 
 * @author lpellegr
 */
public class QuadrupleStatsEvaluator {

    public static void main(String[] args) throws FileNotFoundException {
        final DescriptiveStatistics g = new DescriptiveStatistics();
        final DescriptiveStatistics s = new DescriptiveStatistics();
        final DescriptiveStatistics p = new DescriptiveStatistics();
        final DescriptiveStatistics o = new DescriptiveStatistics();

        RDFDataMgr.parse(new StreamRDF() {

            @Override
            public void tuple(Tuple<Node> tuple) {
            }

            @Override
            public void triple(Triple triple) {
            }

            @Override
            public void start() {
            }

            @Override
            public void quad(Quad quad) {

                g.addValue(SemanticCoordinate.applyDopingFunction(
                        quad.getGraph())
                // quad.getGraph().toString()
                        .length());
                s.addValue(SemanticCoordinate.applyDopingFunction(
                        quad.getSubject())
                // quad.getSubject().toString()
                        .length());
                p.addValue(SemanticCoordinate.applyDopingFunction(
                        quad.getPredicate())
                // quad.getPredicate().toString()
                        .length());
                o.addValue(SemanticCoordinate.applyDopingFunction(
                        quad.getObject())
                // quad.getObject().toString()
                        .length());
            }

            @Override
            public void prefix(String prefix, String iri) {
            }

            @Override
            public void finish() {
            }

            @Override
            public void base(String base) {
            }
        }, new FileInputStream(args[0]), Lang.TRIG);

        double percentage = 0.99;

        System.out.println("g --> " + g.getPercentile(percentage));
        System.out.println("s --> " + s.getPercentile(percentage));
        System.out.println("p --> " + p.getPercentile(percentage));
        System.out.println("o --> " + o.getPercentile(percentage));
    }

}
