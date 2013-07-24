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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Iterator;

import org.objectweb.proactive.core.ProActiveRuntimeException;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.logger.JobLogger;
import org.objectweb.proactive.extensions.p2p.structured.messages.AnycastRoutingEntry;
import org.objectweb.proactive.extensions.p2p.structured.messages.ResponseEntry;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.AnycastResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.PeerInternal;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanRequestResponseManager;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.NeighborTable;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This router is used to route the messages of type {@link AnycastRequest}. The
 * request is supposed to reach one or more peers depending of the key
 * associated to the request to route.
 * 
 * @param <T>
 *            the request type to route.
 * @param <E>
 *            the {@link Element}s type manipulated.
 * 
 * @author lpellegr
 */
public class AnycastRequestRouter<T extends AnycastRequest<E>, E extends Element>
        extends Router<AnycastRequest<E>, Coordinate<E>> {

    private static final Logger logger =
            LoggerFactory.getLogger(AnycastRequestRouter.class);

    public AnycastRequestRouter() {
        super();
    }

    /**
     * This method is called just before the next routing step when the request
     * reach a peer which validates all the routing constraints. By default the
     * implementation is empty. This method must be overridden if necessary.
     * 
     * @param overlay
     *            the {@link CanOverlay} of the peer which validates
     *            constraints.
     * 
     * @param request
     *            the message which is handled.
     */
    public void onPeerValidatingKeyConstraints(CanOverlay<E> overlay,
                                               AnycastRequest<E> request) {
        // to be override if necessary
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void makeDecision(StructuredOverlay overlay,
                             AnycastRequest<E> request) {
        CanOverlay<E> canOverlay = ((CanOverlay<E>) overlay);
        CanRequestResponseManager messagingManager =
                canOverlay.getRequestResponseManager();

        // retrieves the hostname for debugging purpose
        String hostname = "";
        if (JobLogger.getBcastDebug()) {
            try {
                hostname = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                logger.error("Cannot log broadcast algorithm : "
                        + "hostname couldn't be retrieved");
                e.printStackTrace();
            }
        }

        // the current overlay has already received the request
        if (!messagingManager.receiveRequest(request.getId())) {
            logger.debug(
                    "Request {} reached peer {} which has already received it",
                    request.getId(), canOverlay.getZone().toString());
            if (JobLogger.getBcastDebug()) {
                Date receiveTime = new Date();
                String timestamp =
                        JobLogger.getDateFormat().format(receiveTime);
                JobLogger.logMessage(request.getId().toString() + "_"
                        + "FloodingBroadcast_" + hostname, "1 " + timestamp
                        + " " + canOverlay.getId() + " "
                        + canOverlay.getNeighborTable().size()
                        + JobLogger.getLineSeparator());

            }

            if (request.getResponseProvider() != null) {
                // sends back an empty response
                canOverlay.getStub().route(
                        request.getResponseProvider().get(request, overlay));
            }
        } else {
            // the current overlay validates the constraints
            if (request.validatesKeyConstraints(canOverlay)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Request " + request.getId() + " is on peer "
                            + overlay + " which validates constraints "
                            + request.getKey());
                }

                if (JobLogger.getBcastDebug()) {
                    Date receiveTime = new Date();
                    String timestamp =
                            JobLogger.getDateFormat().format(receiveTime);
                    JobLogger.logMessage(request.getId().toString() + "_"
                            + "FloodingBroadcast_" + hostname, "0 " + timestamp
                            + " " + canOverlay.getId() + " "
                            + canOverlay.getNeighborTable().size()
                            + JobLogger.getLineSeparator());
                }

                this.onPeerValidatingKeyConstraints(canOverlay, request);

                // sends the message to the other neighbors which validates the
                // constraints
                this.handle(overlay, request);
            } else {
                this.route(overlay, request);
            }
        }
    }

    /**
     * When this method is called we can be sure that the specified
     * {@code overlay} validates the constraints. The next step is to propagate
     * the request to the neighbors which validates the constraints.
     */
    @Override
    protected void handle(final StructuredOverlay overlay,
                          final AnycastRequest<E> request) {
        @SuppressWarnings("unchecked")
        CanOverlay<E> canOverlay = (CanOverlay<E>) overlay;

        // the current peer has no neighbor: this means that the query can
        // only be handled by itself
        if (canOverlay.getNeighborTable().size() == 0) {
            super.onDestinationReached(overlay, request);

            if (request.getResponseProvider() != null) {
                AnycastResponse<E> response =
                        (AnycastResponse<E>) request.getResponseProvider().get(
                                request, overlay);
                response.route(overlay);
            }
        } else {
            NeighborTable<E> neighborsToSendTo =
                    this.getNeighborsToSendTo(canOverlay, request);

            // neighborsToSendTo equals 0 means that we don't have to route the
            // query anymore: we are on a leaf and the response must be
            // returned;
            if (neighborsToSendTo.size() == 0) {
                super.onDestinationReached(overlay, request);

                if (request.getResponseProvider() != null) {
                    AnycastResponse<E> response =
                            (AnycastResponse<E>) request.getResponseProvider()
                                    .get(request, overlay);
                    response.route(overlay);
                }
            }
            // neighborsToSendTo > 0 means we have to perform many send
            // operation and the current peer must await for the number
            // of responses sent.
            else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Sending request " + request.getId() + " to "
                            + neighborsToSendTo.size() + " neighbor(s) from "
                            + overlay);
                }

                // adds entry containing the number of responses awaited
                final ResponseEntry entry =
                        new ResponseEntry(neighborsToSendTo.size());

                if (request.getResponseProvider() != null) {
                    overlay.getRequestResponseManager().putResponseEntry(
                            request, entry);

                    // constructs the routing list used for routing back the
                    // responses
                    request.getAnycastRoutingList().add(
                            new AnycastRoutingEntry<E>(
                                    overlay.getId(), canOverlay.getZone()
                                            .getLowerBound()));
                }

                for (byte dim = 0; dim < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dim++) {
                    for (byte direction = 0; direction < 2; direction++) {
                        Iterator<NeighborEntry<E>> it =
                                neighborsToSendTo.get(dim, direction)
                                        .values()
                                        .iterator();

                        while (it.hasNext()) {
                            Peer p = it.next().getStub();

                            if (logger.isDebugEnabled()) {
                                logger.debug("Sending request "
                                        + request.getId() + " from " + overlay
                                        + " to " + p);
                            }

                            ((PeerInternal) p).forward(request);
                        }
                    }
                }
            }
        }
    }

    private NeighborTable<E> getNeighborsToSendTo(final CanOverlay<E> overlay,
                                                  final AnycastRequest<E> msg) {
        NeighborTable<E> neighborsToSendTo = new NeighborTable<E>();

        for (byte dimension = 0; dimension < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dimension++) {
            for (byte direction = 0; direction < 2; direction++) {
                for (NeighborEntry<E> entry : overlay.getNeighborTable().get(
                        dimension, direction).values()) {
                    AnycastRoutingEntry<E> entryCommingFromSender =
                            msg.getAnycastRoutingList()
                                    .getRoutingResponseEntryBy(entry.getId());
                    /*
                     * If msg contains the neighbor identifier in its routing
                     * list, we know that this neighbor has already received the
                     * query and we can remove it from the neighbors we need to
                     * send the query: it avoids sometimes a remote call.
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
    protected void route(StructuredOverlay overlay, AnycastRequest<E> request) {
        @SuppressWarnings("unchecked")
        CanOverlay<E> overlayCAN = (CanOverlay<E>) overlay;

        byte dimension = 0;
        byte direction = NeighborTable.DIRECTION_ANY;

        // finds the dimension on which the key to reach is not contained
        for (; dimension < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); dimension++) {
            direction =
                    overlayCAN.getZone().contains(
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
                overlayCAN.nearestNeighbor(
                        request.getKey(), dimension, direction);

        if (neighborChosen == null) {
            if (request.getResponseProvider() != null) {
                overlay.getRequestResponseManager().putResponseEntry(
                        request, new ResponseEntry(1));
                request.getResponseProvider().get(request, overlay).route(
                        overlayCAN);
            }

            return;
        }

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
            if (request.getResponseProvider() != null) {
                overlay.getRequestResponseManager().putResponseEntry(
                        request, new ResponseEntry(1));

                request.getAnycastRoutingList().add(
                        new AnycastRoutingEntry<E>(
                                overlay.getId(), overlayCAN.getZone()
                                        .getLowerBound()));
            }

            ((PeerInternal) neighborChosen.getStub()).forward(request);
        } catch (ProActiveRuntimeException e) {
            logger.error("Error while sending the message to the neighbor managing "
                    + neighborChosen.getZone());
            e.printStackTrace();
        }
    }

}
