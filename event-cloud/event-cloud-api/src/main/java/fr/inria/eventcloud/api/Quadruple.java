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
package fr.inria.eventcloud.api;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;

import org.openjena.riot.out.OutputLangUtils;
import org.openjena.riot.tokens.Tokenizer;
import org.openjena.riot.tokens.TokenizerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_ANY;
import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;

import fr.inria.eventcloud.utils.MurmurHash;

/**
 * A quadruple is a 4-tuple containing respectively a graph, a subject, a
 * predicate and an object value. The object value can be either an IRI or a
 * Literal whereas for all the others elements an IRI is required.
 * <p>
 * Jena already provides its own abstraction for quadruples. However, the event
 * cloud has to impose some restrictions on the values of a Quadruple (e.g.
 * Blank Nodes are not allowed). By providing our own Quadruple abstraction, we
 * can check for this kind of rule when the object is created. Also, the
 * Quadruple class provides a constructor that use Jena objects for
 * type-checking at compile time. However, these objects are not serializable,
 * that's why the quadruple abstraction overrides the readObject and writeObject
 * methods.
 * <p>
 * For backwards compatibility with linked data tools, the graph value is kept
 * separated from the Triple value.
 * 
 * @author lpellegr
 */
public class Quadruple implements Serializable {

    private static final long serialVersionUID = 1L;

    // contains respectively the graph, the subject, the predicate and the
    // object value
    private transient Node[] nodes;

    /**
     * Defines the different formats that are allowed to read quadruples or to
     * write quadruples from an input stream.
     */
    public enum SerializationFormat {
        TriG, NQuads
    }

    public Quadruple() {
        this.nodes = new Node[4];
    }

    /**
     * Constructs a new Quadruple with the specified {@code graph} node and the
     * given {@code triple}.
     * 
     * @param graph
     *            the graph value.
     * @param triple
     *            the triple to use in order to extract the subject, the
     *            predicate and the object.
     */
    public Quadruple(Node graph, Triple triple) {
        this(graph, triple.getSubject(), triple.getPredicate(),
                triple.getObject());
    }

    /**
     * Constructs a new Quadruple with the specified {@code graph},
     * {@code subject}, {@code predicate} and {@code object} nodes. This
     * constructor will perform a type checking on each node value.
     * 
     * @param graph
     *            the graph value.
     * @param subject
     *            the subject value.
     * @param predicate
     *            the predicate value.
     * @param object
     *            the object value.
     */
    public Quadruple(Node graph, Node subject, Node predicate, Node object) {
        this(graph, subject, predicate, object, true);
    }

    /**
     * Creates a new Quadruple with the specified {@code graph}, {@code subject}
     * , {@code predicate} and {@code object} nodes without checking the type of
     * the nodes. This method has to be used with care!
     * 
     * @param graph
     *            the graph value.
     * @param subject
     *            the subject value.
     * @param predicate
     *            the predicate value.
     * @param object
     *            the object value.
     * 
     * @return the quadruple which has been created.
     */
    public static final Quadruple createWithoutTypeChecking(Node graph,
                                                            Node subject,
                                                            Node predicate,
                                                            Node object) {
        return new Quadruple(graph, subject, predicate, object, false);
    }

    protected Quadruple(Node graph, Node subject, Node predicate, Node object,
            boolean typeChecking) {
        this();
        if (typeChecking) {
            if (graph instanceof Node_Blank || subject instanceof Node_Blank
                    || predicate instanceof Node_Blank
                    || object instanceof Node_Blank) {
                throw new IllegalArgumentException(
                        "Blank nodes are not supported");
            }

            if (graph instanceof Node_ANY || subject instanceof Node_ANY
                    || predicate instanceof Node_ANY
                    || object instanceof Node_ANY
                    || graph instanceof Node_Variable
                    || subject instanceof Node_Variable
                    || predicate instanceof Node_Variable
                    || object instanceof Node_Variable) {
                throw new IllegalArgumentException(
                        "Variables are not allowed in a quadruple, use a QuadruplePattern instead");
            }

            if (graph == null) {
                throw new IllegalArgumentException("graph cannot be null");
            }

            if (subject == null) {
                throw new IllegalArgumentException("subject cannot be null");
            }

            if (predicate == null) {
                throw new IllegalArgumentException("predicate cannot be null");
            }

            if (object == null) {
                throw new IllegalArgumentException("object cannot be null");
            }
        }

        this.nodes[0] = graph;
        this.nodes[1] = subject;
        this.nodes[2] = predicate;
        this.nodes[3] = object;
    }

    /**
     * Returns the graph value.
     * 
     * @return the graph value.
     */
    public final Node getGraph() {
        return this.nodes[0];
    }

    /**
     * Returns the subject value.
     * 
     * @return the subject value.
     */
    public final Node getSubject() {
        return this.nodes[1];
    }

    /**
     * Returns the predicate value.
     * 
     * @return the predicate value.
     */
    public final Node getPredicate() {
        return this.nodes[2];
    }

    /**
     * Returns the object value.
     * 
     * @return the object value.
     */
    public final Node getObject() {
        return this.nodes[3];
    }

    /**
     * Returns a 64 bits hash value for the current quadruple by using
     * {@link MurmurHash} function.
     * 
     * @return a 64 bits hash value for the current quadruple by using
     *         {@link MurmurHash} function.
     */
    public long hashValue() {
        return MurmurHash.hash64(
                this.nodes[0].toString(), this.nodes[1].toString(),
                this.nodes[2].toString(), this.nodes[3].toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return 31
                * (31 * (31 * (31 + this.nodes[0].hashCode()) + this.nodes[1].hashCode()) + this.nodes[2].hashCode())
                + this.nodes[3].hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Quadruple
                && this.nodes[0].equals(((Quadruple) obj).getGraph())
                && this.nodes[1].equals(((Quadruple) obj).getSubject())
                && this.nodes[2].equals(((Quadruple) obj).getPredicate())
                && this.nodes[3].equals(((Quadruple) obj).getObject());
    }

    /**
     * Returns the quadruple as an array of {@link Node}s.
     * 
     * @return the quadruple as an array of {@link Node}s. The array contains
     *         respectively the graph, the subject, the predicate, and the
     *         object value.
     */
    public Node[] toArray() {
        return this.nodes;
    }

    /**
     * Returns the {@link Triple} value associated to the quadruple by cutting
     * the graph value.
     * 
     * @return the {@link Triple} value associated to the quadruple by cutting
     *         the graph value.
     */
    public Triple toTriple() {
        return Triple.create(this.nodes[1], this.nodes[2], this.nodes[3]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("(");
        for (int i = 0; i < this.nodes.length; i++) {
            result.append(this.nodes[i].toString());
            if (i < this.nodes.length - 1) {
                result.append(", ");
            }
        }
        result.append(")");

        return result.toString();
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();

        Tokenizer tokenizer = TokenizerFactory.makeTokenizerUTF8(in);
        this.nodes = new Node[4];
        for (int i = 0; i < nodes.length; i++) {
            this.nodes[i] = tokenizer.next().asNode();
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();

        OutputStreamWriter outWriter = new OutputStreamWriter(out);
        for (int i = 0; i < this.nodes.length; i++) {
            OutputLangUtils.output(outWriter, this.nodes[i], null);
            if (i < this.nodes.length - 1) {
                outWriter.write(' ');
            }
        }
        outWriter.flush();
    }

}
