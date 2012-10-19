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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

/**
 * A compound event is a collection of {@link Quadruple}s such that each
 * quadruple share the same graph value. The graph value could be kept separated
 * from the triple value for backward compatibility with linked data tools.
 * <p>
 * Please note that a compound event is not alterable. Any attempt to update the
 * content of a compound event by calling {@link #getQuadruples()} or
 * {@link #getTriples()} followed by {@link Collection#add(Object)} will result
 * in a {@link UnsupportedOperationException}.
 * 
 * @author lpellegr
 */
public class CompoundEvent implements Event, Iterable<Quadruple> {

    private static final long serialVersionUID = 1L;

    // this internal list does not contain the meta quadruple
    // it is automatically added during a publish operation from a proxy
    private final List<Quadruple> quadruples;

    private transient List<Triple> triples;

    private transient Node graph;

    /**
     * Creates a new compound event from the specified list of quadruples. No
     * check is performed against the quadruples that are passed to the
     * constructor. It is up to you to ensure that all quadruples share the same
     * graph value, if not the behavior is unpredictable.
     * 
     * @param quads
     *            the quadruples to put in the compound event.
     */
    public CompoundEvent(Quadruple... quads) {
        checkDataStructureSize(quads.length);

        this.quadruples = ImmutableList.copyOf(quads);
    }

    /**
     * Creates a new compound event from the specified collection of
     * {@code quadruples}. No check is performed against the quadruples that are
     * passed to the constructor. It is up to you to ensure that all quadruples
     * share the same graph value, if not the behavior is unpredictable.
     * 
     * @param quadruples
     *            the quadruples to put in the compound event.
     */
    public CompoundEvent(List<Quadruple> quadruples) {
        checkDataStructureSize(quadruples.size());

        if (quadruples instanceof ImmutableList) {
            this.quadruples = quadruples;
        } else {
            this.quadruples = ImmutableList.copyOf(quadruples);
        }

    }

    private static final void checkDataStructureSize(int size) {
        if (size < 1) {
            throw new IllegalArgumentException(
                    "The specified list or array must contain at least one quadruple");
        }
    }

    /**
     * Creates a new compound event from the specified graph value and the given
     * collection of triples.
     * 
     * @param graph
     *            the graph value associated to each triple.
     * @param triples
     *            the triples to put in the compound event.
     */
    public CompoundEvent(Node graph, List<Triple> triples) {
        this.triples = ImmutableList.copyOf(triples);

        Builder<Quadruple> builder = new ImmutableList.Builder<Quadruple>();
        for (Triple triple : this.triples) {
            builder.add(new Quadruple(graph, triple));
        }

        this.quadruples = builder.build();
    }

    /**
     * Returns an unmodifiable list of the quadruples contained by the compound
     * event.
     * 
     * @return an unmodifiable list of the quadruples contained by the compound
     *         event.
     */
    public List<Quadruple> getQuadruples() {
        return this.quadruples;
    }

    /**
     * Returns the graph value which is assumed to be the same for each
     * quadruple that is contained by the compound event.
     * 
     * @return the graph value which is assumed to be the same for each
     *         quadruple that is contained by the compound event.
     */
    public synchronized Node getGraph() {
        if (this.graph == null) {
            this.graph = this.quadruples.get(0).getGraph();
        }

        return this.graph;
    }

    /**
     * Returns an unmodifiable list of triples contained by the compound event.
     * This method is a convenient method for backward compatibility with the
     * linked data tools.
     * 
     * @return the list of triples contained by the compound event.
     */
    public synchronized List<Triple> getTriples() {
        if (this.triples == null) {
            Builder<Triple> builder = new ImmutableList.Builder<Triple>();

            for (Quadruple quad : this.quadruples) {
                builder.add(new Triple(
                        quad.getSubject(), quad.getPredicate(),
                        quad.getObject()));
            }

            this.triples = builder.build();
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
        return obj instanceof CompoundEvent
                && this.quadruples.containsAll(((CompoundEvent) obj).quadruples);
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
                buf.append('\n');
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
     * Creates a meta quadruple for the specified compound event. The meta
     * quadruple is a quadruple that indicates the number of quadruples embed by
     * the compound event.
     * 
     * @param compoundEvent
     *            the compound event for which the meta quadruple is created.
     * 
     * @return a meta quadruple.
     */
    public static final Quadruple createMetaQuadruple(CompoundEvent compoundEvent) {
        Quadruple metaQuadruple =
                createMetaQuadruple(
                        compoundEvent.getGraph(),
                        compoundEvent.quadruples.size());

        String publicationSource =
                compoundEvent.quadruples.get(0).getPublicationSource();

        if (publicationSource != null) {
            metaQuadruple.setPublicationSource(publicationSource);
        }

        return metaQuadruple;
    }

    /**
     * Creates a meta quadruple for the specified graph value and compound event
     * size. The meta quadruple is a quadruple that indicates the number of
     * quadruples embed by the compound event.
     * 
     * @param graph
     *            the event id.`
     * @param compoundEventSize
     *            the number of quadruples contained by the compound event.
     * 
     * @return a meta quadruple.
     */
    public static final Quadruple createMetaQuadruple(Node graph,
                                                      int compoundEventSize) {
        Quadruple metaQuadruple =
                new Quadruple(
                        graph, graph,
                        PublishSubscribeConstants.EVENT_NB_QUADRUPLES_NODE,
                        Node.createLiteral(
                                Integer.toString(compoundEventSize),
                                XSDDatatype.XSDint));

        return metaQuadruple;
    }

    /**
     * Checks whether the specified {@link CompoundEvent} is valid or not (i.e.
     * whether all the quadruples that are contained by the compound event share
     * the same graph value or not).
     * 
     * @param e
     *            the compound event to check.
     * 
     * @return {@code true} if the event is valid, {@code false} otherwise.
     */
    public static final boolean isValid(CompoundEvent e) {
        Node graph = e.quadruples.get(0).getGraph();
        Iterator<Quadruple> it = e.quadruples.iterator();

        // skip the first one because we know it is equals
        it.next();

        while (it.hasNext()) {
            if (!it.next().getGraph().equals(graph)) {
                return false;
            }
        }

        return true;
    }

}
