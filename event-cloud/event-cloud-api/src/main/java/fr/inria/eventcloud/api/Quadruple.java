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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.api;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Node_Blank;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.tdb.nodetable.NodecLib;

/**
 * A quadruple is 4-tuple containing respectively a graph value, a subject
 * value, a predicate value and an object value.
 * <p>
 * Jena already provides its own abstraction for quadruples. However, from our
 * point of view an Event-Cloud wants to impose some restrictions on the
 * Quadruple components values. For example, an Event-Cloud does not accept
 * Blank Nodes. By providing our own Quadruple abstraction, we can check for
 * this kind of rules when the object is created. Also, the Quadruple class
 * provides a constructor with Jena Node objects to force a type-checking at
 * compile time but it is important to notice that Jena objects are not
 * serializable. Due to this restriction this class implements the
 * Externalizable Java interface to provide its own serialization protocol.
 * <p>
 * Also, the graph value is kept separated from the Triple value (backwards
 * compatibility with tools from linked data people).
 * 
 * @author lpellegr
 */
public class Quadruple implements Externalizable {

    private Node graph;

    private Node subject;

    private Node predicate;

    private Node object;

    // Serialized value used to improve the serialization if a quadruple have to
    // be serialized several times

    private transient String serializedGraph;

    private transient String serializedSubject;

    private transient String serializedPredicate;

    private transient String serializedObject;

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
     * {@code subject}, {@code predicate}, {@code object} node values.
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
        if (graph instanceof Node_Blank || subject instanceof Node_Blank
                || predicate instanceof Node_Blank
                || object instanceof Node_Blank) {
            throw new IllegalArgumentException("Blank nodes are not supported!");
        }

        this.graph = graph;
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    public final Node getGraph() {
        return this.graph;
    }

    public final Node getSubject() {
        return this.subject;
    }

    public final Node getPredicate() {
        return this.predicate;
    }

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
    public void writeExternal(ObjectOutput out) throws IOException {
        if (this.serializedGraph == null) {
            this.serializedGraph = NodecLib.encode(this.graph);
            this.serializedSubject = NodecLib.encode(this.subject);
            this.serializedPredicate = NodecLib.encode(this.predicate);
            this.serializedObject = NodecLib.encode(this.object);

        }

        out.writeUTF(this.serializedGraph);
        out.writeUTF(this.serializedSubject);
        out.writeUTF(this.serializedPredicate);
        out.writeUTF(this.serializedObject);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        this.graph = NodecLib.decode(in.readUTF());
        this.subject = NodecLib.decode(in.readUTF());
        this.predicate = NodecLib.decode(in.readUTF());
        this.object = NodecLib.decode(in.readUTF());
    }

}
