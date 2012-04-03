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

import java.util.Iterator;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.messages.AnycastRoutingEntry;
import org.objectweb.proactive.extensions.p2p.structured.messages.ResponseEntry;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.AnycastResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanRequestResponseManager;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborTable;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This router is used to route the messages of type {@link AnycastRequest}. The
 * request is supposed to reach one or more peers depending of the key
 * associated to the request to route.
 * 
 * @author lpellegr
 * 
 * @param <T>
 *            the request type to route.
 */
public abstract class AnycastRequestRouter<T extends AnycastRequest> extends
        Router<AnycastRequest, StringCoordinate> {

    private static final Logger logger =
            LoggerFactory.getLogger(AnycastRequestRouter.class);

    public AnycastRequestRouter() {
        super();
    }

    /**
     * This method is called just before the next routing step when the request
     * reach a peer which validates the routing constraints.
     * 
     * @param overlay
     *            the {@link CanOverlay} of the peer which validates
     *            constraints.
     * 
     * @param request
     *            the message which is handled.
     */
    public abstract void onPeerValidatingKeyConstraints(CanOverlay overlay,
                                                        AnycastRequest request);

    /**
     * {@inheritDoc}
     */
    @Override
    public void makeDecision(StructuredOverlay overlay, AnycastRequest request) {
        CanOverlay canOverlay = ((CanOverlay) overlay);
        CanRequestResponseManager messagingManager =
                (CanRequestResponseManager) canOverlay.getRequestResponseManager();

        // the current overlay has already received the request
        if (messagingManager.hasReceivedRequest(request.getId())) {
            request.getAnycastRoutingList().removeLast().getPeerStub().route(
                    request.createResponse(overlay));
            if (logger.isDebugEnabled()) {
                logger.debug("Request " + request.getId() + " has reach peer "
                        + canOverlay + " which has already received it");
            }
        } else {
            // the current overlay validates the constraints
            if (request.validatesKeyConstraints(canOverlay)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Request " + request.getId() + " is on peer "
                            + overlay + " which validates constraints "
                            + request.getKey());
                }

                this.onPeerValidatingKeyConstraints(canOverlay, request);

                messagingManager.markRequestAsReceived(request.getId());

                // sends the message to the other neighbors which validates the
                // constraints
                this.doHandle(overlay, request);
            } else {
                this.route(overlay, request);
            }
        }
    }

    /**
     * When this method is called we can be sure that the specified
     * <code>overlay</code> validates the constraints. The next step is to
     * propagate the request to the neighbors which validates the constraints.
     */
    @Override
    protected void doHandle(final StructuredOverlay overlay,
                            final AnycastRequest request) {
        CanOverlay canOverlay = ((CanOverlay) overlay);

        // the current peer has no neighbor: this means that the query can
        // only be handled by itself
        if (canOverlay.getNeighborTable().size() == 0) {
            super.onDestinationReached(overlay, request);
            overlay.getResponseEntries().put(
                    request.getId(), new ResponseEntry(1));
            AnycastResponse response =
                    (AnycastResponse) request.createResponse(overlay);
            response.incrementHopCount(1);
            response.route(overlay);
        } else {
            NeighborTable neighborsToSendTo =
                    this.getNeighborsToSendTo(overlay, request);

            // neighborsToSendTo equals 0 means that we don't have to route the
            // query anymore: we are on a leaf and the response must be
            // returned;
            if (neighborsToSendTo.size() == 0) {
                super.onDestinationReached(overlay, request);
                AnycastResponse response =
                        (AnycastResponse) request.createResponse(overlay);
                response.incrementHopCount(1);
                overlay.getResponseEntries().put(
                        response.getId(), new ResponseEntry(1));
                response.route(overlay);
            }
            // neighborsToSendTo > 0 means we have to perform many send
            // operation and the current peer must await for the number
            // of responses sent.
            else {
                // adds entry containing the number of responses awaited
                final ResponseEntry entry =
                        new ResponseEntry(neighborsToSendTo.size());

                overlay.getResponseEntries().put(request.getId(), entry);

                // constructs the routing list used by responses for return trip
                request.getAnycastRoutingList().add(
                        new AnycastRoutingEntry(
                                overlay.getId(), overlay.getStub()));

                for (byte dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
                    for (byte direction = 0; direction < 2; direction++) {
                        Iterator<NeighborEntry> it =
                                neighborsToSendTo.get(dim, direction)
                                        .values()
                                        .iterator();
                        while (it.hasNext()) {
                            it.next().getStub().route(request);
                        }
                    }
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Request " + request.getId()
                        + " has been sent to " + neighborsToSendTo.size()
                        + " neighbor(s) from " + overlay);
            }
        }

    }

    private NeighborTable getNeighborsToSendTo(final StructuredOverlay overlay,
                                               final AnycastRequest msg) {
        NeighborTable neighborsToSendTo = new NeighborTable();

        for (byte dimension = 0; dimension < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dimension++) {
            for (byte direction = 0; direction < 2; direction++) {
                for (NeighborEntry entry : ((CanOverlay) overlay).getNeighborTable()
                        .get(dimension, direction)
                        .values()) {
                    AnycastRoutingEntry entryCommingFromSender =
                            msg.getAnycastRoutingList()
                                    .getRoutingResponseEntryBy(entry.getId());
                    /*
                     * If message contains the neighbor identifier in its
                     * routing list, we know that this neighbor has
                     * already receive the query and we can remove it from the
                     * neighbors we need to send the query: it avoids sometimes a
                     * remote call.
                     */
                    if (entryCommingFromSender == null
                            && msg.validatesKeyConstraints(entry.getZone())) {
                        neighborsToSendTo.add(entry, dimension, direction);
                    }
                }
            }
        }

        return neighborsToSendTo;
    }

    /**
     * Forward the request to the neighbors until a {@link Peer} which validates
     * the constraints is found.
     * 
     * @param overlay
     *            the overlay used to route the message.
     * 
     * @param request
     *            the message to route.
     */
    @Override
    protected void doRoute(StructuredOverlay overlay, AnycastRequest request) {
        CanOverlay overlayCAN = ((CanOverlay) overlay);

        byte dimension = 0;
        byte direction = NeighborTable.DIRECTION_ANY;

        // finds the dimension on which the key to reach is not contained
        for (; dimension < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dimension++) {
            direction =
                    overlayCAN.getZone().getUnicodeView().containsLexicographically(
                            dimension, request.getKey().getElement(dimension));

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
                        request.getKey(), dimension, direction);

        if (logger.isDebugEnabled()) {
            logger.debug("The message is routed to a neigbour because the current peer "
                    + "managing "
                    + overlay
                    + " does not contains the key to reach ("
                    + request.getKey()
                    + "). Neighbor is selected from dimension "
                    + dimension
                    + " and direction " + direction + ": " + neighborChosen);
        }

        // sends the message to it
        try {
            overlay.getResponseEntries().put(
                    request.getId(), new ResponseEntry(1));
            request.getAnycastRoutingList()
                    .add(
                            new AnycastRoutingEntry(
                                    overlay.getId(), overlay.getStub()));
            neighborChosen.getStub().route(request);
        } catch (ProActiveRuntimeException e) {
            logger.error("Error while sending the message to the neighbor managing "
                    + neighborChosen.getZone());
            e.printStackTrace();
        }
    }

}
