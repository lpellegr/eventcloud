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
import fr.inria.eventcloud.datastore.PersistentJenaTdbDatastore;

/**
 * An AddQuadrupleRequest is used to insert a quadruple into the network. The
 * request will be routed on the network until to reach the peer which manage
 * the quad components. Then, the quad is stored on that peer.
 * 
 * @author lpellegr
 */
public class AddQuadrupleRequest extends QuadrupleRequest {

    private static final long serialVersionUID = 1L;

    private final static Logger logger =
            LoggerFactory.getLogger(AddQuadrupleRequest.class);

    public AddQuadrupleRequest(final Quadruple quad) {
        super(quad);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestinationReached(StructuredOverlay overlay, Quadruple quad) {
        ((PersistentJenaTdbDatastore) overlay.getDatastore()).add(quad);
        logger.info("Quadruple {} has been added on {}", quad, overlay);
    }

}
