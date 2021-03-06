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
package fr.inria.eventcloud.datastore;

import java.util.Collection;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.sparql.core.DatasetGraph;
import com.hp.hpl.jena.tdb.transaction.DatasetGraphTxn;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.datastore.stats.StatsRecorder;

/**
 * This class provides an implementation of {@link TransactionalDatasetGraph} by
 * delegating all the calls to an instance of {@link DatasetGraphTxn}. This is
 * done to offer the possibility to external users to work with our API whereas
 * the underlying system works thanks to Jena.
 * 
 * @author lpellegr
 */
public final class TransactionalDatasetGraphImpl implements
        TransactionalDatasetGraph {

    private final Dataset dataset;

    private final DatasetGraph datasetGraph;

    private final StatsRecorder statsRecorder;

    public TransactionalDatasetGraphImpl(Dataset dataset,
            StatsRecorder statsRecorder) {
        this.dataset = dataset;
        this.datasetGraph = dataset.asDatasetGraph();
        this.statsRecorder = statsRecorder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(final Node g, final Node s, final Node p, final Node o) {
        this.statsRecorder.register(g, s, p, o);
        this.datasetGraph.add(g, s, p, o);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(Quadruple quadruple) {
        this.add(
                quadruple.getGraph(), quadruple.getSubject(),
                quadruple.getPredicate(), quadruple.getObject());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(Collection<Quadruple> quadruples) {
        for (Quadruple q : quadruples) {
            this.add(q);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Quadruple quadruple) {
        return this.datasetGraph.contains(
                quadruple.getGraph(), quadruple.getSubject(),
                quadruple.getPredicate(), quadruple.getObject());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(final Quadruple quadruple) {
        this.statsRecorder.unregister(
                quadruple.createMetaGraphNode(), quadruple.getSubject(),
                quadruple.getPredicate(), quadruple.getObject());

        // FIXME hack because we don't enforce quadruple storage with meta graph
        // node, therefore deletion must be made with and without meta graph
        // value
        this.datasetGraph.delete(
                quadruple.createMetaGraphNode(), quadruple.getSubject(),
                quadruple.getPredicate(), quadruple.getObject());
        this.datasetGraph.delete(
                quadruple.getGraph(), quadruple.getSubject(),
                quadruple.getPredicate(), quadruple.getObject());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(Collection<Quadruple> quadruples) {
        for (Quadruple q : quadruples) {
            this.delete(q);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(QuadruplePattern quadruplePattern) {
        this.delete(
                quadruplePattern.getGraph(), quadruplePattern.getSubject(),
                quadruplePattern.getPredicate(), quadruplePattern.getObject());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void delete(Node g, Node s, Node p, Node o) {
        this.datasetGraph.deleteAny(g, s, p, o);

        // TODO: perform unregister on the statsRecorder instance for the
        // quadruples that are removed
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QuadrupleIterator find(QuadruplePattern quadruplePattern) {
        return this.find(
                quadruplePattern.getGraph(), quadruplePattern.getSubject(),
                quadruplePattern.getPredicate(), quadruplePattern.getObject());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QuadrupleIterator find(Node g, Node s, Node p, Node o) {
        return new QuadrupleIterator(this.datasetGraph.findNG(g, s, p, o));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void abort() {
        this.dataset.abort();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void commit() {
        this.dataset.commit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void end() {
        this.dataset.end();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dataset getUnderlyingDataset() {
        return this.dataset;
    }

}
