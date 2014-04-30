/**
 * Copyright (c) 2011-2014 INRIA.
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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.utils.UnicodeUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Iterators;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.graph.Triple;

import fr.inria.eventcloud.utils.NodeSerializer;

/**
 * A compound event is a list of {@link Quadruple}s so that each quadruple share
 * the same graph value. The graph value could be kept separated from the triple
 * value for backward compatibility with linked data tools.
 * <p>
 * Please note that a compound event is not alterable. Any attempt to update the
 * content of a compound event by calling {@link #getTriples()} followed by
 * {@link Collection#add(Object)} will result in a
 * {@link UnsupportedOperationException}.
 * 
 * @author lpellegr
 */
public class CompoundEvent implements Event, Externalizable, List<Quadruple> {

    private static final long serialVersionUID = 160L;

    // this internal list does not contain the meta quadruple
    // it is automatically injected to the network during a publish
    // operation from a proxy
    private List<Quadruple> quadruples;

    private transient List<Triple> triples;

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
    public CompoundEvent(Collection<Quadruple> quadruples) {
        checkDataStructureSize(quadruples.size());

        if (quadruples instanceof List) {
            this.quadruples = (List<Quadruple>) quadruples;
        } else {
            this.quadruples = ImmutableList.copyOf(quadruples);
        }
    }

    public CompoundEvent() {
        // do nothing
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
     * Returns the quadruple at the specified {@code index}.
     * 
     * @param index
     * 
     * @return the quadruple at the specified {@code index}.
     */
    @Override
    public Quadruple get(int index) {
        return this.quadruples.get(index);
    }

    /**
     * Returns the graph value which is assumed to be the same for each
     * quadruple that is contained by the compound event.
     * 
     * @return the graph value which is assumed to be the same for each
     *         quadruple that is contained by the compound event.
     */
    public Node getGraph() {
        return this.quadruples.get(0).getGraph();
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
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.quadruples == null)
                ? 0 : this.quadruples.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }

        if (obj instanceof CompoundEvent) {
            CompoundEvent that = (CompoundEvent) obj;

            // TODO: should be replaced by equals between quadruple lists but it
            // requires to update
            // SemanticNotificationTranslatorTest#testTranslationWithBlankNodes
            // because this last was implemented when CEs were unordered sets of
            // elements
            return this.quadruples.size() == that.quadruples.size()
                    && this.quadruples.containsAll(that.quadruples);

        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Quadruple> iterator() {
        return Iterators.unmodifiableIterator(this.quadruples.iterator());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        Iterator<Quadruple> it = this.quadruples.iterator();

        while (it.hasNext()) {
            buf.append(it.next().toString());

            if (it.hasNext()) {
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
    @Override
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
     *            the event id.
     * @param compoundEventSize
     *            the number of quadruples contained by the compound event.
     * 
     * @return a meta quadruple.
     */
    public static final Quadruple createMetaQuadruple(Node graph,
                                                      int compoundEventSize) {
        String objectValue = Integer.toString(compoundEventSize);

        if ('0' < P2PStructuredProperties.CAN_LOWER_BOUND.getValue()) {
            objectValue =
                    UnicodeUtils.translate(
                            objectValue,
                            P2PStructuredProperties.CAN_LOWER_BOUND.getValue() - '0');
        }

        Quadruple metaQuadruple =
                new Quadruple(
                        graph, graph,
                        PublishSubscribeConstants.EVENT_NB_QUADRUPLES_NODE,
                        NodeFactory.createLiteral(objectValue));

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

    /**
     * {@inheritDoc}
     */
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        int nbQuads = this.quadruples.size();

        // writes the meta graph value
        NodeSerializer.writeURI(out, this.quadruples.get(0)
                .createMetaGraphNode());

        // writes the number of quadruples contained by the CE
        out.writeInt(nbQuads);

        StringBuilder buffer = new StringBuilder();
        // prepare to write subject and predicate values
        for (int i = 1; i < 3; i++) {
            this.appendRdfTerms(buffer, i);

            if (i == 1) {
                buffer.append(' ');
            }
        }

        // TODO: the serialization could be improved by using compression or
        // common terms aggregation. However, these methods will increase the
        // serialization time. When such changes will be performed, they have to
        // be evaluated accurately

        // writes the subject and predicate values of each quadruple
        NodeSerializer.writeString(out, buffer.toString());

        // writes the object values
        for (int i = 0; i < nbQuads; i++) {
            NodeSerializer.writeLiteralOrURI(out, this.quadruples.get(i)
                    .getObject());
        }
    }

    private void appendRdfTerms(StringBuilder buffer, int rdfTermsIndex) {
        Iterator<Quadruple> it = this.quadruples.iterator();

        while (it.hasNext()) {
            buffer.append(it.next().getTermByIndex(rdfTermsIndex).getURI());

            if (it.hasNext()) {
                buffer.append(' ');
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readExternal(ObjectInput in) throws IOException,
            ClassNotFoundException {
        Node metaGraphNode = NodeSerializer.readURI(in);

        int nbQuads = in.readInt();

        String subjectPredicateString = NodeSerializer.readString(in);
        String[] subjectPredicateValues = subjectPredicateString.split(" ");

        Builder<Quadruple> quadruples = new ImmutableList.Builder<Quadruple>();

        for (int i = 0; i < nbQuads; i++) {
            Node object = NodeSerializer.readLiteralOrURI(in);

            quadruples.add(new Quadruple(
                    metaGraphNode,
                    NodeFactory.createURI(subjectPredicateValues[i]),
                    NodeFactory.createURI(subjectPredicateValues[i + nbQuads]),
                    object, false, true));
        }

        this.quadruples = quadruples.build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Object o) {
        return this.quadruples.contains(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object[] toArray() {
        return this.quadruples.toArray();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T[] toArray(T[] a) {
        return this.quadruples.toArray(a);
    }

    /**
     * Guaranteed to throw an exception and leave the list unmodified.
     * 
     * @throws UnsupportedOperationException
     *             always
     * @deprecated Unsupported operation.
     */
    @Deprecated
    @Override
    public boolean add(Quadruple e) {
        throw new UnsupportedOperationException();
    }

    /**
     * Guaranteed to throw an exception and leave the list unmodified.
     * 
     * @throws UnsupportedOperationException
     *             always
     * @deprecated Unsupported operation.
     */
    @Deprecated
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        return this.quadruples.containsAll(c);
    }

    /**
     * Guaranteed to throw an exception and leave the list unmodified.
     * 
     * @throws UnsupportedOperationException
     *             always
     * @deprecated Unsupported operation.
     */
    @Deprecated
    @Override
    public boolean addAll(Collection<? extends Quadruple> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * Guaranteed to throw an exception and leave the list unmodified.
     * 
     * @throws UnsupportedOperationException
     *             always
     * @deprecated Unsupported operation.
     */
    @Deprecated
    @Override
    public boolean addAll(int index, Collection<? extends Quadruple> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * Guaranteed to throw an exception and leave the list unmodified.
     * 
     * @throws UnsupportedOperationException
     *             always
     * @deprecated Unsupported operation.
     */
    @Deprecated
    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * Guaranteed to throw an exception and leave the list unmodified.
     * 
     * @throws UnsupportedOperationException
     *             always
     * @deprecated Unsupported operation.
     */
    @Deprecated
    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    /**
     * Guaranteed to throw an exception and leave the list unmodified.
     * 
     * @throws UnsupportedOperationException
     *             always
     * @deprecated Unsupported operation.
     */
    @Deprecated
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    /**
     * Guaranteed to throw an exception and leave the list unmodified.
     * 
     * @throws UnsupportedOperationException
     *             always
     * @deprecated Unsupported operation.
     */
    @Deprecated
    @Override
    public Quadruple set(int index, Quadruple element) {
        throw new UnsupportedOperationException();
    }

    /**
     * Guaranteed to throw an exception and leave the list unmodified.
     * 
     * @throws UnsupportedOperationException
     *             always
     * @deprecated Unsupported operation.
     */
    @Deprecated
    @Override
    public void add(int index, Quadruple element) {
        throw new UnsupportedOperationException();
    }

    /**
     * Guaranteed to throw an exception and leave the list unmodified.
     * 
     * @throws UnsupportedOperationException
     *             always
     * @deprecated Unsupported operation.
     */
    @Deprecated
    @Override
    public Quadruple remove(int index) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int indexOf(Object o) {
        return this.quadruples.indexOf(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int lastIndexOf(Object o) {
        return this.quadruples.lastIndexOf(o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListIterator<Quadruple> listIterator() {
        return this.quadruples.listIterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ListIterator<Quadruple> listIterator(int index) {
        return this.quadruples.listIterator(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Quadruple> subList(int fromIndex, int toIndex) {
        return this.quadruples.subList(fromIndex, toIndex);
    }

}
