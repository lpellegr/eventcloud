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
package fr.inria.eventcloud.messages.request.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.Response;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.QuadrupleIterator;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.messages.response.can.CountQuadruplePatternResponse;

/**
 * Retrieves the number of quadruples that match the {@link QuadruplePattern}
 * that is specified when the object is constructed.
 * 
 * @author lpellegr
 */
public class CountQuadruplePatternRequest extends
        StatefulQuadruplePatternRequest<Long> {

    private static final long serialVersionUID = 1L;

    public CountQuadruplePatternRequest(QuadruplePattern quadruplePattern) {
        super(quadruplePattern);
    }

    public CountQuadruplePatternRequest(Node g, Node s, Node p, Node o) {
        super(new QuadruplePattern(g, s, p, o));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response<StringCoordinate> createResponse(StructuredOverlay overlay) {
        return new CountQuadruplePatternResponse(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long onPeerValidatingKeyConstraints(CanOverlay overlay,
                                               AnycastRequest request,
                                               QuadruplePattern quadruplePattern) {
        TransactionalDatasetGraph txnGraph =
                ((TransactionalTdbDatastore) overlay.getDatastore()).begin(AccessMode.READ_ONLY);

        try {
            QuadrupleIterator it = txnGraph.find(quadruplePattern);
            return it.count();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        } finally {
            txnGraph.end();
        }
    }

}
