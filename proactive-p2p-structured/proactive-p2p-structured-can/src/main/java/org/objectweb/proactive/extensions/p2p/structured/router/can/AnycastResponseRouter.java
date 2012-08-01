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

import org.objectweb.proactive.extensions.p2p.structured.messages.AnycastRoutingEntry;
import org.objectweb.proactive.extensions.p2p.structured.messages.ResponseEntry;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.AnycastResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Router used to route {@link AnycastResponse}s. The path followed by the
 * response is the reverse path of the initial path followed by the request.
 * 
 * @param <T>
 *            the response type to route.
 * @param <E>
 *            the {@link Element}s type manipulated.
 * 
 * @author lpellegr
 */
public class AnycastResponseRouter<T extends AnycastResponse<E>, E extends Element>
        extends Router<AnycastResponse<E>, Coordinate<E>> {

    private static final Logger logger =
            LoggerFactory.getLogger(AnycastResponseRouter.class);

    /**
     * Constructs a new AnycastResponseRouter without any constraints validator.
     */
    public AnycastResponseRouter() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void makeDecision(StructuredOverlay overlay,
                             AnycastResponse<E> response) {
        ResponseEntry entry = overlay.getResponseEntry(response.getId());

        @SuppressWarnings("unchecked")
        AnycastResponse<E> entryResponse =
                (AnycastResponse<E>) entry.getResponse();
        entryResponse = AnycastResponse.merge(entryResponse, response);
        entry.setResponse(entryResponse);
        entry.incrementResponsesCount(1);

        // we are on a synchronization point and all responses are received,
        // we must ensure that the query datastore operation is terminated
        // before to send back the response.
        if (entry.getStatus() == ResponseEntry.Status.RECEIPT_COMPLETED) {
            entryResponse.synchronizationPointUnlocked(overlay);

            // we are on the initiator of the query we need to wake up its
            // thread in order to remove the synchronization point
            if (entryResponse.getAnycastRoutingList().size() == 0) {
                this.handle(overlay, entryResponse);
            } else {
                // the synchronization point is on a peer in the sub-tree.
                // we call the route method in order to know where to sent back
                // the response.
                this.route(overlay, entryResponse);

                // the response has been handled and sent back so we can remove
                // it from the table.
                overlay.getRequestResponseManager()
                        .getResponsesReceived()
                        .remove(entryResponse.getId());

                logger.debug(
                        "All subreplies received on {} for request {}",
                        overlay, response.getId());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void handle(StructuredOverlay overlay, AnycastResponse<E> response) {
        // the number of outbound hop count is equal to the number
        // of inbound hop count because the message follows the same
        // path in the forward and backward direction.
        response.setOutboundHopCount(response.getInboundHopCount());

        ResponseEntry entry =
                overlay.getResponseEntries().get(response.getId());
        synchronized (entry) {
            entry.notifyAll();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void route(StructuredOverlay overlay, AnycastResponse<E> response) {
        AnycastRoutingEntry entry =
                response.getAnycastRoutingList().removeLast();
        response.incrementHopCount(1);
        entry.getPeerStub().route(response);

        if (logger.isDebugEnabled()) {
            logger.debug("Routing response " + response.getId() + " from "
                    + overlay + " to " + entry.getPeerStub());
        }
    }

}
