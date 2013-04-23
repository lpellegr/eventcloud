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
package fr.inria.eventcloud.messages.request.can;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.messages.response.can.QuadruplePatternResponseProvider;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.overlay.can.SemanticElement;

/**
 * Retrieves all the {@link Quadruple}s that match the {@link QuadruplePattern}
 * that is specified when the object is constructed.
 * 
 * @author lpellegr
 */
public class QuadruplePatternRequest extends
        StatefulQuadruplePatternRequest<List<Quadruple>> {

    private static final long serialVersionUID = 150L;

    public QuadruplePatternRequest(Node g, Node s, Node p, Node o) {
        this(new QuadruplePattern(g, s, p, o));
    }

    public QuadruplePatternRequest(QuadruplePattern quadruplePattern) {
        super(quadruplePattern, new QuadruplePatternResponseProvider());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Quadruple> onPeerValidatingKeyConstraints(CanOverlay<SemanticElement> overlay,
                                                          AnycastRequest<SemanticElement> request,
                                                          QuadruplePattern quadruplePattern) {
        TransactionalDatasetGraph txnGraph =
                ((SemanticCanOverlay) overlay).getMiscDatastore().begin(
                        AccessMode.READ_ONLY);

        try {
            return Lists.newArrayList(txnGraph.find(quadruplePattern));
        } catch (Exception e) {
            // keep trace of the error but return a result to avoid the overall
            // system to crash
            e.printStackTrace();
            return new ArrayList<Quadruple>(0);
        } finally {
            txnGraph.end();
        }
    }

}
