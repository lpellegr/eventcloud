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
package fr.inria.eventcloud.messages.request;

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.MulticastRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.points.Point;
import org.objectweb.proactive.extensions.p2p.structured.providers.ResponseProvider;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.QuadrupleIterator;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.messages.response.CountQuadruplePatternResponse;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.overlay.can.SemanticCoordinate;

/**
 * Retrieves the number of quadruples that match the {@link QuadruplePattern}
 * which is specified when the object is constructed.
 * 
 * @author lpellegr
 */
public class CountQuadruplePatternRequest extends
        StatefulQuadruplePatternRequest<Long> {

    private static final long serialVersionUID = 160L;

    public CountQuadruplePatternRequest(Node g, Node s, Node p, Node o) {
        this(new QuadruplePattern(g, s, p, o));
    }

    public CountQuadruplePatternRequest(QuadruplePattern quadruplePattern) {
        super(
                quadruplePattern,
                new ResponseProvider<CountQuadruplePatternResponse, Point<SemanticCoordinate>>() {
                    private static final long serialVersionUID = 160L;

                    @Override
                    public CountQuadruplePatternResponse get() {
                        return new CountQuadruplePatternResponse();
                    }
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long onPeerValidatingKeyConstraints(CanOverlay<SemanticCoordinate> overlay,
                                               MulticastRequest<SemanticCoordinate> request,
                                               QuadruplePattern quadruplePattern) {
        TransactionalDatasetGraph txnGraph =
                ((SemanticCanOverlay) overlay).getMiscDatastore().begin(
                        AccessMode.READ_ONLY);

        try {
            QuadrupleIterator it = txnGraph.find(quadruplePattern);
            return it.count();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        } finally {
            txnGraph.end();
        }
    }

}
