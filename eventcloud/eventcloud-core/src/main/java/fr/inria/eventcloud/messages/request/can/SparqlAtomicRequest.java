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
package fr.inria.eventcloud.messages.request.can;

import java.util.List;

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.algebra.Algebra;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.messages.response.can.QuadruplePatternResponseProvider;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.overlay.can.SemanticElement;
import fr.inria.eventcloud.reasoner.AtomicQuery;
import fr.inria.eventcloud.validator.AtomicQueryConstraintsValidator;

/**
 * Retrieves all the {@link Quadruple}s that match the {@link AtomicQuery} that
 * is specified when the object is constructed. A {@link SparqlAtomicRequest}
 * differs from a {@link QuadruplePatternRequest}. Indeed, with a
 * {@link SparqlAtomicRequest} the request routing can be improved by using some
 * filter constraints and the result will be filtered according to these
 * constraints.
 * 
 * @author lpellegr
 * @author mantoine
 */
public class SparqlAtomicRequest extends
        StatefulQuadruplePatternRequest<List<Quadruple>> {

    private static final long serialVersionUID = 150L;

    private final AtomicQuery atomicQuery;

    public SparqlAtomicRequest(AtomicQuery atomicQuery) {
        super(new AtomicQueryConstraintsValidator(atomicQuery),
                atomicQuery.getQuadruplePattern(),
                new QuadruplePatternResponseProvider());

        this.atomicQuery = atomicQuery;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Quadruple> onPeerValidatingKeyConstraints(CanOverlay<SemanticElement> overlay,
                                                          AnycastRequest<SemanticElement> request,
                                                          fr.inria.eventcloud.api.QuadruplePattern quadruplePattern) {
        TransactionalDatasetGraph txnGraph =
                ((SemanticCanOverlay) overlay).getMiscDatastore().begin(
                        AccessMode.READ_ONLY);

        try {
            QueryIterator iterator =
                    Algebra.exec(
                            this.atomicQuery.getOpRepresentation(),
                            txnGraph.getUnderlyingDataset());

            return toQuadruples(iterator, this.atomicQuery);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            txnGraph.end();
        }
    }

    private static List<Quadruple> toQuadruples(QueryIterator it,
                                                AtomicQuery atomicQuery) {
        Builder<Quadruple> builder = ImmutableList.builder();

        while (it.hasNext()) {
            Binding binding = it.next();

            Node graph = getBoundValue(binding, atomicQuery.getGraph());
            Node subject = getBoundValue(binding, atomicQuery.getSubject());
            Node predicate = getBoundValue(binding, atomicQuery.getPredicate());
            Node object = getBoundValue(binding, atomicQuery.getObject());

            builder.add(new Quadruple(graph, subject, predicate, object));
        }

        return builder.build();
    }

    private static Node getBoundValue(Binding binding, Node node) {
        if (node.isVariable()) {
            return binding.get(Var.alloc(node.getName()));
        } else {
            return node;
        }
    }

    public String getQuery() {
        return this.atomicQuery.toString();
    }

}
