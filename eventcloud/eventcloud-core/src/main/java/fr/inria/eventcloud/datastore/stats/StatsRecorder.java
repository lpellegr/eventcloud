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

import java.io.Serializable;

import org.apfloat.Apfloat;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.overlay.can.SemanticCoordinate;

/**
 * Defines operation available on any StatsRecorder.
 * 
 * @author lpellegr
 */
public interface StatsRecorder extends Serializable {

    void register(Node g, Node s, Node p, Node o);

    void unregister(Node g, Node s, Node p, Node o);

    Apfloat computeGraphEstimation();

    Apfloat computeSubjectEstimation();

    Apfloat computePredicateEstimation();

    Apfloat computeObjectEstimation();

    SemanticCoordinate computeSplitEstimation(byte dimension);

    /**
     * Returns the number of quadruples which have been recorded.
     * 
     * @return the number of quadruples which have been recorded.
     */
    long getNbQuadruples();

    void reset();

    void sync();

}
