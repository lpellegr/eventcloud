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

import java.util.List;

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.Response;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;

import com.google.common.collect.Lists;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.messages.response.can.SparqlAtomicResponse;
import fr.inria.eventcloud.reasoner.AtomicQuery;

/**
 * Retrieves all the {@link Quadruple}s that match the {@link AtomicQuery} that
 * is specified when the object is constructed. A {@link SparqlAtomicRequest}
 * differs from a {@link QuadruplePatternRequest}. Indeed, with a
 * {@link SparqlAtomicRequest} the request routing can be improved by using some
 * filter constraints and the result will be filtered according to these
 * constraints.
 * 
 * @author lpellegr
 */
public class SparqlAtomicRequest extends
        StatefulQuadruplePatternRequest<List<Quadruple>> {

    private static final long serialVersionUID = 1L;

    public SparqlAtomicRequest(AtomicQuery atomicQuery) {
        // TODO offer the possibility to use a constraints validator that will
        // use the filter constraints contained by the quadruple pattern to
        // route the request
        super(atomicQuery.getQuadruplePattern());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Response<StringCoordinate> createResponse(StructuredOverlay overlay) {
        return new SparqlAtomicResponse(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Quadruple> onPeerValidatingKeyConstraints(CanOverlay overlay,
                                                          AnycastRequest request,
                                                          fr.inria.eventcloud.api.QuadruplePattern quadruplePattern) {
        List<Quadruple> result = null;
        TransactionalDatasetGraph txnGraph =
                ((TransactionalTdbDatastore) overlay.getDatastore()).begin(AccessMode.READ_ONLY);

        try {
            result = Lists.newArrayList(txnGraph.find(quadruplePattern));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            txnGraph.end();
        }

        return result;
    }

}
