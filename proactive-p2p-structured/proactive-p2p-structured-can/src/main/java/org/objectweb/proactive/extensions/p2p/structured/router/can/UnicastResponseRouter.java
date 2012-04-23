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
package org.objectweb.proactive.extensions.p2p.structured.router.can;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.messages.ResponseEntry;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.Response;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborTable;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Router used to route a {@link Response} from a peer to an another.
 * 
 * @author lpellegr
 * 
 * @param <T>
 *            the response type to route.
 */
public class UnicastResponseRouter<T extends Response<StringCoordinate>>
        extends Router<T, StringCoordinate> {

    private static final Logger logger =
            LoggerFactory.getLogger(UnicastResponseRouter.class);

    public UnicastResponseRouter() {
        super();
    }

    @Override
    public void makeDecision(StructuredOverlay overlay, T response) {
        if (response.getHopCount() == 0) {
            overlay.getResponseEntries().put(
                    response.getId(), new ResponseEntry(1));
        }

        if (response.validatesKeyConstraints(overlay)) {
            this.handle(overlay, response);
        } else {
            this.route(overlay, response);
        }
    }

    @Override
    protected void handle(StructuredOverlay overlay, T response) {
        logger.debug(
                "The peer {} contains the key to reach {}", overlay,
                response.getKey());

        overlay.getRequestResponseManager().pushFinalResponse(response);
    }

    @Override
    protected void route(StructuredOverlay overlay, T response) {
        CanOverlay overlayCAN = ((CanOverlay) overlay);

        byte dimension = 0;
        byte direction = NeighborTable.DIRECTION_ANY;

        // finds the dimension on which the key to reach is not contained
        for (; dimension < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dimension++) {
            direction =
                    overlayCAN.getZone()
                            .getUnicodeView()
                            .containsLexicographically(
                                    dimension,
                                    response.getKey().getElement(dimension));

            if (direction == -1) {
                direction = NeighborTable.DIRECTION_INFERIOR;
                break;
            } else if (direction == 1) {
                direction = NeighborTable.DIRECTION_SUPERIOR;
                break;
            }
        }

        // selects one neighbor in the dimension and the direction previously
        // affected
        NeighborEntry neighborChosen =
                overlayCAN.nearestNeighbor(
                        response.getKey(), dimension, direction);

        if (logger.isDebugEnabled()) {
            logger.debug("The message is routed to a neigbour because the current peer "
                    + "managing "
                    + overlay
                    + " does not contains the key to reach "
                    + response.getKey()
                    + ". Neighbor is selected from dimension "
                    + dimension
                    + " and direction " + direction + ": " + neighborChosen);
        }

        // sends the message to it
        try {
            response.incrementHopCount(1);
            neighborChosen.getStub().route(response);
        } catch (ProActiveRuntimeException e) {
            logger.error(
                    "Error while sending the message to the neighbor managing {}",
                    neighborChosen.getZone());
            e.printStackTrace();
        }
    }

}
