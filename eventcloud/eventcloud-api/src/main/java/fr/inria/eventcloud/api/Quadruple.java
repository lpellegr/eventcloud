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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

import org.objectweb.proactive.extensions.p2p.structured.utils.EnumConverter;
import org.objectweb.proactive.extensions.p2p.structured.utils.ReverseEnumMap;
import org.openjena.riot.out.OutputLangUtils;
import org.openjena.riot.tokens.Tokenizer;
import org.openjena.riot.tokens.TokenizerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_ANY;
import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.graph.Triple;

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
 * 
 * @author lpellegr
 */
public class Quadruple implements Event {

    private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(Quadruple.class);

    public static final String META_INFORMATION_SEPARATOR = "/$";

    // contains respectively the graph, the subject, the predicate
    // and the object value of the quadruple
    private transient Node[] nodes;

    // contains the meta information associated to this quadruple such that the
    // publication time, the publication source, etc.
    private transient ConcurrentMap<MetaInformationType, Object> metaInformations;

    /**
     * Defines the different formats that are allowed to read quadruples or to
     * write quadruples from and/or to an input stream.
     */
    public enum SerializationFormat {
        TriG, NQuads
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
     * 
     * @throws IllegalArgumentException
     *             if the type of one element is not allowed (e.g. blank node,
     *             variable or {@code null} value).
     */
    public Quadruple(Node graph, Triple triple) {
        this(graph, triple.getSubject(), triple.getPredicate(),
                triple.getObject());
    }

    /**
     * Constructs a new Quadruple with the specified {@code graph},
     * {@code subject}, {@code predicate} and {@code object} nodes. This
     * constructor will check the type of each node and throw an
     * {@link IllegalArgumentException} if the type of one element is not
     * allowed.
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
     * @throws IllegalArgumentException
     *             if the type of one element is not allowed (e.g. blank node,
     *             variable or {@code null} value).
     */
    public Quadruple(Node graph, Node subject, Node predicate, Node object) {
        this(graph, subject, predicate, object, true, true);
    }

    /**
     * Creates a new Quadruple with the specified {@code graph}, {@code subject}
     * , {@code predicate} and {@code object}. This constructor allows to skip
     * some steps like the type checking operation and the parse meta
     * information operation. <strong>It must be used with care because it is
     * possible to create quadruples which are not supported by the
     * system</strong>.
     * 
     * @param graph
     *            the graph value.
     * @param subject
     *            the subject value.
     * @param predicate
     *            the predicate value.
     * @param object
     *            the object value.
     * @param checkType
     *            indicates whether the type of each element has to be check or
     *            not.
     * @param parseMetaInformation
     *            indicates whether the graph value has to be parsed or not.
     */
    public Quadruple(Node graph, Node subject, Node predicate, Node object,
            boolean checkType, boolean parseMetaInformation) {
        this.nodes = new Node[4];
        this.metaInformations =
                new ConcurrentHashMap<MetaInformationType, Object>(2);

        if (checkType) {
            isAllowed(graph);
            isAllowed(subject);
            isAllowed(predicate);
            isAllowed(object);
        }

        this.nodes[0] = parseMetaInformation
                ? this.extractAndSetMetaInformation(graph) : graph;
        this.nodes[1] = subject;
        this.nodes[2] = predicate;
        this.nodes[3] = object;
    }

    private static void isAllowed(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("Node value is null");
        }

        if (node instanceof Node_Blank) {
            throw new IllegalArgumentException(
                    "Blank nodes are not supported: " + node.toString());
        }

        if (node instanceof Node_ANY) {
            throw new IllegalArgumentException(
                    "Variables are not allowed in a quadruple (see QuadruplePattern): "
                            + node.toString());
        }
    }

    /**
     * Creates a new node containing the meta information associated to the
     * quadruple. This new node contains the concatenation of the original graph
     * value and the meta informations (e.g. the publication time, the
     * publication source, etc.).
     * 
     * @return a new node for the graph value whose the content is equals to the
     *         concatenation of the original graph value and the meta
     *         informations.
     */
    public Node createMetaGraphNode() {
        if (this.nodes[0].isURI()) {
            StringBuilder uri = new StringBuilder();
            uri.append(this.nodes[0].getURI());
            uri.append(META_INFORMATION_SEPARATOR);
            // adds the publication time in the first position
            uri.append(this.getPublicationTime());
            uri.append(META_INFORMATION_SEPARATOR);
            // adds the publication source in the second position
            uri.append(this.getPublicationSource());

            return Node.createURI(uri.toString());
        }

        return this.nodes[0];
    }

    /**
     * Returns a timestamp indicating when the quadruple has been published or
     * {@code -1} if the quadruple has not been yet published.
     * 
     * @return a timestamp indicating when the quadruple has been published or
     *         {@code -1} if the quadruple has not been yet published.
     */
    public long getPublicationTime() {
        Object result =
                this.metaInformations.get(MetaInformationType.PUBLICATION_TIME);

        if (result == null) {
            return -1;
        }

        return (Long) result;
    }

    /**
     * Sets the publication time of the quadruple to the current time when the
     * method call is performed. This is strictly equivalent to
     * {@code setPublicationTime(System.currentTimeMillis())}.
     */
    public void setPublicationTime() {
        this.setPublicationTime(System.currentTimeMillis());
    }

    /**
     * Sets the publication time of the quadruple. The publication time is
     * assumed to be a Java timestamp (with millisecond precision) retrieved by
     * calling for example {@link System#currentTimeMillis()}.
     * 
     * @param publicationTime
     *            the time value to use in order to timestamp this quadruple.
     */
    public void setPublicationTime(long publicationTime) {
        if (publicationTime <= 0) {
            throw new IllegalArgumentException(
                    "Expected publication datetime greater than 0 but was: "
                            + publicationTime);
        }

        this.addMetaInformation(
                MetaInformationType.PUBLICATION_TIME, publicationTime);
    }

    /**
     * Returns an URL representing the endpoint of the publisher which has
     * published the quadruple, or {@code null}.
     * 
     * @return an URL representing the endpoint of the publisher which has
     *         published the quadruple, or {@code null}.
     */
    public String getPublicationSource() {
        return (String) this.metaInformations.get(MetaInformationType.PUBLICATION_SOURCE);
    }

    /**
     * Sets the publication source of the quadruple. The source is assumed to be
     * an URL representing the endpoint of the publisher.
     * 
     * @param source
     *            an URL representing the endpoint of the publisher.
     */
    public void setPublicationSource(String source) {
        if (source == null) {
            throw new IllegalArgumentException(
                    "Publication source cannot be null");
        }

        this.addMetaInformation(MetaInformationType.PUBLICATION_SOURCE, source);
    }

    private void addMetaInformation(MetaInformationType type, Object value) {
        if (log.isWarnEnabled() && this.metaInformations.containsKey(type)) {
            log.warn(
                    "Meta information {} is already set on quadruple {} and will be overriden! This is correct only if you are publishing an event which has been received",
                    type, this);
        }

        this.metaInformations.put(type, value);
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
     * Returns a 128 bits hash value for the current quadruple.
     * 
     * @return a 128 bits hash value for the current quadruple.
     */
    public HashCode hashValue() {
        Hasher hasher = Hashing.murmur3_128().newHasher();

        for (int i = 0; i < this.nodes.length; i++) {
            hasher.putString(this.nodes[i].toString());
        }

        Iterator<Object> iterator = this.metaInformations.values().iterator();
        for (int i = this.nodes.length; i < this.nodes.length
                + this.metaInformations.size(); i++) {
            hasher.putString(iterator.next().toString());
        }

        return hasher.hash();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;

        result = prime * result + Arrays.hashCode(this.nodes);

        for (Object metaInfo : this.metaInformations.values()) {
            result = prime * result + metaInfo.hashCode();
        }

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

            for (MetaInformationType type : this.metaInformations.keySet()) {
                if (!other.metaInformations.containsKey(type)) {
                    return false;
                } else {
                    result &=
                            this.metaInformations.get(type).equals(
                                    other.metaInformations.get(type));
                }
            }

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
        result.append('(');

        for (int i = 0; i < this.nodes.length; i++) {
            result.append(this.nodes[i].toString());
            if (i < this.nodes.length - 1) {
                result.append(", ");
            }
        }
        result.append(')');

        if (this.metaInformations.size() != 0) {
            result.append('{');
        }

        int i = 0;
        for (Entry<MetaInformationType, Object> entry : this.metaInformations.entrySet()) {
            result.append(entry.getKey().getName());
            result.append('=');
            result.append(entry.getValue());

            if (i < this.metaInformations.entrySet().size() - 1) {
                result.append(", ");
            }

            i++;
        }

        if (this.metaInformations.size() != 0) {
            result.append('}');
        }

        return result.toString();
    }

    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();

        this.nodes = new Node[4];
        this.metaInformations =
                new ConcurrentHashMap<MetaInformationType, Object>(2);

        Tokenizer tokenizer = TokenizerFactory.makeTokenizerUTF8(in);

        this.nodes[0] =
                this.extractAndSetMetaInformation(tokenizer.next().asNode());

        for (int i = 1; i < this.nodes.length; i++) {
            this.nodes[i] = tokenizer.next().asNode();
        }
    }

    private Node extractAndSetMetaInformation(Node graph) {
        Object[] metaInformation = parseMetaInformation(graph);

        if (metaInformation != null) {
            long publicationTime = (Long) metaInformation[1];

            if (publicationTime != -1) {
                this.setPublicationTime(publicationTime);
            }

            if (metaInformation[2] != null) {
                this.setPublicationSource((String) metaInformation[2]);
            }

            // returns a graph value with any meta information
            return Node.createURI((String) metaInformation[0]);
        }

        return graph;

    }

    private static Object[] parseMetaInformation(Node graph) {
        if (graph.isURI()
                && graph.getURI().contains(META_INFORMATION_SEPARATOR)) {
            String[] splits =
                    graph.getURI().split(
                            Pattern.quote(META_INFORMATION_SEPARATOR));

            return new Object[] {
                    splits[0], Long.parseLong(splits[1]),
                    splits[2].equals("null")
                            ? null : splits[2]};
        }

        return null;
    }

    public static Node removeMetaInformation(Node graph) {
        String[] splits =
                graph.getURI().split(Pattern.quote(META_INFORMATION_SEPARATOR));

        if (splits.length == 1) {
            return graph;
        } else {
            return Node.createURI(splits[0]);
        }
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();

        OutputStreamWriter outWriter = new OutputStreamWriter(out);
        OutputLangUtils.output(outWriter, this.createMetaGraphNode(), null);
        outWriter.write(' ');

        for (int i = 1; i < this.nodes.length; i++) {
            OutputLangUtils.output(outWriter, this.nodes[i], null);
            if (i < this.nodes.length - 1) {
                outWriter.write(' ');
            }
        }
        outWriter.flush();
    }

    /**
     * Returns the publication time associated to the specified
     * {@code metaGraphNode} or {@code -1} if the publication time is not
     * defined or if the specified node is not a meta graph node.
     * 
     * @param metaGraphNode
     *            the meta graph node to parse.
     * 
     * @return the publication time associated to the specified
     *         {@code metaGraphNode} or {@code -1} if the publication time is
     *         not defined or if the specified node is not a meta graph node.
     */
    public static long getPublicationTime(Node metaGraphNode) {
        checkGraphType(metaGraphNode);

        Object[] metaInformation = parseMetaInformation(metaGraphNode);

        if (metaInformation != null) {
            return (Long) metaInformation[1];
        }

        return -1;
    }

    /**
     * Returns the publication source associated to the specified
     * {@code metaGraphNode} or {@code null} if the publication source is not
     * defined or if the specified node is not a meta graph node.
     * 
     * @param metaGraphNode
     *            the meta graph node to parse.
     * 
     * @return the publication source associated to the specified
     *         {@code metaGraphNode} or {@code null} if the publication source
     *         is not defined or if the specified node is not a meta graph node.
     */
    public static String getPublicationSource(Node metaGraphNode) {
        checkGraphType(metaGraphNode);

        Object[] metaInformation = parseMetaInformation(metaGraphNode);

        if (metaInformation != null) {
            return (String) metaInformation[2];
        }

        return null;
    }

    /**
     * Returns a boolean indicating whether the specified {@code node} is a meta
     * graph node (a node containing meta information about a quadruple) or not.
     * 
     * @param node
     *            the node to check.
     * 
     * @return a boolean indicating whether the specified {@code node} is a meta
     *         graph node (a node containing meta information about a quadruple)
     *         or not.
     */
    public static boolean isMetaGraphNode(Node node) {
        return parseMetaInformation(node) != null;
    }

    private static void checkGraphType(Node graph) {
        if (!graph.isURI()) {
            throw new IllegalArgumentException(
                    "The specified graph value is not an URI: "
                            + graph.toString());
        }
    }

    private enum MetaInformationType
            implements
            EnumConverter<MetaInformationType> {

        PUBLICATION_TIME("time", (short) 0), PUBLICATION_SOURCE(
                "source",
                (short) 1);

        private static ReverseEnumMap<MetaInformationType> map =
                new ReverseEnumMap<MetaInformationType>(
                        MetaInformationType.class);

        private final String name;

        private final short value;

        MetaInformationType(String name, short value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public short convert() {
            return this.value;
        }

        @Override
        public MetaInformationType convert(short val) {
            return map.get(val);
        }

        public String getName() {
            return this.name;
        }

    }

}
