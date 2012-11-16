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
package fr.inria.eventcloud.api;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Variable;

/**
 * A QuadruplePattern is {@link Quadruple} where each component value may be
 * either a regular value (the same as a Quadruple) or a variable. A variable is
 * represented by using {@link Node#ANY} or {@code null} as value.
 * <p>
 * For example, a quadruple {@code Q=(Node.ANY, v1, v2, v3)} means that we want
 * to retrieve all the quadruples that have a graph value which is set to any
 * value and a subject, predicate and object value respectively set to
 * {@code v1}, {@code v2} and {@code v3}.
 * 
 * @author lpellegr
 */
public class QuadruplePattern extends Quadruple {

    private static final long serialVersionUID = 130L;

    /**
     * QuadruplePattern that may be used to retrieve all the quadruples.
     */
    public static final QuadruplePattern ANY = new QuadruplePattern(
            Node.ANY, Node.ANY, Node.ANY, Node.ANY);

    public QuadruplePattern() {
        super();

        for (int i = 0; i < super.nodes.length; i++) {
            super.nodes[i] = Node.ANY;
        }
    }

    public QuadruplePattern(Node g, Node s, Node p, Node o) {
        this(g, s, p, o, false);
    }

    public QuadruplePattern(Node g, Node s, Node p, Node o,
            boolean parseMetaInformation) {
        super(replaceNullByNodeAny(g), replaceNullByNodeAny(s),
                replaceNullByNodeAny(p), replaceNullByNodeAny(o), false,
                parseMetaInformation);

        if (g instanceof Node_Variable || s instanceof Node_Variable
                || p instanceof Node_Variable || o instanceof Node_Variable) {
            throw new IllegalArgumentException(
                    "Node_Var is not allowed inside a quadruple pattern, only Node.ANY and null can be used");
        }
    }

    protected static final Node replaceNullByNodeAny(Node node) {
        if (node == null) {
            return Node.ANY;
        }

        return node;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        // indicates what are the nodes that are outputted
        byte bitmap = 0;

        for (int i = 0; i < super.nodes.length; i++) {
            if (super.nodes[i] != Node.ANY) {
                bitmap |= (1 << i);
            }
        }

        out.writeByte(bitmap);

        // outputs nodes that are not null
        if (super.nodes[0] != Node.ANY) {
            super.writeGraph(out);
        }

        if (super.nodes[1] != Node.ANY) {
            super.writeSubject(out);
        }

        if (super.nodes[2] != Node.ANY) {
            super.writePredicate(out);
        }

        if (super.nodes[3] != Node.ANY) {
            super.writeObject(out);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        byte bitmap = in.readByte();

        for (int i = 0; i < 4; i++) {
            if ((1 & (bitmap >> i)) == 1) {
                switch (i) {
                    case 0:
                        super.readGraph(in);
                        break;
                    case 1:
                        super.readSubject(in);
                        break;
                    case 2:
                        super.readPredicate(in);
                        break;
                    case 3:
                        super.readObject(in);
                        break;
                }
            }
        }
    }

}
