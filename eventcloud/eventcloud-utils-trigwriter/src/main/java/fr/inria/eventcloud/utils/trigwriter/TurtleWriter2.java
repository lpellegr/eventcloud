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
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openjena.atlas.io.IndentedWriter;
import org.openjena.atlas.lib.Pair;
import org.openjena.riot.out.OutputLangUtils;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.vocabulary.RDF;

public class TurtleWriter2 {
    /*public static void main(String ... args)
    {
        SysRIOT.wireIntoJena() ;
        Model m = FileManager.get().loadModel("D.ttl") ;
        
        write(System.out, m) ;
        System.out.println("----------------------------------") ;
        
        
        ByteArrayOutputStream out = new ByteArrayOutputStream() ;
        write(out, m) ;
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray()) ;
        String s = StrUtils.fromUTF8bytes(out.toByteArray()) ;
        Model m2 = ModelFactory.createDefaultModel() ;
        m2.read(in, null, "TTL") ;
        if ( ! m.isIsomorphicWith(m2) )
            System.out.println("**** DIFFERENT") ;
        
        m.write(System.out, "TTL") ;
        
    }*/

    // TODO
    // Order subjects to write
    // ==> scan by subject, not triples

    // Single pass analysis

    // Lists
    // Do before embeddable objects because list take precedence.

    // Subjects \ one-connected objects

    // PredicateObjectLists
    // type to front.
    // Property order is:
    // 1 - rdf:type (as "a")
    // 2 - other rdf: rdfs: namespace items (sorted)
    // 3 - all other properties, sorted by URI (not qname)
    // same properties together.
    // use object lists
    // Configuration.

    // Check old code for special cases.
    // Use better output(Node) code.

    // Check legality of prefix names generated.
    // use stream node output (??)

    // Generally, all padding should level dependent.

    // OLD
    // Width of property before wrapping.
    // This is not necessarily a control of total width
    // e.g. the pretty writer may be writing properties inside indented one ref
    // bNodes
    // protected int widePropertyLen = getIntValue("widePropertyLen", 20) ;
    //
    // // Column for property when an object follows a property on the same line
    // protected int propertyCol = getIntValue("propertyColumn", 8) ;
    //
    // // Minimum gap from property to object when object on a new line.
    // protected int indentObject = propertyCol ;
    //
    // // If a subject is shorter than this, the first property may go on same
    // line.
    // protected int subjectColumn = getIntValue("subjectColumn",
    // indentProperty) ;
    // // Require shortSubject < subjectCol (strict less than)

    private static final int LONG_SUBJECT = 20;
    private static final int LONG_PREDICATE = 30;
    private static final int PREFIX_IRI = 15;

    // Column widths.
    // private static int COLW_SUBJECT = 6;
    // private static int COLW_PREDICATE = 8;

    // Column for start of predicate
    private static final int INDENT_PREDICATE = 8;

    // Column for start of object
    // Usually this is exceeded and predicate, objects are print with min gap.
    private static final int INDENT_OBJECT = 8;

    private static final String iriType = RDF.type.getURI();

    private static final int MIN_GAP = 2;

    private static final String rdfNS = RDF.getURI();

    // Prepare prefixes.
    // Need fast String=>prefix.

    public static void write(OutputStream out, Model model) {
        write(out, model.getGraph(), model.getNsPrefixMap());
    }

    public static void write(OutputStream out, Graph graph,
                             Map<String, String> prefixMap) {
        IndentedWriter iOut = new IndentedWriter(out, false);
        write(iOut, graph, prefixMap);
    }

    // Call from TriG as well.
    static void write(IndentedWriter out, Graph graph,
                      Map<String, String> prefixMap) {
        // Configuration.
        Pair<Set<Node>, Set<Triple>> p =
                TW2.findOneConnectedBNodeObjects(graph);
        Set<Node> bNodesObj1 = p.getLeft();
        Set<Triple> triplesObj1 = p.getRight();

        // Lists
        writePrefixes(out, prefixMap);

        // Or - listSubjects and sort.
        Iterator<Node> subjects = TW2.subjects(graph);
        writeBySubject(
                out, graph, subjects, bNodesObj1, triplesObj1,
                new HashSet<Object>(), prefixMap);
        out.flush();
    }

    static void writePrefixes(IndentedWriter out, Map<String, String> prefixMap) {
        if (!prefixMap.isEmpty()) {
            // prepare?
            for (Map.Entry<String, String> e : prefixMap.entrySet()) {
                print(out, "@prefix ");
                print(out, e.getKey());
                print(out, ": ");
                pad(out, PREFIX_IRI);
                print(out, "<");
                print(out, e.getValue()); // Check?
                print(out, ">");
                print(out, " .");
                println(out);
            }
            // Blank line.
            println(out);
        }
    }

    static void writeBySubject(IndentedWriter out, Graph graph,
                               Iterator<Node> subjects,
                               Collection<Node> nestedObjects,
                               Collection<Triple> skip,
                               Collection<Object> lists,
                               Map<String, String> prefixMap) {
        for (; subjects.hasNext();) {
            Node subj = subjects.next();
            if (nestedObjects.contains(subj)) {
                continue;
            }

            Collection<Triple> cluster = TW2.triplesOfSubject(graph, subj);
            writeCluster(out, graph, subj, cluster, nestedObjects, prefixMap);
        }
    }

    // Common subject
    // Used by the blocks writer as well.
    static void writeCluster(IndentedWriter out, Graph graph, Node subject,
                             Collection<Triple> cluster,
                             Collection<Node> nestedObjects,
                             Map<String, String> prefixMap) {
        // int OFFSET = out.getIndent() ;

        if (cluster.isEmpty()) {
            return;
        }
        writeNode(out, subject, prefixMap);

        if (out.getCol() > LONG_SUBJECT) {
            println(out);
        } else {
            gap(out);
        }
        out.incIndent(INDENT_PREDICATE);
        out.pad();
        writePredicateObjectList(out, graph, cluster, nestedObjects, prefixMap);
        out.decIndent(INDENT_PREDICATE);
        print(out, " ."); // Not perfect
        println(out);
        println(out);
    }

    // need to skip the triples nested.

    private static void writePredicateObjectList(IndentedWriter out,
                                                 Graph graph,
                                                 Collection<Triple> cluster,
                                                 Collection<Node> nestedObjects,
                                                 Map<String, String> prefixMap) {
        boolean first = true;
        // Calc columns

        // Sort triples.
        // rdf:type
        // other rdf and rdfs
        // properties together
        // object lists?
        // Find the colject pad column.

        for (Triple triple : cluster) {
            if (first) {
                first = false;
            } else {
                print(out, " ;");
                println(out);
            }

            // Write predicate.
            int colPredicateStart = out.getCol();

            if (!prefixMap.containsValue(rdfNS)
                    && triple.getPredicate().getURI().equals(iriType)) {
                // I prefer rdf:type when available.
                print(out, "a");
            } else {
                writeNode(out, triple.getPredicate(), prefixMap);
            }
            int colPredicateFinish = out.getCol();
            int wPredicate = (colPredicateFinish - colPredicateStart);

            // Needs to be relative?
            if (wPredicate > LONG_PREDICATE) {
                out.println();
            } else {
                gap(out);
            }

            // Secondary one should be less
            out.incIndent(INDENT_OBJECT);
            out.pad();
            Node obj = triple.getObject();
            if (nestedObjects.contains(obj)) {
                nestedObject(out, graph, obj, nestedObjects, prefixMap);
            } else {
                writeNode(out, triple.getObject(), prefixMap);
            }
            out.decIndent(INDENT_OBJECT);
        }
    }

    private static void nestedObject(IndentedWriter out, Graph graph, Node obj,
                                     Collection<Node> nestedObjects,
                                     Map<String, String> prefixMap) {
        Collection<Triple> x = TW2.triplesOfSubject(graph, obj);

        if (x.isEmpty()) {
            print(out, "[] ");
            return;
        }

        if (x.size() == 1) {
            print(out, "[ ");
            // Includes nested object in triple.
            writePredicateObjectList(out, graph, x, nestedObjects, prefixMap);
            print(out, " ]");
            return;
        }

        // Two or more.
        int here = out.getCol(); // before "["
        print(out, "[");
        int i1 = out.getIndent();
        out.setAbsoluteIndent(here);
        // Inline: println(out) ;
        out.incIndent(2);
        writePredicateObjectList(out, graph, x, nestedObjects, prefixMap);
        out.decIndent(2);
        if (true) {
            println(out); // Newline for "]"
            print(out, "]");
        }
        // else { // Compact
        // print(out, " ]");
        // }
        out.setAbsoluteIndent(i1);
    }

    static void writeNode(IndentedWriter out, Node node,
                          Map<String, String> prefixMap) {
        // See RIOT NodeFormatter
        if (node.isURI()) {
            String iri = node.getURI();
            // Crude.
            String x = abbreviate(iri, prefixMap);
            if (x != null) {
                print(out, x);
                return;
            }
        }

        StringWriter tmp = new StringWriter();
        OutputLangUtils.output(tmp, node, null);

        print(out, tmp.getBuffer().toString());
    }

    /** Abbreviate an IRI or return null */
    private static String abbreviate(String uriStr,
                                     Map<String, String> prefixMap) {
        for (Entry<String, String> e : prefixMap.entrySet()) {
            String prefix = e.getValue().toString();

            if (uriStr.startsWith(prefix)) {
                String ln = uriStr.substring(prefix.length());
                if (strSafeFor(ln, '/') && strSafeFor(ln, '#')
                        && strSafeFor(ln, ':')) {
                    return e.getKey() + ":" + ln;
                }
            }
        }
        return null;
    }

    private static boolean strSafeFor(String str, char ch) {
        return str.indexOf(ch) == -1;
    }

    // flush aggressively (debugging)

    private static void flush(IndentedWriter out) {
        out.flush();
    }

    private static void print(IndentedWriter out, String string) {
        out.print(string);
        flush(out);
    }

    private static void gap(IndentedWriter out) {
        out.print(' ', MIN_GAP);
    }

    private static void pad(IndentedWriter out, int col) {
        out.pad(col, true);
    }

    private static void println(IndentedWriter out) {
        out.println();
        flush(out);
        // System.err.println(out.getIndent()) ;
    }

}
