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

import java.util.Iterator;

import org.openjena.atlas.io.IndentedWriter;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

/** Write Turtle as one line of prefixed names */
public class TurtleWriterFlat {
    static public final int colWidth = 8;
    static public final int predCol = 8;
    static public final int objCol = 8 + predCol;

    public static void write(IndentedWriter out, Graph graph) {
        Iterator<Triple> iter = graph.find(Node.ANY, Node.ANY, Node.ANY);

        for (; iter.hasNext();) {
            Triple triple = iter.next();

            triple.getSubject();
            out.pad(predCol - 1);
            out.print(' ');

            triple.getPredicate();
            out.pad(objCol - 1);
            out.print(' ');

            triple.getObject();

            out.println(" .");
        }
    }
}