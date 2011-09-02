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

import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.datastore.SynchronizedJenaDatasetGraph;

/**
 * A DeleteQuadrupleRequest is used to remove a quadruple from the network. The
 * request will be routed on the network until to reach the peer which manage
 * the specified quadruple components. Then, the quadruple is removed from the
 * datastore associated to the peer.
 * 
 * @author lpellegr
 */
public class DeleteQuadrupleRequest extends QuadrupleRequest {

    private static final long serialVersionUID = 1L;

    private final static Logger logger =
            LoggerFactory.getLogger(DeleteQuadrupleRequest.class);

    public DeleteQuadrupleRequest(Quadruple quad) {
        super(quad);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestinationReached(StructuredOverlay overlay, Quadruple quad) {
        ((SynchronizedJenaDatasetGraph) overlay.getDatastore()).delete(quad);
        logger.info("Quadruple {} has been removed from {}", quad, overlay);
    }

}
