/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.inria.eventcloud.utils.trigwriter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

import org.apache.jena.atlas.io.IndentedWriter;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.sparql.core.DatasetGraph;

import eu.play_project.play_commons.eventformat.EventFormatHelpers;

public class TriGWriter {

    public static final int GRAPH_INDENT = 4;

    public static void write(OutputStream out, Dataset dataset) {
        try {
            out.write(EventFormatHelpers.getNsPrefixAbbreviate().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        write(out, dataset.asDatasetGraph(), dataset.getDefaultModel()
                .getNsPrefixMap());

    }

    public static void write(OutputStream out, DatasetGraph dsg,
                             Map<String, String> prefixMap) {
        IndentedWriter iOut = new IndentedWriter(out, false);
        Iterator<Node> graphNames = dsg.listGraphNodes();

        writeGraph(iOut, null, dsg.getDefaultGraph(), prefixMap);
        for (; graphNames.hasNext();) {
            iOut.println();
            Node gn = graphNames.next();
            writeGraph(iOut, gn, dsg.getGraph(gn), prefixMap);
        }
        iOut.flush();

    }

    public static void writeGraph(IndentedWriter out, Node name, Graph graph,
                                  Map<String, String> prefixMap) {
        if (name != null) {
            TurtleWriter2.writeNode(out, name, prefixMap);
            out.print("  ");
        }
        out.println('{');
        out.incIndent(GRAPH_INDENT);
        TurtleWriterBlocks.write(out, graph, prefixMap);

        out.decIndent(GRAPH_INDENT);
        out.ensureStartOfLine();
        out.println('}');
    }

    // public static void main(String... argv) throws FileNotFoundException {
    // DatasetGraph dsg = DatasetGraphFactory.createMem();
    //
    // Tokenizer tokenizer =
    // TokenizerFactory.makeTokenizerUTF8(TW2.class.getResourceAsStream("/D.trig"));
    //
    // Sink<Quad> sink = new SinkQuadsToDataset(dsg);
    // RiotReader.createParserQuads(tokenizer, Lang.TRIG, null, sink).parse();
    // sink.flush();
    // write(System.out, DatasetFactory.create(dsg));
    // }

}
