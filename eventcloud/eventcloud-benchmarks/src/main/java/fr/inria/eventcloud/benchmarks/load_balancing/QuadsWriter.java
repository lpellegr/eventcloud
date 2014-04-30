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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.generators.QuadrupleGenerator;

/**
 * Simple quads writer to pre-generate quadruples that may be read and reused
 * for all load balancing stats overhead benchmarks.
 * 
 * @author lpellegr
 */
public class QuadsWriter {

    private static final int NB_QUADS = 10000;

    private static final int NB_CHARS = 100;

    public static void main(String[] args) throws FileNotFoundException,
            IOException {

        File file =
                new File("/Users/lpellegr/Desktop/" + NB_QUADS + "-" + NB_CHARS
                        + "c.nq");

        BufferedWriter bw = new BufferedWriter(new FileWriter(file));

        for (int i = 0; i < NB_QUADS; i++) {
            Quadruple q = QuadrupleGenerator.randomWithoutLiteral(NB_CHARS);
            bw.write(escape(q.getGraph().getURI()) + " "
                    + escape(q.getSubject().getURI()) + " "
                    + escape(q.getPredicate().getURI()) + " "
                    + escape(q.getObject().getURI()) + " .\n");
        }

        bw.close();

        System.out.println("File " + file + " ready!");
    }

    private static String escape(String s) {
        return "<" + s + ">";
    }

}
