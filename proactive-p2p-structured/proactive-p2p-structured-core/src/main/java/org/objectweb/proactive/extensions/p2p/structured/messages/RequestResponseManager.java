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
package org.objectweb.proactive.extensions.p2p.structured.messages;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The request/response manager is in charge of maintaining the state of the
 * requests which are dispatched over the overlay by using message passing.
 * 
 * @author lpellegr
 */
public abstract class RequestResponseManager implements Serializable {

    private static final long serialVersionUID = 150L;

    private static final Logger log =
            LoggerFactory.getLogger(RequestResponseManager.class);

    private Map<MessageId, ResponseEntry> repliesReceived;

    protected StructuredOverlay overlay;

    protected RequestResponseManager() {
        this.repliesReceived =
                new ConcurrentHashMap<MessageId, ResponseEntry>(
                        16, 0.75f,
                        P2PStructuredProperties.MAO_SOFT_LIMIT_PEERS.getValue());
    }

    /**
     * Dispatches the request over the overlay by using message passing. The
     * request is supposed to create a response which is sent back to the
     * sender.
     * 
     * @param request
     *            the request to dispatch.
     * @param overlay
     *            the overlay from where the request is sent.
     * 
     * @return a response associated to the type of the request.
     */
    public Response<?> dispatch(Request<?> request, StructuredOverlay overlay) {
        this.setMessageId(request);
        this.dispatchv(request, overlay);

        return this.pullResponse(request.getId());
    }

    /**
     * Dispatches the request over the overlay by using message passing. The
     * request is supposed to create a response which is sent back to the
     * sender.
     * 
     * @param request
     *            the request to dispatch.
     * @param overlay
     *            the overlay from where the request is sent.
     */
    public void dispatchv(Request<?> request, StructuredOverlay overlay) {
        this.setMessageId(request);
        request.route(overlay);
    }

    private void setMessageId(Request<?> request) {
        if (request.id == null) {
            request.id = this.overlay.newMessageId();
        }
    }

    /**
     * Pulls the response for the specified {@code requestId} from the list of
     * responses received. If the response has not been yet received, a
     * synchronization point is created in order to wait the response.
     * 
     * @param requestId
     *            indicates what is the id of the response to retrieve.
     * 
     * @return the response for the specified requestId.
     */
    protected Response<?> pullResponse(MessageId requestId) {
        this.overlay.incrementExtraActiveRequestCount(this.getResponsesReceived()
                .get(requestId)
                .getExpectedResponsesCount());

        // waits for the final response
        this.waitForFinalResponse(requestId);

        Response<?> response =
                this.repliesReceived.remove(requestId).getResponse();
        // sets the delivery time for latency computation
        response.setDeliveryTime();

        log.debug("Final response received for request {}", requestId);

        return response;
    }

    /**
     * Creates a synchronization point in order to wait the response associated
     * to the specified requestId.
     * 
     * @param requestId
     *            indicates for which request we are waiting a response.
     */
    private void waitForFinalResponse(MessageId requestId) {
        log.debug(
                "Waiting for {} response(s) with id {}",
                this.repliesReceived.get(requestId).getExpectedResponsesCount(),
                requestId);

        ResponseEntry entry = this.repliesReceived.get(requestId);

        synchronized (entry) {
            while (entry.getStatus() != ResponseEntry.Status.RECEIPT_COMPLETED) {
                try {
                    entry.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        this.overlay.decrementExtraActiveRequestCount(this.repliesReceived.get(
                requestId).getExpectedResponsesCount());
    }

    /**
     * Pushes the final response and notify the initial sender that a new
     * response has been received. The notification is done to remove the
     * synchronization point (which has been previously set by
     * {@link #waitForFinalResponse(MessageId)}).
     * 
     * @param response
     *            the response to add to the list of responses received.
     */
    public void pushFinalResponse(Response<?> response) {
        ResponseEntry entry = this.repliesReceived.get(response.getId());

        synchronized (entry) {
            entry.incrementResponsesCount(1);
            entry.setResponse(response);
            entry.notifyAll();
        }
    }

    public Map<MessageId, ResponseEntry> getResponsesReceived() {
        return this.repliesReceived;
    }

    public void setOverlay(StructuredOverlay overlay) {
        this.overlay = overlay;
    }

    public void close() {

    }

    public void clear() {
        this.repliesReceived.clear();
    }

}
