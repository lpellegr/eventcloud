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
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import org.openjena.riot.out.OutputLangUtils;
import org.openjena.riot.tokens.Tokenizer;
import org.openjena.riot.tokens.TokenizerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_ANY;
import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.graph.Node_Variable;
import com.hp.hpl.jena.graph.Triple;

import fr.inria.eventcloud.utils.LongLong;
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

    protected static final String TIMESTAMP_SEPARATOR = "#t$";

    // contains respectively the graph, the subject, the predicate
    // and the object value
    private transient Node[] nodes;

    // contains the graph value with its publication datetime
    private transient Node timestampedNode;

    // the publication datetime or -1
    private transient AtomicLong timestamp;

    /**
     * Defines the different formats that are allowed to read quadruples or to
     * write quadruples from and/or to an input stream.
     */
    public enum SerializationFormat {
        TriG, NQuads
    }

    private Quadruple() {
        this.nodes = new Node[4];
        this.timestamp = new AtomicLong(-1);
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
        this(graph, subject, predicate, object, -1, true, true);
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
        return new Quadruple(graph, subject, predicate, object, -1, false, true);
    }

    protected Quadruple(Node graph, Node subject, Node predicate, Node object,
            long timestamp, boolean typeChecking, boolean extractTimestamp) {
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

        if (timestamp != -1) {
            this.timestamp = new AtomicLong(timestamp);
            this.timestampedNode = graph;
        }

        if (extractTimestamp) {
            this.tryToExtractPublicationDateTime();
        }
    }

    /**
     * Indicates whether the quadruple is timestamped or not.
     * 
     * @return {@code true} if the quadruple is timestamped, {@code false}
     *         otherwise.
     */
    public boolean isTimestamped() {
        return this.timestamp.get() != -1;
    }

    /**
     * Timestamps the quadruple with the specified publication datetime. The
     * publication datetime is assumed to be a Java timestamp (with millisecond
     * precision) retrieved by calling for example
     * {@link System#currentTimeMillis()}. Once the quadruple is timestamped,
     * any call to this method will throw an {@link IllegalStateException}.
     * 
     * @param publicationDateTime
     *            the datatime value to use in order to timestamp this
     *            quadruple.
     * 
     * @return the instance of the quadruple which has been timestamped. The
     *         value which is return is not a copy but the original object which
     *         has been altered.
     */
    public Quadruple timestamp(long publicationDateTime) {
        if (publicationDateTime <= 0) {
            throw new IllegalArgumentException(
                    "Expected publication datetime greater than 0 but was:"
                            + publicationDateTime);
        }

        if (this.timestamp.compareAndSet(-1, publicationDateTime)) {
            StringBuilder buf = new StringBuilder();
            buf.append(this.nodes[0].getURI());
            buf.append(TIMESTAMP_SEPARATOR);
            buf.append(this.timestamp);

            this.timestampedNode = Node.createURI(buf.toString());

            return this;
        }

        throw new IllegalStateException("Quadruple already timestamped: "
                + this.timestamp);
    }

    /**
     * Timestamps the quadruple with a publication datetime value equals to the
     * current datetime when the method is called. Once the quadruple is
     * timestamped, any call to this method will throw an
     * {@link IllegalStateException}.
     * 
     * @return the instance of the quadruple which has been timestamped. The
     *         value which is return is not a copy but the original object which
     *         has been altered.
     */
    public Quadruple timestamp() {
        return this.timestamp(System.currentTimeMillis());
    }

    /**
     * Resets the state of the quadruple by removing the timestamp value.
     */
    public synchronized void reset() {
        this.timestamp.set(-1);
        this.timestampedNode = null;
    }

    /**
     * Returns a new quadruple whose the graph value has been replaced by the
     * concatenation of the original graph value and a datetime representing the
     * publication datime of the quadruple.
     * 
     * @return a new quadruple whose the graph value has been replaced by the
     *         concatenation of the original graph value and a datetime
     *         representing the publication datime of the quadruple.
     */
    public Quadruple toTimestampedQuadruple() {
        if (this.timestampedNode == null) {
            return null;
        }

        return new Quadruple(
                this.timestampedNode, this.getSubject(), this.getPredicate(),
                this.getObject(), this.timestamp.get(), false, false);
    }

    /**
     * Returns a timestamp indicating when the quadruple has been published.
     * 
     * @return a timestamp indicating when the quadruple has been published.
     */
    public long getPublicationDateTime() {
        return this.timestamp.get();
    }

    /**
     * Returns the graph value.
     * 
     * @return the graph value.
     */
    public final Node getGraph() {
        return this.nodes[0];
    }

    public Node getTimestampedGraph() {
        return this.timestampedNode;
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
     * Returns a 128 bits hash value for the current quadruple by using
     * {@link MurmurHash} function.
     * 
     * @return a 128 bits hash value for the current quadruple by using
     *         {@link MurmurHash} function.
     */
    public LongLong hashValue() {
        return new LongLong(MurmurHash.hash128(
                this.nodes[0].toString(), this.nodes[1].toString(),
                this.nodes[2].toString(), this.nodes[3].toString(),
                Long.toString(this.timestamp.get())));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(nodes);
        result = prime * result + ((Long) timestamp.get()).hashCode();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Quadruple) {
            Quadruple other = (Quadruple) obj;

            boolean result = true;
            for (int i = 0; i < this.nodes.length; i++) {
                result &= this.nodes[i].equals(other.nodes[i]);
            }

            result &= this.timestamp.get() == other.timestamp.get();
            return result;
        }

        return false;
    }

    /**
     * Returns the quadruple as an array of {@link Node}s.
     * 
     * @return the quadruple as an array of {@link Node}s. The array contains
     *         respectively the graph, the subject, the predicate, and the
     *         object value.
     */
    public Node[] toArray() {
        return Arrays.copyOf(this.nodes, this.nodes.length);
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

        int startIndex = 0;
        if (this.timestampedNode != null) {
            result.append(this.timestampedNode);
            result.append(", ");
            startIndex = 1;
        }

        for (int i = startIndex; i < this.nodes.length; i++) {
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

        this.tryToExtractPublicationDateTime();
    }

    private void tryToExtractPublicationDateTime() {
        this.timestamp = new AtomicLong(-1);

        // the graph value is not a variable
        if (this.nodes[0].isURI()) {
            String uri = this.nodes[0].getURI();
            int timestampSeparatorIndex = uri.lastIndexOf(TIMESTAMP_SEPARATOR);

            // extracts the timestamp associated to the quadruple
            if (timestampSeparatorIndex != -1) {
                this.timestamp =
                        new AtomicLong(Long.parseLong(uri.substring(
                                timestampSeparatorIndex
                                        + TIMESTAMP_SEPARATOR.length(),
                                uri.length())));

                this.timestampedNode = this.nodes[0];
                this.nodes[0] =
                        Node.createURI(uri.substring(0, timestampSeparatorIndex));
            }
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();

        Node graphValue = this.nodes[0];
        if (this.timestampedNode != null) {
            graphValue = this.timestampedNode;
        }

        OutputStreamWriter outWriter = new OutputStreamWriter(out);
        OutputLangUtils.output(outWriter, graphValue, null);
        outWriter.write(' ');

        for (int i = 1; i < this.nodes.length; i++) {
            OutputLangUtils.output(outWriter, this.nodes[i], null);
            if (i < this.nodes.length - 1) {
                outWriter.write(' ');
            }
        }
        outWriter.flush();
    }

}
