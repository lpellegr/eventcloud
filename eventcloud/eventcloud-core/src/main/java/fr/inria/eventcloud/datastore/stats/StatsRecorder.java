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

import java.io.Serializable;

import org.apfloat.Apfloat;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.StringElement;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.overlay.can.SemanticElement;

/**
 * Defines methods to record statistics about {@link Quadruple}s which are
 * inserted into a {@link TransactionalTdbDatastore}.
 * 
 * @author lpellegr
 */
public abstract class StatsRecorder implements Serializable {

    private static final long serialVersionUID = 1L;

    private long nbQuads = 0;

    public synchronized void quadrupleAdded(Node g, Node s, Node p, Node o) {
        this.quadrupleAddedComputeStats(g, s, p, o);
        this.nbQuads++;
    }

    protected abstract void quadrupleAddedComputeStats(Node g, Node s, Node p,
                                                       Node o);

    public synchronized void quadrupleRemoved(Node g, Node s, Node p, Node o) {
        this.quadrupleRemovedComputeStats(g, s, p, o);
        this.nbQuads--;
    }

    protected abstract void quadrupleRemovedComputeStats(Node g, Node s,
                                                         Node p, Node o);

    public abstract Apfloat computeGraphEstimation();

    public abstract Apfloat computeSubjectEstimation();

    public abstract Apfloat computePredicateEstimation();

    public abstract Apfloat computeObjectEstimation();

    public SemanticElement computeSplitEstimation(byte dimension) {
        Apfloat estimatedSplitValue = null;

        switch (dimension) {
            case 0:
                estimatedSplitValue = this.computeGraphEstimation();
                break;
            case 1:
                estimatedSplitValue = this.computeSubjectEstimation();
                break;
            case 2:
                estimatedSplitValue = this.computePredicateEstimation();
                break;
            case 3:
                estimatedSplitValue = this.computeObjectEstimation();
                break;
            default:
                throw new IllegalArgumentException(
                        "Invalid dimension specified: " + dimension);
        }

        return new SemanticElement(estimatedSplitValue);
    }

    /**
     * Returns the number of quadruples which have been recorded.
     * 
     * @return the number of quadruples which have been recorded.
     */
    public long getNbQuads() {
        return this.nbQuads;
    }

    protected static Apfloat toRadix10(Node n) {
        return StringElement.toFloatRadix10(SemanticElement.removePrefix(n));
    }

}
