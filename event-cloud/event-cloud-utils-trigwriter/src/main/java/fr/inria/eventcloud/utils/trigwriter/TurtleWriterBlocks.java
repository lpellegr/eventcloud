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

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.openjena.atlas.io.IndentedWriter;
import org.openjena.atlas.iterator.PeekIterator;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class TurtleWriterBlocks {
    public static void write(OutputStream out, Model model) {
        write(out, model.getGraph(), model.getNsPrefixMap());
    }

    public static void write(OutputStream out, Graph graph,
                             Map<String, String> prefixMap) {
        IndentedWriter iOut = new IndentedWriter(out, false);
        write(iOut, graph, prefixMap);
    }

    static void write(IndentedWriter out, Graph graph,
                      Map<String, String> prefixMap) {
        // Lists
        // TurtleWriter2.writePrefixes(out, prefixMap);
        writeTriples(
                out, graph, graph.find(Node.ANY, Node.ANY, Node.ANY), prefixMap);
    }

    // Top level writer.
    // Write blocks of same-subject triples, skipping anything we are going to
    // process specially inline.
    // If the collections are empty, this is about as good as streaming writing
    // gets for Turtle.

    // Change this to be a pure streaming, nested writer.
    static void writeTriples(IndentedWriter out, Graph graph,
                             ExtendedIterator<Triple> triples,
                             Map<String, String> prefixMap) {
        Collection<Node> nestedObjects = Collections.emptyList();
        Collection<Triple> skip = Collections.emptyList();

        PeekIterator<Triple> stream = PeekIterator.create(triples);
        List<Triple> cluster = new ArrayList<Triple>();
        Node subject = null;
        for (;;) {
            cluster.clear();
            for (; stream.hasNext();) {
                Triple t = stream.peek();
                if (skip != null && skip.contains(t)) {
                    stream.next();
                    continue;
                }

                if (subject == null) {
                    subject = t.getSubject();
                } else if (!subject.equals(t.getSubject())) {
                    break;
                }
                cluster.add(t);
                stream.next();
            }
            if (subject != null) {
                TurtleWriter2.writeCluster(
                        out, graph, subject, cluster, nestedObjects, prefixMap);
                subject = null;
            } else {
                break;
            }
        }
        triples.close();
    }

}
