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

import java.util.Iterator;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

/**
 * A compound event is a collection of {@link Quadruple}s where each quadruple
 * is assumed to share the same graph value. The graph value is kept separated
 * from the Triple value for backward compatibility with linked data tools.
 * Please note that when a compound event is constructed, a new quadruple
 * (indicating the number of quadruples associated to the compound event) is
 * added to the list of quadruples.
 * <p>
 * Also, it is assumed that a compound event is not alterable. Hence, if you try
 * to update the content of a compound event by calling {@link #getQuadruples()}
 * followed by {@link Collection#add(Object)}, then any call to the methods
 * {@link #getGraph()} and {@link #getTriples()} may return a wrong value. This
 * is also true if you construct a compound event with a collection of
 * quadruples that do not share the same graph value. Indeed, no type checking
 * is performed for performance reasons. If necessary, you can use
 * {@link #isValid(CompoundEvent)} to check whether a compound event is valid or
 * not.
 * 
 * @author lpellegr
 */
public class CompoundEvent implements Event, Iterable<Quadruple> {

    private static final long serialVersionUID = 1L;

    private final Collection<Quadruple> quadruples;

    private transient Collection<Triple> triples;

    private transient Node graph;

    /**
     * Creates a new compound event from the specified list of quadruples.
     * 
     * @param quads
     *            the quadruples to put into the Event.
     */
    public CompoundEvent(Quadruple... quads) {
        this(new Collection<Quadruple>(quads));
    }

    /**
     * Creates a new compound event from the specified collection of quadruples.
     * 
     * @param quads
     *            the quadruples to put into the Event.
     */
    public CompoundEvent(Collection<Quadruple> quads) {
        this(quads, true);
    }

    /**
     * Creates a new compound event from the specified collection of
     * {@code quadruples} by offering the possibility to choose whether meta
     * information should be added or not (e.g. a quadruple indicating the
     * number of quadruples contained by the Event).
     * 
     * @param quadruples
     *            the quadruples to put into the compound event.
     * @param addMetaInformation
     *            indicates whether meta information should be added or not.
     */
    public CompoundEvent(Collection<Quadruple> quadruples,
            boolean addMetaInformation) {
        if (quadruples.size() == 0) {
            throw new IllegalArgumentException(
                    "The quads collection cannot be empty");
        }

        this.quadruples = Collection.withShallowCopy(quadruples);

        if (addMetaInformation) {
            this.addMetaInformation();
        }
    }

    /**
     * Creates a new compound event from the specified graph value and the given
     * collection of triples.
     * 
     * @param graph
     *            the graph value associated to each triple.
     * @param triples
     *            the triples to put into the Event.
     */
    public CompoundEvent(Node graph, Collection<Triple> triples) {
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
     * Returns the collection of quadruples that belong to the compound event.
     * 
     * @return the collection of quadruples that belong to the compound event.
     */
    public Collection<Quadruple> getQuadruples() {
        return new Collection<Quadruple>(this.quadruples);
    }

    /**
     * Returns the graph value which is assumed to be the same for each
     * quadruple or triple that is contained by the compound event.
     * 
     * @return the graph value which is assumed to be the same for each
     *         quadruple or triple that is contained by the compound event.
     */
    public Node getGraph() {
        if (this.graph == null) {
            this.graph = this.quadruples.iterator().next().getGraph();
        }

        return this.graph;
    }

    /**
     * Returns the collection of triples that belong to the compound event. This
     * method is a convenient method for backward compatibility with the linked
     * data tools.
     * 
     * @return the collection of triples that belong to the compound event.
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
        if (obj instanceof CompoundEvent) {
            CompoundEvent other = (CompoundEvent) obj;

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

        Iterator<Quadruple> it = this.quadruples.iterator();

        for (int i = 0; i < this.quadruples.size(); i++) {
            buf.append(it.next().toString());
            if (i < this.quadruples.size() - 1) {
                buf.append("\n");
            }
        }

        return buf.toString();
    }

    /**
     * Returns the size of this compound event (i.e. the number of quadruples
     * contained by this compound event).
     * 
     * @return the size of this compound event (i.e. the number of quadruples
     *         contained by this compound event).
     */
    public int size() {
        return this.quadruples.size();
    }

    /**
     * Checks whether the specified {@link CompoundEvent} is valid or not (i.e.
     * if all the quadruples that are contained by the compound event share the
     * same graph value).
     * 
     * @param e
     *            the compound event to check.
     * 
     * @return {@code true} if the event is valid, {@code false} otherwise.
     */
    public static final boolean isValid(CompoundEvent e) {
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
