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

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.Response;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.datastore.SynchronizedJenaDatasetGraph;
import fr.inria.eventcloud.messages.response.can.QuadruplePatternResponse;

/**
 * Retrieves all the {@link Quadruple}s that match the {@link QuadruplePattern}
 * that is specified when the object is constructed.
 * 
 * @author lpellegr
 */
public class QuadruplePatternRequest extends
        StatefulQuadruplePatternRequest<Collection<Quadruple>> {

    private static final long serialVersionUID = 1L;

    public QuadruplePatternRequest(QuadruplePattern quadruplePattern) {
        super(quadruplePattern);
    }

    public QuadruplePatternRequest(Node g, Node s, Node p, Node o) {
        super(new QuadruplePattern(g, s, p, o));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response<StringCoordinate> createResponse(StructuredOverlay overlay) {
        return new QuadruplePatternResponse(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Quadruple> onPeerValidatingKeyConstraints(CanOverlay overlay,
                                                                AnycastRequest request,
                                                                QuadruplePattern quadruplePattern) {
        return ((SynchronizedJenaDatasetGraph) overlay.getDatastore()).find(quadruplePattern);
    }

}