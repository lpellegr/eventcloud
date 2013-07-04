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
package fr.inria.eventcloud.datastore.stats;

import org.apfloat.Apfloat;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.overlay.can.SemanticElement;

/**
 * A Null StatRecorder. It does nothing but it exists to avoid multiple if test
 * in several classes which is ugly.
 * 
 * @author lpellegr
 */
public class NullStatsRecorder implements StatsRecorder {

    private static final long serialVersionUID = 150L;

    private static class Singleton {

        public static NullStatsRecorder instance = new NullStatsRecorder();

    }

    public static NullStatsRecorder getInstance() {
        return Singleton.instance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void register(Node g, Node s, Node p, Node o) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregister(Node g, Node s, Node p, Node o) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Apfloat computeGraphEstimation() {
        return Apfloat.ZERO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Apfloat computeSubjectEstimation() {
        return Apfloat.ZERO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Apfloat computePredicateEstimation() {
        return Apfloat.ZERO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Apfloat computeObjectEstimation() {
        return Apfloat.ZERO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SemanticElement computeSplitEstimation(byte dimension) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getNbQuadruples() {
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reset() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sync() {
    }

}
