/**
 * Copyright (c) 2011-2013 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.api;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.jena.riot.Lang;
import org.objectweb.proactive.extensions.p2p.structured.utils.StringRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Longs;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Node_ANY;
import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.graph.Triple;

import fr.inria.eventcloud.utils.NodeSerializer;

/**
 * A quadruple is a 4-tuple containing respectively a graph, a subject, a
 * predicate and an object value. The object value can be either an IRI or a
 * Literal whereas for all the others elements an IRI is required.
 * <p>
 * Jena already provides its own abstraction for quadruples. However, we have to
 * impose some restrictions on the values of a quadruple. For example we do not
 * allow Blank Nodes. By providing our own quadruple abstraction, we can check
 * for this kind of rule when the object is created. Moreover, the quadruple
 * class provides a constructor that use Jena objects for type-checking at
 * compile time. However, these objects are not serializable, that's why the
 * quadruple abstraction overrides the readObject and writeObject methods.
 * <p>
 * Such a quadruple can be published and handled as an event. It embeds some
 * meta information such as the publication time that indicates when the event
 * has been published and optionally the source.
 * 
 * @author lpellegr
 */
public class Quadruple implements Externalizable, Event {

    private static final long serialVersionUID = 150L;

    private static final Logger log = LoggerFactory.getLogger(Quadruple.class);

    public static final String PUBLICATION_TIME_SEPARATOR = "$$";

    public static final String PUBLICATION_SOURCE_SEPARATOR = "@@";

    // contains respectively the graph, the subject, the predicate
    // and the object value of the quadruple
    protected transient Node[] nodes;

    protected transient long publicationTime;

    protected transient String publicationSource;

    /**
     * Defines the different formats that are allowed to read quadruples or to
     * write quadruples from and/or to an input stream.
     */
    public enum SerializationFormat {
        NQuads, TriG;

        public Lang toJenaLang() {
            if (super.ordinal() == 0) {
                return Lang.NQUADS;
            } else {
                return Lang.TRIG;
            }
        }
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
     * the type checking operation. <strong>It must be used with care because it
     * is possible to create quadruples which are not supported by the
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
        this();

        if (checkType) {
            isAllowed(graph);
            isAllowed(subject);
            isAllowed(predicate);
            isAllowed(object);
        }

        if (parseMetaInformation) {
            this.nodes[0] = this.extractAndSetMetaInformation(graph);
        } else {
            this.nodes[0] = graph;
        }

        this.nodes[1] = subject;
        this.nodes[2] = predicate;
        this.nodes[3] = object;
    }

    public Quadruple() {
        this.nodes = new Node[4];
        this.publicationTime = -1;
    }

    private static final void isAllowed(Node node) {
        if (node == null) {
            throw new IllegalArgumentException(
                    "The specified node value is null");
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
     * Returns the RDF term found at the specified {@code index}. A quadruple
     * embeds only four RDF terms. The index value starts at 0. The terms are
     * stored in the following order: graph, subject, predicate and object.
     * 
     * @param index
     *            the index to use for retrieving an RDF term contained by the
     *            quadruple.
     * 
     * @return the RDF term found at the specified {@code index}.
     */
    public final Node getTermByIndex(int index) {
        if (index < 0 || index > 3) {
            throw new IllegalArgumentException("Invalid index: " + index);
        }

        return this.nodes[index];
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

        if (this.publicationSource != null) {
            hasher.putString(this.publicationSource);
        }
        hasher.putLong(this.publicationTime);

        return hasher.hash();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int hash = 1;

        hash = hash * 31 + Arrays.hashCode(this.nodes);
        hash = hash * 31 + Longs.hashCode(this.publicationTime);
        if (this.publicationSource != null) {
            hash = hash * 31 + this.publicationSource.hashCode();
        }

        return hash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Quadruple) {
            Quadruple other = (Quadruple) obj;

            for (int i = 0; i < this.nodes.length; i++) {
                if (!this.nodes[i].equals(other.nodes[i])) {
                    return false;
                }
            }

            if (!equalsNull(this.publicationSource, other.publicationSource)) {
                return false;
            }

            if (this.publicationTime != other.publicationTime) {
                return false;
            }

            return true;
        }

        return false;
    }

    private static final boolean equalsNull(String s1, String s2) {
        if (s1 == s2) {
            return true;
        }

        if (s1 != null && s2 != null) {
            return s1.equals(s2);
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
     * Creates a new node containing the meta information associated to the
     * quadruple. This new node is the concatenation of the original graph value
     * and the meta information (e.g. the publication time, the publication
     * source, etc.).
     * 
     * @return a new node for the graph value whose the content is equals to the
     *         concatenation of the original graph value and the meta
     *         information.
     */
    public Node createMetaGraphNode() {
        if (this.nodes[0].isURI()) {
            StringBuilder uri = new StringBuilder();
            uri.append(this.nodes[0].getURI());

            boolean isPublicationTimeSet = this.publicationTime != -1;
            boolean isPublicationSourceSet = this.publicationSource != null;

            if (isPublicationTimeSet) {
                uri.append(PUBLICATION_TIME_SEPARATOR);
                // adds the publication time in the first position
                uri.append(this.publicationTime);
            }

            if (isPublicationSourceSet) {
                uri.append(PUBLICATION_SOURCE_SEPARATOR);
                // adds the publication source in the second position
                uri.append(this.publicationSource);
            }

            return NodeFactory.createURI(uri.toString());
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
        return this.publicationTime;
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
        if (publicationTime < 1) {
            throw new IllegalArgumentException(
                    "Expected publication datetime greater than 0 but was: "
                            + publicationTime);
        }

        if (this.publicationTime > 0) {
            log.warn(
                    "Publication time {} overriden by {}",
                    this.publicationTime, publicationTime);
        }

        synchronized (this) {
            this.publicationTime = publicationTime;
        }
    }

    /**
     * Returns an URL representing the endpoint of the publisher which has
     * published the quadruple, or {@code null}.
     * 
     * @return an URL representing the endpoint of the publisher which has
     *         published the quadruple, or {@code null}.
     */
    public String getPublicationSource() {
        return this.publicationSource;
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
            throw new IllegalArgumentException("Invalid source: " + source);
        }

        if (this.publicationSource != null) {
            log.warn(
                    "Publication source '{}' overriden by '{}'",
                    this.publicationSource, source);
        }

        synchronized (this) {
            this.publicationSource = source;
        }
    }

    private final boolean metaInformationSet() {
        return this.publicationTime > 0 || this.publicationSource != null;
    }

    protected final Node extractAndSetMetaInformation(Node graph) {
        MetaGraph metaGraph = parse(graph);

        if (metaGraph != null) {
            if (metaGraph.publicationTime != -1) {
                this.setPublicationTime(metaGraph.publicationTime);
            }

            if (metaGraph.publicationSource != null) {
                this.setPublicationSource(metaGraph.publicationSource);
            }

            // returns a graph value without any meta information
            return NodeFactory.createURI(metaGraph.graph);
        }

        return graph;

    }

    private static MetaGraph parse(Node graph) {
        if (graph.isURI()) {
            String uri = graph.getURI();
            return parse(uri);
        }

        // return null and do not thrown an exception because this method may be
        // invoked during the deserialization of a QuadruplePattern. In such a
        // case the graph may not be an URI and the null value is useful on high
        // level to detect that no meta information has been parsed
        return null;
    }

    private static MetaGraph parse(String graph) {
        int publicationTimeSeparatorIndex =
                graph.lastIndexOf(PUBLICATION_TIME_SEPARATOR);
        int publicationSourceSeparatorIndex =
                graph.lastIndexOf(PUBLICATION_SOURCE_SEPARATOR);

        if (publicationTimeSeparatorIndex == -1
                && publicationSourceSeparatorIndex == -1) {
            return null;
        } else if (publicationTimeSeparatorIndex >= 0
                && publicationSourceSeparatorIndex == -1) {
            String publicationTime =
                    graph.substring(publicationTimeSeparatorIndex
                            + PUBLICATION_TIME_SEPARATOR.length());

            try {
                return new MetaGraph(
                        graph.substring(0, publicationTimeSeparatorIndex),
                        Long.parseLong(publicationTime), null);

            } catch (NumberFormatException e) {
                throw new IllegalStateException("Invalid publication time: "
                        + publicationTime);
            }
        } else if (publicationTimeSeparatorIndex == -1
                && publicationSourceSeparatorIndex >= 0) {
            return new MetaGraph(
                    graph.substring(0, publicationSourceSeparatorIndex), -1,
                    graph.substring(publicationSourceSeparatorIndex
                            + PUBLICATION_SOURCE_SEPARATOR.length()));
        } else {
            String publicationTime =
                    graph.substring(
                            publicationTimeSeparatorIndex
                                    + PUBLICATION_TIME_SEPARATOR.length(),
                            publicationSourceSeparatorIndex);

            // if publication time and source are specified, they are
            // necessarily in the order publicationTime, publicationSource
            try {
                return new MetaGraph(
                        graph.substring(0, publicationTimeSeparatorIndex),
                        Long.parseLong(publicationTime),
                        graph.substring(publicationSourceSeparatorIndex
                                + PUBLICATION_SOURCE_SEPARATOR.length()));
            } catch (NumberFormatException e) {
                throw new IllegalStateException("Invalid publication time: "
                        + publicationTime);
            }
        }
    }

    private static class MetaGraph {

        protected final String graph;

        protected final long publicationTime;

        protected final String publicationSource;

        public MetaGraph(String graph, long publicationTime,
                String publicationSource) {
            this.graph = graph;
            this.publicationTime = publicationTime;
            this.publicationSource = publicationSource;
        }

    }

    public static Node removeMetaInformation(Node graph) {
        return NodeFactory.createURI(removeMetaInformation(graph.getURI()));
    }

    public static String removeMetaInformation(String graph) {
        String[] splits =
                graph.split(Pattern.quote(PUBLICATION_TIME_SEPARATOR));

        if (splits.length == 1) {
            return graph;
        } else {
            return splits[0];
        }
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
    public static final long getPublicationTime(Node metaGraphNode) {
        MetaGraph metaGraph = parse(metaGraphNode);

        if (metaGraph != null) {
            return metaGraph.publicationTime;
        }

        return -1;
    }

    /**
     * Returns the publication time associated to the specified
     * {@code metaGraphNode} or {@code -1} if the publication time is not
     * defined or if the specified node is not a meta graph node.
     * 
     * @param metaGraphValue
     *            the meta graph value to parse.
     * 
     * @return the publication time associated to the specified
     *         {@code metaGraphNode} or {@code -1} if the publication time is
     *         not defined or if the specified node is not a meta graph node.
     */
    public static final long getPublicationTime(String metaGraphValue) {
        MetaGraph metaGraph = parse(metaGraphValue);

        if (metaGraph != null) {
            return metaGraph.publicationTime;
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
    public static final String getPublicationSource(Node metaGraphNode) {
        MetaGraph metaGraph = parse(metaGraphNode);

        if (metaGraph != null) {
            return metaGraph.publicationSource;
        }

        return null;
    }

    /**
     * Returns the publication source associated to the specified
     * {@code metaGraphNode} or {@code null} if the publication source is not
     * defined or if the specified node is not a meta graph node.
     * 
     * @param metaGraphValue
     *            the meta graph value to parse.
     * 
     * @return the publication source associated to the specified
     *         {@code metaGraphNode} or {@code null} if the publication source
     *         is not defined or if the specified node is not a meta graph node.
     */
    public static final String getPublicationSource(String metaGraphValue) {
        MetaGraph metaGraph = parse(metaGraphValue);

        if (metaGraph != null) {
            return metaGraph.publicationSource;
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
        return parse(node) != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.toString(StringRepresentation.STRING);
    }

    public String toString(StringRepresentation representation) {
        StringBuilder result = new StringBuilder("(");

        for (int i = 0; i < this.nodes.length; i++) {
            result.append(format(this.nodes[i], representation));
            if (i < this.nodes.length - 1) {
                result.append(", ");
            }
        }
        result.append(')');

        if (this.metaInformationSet()) {
            result.append('{');

            if (this.publicationTime > 0) {
                result.append(this.publicationTime);
            }

            if (this.publicationSource != null) {
                result.append(", ");
                result.append(this.publicationSource);
            }

            result.append('}');
        }

        return result.toString();
    }

    private static String format(Node node, StringRepresentation representation) {
        if (node == Node.ANY) {
            return "?";
        }

        return representation.apply(node.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        // graph, subject and predicate are necessarily IRI values
        String graph = this.createMetaGraphNode().getURI();
        String subject = this.nodes[1].toString();
        String predicate = this.nodes[2].toString();

        StringBuilder gsp = new StringBuilder(graph);
        gsp.append(' ');
        gsp.append(subject);
        gsp.append(' ');
        gsp.append(predicate);

        out.writeUTF(gsp.toString());

        NodeSerializer.writeLiteralOrURI(out, this.nodes[3]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        String[] chunks = new String(in.readUTF()).split(" ");

        this.nodes[0] =
                this.extractAndSetMetaInformation(NodeFactory.createURI(chunks[0]));
        this.nodes[1] = NodeFactory.createURI(chunks[1]);
        this.nodes[2] = NodeFactory.createURI(chunks[2]);
        this.nodes[3] = NodeSerializer.readLiteralOrURI(in);
    }

}
