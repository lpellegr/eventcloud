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
package org.objectweb.proactive.extensions.p2p.structured.router.can;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.messages.Request;
import org.objectweb.proactive.extensions.p2p.structured.messages.RequestResponseMessage;
import org.objectweb.proactive.extensions.p2p.structured.messages.Response;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.PeerInternal;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborTable;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Router used to route {@link RequestResponseMessage}s from a {@link Peer} to
 * an another {@link Peer}.
 * 
 * @param <T>
 *            the response type to route.
 * @param <E>
 *            the {@link Element}s type manipulated.
 * 
 * @author lpellegr
 */
public class UnicastRequestRouter<T extends Request<Coordinate<E>>, E extends Element>
        extends Router<T, Coordinate<E>> {

    private static final Logger log =
            LoggerFactory.getLogger(UnicastRequestRouter.class);

    public UnicastRequestRouter() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void makeDecision(StructuredOverlay overlay, T request) {
        if (request.validatesKeyConstraints(overlay)) {
            this.handle(overlay, request);
        } else {
            this.route(overlay, request);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handle(StructuredOverlay overlay, T request) {
        this.onDestinationReached(overlay, request);

        if (request.getResponseProvider() != null) {
            Response<Coordinate<E>> response =
                    request.getResponseProvider().get(request, overlay);

            response.route(overlay);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void route(StructuredOverlay overlay, T request) {
        CanOverlay<E> canOverlay = ((CanOverlay<E>) overlay);

        byte dimension = 0;
        byte direction = NeighborTable.DIRECTION_ANY;

        // finds the dimension on which the key to reach is not contained
        for (; dimension < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dimension++) {
            direction =
                    canOverlay.getZone().contains(
                            dimension, request.getKey().getElement(dimension));

            if (direction == -1) {
                direction = NeighborTable.DIRECTION_INFERIOR;
                break;
            } else if (direction == 1) {
                direction = NeighborTable.DIRECTION_SUPERIOR;
                break;
            }
        }

        if (dimension == P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue()) {
            dimension =
                    (byte) (P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue() - 1);
        }

        // selects one neighbor in the dimension and the direction previously
        // affected
        NeighborEntry<E> neighborChosen =
                canOverlay.nearestNeighbor(
                        request.getKey(), dimension, direction);

        if (neighborChosen == null) {
            log.error(
                    "Trying to route a {} request but the key {} used "
                            + "is managed by no peer. You are probably using a key with "
                            + "values that are not between the minimum and the upper "
                            + "bound managed by the network.", this.getClass()
                            .getSimpleName(), request.getKey());
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Request routed to a neigbour because the current peer "
                    + "managing " + overlay
                    + " does not contains the key to reach ("
                    + request.getKey()
                    + "). Neighbor is selected from dimension " + dimension
                    + " and direction " + direction + ": " + neighborChosen);
        }

        try {
            ((PeerInternal) neighborChosen.getStub()).forward(request);
        } catch (ProActiveRuntimeException e) {
            log.error("Error while forwarding a request to the neighbor managing "
                    + neighborChosen.getZone());
            e.printStackTrace();
        }
    }

}
