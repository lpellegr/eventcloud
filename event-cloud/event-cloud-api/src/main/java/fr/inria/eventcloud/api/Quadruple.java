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

/**
 * A quadruple is a 4-tuple containing respectively a graph, a subject, a
 * predicate and an object value. The object value can be either an IRI or a
 * Literal whereas for all the others components an IRI is required.
 * <p>
 * Jena already provides its own abstraction for quadruples. However, the
 * event-cloud has to impose some restrictions on the values of a Quadruple
 * (e.g. the Blank Nodes are not allowed). By providing our own Quadruple
 * abstraction, we can check for this kind of rule when the object is created.
 * Also, the Quadruple class provides a constructor that use Jena objects for
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

    private transient Node graph;

    private transient Node subject;

    private transient Node predicate;

    private transient Node object;

    /**
     * Defines the different formats that are allowed to read quadruples or to
     * write quadruples from an input stream.
     */
    public enum SerializationFormat {
        TriG, NQuads
    }

    public Quadruple() {
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

        this.graph = graph;
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    /**
     * Returns the graph value.
     * 
     * @return the graph value.
     */
    public final Node getGraph() {
        return this.graph;
    }

    /**
     * Returns the subject value.
     * 
     * @return the subject value.
     */
    public final Node getSubject() {
        return this.subject;
    }

    /**
     * Returns the predicate value.
     * 
     * @return the predicate value.
     */
    public final Node getPredicate() {
        return this.predicate;
    }

    /**
     * Returns the object value.
     * 
     * @return the object value.
     */
    public final Node getObject() {
        return this.object;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return 31
                * (31 * (31 * (31 + this.graph.hashCode()) + this.subject.hashCode()) + this.predicate.hashCode())
                + this.object.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof Quadruple
                && this.graph.equals(((Quadruple) obj).getGraph())
                && this.subject.equals(((Quadruple) obj).getSubject())
                && this.predicate.equals(((Quadruple) obj).getPredicate())
                && this.object.equals(((Quadruple) obj).getObject());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("(");
        result.append(this.graph.toString());
        result.append(", ");
        result.append(this.subject.toString());
        result.append(", ");
        result.append(this.predicate.toString());
        result.append(", ");
        result.append(this.object.toString());
        result.append(")");
        return result.toString();
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();

        Tokenizer tokenizer = TokenizerFactory.makeTokenizerUTF8(in);
        this.graph = tokenizer.next().asNode();
        this.subject = tokenizer.next().asNode();
        this.predicate = tokenizer.next().asNode();
        this.object = tokenizer.next().asNode();
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        OutputStreamWriter outWriter = new OutputStreamWriter(out);
        OutputLangUtils.output(outWriter, this.graph, null);
        outWriter.write(' ');
        OutputLangUtils.output(outWriter, this.subject, null);
        outWriter.write(' ');
        OutputLangUtils.output(outWriter, this.predicate, null);
        outWriter.write(' ');
        OutputLangUtils.output(outWriter, this.object, null);
        outWriter.flush();
    }

}
