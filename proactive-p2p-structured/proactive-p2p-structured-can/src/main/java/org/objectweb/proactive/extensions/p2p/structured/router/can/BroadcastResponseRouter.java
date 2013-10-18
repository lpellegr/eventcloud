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
import org.objectweb.proactive.extensions.p2p.structured.messages.ResponseEntry;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.MulticastResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.PeerInternal;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborTable;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.UnicastConstraintsValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Router used to route {@link MulticastResponse}s. The path followed by the
 * response is the reverse path of the initial path followed by the request.
 * 
 * @param <T>
 *            the response type to route.
 * @param <E>
 *            the {@link Element}s type manipulated.
 * 
 * @author lpellegr
 */
public class BroadcastResponseRouter<T extends MulticastResponse<E>, E extends Element>
        extends Router<MulticastResponse<E>, Coordinate<E>> {

    private static final Logger log =
            LoggerFactory.getLogger(BroadcastResponseRouter.class);

    /**
     * Constructs a new AnycastResponseRouter without any constraints validator.
     */
    public BroadcastResponseRouter() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void makeDecision(StructuredOverlay overlay,
                             MulticastResponse<E> response) {
        if (response.isEmpty()) {
            response.setConstraintsValidator(new UnicastConstraintsValidator<E>(
                    response.getReversePathStack()
                            .removeLast()
                            .getPeerCoordinate()));
            response.setIsEmpty(false);

            this.route(overlay, response);
        } else {
            if (response.validatesKeyConstraints(overlay)) {
                ResponseEntry entry =
                        overlay.getRequestResponseManager().getResponseEntry(
                                response.getId());

                if (entry == null) {
                    throw new IllegalStateException("No entry found on "
                            + overlay.getId() + " for message id "
                            + response.getId());
                }

                // ensure that only one thread at a time can access the response
                // entry when we receive two responses related to a same initial
                // request
                synchronized (entry) {
                    @SuppressWarnings("unchecked")
                    MulticastResponse<E> localResponse =
                            (MulticastResponse<E>) entry.getResponse();
                    localResponse =
                            MulticastResponse.merge(localResponse, response);
                    entry.setResponse(localResponse);
                    entry.incrementResponsesCount(1);

                    // we are on a synchronization point and all responses are
                    // received, we must ensure that operations performed in
                    // background are terminated before to send back the
                    // response.
                    if (entry.getStatus() == ResponseEntry.Status.RECEIPT_COMPLETED) {
                        localResponse.beforeSendingBackResponse(overlay);

                        // we are on the initiator of the query we need to wake
                        // up its thread in order to remove the synchronization
                        // point
                        if (localResponse.getReversePathStack().size() == 0) {
                            this.handle(overlay, localResponse);
                        } else {
                            log.debug(
                                    "All subreplies received on {} for request {}",
                                    overlay, response.getId());

                            // the response has been handled and sent back so we
                            // can remove it from the table.
                            localResponse.setConstraintsValidator(new UnicastConstraintsValidator<E>(
                                    localResponse.getReversePathStack()
                                            .removeLast()
                                            .getPeerCoordinate()));

                            // the response has been handled and sent back so we
                            // can remove it from the table.
                            overlay.getRequestResponseManager()
                                    .removeResponseEntry(localResponse.getId());

                            // the synchronization point is on a peer in the
                            // sub-tree. we call the route method in order to
                            // know where to sent back the response.
                            this.route(overlay, localResponse);
                        }
                    }
                }
            } else {
                this.route(overlay, response);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handle(StructuredOverlay overlay,
                          MulticastResponse<E> response) {
        // the number of outbound hop count is equal to the number
        // of inbound hop count because the message follows the same
        // path in the forward and backward direction.
        response.setOutboundHopCount(response.getInboundHopCount());

        overlay.getRequestResponseManager().notifyRequester(
                response.getId(), response.getAggregationId());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void route(StructuredOverlay overlay,
                         MulticastResponse<E> response) {
        @SuppressWarnings("unchecked")
        CanOverlay<E> canOverlay = ((CanOverlay<E>) overlay);

        byte dimension = 0;
        byte direction = NeighborTable.DIRECTION_ANY;

        // finds the dimension on which the key to reach is not contained
        for (; dimension < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dimension++) {
            direction =
                    canOverlay.getZone().contains(
                            dimension, response.getKey().getElement(dimension));

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
                        response.getKey(), dimension, direction);

        if (neighborChosen == null) {
            log.warn(
                    "Trying to route a {} response but the key {} used "
                            + "is managed by no peer. You are probably using a key with "
                            + "values that are not between the minimum and the upper "
                            + "bound managed by the network.", this.getClass()
                            .getSimpleName(), response.getKey());
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Request routed to a neighbor because the current peer "
                    + "managing " + overlay
                    + " does not contains the key to reach ("
                    + response.getKey()
                    + "). Neighbor is selected from dimension " + dimension
                    + " and direction " + direction + ": " + neighborChosen);
        }

        try {
            ((PeerInternal) neighborChosen.getStub()).forward(response);
        } catch (ProActiveRuntimeException e) {
            log.error("Error while forwarding a message to the neighbor managing "
                    + neighborChosen.getZone());
            e.printStackTrace();
        }
    }

}
