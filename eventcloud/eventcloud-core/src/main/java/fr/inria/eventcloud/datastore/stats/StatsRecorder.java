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

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.utils.LongAdder;

/**
 * Defines methods to record statistics about {@link Quadruple}s which are
 * inserted into a {@link TransactionalTdbDatastore}.
 * 
 * @author lpellegr
 */
public abstract class StatsRecorder {

    private LongAdder nbQuads;

    public StatsRecorder() {
        this.nbQuads = new LongAdder();
    }

    private void recordHits() {
        this.nbQuads.increment();
    }

    public void recordStats(Node g, Node s, Node p, Node o) {
        this.recordHits();

        synchronized (this) {
            this.recordQuadruple(g, s, p, o);
        }
    }

    protected abstract void recordQuadruple(Node g, Node s, Node p, Node o);

    /**
     * Returns the number of quadruples which have been recorded.
     * 
     * @return the number of quadruples which have been recorded.
     */
    public long getNbQuads() {
        return this.nbQuads.sum();
    }

}
