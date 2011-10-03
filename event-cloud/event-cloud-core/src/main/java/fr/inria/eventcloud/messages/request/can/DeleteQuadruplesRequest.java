/**
 * Copyright (c) 2011 INRIA.
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
package fr.inria.eventcloud.messages.request.can;

import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.datastore.SynchronizedJenaDatasetGraph;

/**
 * Removes all the quadruples that match the given quadpattern.
 * 
 * @author lpellegr
 */
public class DeleteQuadruplesRequest extends StatelessQuadruplePatternRequest {

    private static final long serialVersionUID = 1L;

    public DeleteQuadruplesRequest(Node g, Node s, Node p, Node o) {
        super(new QuadruplePattern(g, s, p, o));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPeerValidatingKeyConstraints(CanOverlay overlay,
                                               QuadruplePattern quadruplePattern) {
        ((SynchronizedJenaDatasetGraph) overlay.getDatastore()).deleteAny(
                quadruplePattern.getGraph(), quadruplePattern.getSubject(),
                quadruplePattern.getPredicate(), quadruplePattern.getObject());
    }

}
