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
package fr.inria.eventcloud.datastore.stats;

import org.apfloat.Apfloat;

import com.hp.hpl.jena.graph.Node;

/**
 * Defines some methods to compute the online mean estimation for each quadruple
 * that is recorded.
 * 
 * @author lpellegr
 */
public class MeanStatsRecorder extends StatsRecorder {

    private static final long serialVersionUID = 1L;

    private Apfloat gsum = Apfloat.ZERO;

    private Apfloat ssum = Apfloat.ZERO;

    private Apfloat psum = Apfloat.ZERO;

    private Apfloat osum = Apfloat.ZERO;

    /**
     * {@inheritDoc}
     */
    @Override
    public void quadrupleAddedComputeStats(Node g, Node s, Node p, Node o) {
        Apfloat gf = toRadix10(g);
        Apfloat sf = toRadix10(s);
        Apfloat pf = toRadix10(p);
        Apfloat of = toRadix10(o);

        this.gsum = this.gsum.add(gf);
        this.ssum = this.ssum.add(sf);
        this.psum = this.psum.add(pf);
        this.osum = this.osum.add(of);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void quadrupleRemovedComputeStats(Node g, Node s, Node p, Node o) {
        Apfloat gf = toRadix10(g);
        Apfloat sf = toRadix10(s);
        Apfloat pf = toRadix10(p);
        Apfloat of = toRadix10(o);

        this.gsum = this.gsum.subtract(gf);
        this.ssum = this.ssum.subtract(sf);
        this.psum = this.psum.subtract(pf);
        this.osum = this.osum.subtract(of);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Apfloat computeGraphEstimation() {
        return this.gsum.divide(new Apfloat(super.getNbQuads()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Apfloat computeSubjectEstimation() {
        return this.ssum.divide(new Apfloat(super.getNbQuads()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Apfloat computePredicateEstimation() {
        return this.psum.divide(new Apfloat(super.getNbQuads()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Apfloat computeObjectEstimation() {
        return this.osum.divide(new Apfloat(super.getNbQuads()));
    }

}
