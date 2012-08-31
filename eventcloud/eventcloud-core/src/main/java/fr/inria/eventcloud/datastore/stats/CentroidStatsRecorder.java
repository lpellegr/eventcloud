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
import org.apfloat.Apint;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.overlay.can.SemanticElement;

/**
 * Defines some methods to compute the online centroid estimation for each
 * quadruple that is recorded.
 * 
 * @author lpellegr
 */
public class CentroidStatsRecorder extends StatsRecorder {

    private Apfloat gsum = Apfloat.ZERO;

    private Apfloat ssum = Apfloat.ZERO;

    private Apfloat psum = Apfloat.ZERO;

    private Apfloat osum = Apfloat.ZERO;

    private Apint wsum = Apint.ZERO;

    /**
     * {@inheritDoc}
     */
    @Override
    public void recordQuadruple(Node g, Node s, Node p, Node o) {
        Apfloat gf = toRadix10(g);
        Apfloat sf = toRadix10(s);
        Apfloat pf = toRadix10(p);
        Apfloat of = toRadix10(o);

        long gs = g.toString().length();
        long ss = s.toString().length();
        long ps = p.toString().length();
        long os = o.toString().length();

        this.gsum = this.gsum.add(gf.multiply(new Apint(gs)));
        this.ssum = this.ssum.add(sf.multiply(new Apint(ss)));
        this.psum = this.psum.add(pf.multiply(new Apint(ps)));
        this.osum = this.osum.add(of.multiply(new Apint(os)));

        this.wsum = this.wsum.add(new Apint(gs + ss + ps + os));
    }

    public Apfloat graphCentroid() {
        return this.gsum.divide(this.wsum);
    }

    public Apfloat subjectCentroid() {
        return this.ssum.divide(this.wsum);
    }

    public Apfloat predicateCentroid() {
        return this.psum.divide(this.wsum);
    }

    public Apfloat objectCentroid() {
        return this.osum.divide(this.wsum);
    }

    private static Apfloat toRadix10(Node n) {
        return StringElement.toIntegerRadix10(SemanticElement.removePrefix(n.toString()));
    }

}
