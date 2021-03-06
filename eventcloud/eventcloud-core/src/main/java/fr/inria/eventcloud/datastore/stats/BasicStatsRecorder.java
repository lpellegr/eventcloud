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
package fr.inria.eventcloud.datastore.stats;

import java.util.concurrent.atomic.AtomicLong;

import org.apfloat.Apfloat;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.overlay.can.SemanticCoordinate;

/**
 * Basic stats recorder whose the purpose is mainly to maintain the number of
 * quadruples managed. Other operations used to compute measurements about RDF
 * terms are returning {@code null}.
 * 
 * @author lpellegr
 */
public class BasicStatsRecorder extends AbstractStatsRecorder {

    private static final long serialVersionUID = 160L;

    private AtomicLong nbQuadruples;

    public BasicStatsRecorder() {
        super(0);
        this.nbQuadruples = new AtomicLong();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SemanticCoordinate computeSplitEstimation(byte dimension) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Apfloat computeGraphEstimation() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Apfloat computeSubjectEstimation() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Apfloat computePredicateEstimation() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Apfloat computeObjectEstimation() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getNbQuadruples() {
        return this.nbQuadruples.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void _register(Node g, Node s, Node p, Node o) {
        this.nbQuadruples.incrementAndGet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void _unregister(Node g, Node s, Node p, Node o) {
        this.nbQuadruples.decrementAndGet();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
        super.reset();
        this.nbQuadruples.set(0);
    }

}
