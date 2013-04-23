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

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.atlas.iterator.Iter;
import org.apache.jena.atlas.iterator.Transform;
import org.apache.jena.atlas.lib.Pair;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

/** Support code for the RIOT TurtleWriter */
public class TW2 {
    static final boolean recordObjectMisses = true;

    // Single, multi-function, pass over the graph

    /** Find all embeddable objects */
    static Pair<Set<Node>, Set<Triple>> findOneConnectedBNodeObjects(Graph graph) {
        ExtendedIterator<Triple> iter =
                graph.find(Node.ANY, Node.ANY, Node.ANY);

        Set<Node> bNodesObj1 = new HashSet<Node>(); // The subject of exactly
                                                    // one triple.
        Set<Triple> triplesObj1 = new HashSet<Triple>(); // The triples of such
                                                         // a thing.

        Set<Node> rejects = recordObjectMisses
                ? new HashSet<Node>() : null; // Nodes known not to meet the
                                              // requirement.

        for (; iter.hasNext();) {
            Triple t = iter.next();
            Node obj = t.getObject();
            if (!obj.isBlank()) {
                continue;
            }
            if (rejects != null && rejects.contains(obj)) {
                continue;
            }
            // No point checking bNodesObj1.
            Node n = connectedOnce(graph, obj);
            if (n != null) {
                bNodesObj1.add(n);
                // find triples to skip.
                accTriplesOfSubject(triplesObj1, graph, obj);
            }
        }
        iter.close();
        return Pair.create(bNodesObj1, triplesObj1);
    }

    // CALCULATE FOR LISTS

    static Transform<Triple, Node> subjects = new Transform<Triple, Node>() {

        @Override
        public Node convert(Triple item) {
            return item.getSubject();
        }
    };

    // Combine into a single pass.
    // DISTINCT means it's space using.
    static Iterator<Node> subjects(Graph graph) {
        // Later:
        ExtendedIterator<Triple> iter =
                graph.find(Node.ANY, Node.ANY, Node.ANY);
        return Iter.iter(iter).map(subjects).distinct();
    }

    static Node connectedOnce(Graph graph, Node obj) {
        ExtendedIterator<Triple> iter = graph.find(Node.ANY, Node.ANY, obj);
        try {
            if (!iter.hasNext()) {
                return null;
            }
            iter.next();
            if (!iter.hasNext()) {
                return obj;
            }
            return null;
        } finally {
            iter.close();
        }
    }

    static Collection<Triple> triplesOfSubject(Graph graph, Node subj) {
        Collection<Triple> x = new HashSet<Triple>();
        accTriplesOfSubject(x, graph, subj);
        return x;
    }

    static void accTriplesOfSubject(Collection<Triple> acc, Graph graph,
                                    Node subj) {
        ExtendedIterator<Triple> iter = graph.find(subj, Node.ANY, Node.ANY);
        for (; iter.hasNext();) {
            acc.add(iter.next());
        }
        iter.close();
    }
}
