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
import org.objectweb.proactive.extensions.p2p.structured.messages.response.Response;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.AnycastResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Router used to route {@link AnycastResponse}s. The path followed by the
 * response is the reverse path of the initial path followed by the request.
 * 
 * @author lpellegr
 * 
 * @param <T>
 *            the response type to route.
 */
public class AnycastResponseRouter<T extends AnycastResponse> extends
        Router<AnycastResponse, StringCoordinate> {

    private static final Logger logger =
            LoggerFactory.getLogger(AnycastResponseRouter.class);

    /**
     * Constructs a new AnycastResponseRouter without any constraints validator.
     */
    public AnycastResponseRouter() {
        super();
    }

    @Override
    protected void doHandle(StructuredOverlay overlay, AnycastResponse response) {
        // the number of outbound hop count is equal to the number
        // of inbound hop count because the message follows the same
        // path in the both cases.
        response.setOutboundHopCount(response.getInboundHopCount());

        ResponseEntry entry =
                overlay.getResponseEntries().get(response.getId());
        synchronized (entry) {
            entry.notifyAll();
        }
    }

    @Override
    public void makeDecision(StructuredOverlay overlay, AnycastResponse response) {
        // TODO: Check if it is correct for the merge operation
        ResponseEntry entry = overlay.getResponseEntry(response.getId());

        Response<?> tmpResponse = entry.getResponse();

        if (tmpResponse == null) {
            entry.setResponse(response);
        } else {
            synchronized (entry) {
                ((AnycastResponse) tmpResponse).addSubResult(response);
                tmpResponse.incrementHopCount(response.getOutboundHopCount());
            }
        }
        entry.incrementResponsesCount(1);

        AnycastResponse currentResponse = (AnycastResponse) entry.getResponse();

        // we are on a synchronization point and all responses are received,
        // we must ensure that the query datastore operation is terminated
        // before to send back the response.
        if (entry.getStatus() == ResponseEntry.Status.RECEIPT_COMPLETED) {
            // we are on the initiator of the query we need to wake up its
            // thread in order to remove the synchronization point
            if (currentResponse.getAnycastRoutingList().size() == 0) {
                this.handle(overlay, currentResponse);
            } else {
                // the synchronization point is on a peer in the sub-tree.
                // we call the route method in order to know where to sent back
                // the response.
                this.doRoute(overlay, currentResponse);

                // the response has been handled and sent back so we can remove
                // it from the table.
                overlay.getRequestResponseManager()
                        .getResponsesReceived()
                        .remove(currentResponse.getId());
                if (logger.isDebugEnabled()) {
                    logger.debug("All subreplies received on " + overlay
                            + " for request " + response.getId());
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doRoute(StructuredOverlay overlay, AnycastResponse response) {
        AnycastRoutingEntry entry =
                response.getAnycastRoutingList().removeLast();
        response.incrementHopCount(1);
        entry.getPeerStub().route(response);

        if (logger.isDebugEnabled()) {
            logger.debug("On peer " + overlay + ", route response on peer "
                    + entry.getPeerStub() + " validating constraints");
        }
    }

}
