package org.objectweb.proactive.extensions.p2p.structured.overlay;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.objectweb.proactive.extensions.p2p.structured.messages.ReplyEntry;
import org.objectweb.proactive.extensions.p2p.structured.messages.RequestReplyMessage;
import org.objectweb.proactive.extensions.p2p.structured.messages.reply.Reply;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The request/reply manager is in charge of maintaining the state of the
 * requests which are dispatched over the overlay by using message passing.
 * 
 * @author lpellegr
 */
public abstract class RequestReplyManager implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(RequestReplyManager.class);

    protected StructuredOverlay overlay;

    private Map<UUID, ReplyEntry> repliesReceived = new ConcurrentHashMap<UUID, ReplyEntry>();

    public RequestReplyManager() { 
    	
    }

	/**
	 * Dispatches the request over the overlay by using message passing.
	 * 
	 * @param request
	 *            the request to dispatch.
	 * 
	 * @return a reply associated to the type of the request.
	 * 
	 * @see #waitForFinalReply(UUID)
	 * @see #pullReply(UUID)
	 */
    public Reply<?> dispatch(Request<?> request) throws DispatchException {
    	 if (logger.isDebugEnabled()) {
             logger.debug("Dispatching request " + request.getId() + " from " + this.overlay.toString());
         }

    	 // sends the request over the overlay
    	 request.route(this.overlay);
    	 
         return this.pullReply(request.getId());
    }

	/**
	 * Pulls the reply for the specified requestId from the list of responses
	 * received. If the reply has not been yet received, a synchronization point
	 * is created in order to wait the response.
	 * 
	 * @param requestId
	 *            indicates what is the id of the reply to retrieve.
	 * 
	 * @return the reply for the specified requestId.
	 */
    protected Reply<?> pullReply(UUID requestId) {
    	// waits for the final reply
    	this.waitForFinalReply(requestId);
    	
        Reply<?> response = 
        	(Reply<?>) this.repliesReceived
                .remove(requestId).getReply();
        // sets the delivery time for latency computation
        response.setDeliveryTime();

        if (logger.isDebugEnabled()) {
            logger.debug(
                    "Final reply received for request " + requestId + " on " + this.overlay);
        }

        return response;
    }
    
	/**
	 * Creates a synchronization point in order to wait the response associated
	 * to the specified requestId.
	 * 
	 * @param requestId
	 *            indicates for which request we are waiting a reply.
	 */
    protected void waitForFinalReply(UUID requestId) {
        if (logger.isDebugEnabled()) {
            StringBuffer log = new StringBuffer();
            log.append("Waiting for ");
            log.append(this.repliesReceived.get(requestId).getExpectedRepliesCount());
            log.append(" replies with id ");
            log.append(requestId);
            log.append(" on ");
            log.append(this.overlay);
            logger.debug(log.toString());
        }
        
        synchronized (this.repliesReceived) {
            while (this.repliesReceived.get(requestId).getStatus() 
                        != ReplyEntry.Status.FINAL_REPLY_RECEIVED) {
                try {
                    this.repliesReceived.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

	/**
	 * Pushes the final reply and notify the initial sender that a new reply has
	 * been received. The notification is done to remove the synchronization
	 * point (which has been previously set by {@link #waitForFinalReply(UUID)}).
	 * 
	 * @param reply
	 *            the reply to add to the list of responses received.
	 */
    public void pushFinalReply(Reply<?> reply) {
        ReplyEntry entry = 
        	this.overlay.getReplyEntries().get(reply.getId());
                
        entry.incrementRepliesCount(1);
        entry.setResponse(reply);

        synchronized (this.overlay.getReplyEntries()) {
            this.overlay.getReplyEntries().notifyAll();
        }
    }
    
    public Map<UUID, ReplyEntry> getRepliesReceived() {
        return this.repliesReceived;
    }

    /**
     * Routes the given message over the overlay.
     * 
     * @param <T>
     * @param msg
     * 
     * @return
     */
    public <T extends RequestReplyMessage<?>> void route(T msg) {
        msg.route(this.overlay);
    }
    
    public void setOverlay(StructuredOverlay overlay) {
        this.overlay = overlay;
    }

}
