/**
 * Copyright (c) 2011-2012 INRIA.
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
package fr.inria.eventcloud.formatters;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.util.FmtUtils;

import fr.inria.eventcloud.api.Quadruple;

/**
 * A simple formatter that outputs collection of {@link Quadruple}s to the
 * standard output or the specified {@link OutputStream}. This class is
 * convenient for debugging purposes.
 * 
 * @author lpellegr
 */
public class QuadruplesFormatter {

    private static final String GRAPH_TITLE = "Graph";

    private static final String META_GRAPH_TITLE = "Meta Graph";

    private static final String SUBJECT_TITLE = "Subject";

    private static final String PREDICATE_TITLE = "Predicate";

    private static final String OBJECT_TITLE = "Object";

    private QuadruplesFormatter() {

    }

    public static void output(Quadruple... quadruples) {
        output(System.out, Lists.newArrayList(quadruples), false);
    }

    public static void output(Collection<Quadruple> quadruples) {
        output(System.out, quadruples, false);
    }

    public static void output(Collection<Quadruple> quadruples,
                              boolean showMetaGraphValue) {
        output(System.out, quadruples, showMetaGraphValue);
    }

    public static void output(OutputStream out,
                              Collection<Quadruple> quadruples,
                              boolean showMetaGraphValue) {
        int[] columnWidths = findColumnWidths(quadruples, showMetaGraphValue);
        int columnWidthSum = sum(columnWidths);
        byte extraSpaces = 8 + 3;

        StringBuilder result = new StringBuilder();
        result.append('|');
        append(result, '-', columnWidthSum + extraSpaces);
        result.append("|\n| ");

        if (showMetaGraphValue) {
            result.append(META_GRAPH_TITLE);
            append(result, ' ', columnWidths[0] - META_GRAPH_TITLE.length());
        } else {
            result.append(GRAPH_TITLE);
            append(result, ' ', columnWidths[0] - GRAPH_TITLE.length());
        }

        result.append(" | ");
        result.append(SUBJECT_TITLE);
        append(result, ' ', columnWidths[1] - SUBJECT_TITLE.length());
        result.append(" | ");
        result.append(PREDICATE_TITLE);
        append(result, ' ', columnWidths[2] - PREDICATE_TITLE.length());
        result.append(" | ");
        result.append(OBJECT_TITLE);
        append(result, ' ', columnWidths[3] - OBJECT_TITLE.length());
        result.append(" |\n");
        result.append('|');
        append(result, '-', columnWidthSum + extraSpaces);
        result.append("|\n");

        for (Quadruple q : quadruples) {
            Node[] nodes = q.toArray();

            if (showMetaGraphValue) {
                nodes[0] = q.createMetaGraphNode();
            }

            for (int i = 0; i < nodes.length; i++) {
                String rdfTerm = FmtUtils.stringForNode(nodes[i]);

                result.append("| ");
                result.append(rdfTerm);
                result.append(' ');

                append(result, ' ', columnWidths[i] - rdfTerm.length());

                if (i == nodes.length - 1) {
                    result.append('|');
                }
            }

            result.append('\n');
        }

        result.append('|');
        append(result, '-', columnWidthSum + extraSpaces);
        result.append('|');

        try {
            out.write(result.toString().getBytes());
        } catch (IOException e) {
            throw new IllegalArgumentException();
        }
    }

    private static void append(StringBuilder stringBuilder, char character,
                               int nbTimes) {
        for (int i = 0; i < nbTimes; i++) {
            stringBuilder.append(character);
        }
    }

    private static int sum(int... integers) {
        int result = 0;

        for (int integer : integers) {
            result += integer;
        }

        return result;
    }

    private static int[] findColumnWidths(Collection<Quadruple> quadruples,
                                          boolean showMetaGraphValue) {
        int[] columnWidths =
                new int[] {
                        showMetaGraphValue
                                ? META_GRAPH_TITLE.length()
                                : GRAPH_TITLE.length(), SUBJECT_TITLE.length(),
                        PREDICATE_TITLE.length(), OBJECT_TITLE.length()};

        for (Quadruple q : quadruples) {
            Node[] nodes = q.toArray();
            int termLength;

            if (showMetaGraphValue) {
                nodes[0] = q.createMetaGraphNode();
            }

            for (int i = 0; i < nodes.length; i++) {
                termLength = FmtUtils.stringForNode(nodes[i]).length();

                if (termLength > columnWidths[i]) {
                    columnWidths[i] = termLength;
                }
            }
        }

        return columnWidths;
    }

}
