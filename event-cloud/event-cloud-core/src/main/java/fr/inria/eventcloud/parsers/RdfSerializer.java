/**
 * Copyright (c) 2011 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.parsers;

import java.io.OutputStream;

import org.openjena.riot.RiotWriter;

import com.hp.hpl.jena.query.DatasetFactory;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.sparql.core.DatasetGraphFactory;
import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Quadruple;

/**
 * Defines an RDF serializer which knows how to serialize different format such
 * that TriG or NQuads.
 * 
 * @author ialshaba
 */
public class RdfSerializer {

    /**
     * Write the quadruples into an output stream according to a TriG event format
     * 
     * @param out
     *          the output stream where the event is written
     * @param quads
     *          the collection of quadruples to be written
     */
    public static void triGWriter(OutputStream out, Collection<Quadruple> quads) {

        // Tokenizer tokenizer = TokenizerFactory.makeTokenizerUTF8(new
        // FileInputStream("D.trig")) ;
        // Sink<Quad> sink = new SinkQuadsToDataset(dsg) ;
        // RiotReader.createParserQuads(tokenizer, Lang.TRIG, null,
        // sink).parse() ;
        // sink.flush() ;
        
        TriGWriter.write(
                out, DatasetFactory.create(quadruplesToDatasetGraph(quads)));
        

    }
    
    /**
     * Write the quadruples into an output stream according to the NQuads event format
     * @param out 
     *          the output stream where the event is written 
     * @param quads
     *          the collection of quadruples to be written  
     */
    public static void nQuadsWriter(OutputStream out,
                                    Collection<Quadruple> quads) {

        RiotWriter.writeNQuads(out, quadruplesToDatasetGraph(quads));

    }

    /**
     * A private method to convert a collection of quadruples into the corresponding data set graph
     * to be used in the event format writers 
     * @param quads the collection of the quadruples 
     * @return the corresponding data set graph 
     */
    private static DatasetGraph quadruplesToDatasetGraph(Collection<Quadruple> quads) {
        DatasetGraph dsg = DatasetGraphFactory.createMem();
        for (Quadruple q : quads) {
            dsg.add(
                    q.getGraph(), q.getSubject(), q.getPredicate(),
                    q.getObject());
        }

        return dsg;
    }
}
