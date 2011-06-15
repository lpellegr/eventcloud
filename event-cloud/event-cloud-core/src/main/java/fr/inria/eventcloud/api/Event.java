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

import java.io.Serializable;
import java.util.Iterator;

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
 * performance reasons.
 * 
 * @author lpellegr
 */
public final class Event implements Iterable<Quadruple>, Serializable {

    private static final long serialVersionUID = 1L;

    private final Collection<Quadruple> quadruples;

    private transient Collection<Triple> triples;

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
    }

    /**
     * Returns the collection of quadruples that belong to the Event.
     * 
     * @return the collection of quadruples that belong to the Event.
     */
    public Collection<Quadruple> getQuadruples() {
        return this.quadruples;
    }

    /**
     * Returns the graph value which is assumed to be the same for each
     * quadruple or triple that is contained by the Event.
     * 
     * @return the graph value which is assumed to be the same for each
     *         quadruple or triple that is contained by the Event.
     */
    public Node getGraph() {
        return this.quadruples.iterator().next().getGraph();
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
            Event e = (Event) obj;

            for (Quadruple quad : this.quadruples) {
                if (!e.getQuadruples().contains(quad)) {
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

}
