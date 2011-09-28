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

import java.io.Serializable;
import java.util.Iterator;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

/**
 * An Event is a collection of {@link Quadruple}s where each quadruple is
 * assumed to share the same graph value. The graph value is kept separated from
 * the Triple value for backward compatibility with linked data tools.
 * <p>
 * Also, it is assumed that an Event is not alterable. Hence, if you try to
 * update the content of an Event by calling {@link #getQuadruples()} followed
 * by {@link Collection#add(Object)}, then any call the methods
 * {@link #getGraph()} and {@link #getTriples()} may return a wrong value. This
 * is also true if you construct an Event with a collection of quadruples that
 * do not share the same graph value. Indeed, no type checking is performed for
 * performance reasons. If necessary, you can use {@link #isValid(Event)} to
 * check whether an Event is valid or not.
 * 
 * @author lpellegr
 */
public class Event implements Iterable<Quadruple>, Serializable {

    private static final long serialVersionUID = 1L;

    private final Collection<Quadruple> quadruples;

    private transient Collection<Triple> triples;

    private transient Node graph;

    /**
     * Creates a new Event from the specified list of quadruples.
     * 
     * @param quads
     *            the quadruples to put into the Event.
     */
    public Event(Quadruple... quads) {
        this(new Collection<Quadruple>(quads));
    }

    /**
     * Creates a new Event from the specified collection of quadruples.
     * 
     * @param quads
     *            the quadruples to put into the Event.
     */
    public Event(Collection<Quadruple> quads) {
        if (quads.size() == 0) {
            throw new IllegalArgumentException(
                    "The quads collection cannot be empty");
        }

        this.quadruples = quads;
        this.addMetaInformation();
    }

    /**
     * Creates a new Event from the specified graph value and the given
     * collection of triples.
     * 
     * @param graph
     *            the graph value associated to each triple.
     * @param triples
     *            the triples to put into the Event.
     */
    public Event(Node graph, Collection<Triple> triples) {
        this.triples = triples;
        this.quadruples = new Collection<Quadruple>();
        for (Triple triple : triples) {
            this.quadruples.add(new Quadruple(graph, triple));
        }
        this.addMetaInformation();
    }

    private void addMetaInformation() {
        // adds a quadruple indicating what is the number
        // of quadruples contained by the event
        this.quadruples.add(new Quadruple(
                this.getGraph(), this.getGraph(),
                PublishSubscribeConstants.EVENT_NB_QUADRUPLES_NODE,
                Node.createLiteral(
                        Integer.toString(this.quadruples.size() + 1),
                        XSDDatatype.XSDint)));
    }

    /**
     * Returns the collection of quadruples that belong to the Event.
     * 
     * @return the collection of quadruples that belong to the Event.
     */
    public Collection<Quadruple> getQuadruples() {
        return new Collection<Quadruple>(this.quadruples);
    }

    /**
     * Returns the graph value which is assumed to be the same for each
     * quadruple or triple that is contained by the Event.
     * 
     * @return the graph value which is assumed to be the same for each
     *         quadruple or triple that is contained by the Event.
     */
    public Node getGraph() {
        if (this.graph == null) {
            this.graph = this.quadruples.iterator().next().getGraph();
        }

        return this.graph;
    }

    /**
     * Returns the collection of triples that belong to the Event. This method
     * is a convenient method for backward compatibility with the linked data
     * tools.
     * 
     * @return the collection of triples that belong to the Event.
     */
    public synchronized Collection<Triple> getTriples() {
        if (this.triples == null) {
            this.triples = new Collection<Triple>();
            for (Quadruple quad : this.quadruples) {
                this.triples.add(new Triple(
                        quad.getSubject(), quad.getPredicate(),
                        quad.getObject()));
            }
        }

        return this.triples;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return this.quadruples.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Event) {
            Event other = (Event) obj;

            for (Quadruple quad : this.quadruples) {
                if (!other.getQuadruples().contains(quad)) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Quadruple> iterator() {
        return this.quadruples.iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        for (Quadruple quad : this.quadruples) {
            buf.append(quad.toString());
            buf.append("\n");
        }
        return buf.toString();
    }

    /**
     * Checks whether the specified {@link Event} is valid or not (i.e. if all
     * the quadruples that are contained by the event share the same graph
     * value).
     * 
     * @param e
     *            the event to check.
     * 
     * @return {@code true} if the event is valid, {@code false} otherwise.
     */
    public static final boolean isValid(Event e) {
        Collection<Quadruple> quads = e.getQuadruples();
        Iterator<Quadruple> it = quads.iterator();

        Node ref = it.next().getGraph();
        while (it.hasNext()) {
            if (!it.next().getGraph().equals(ref)) {
                return false;
            }
        }

        return true;
    }

}
