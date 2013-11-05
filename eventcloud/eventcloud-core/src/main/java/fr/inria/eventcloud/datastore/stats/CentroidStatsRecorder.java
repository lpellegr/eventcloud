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
import org.apfloat.Apint;
import org.objectweb.proactive.extensions.p2p.structured.utils.ApfloatUtils;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.overlay.can.SemanticCoordinate;

/**
 * Defines some methods to compute the online centroid estimation for each
 * quadruple that is recorded.
 * 
 * @author lpellegr
 */
public class CentroidStatsRecorder extends AbstractStatsRecorder {

    private static final long serialVersionUID = 160L;

    private Apfloat gsum = Apfloat.ZERO;

    private Apfloat ssum = Apfloat.ZERO;

    private Apfloat psum = Apfloat.ZERO;

    private Apfloat osum = Apfloat.ZERO;

    private Apint gwsum = Apint.ZERO;

    private Apint swsum = Apint.ZERO;

    private Apint pwsum = Apint.ZERO;

    private Apint owsum = Apint.ZERO;

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void _register(Node g, Node s, Node p, Node o) {
        String gs = SemanticCoordinate.removePrefix(g);
        String ss = SemanticCoordinate.removePrefix(s);
        String ps = SemanticCoordinate.removePrefix(p);
        String os = SemanticCoordinate.removePrefix(o);

        Apfloat gf = ApfloatUtils.toFloatRadix10(gs);
        Apfloat sf = ApfloatUtils.toFloatRadix10(ss);
        Apfloat pf = ApfloatUtils.toFloatRadix10(ps);
        Apfloat of = ApfloatUtils.toFloatRadix10(os);

        Apint gw = new Apint(gs.length());
        Apint sw = new Apint(ss.length());
        Apint pw = new Apint(ps.length());
        Apint ow = new Apint(os.length());

        this.gsum = this.gsum.add(gf.multiply(gw));
        this.ssum = this.ssum.add(sf.multiply(sw));
        this.psum = this.psum.add(pf.multiply(pw));
        this.osum = this.osum.add(of.multiply(ow));

        this.gwsum = this.gwsum.add(gw);
        this.swsum = this.swsum.add(sw);
        this.pwsum = this.pwsum.add(pw);
        this.owsum = this.owsum.add(ow);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized void _unregister(Node g, Node s, Node p, Node o) {
        String gs = SemanticCoordinate.removePrefix(g);
        String ss = SemanticCoordinate.removePrefix(s);
        String ps = SemanticCoordinate.removePrefix(p);
        String os = SemanticCoordinate.removePrefix(o);

        Apfloat gf = ApfloatUtils.toFloatRadix10(gs);
        Apfloat sf = ApfloatUtils.toFloatRadix10(ss);
        Apfloat pf = ApfloatUtils.toFloatRadix10(ps);
        Apfloat of = ApfloatUtils.toFloatRadix10(os);

        Apint gw = new Apint(gs.length());
        Apint sw = new Apint(ss.length());
        Apint pw = new Apint(ps.length());
        Apint ow = new Apint(os.length());

        this.gsum = this.gsum.subtract(gf.multiply(gw));
        this.ssum = this.ssum.subtract(sf.multiply(sw));
        this.psum = this.psum.subtract(pf.multiply(pw));
        this.osum = this.osum.subtract(of.multiply(ow));

        this.gwsum = this.gwsum.subtract(gw);
        this.swsum = this.swsum.subtract(sw);
        this.pwsum = this.pwsum.subtract(pw);
        this.owsum = this.owsum.subtract(ow);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Apfloat computeGraphEstimation() {
        return this.gsum.divide(this.gwsum);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Apfloat computeSubjectEstimation() {
        return this.ssum.divide(this.swsum);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Apfloat computePredicateEstimation() {
        return this.psum.divide(this.pwsum);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Apfloat computeObjectEstimation() {
        return this.osum.divide(this.owsum);
    }

}
