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
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;

import com.google.common.collect.Lists;
import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.messages.response.can.QuadruplePatternResponseProvider;

/**
 * Removes all the quadruples that match the given {@code quadruplePattern} and
 * returns the quadruples which have been removed.
 * 
 * @author lpellegr
 */
public class DeleteQuadruplesRequest extends
        StatefulQuadruplePatternRequest<List<Quadruple>> {

    private static final long serialVersionUID = 1L;

    public DeleteQuadruplesRequest(Node g, Node s, Node p, Node o) {
        this(new QuadruplePattern(g, s, p, o));
    }

    public DeleteQuadruplesRequest(QuadruplePattern quadruplePattern) {
        super(quadruplePattern, new QuadruplePatternResponseProvider());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Quadruple> onPeerValidatingKeyConstraints(CanOverlay overlay,
                                                          AnycastRequest request,
                                                          QuadruplePattern quadruplePattern) {
        List<Quadruple> result = null;

        TransactionalTdbDatastore datastore =
                (TransactionalTdbDatastore) overlay.getDatastore();
        TransactionalDatasetGraph txnGraph =
                datastore.begin(AccessMode.READ_ONLY);
        try {
            result = Lists.newArrayList(txnGraph.find(quadruplePattern));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            txnGraph.end();
        }

        txnGraph =
                ((TransactionalTdbDatastore) overlay.getDatastore()).begin(AccessMode.WRITE);

        try {
            txnGraph.delete(quadruplePattern);
            txnGraph.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            txnGraph.end();
        }

        return result;
    }

}
