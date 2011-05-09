package org.objectweb.proactive.extensions.p2p.structured.overlay;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.objectweb.proactive.extensions.p2p.structured.messages.ResponseEntry;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.Request;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The request/response manager is in charge of maintaining the state of the
 * requests which are dispatched over the overlay by using message passing.
 * 
 * @author lpellegr
 */
public abstract class RequestResponseManager implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger logger =
            LoggerFactory.getLogger(RequestResponseManager.class);

    protected StructuredOverlay overlay;

    private Map<UUID, ResponseEntry> repliesReceived;

    protected RequestResponseManager() {
        this.repliesReceived = new ConcurrentHashMap<UUID, ResponseEntry>();
    }
    
    public void init(StructuredOverlay overlay) {
        if (this.overlay == null) {
            this.overlay = overlay;
        }
    }

    /**
     * Dispatches the request over the overlay by using message passing.
     * 
     * @param request
     *            the request to dispatch.
     * 
     * @return a response associated to the type of the request.
     * 
     * @see #waitForFinalResponse(UUID)
     * @see #pullResponse(UUID)
     */
    public Response<?> dispatch(Request<?> request) throws DispatchException {
        if (logger.isDebugEnabled()) {
            logger.debug("Dispatching " + request.getClass().getSimpleName()
                    + " request " + request.getId() + " from "
                    + this.overlay.toString());
        }

        // sends the request over the overlay
        request.route(this.overlay);

        return this.pullResponse(request.getId());
    }

    /**
     * Pulls the response for the specified requestId from the list of responses
     * received. If the response has not been yet received, a synchronization
     * point is created in order to wait the response.
     * 
     * @param requestId
     *            indicates what is the id of the response to retrieve.
     * 
     * @return the response for the specified requestId.
     */
    protected Response<?> pullResponse(UUID requestId) {
        // waits for the final response
        this.waitForFinalResponse(requestId);

        Response<?> response =
                this.repliesReceived.remove(requestId).getResponse();
        // sets the delivery time for latency computation
        response.setDeliveryTime();

        if (logger.isDebugEnabled()) {
            logger.debug("Final response received for request " + requestId
                    + " on " + this.overlay);
        }

        return response;
    }

    /**
     * Creates a synchronization point in order to wait the response associated
     * to the specified requestId.
     * 
     * @param requestId
     *            indicates for which request we are waiting a response.
     */
    protected void waitForFinalResponse(UUID requestId) {
        if (logger.isDebugEnabled()) {
            StringBuffer log = new StringBuffer();
            log.append("Waiting for ");
            log.append(this.repliesReceived.get(requestId)
                    .getExpectedResponsesCount());
            log.append(" responses with id ");
            log.append(requestId);
            log.append(" on ");
            log.append(this.overlay);
            logger.debug(log.toString());
        }

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
    }

    /**
     * Pushes the final response and notify the initial sender that a new
     * response has been received. The notification is done to remove the
     * synchronization point (which has been previously set by
     * {@link #waitForFinalResponse(UUID)}).
     * 
     * @param response
     *            the response to add to the list of responses received.
     */
    public void pushFinalResponse(Response<?> response) {
        ResponseEntry entry =
                this.overlay.getResponseEntries().get(response.getId());

        synchronized (entry) {
            entry.incrementResponsesCount(1);
            entry.setResponse(response);
            entry.notifyAll();
        }
    }

    public Map<UUID, ResponseEntry> getResponsesReceived() {
        return this.repliesReceived;
    }

}
