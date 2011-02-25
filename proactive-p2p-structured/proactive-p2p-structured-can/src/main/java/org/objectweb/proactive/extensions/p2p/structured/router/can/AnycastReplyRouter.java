package org.objectweb.proactive.extensions.p2p.structured.router.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.AnycastRoutingEntry;
import org.objectweb.proactive.extensions.p2p.structured.messages.ReplyEntry;
import org.objectweb.proactive.extensions.p2p.structured.messages.reply.Reply;
import org.objectweb.proactive.extensions.p2p.structured.messages.reply.can.AnycastReply;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.validator.ConstraintsValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Router used to route {@link AnycastReply}s.
 * 
 * @param <T>
 *            the type of response to route.
 *
 * @author lpellegr
 */
public class AnycastReplyRouter<T extends AnycastReply> extends Router<AnycastReply, Coordinate> {

	/**
	 * Constructs a new AnycastreplyRouter without any constraints validator.
	 */
	public AnycastReplyRouter() {
		super(null);
	}
	
    public AnycastReplyRouter(ConstraintsValidator<Coordinate> validator) {
        super(validator);
    }

    private static final Logger logger = LoggerFactory.getLogger(AnycastReplyRouter.class);

    protected void performHandle(StructuredOverlay overlay, AnycastReply reply) {
    	// the number of outbound hop count is equal to the number 
    	// of inbound hop count because the message follows the same
    	// path in the both cases.
    	reply.setOutboundHopCount(reply.getInboundHopCount());
        synchronized (overlay.getReplyEntries()) {
            overlay.getReplyEntries().notifyAll();
        }
    }

    public void makeDecision(StructuredOverlay overlay, AnycastReply reply) {
    	// TODO: Check if it is correct for the merge operation
        ReplyEntry entry = overlay.getReplyEntry(reply.getId());
        
        Reply<?> tmpReply = entry.getReply();
        
        if (tmpReply == null) {
        	entry.setResponse(reply);
        } else {
        	((AnycastReply) tmpReply).merge(reply);
        }
        
        entry.incrementRepliesCount(1);
		AnycastReply response = (AnycastReply) entry.getReply();

        // we are on a synchronization point and all responses are received, 
		// we must ensure that the query datastore operation is terminated 
        // before to send back the response.
		if (entry.getStatus() == ReplyEntry.Status.FINAL_REPLY_RECEIVED) {
//		    FutureTask<Object> pendingQueryResult = 
//                ((SemanticQueryManager) overlay.getRequestReplyManager())
//                    .getPendingQueries().get(response.getId());
//		    // pending query can be null if the response comes
//		    // from a leaf peer. In this case the leaf has already
//		    // queried its datastore, so we have nothing to do
//		    if (pendingQueryResult != null) {
//                // forces the query thread to terminate and merges 
//		    	// the result returned by the query thread
//                try {
//					response.storeData(pendingQueryResult.get());
//				} catch (InterruptedException e) {
//					Thread.currentThread().interrupt();
//					logger.error("Interrupted Exception", e);
//				} catch (ExecutionException e) {
//					logger.error("Error while querying the datastore on " + overlay, e);
//				}
//                // removes the pending query
//                ((SemanticQueryManager) overlay.getRequestReplyManager())
//                	.getPendingQueries().remove(response.getId());
//            }
			
			// we are on the initiator of the query we need to wake up its
			// thread in order to remove the synchronization point
			if (response.getAnycastRoutingList().size() == 0) {
				this.handle(overlay, response);
			} else {
				// the synchronization point is on one peer in the sub-tree.
				// we call the route method in order to know where to sent back
				// the response.
				this.performRoute(overlay, response);

				// the response has been handled and sent back so we can remove
				// it from the table.
				overlay.getRequestReplyManager().getRepliesReceived().remove(response.getId());
				if (logger.isDebugEnabled()) {
					logger.debug("All subreplies received on " + overlay + " for request " + reply.getId());
				}
			}
		}
    }
    
    /**
     * {@inheritDoc}
     */
    protected void performRoute(StructuredOverlay overlay, AnycastReply reply) {
		AnycastRoutingEntry entry = reply.getAnycastRoutingList().removeLast();
		reply.incrementHopCount(1);
		entry.getPeerStub().route(reply);
		
		if (logger.isDebugEnabled()) {
			logger.debug(
					"On peer " + overlay + ", route response on peer " 
					+ entry.getPeerStub() + " validating constraints");
		}
    }

}
