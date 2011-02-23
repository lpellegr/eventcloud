package fr.inria.eventcloud.router.can;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.objectweb.proactive.extensions.p2p.structured.messages.PendingReplyEntry;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.validator.ConstraintsValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.messages.reply.can.AnycastReply;
import fr.inria.eventcloud.messages.request.can.AnycastRoutingEntry;
import fr.inria.eventcloud.overlay.SemanticQueryManager;

/**
 * Router used to route {@link AnycastReply}s.
 * 
 * @param <T>
 *            the type of response to route.
 * @param <D>
 *            data type contained by response.
 * 
 * @author lpellegr
 */
public class AnycastReplyRouter<T extends AnycastReply<D>, D> extends
        Router<AnycastReply<D>, Coordinate> {

    public AnycastReplyRouter(ConstraintsValidator<Coordinate> validator) {
        super(validator);
    }

    private static final Logger logger = 
        LoggerFactory.getLogger(AnycastReplyRouter.class);

    protected void performHandle(StructuredOverlay overlay, AnycastReply<D> msg) {
    	// the number of outbound hop count is equal to the
    	// number of inbound hop count because the message follows
    	// the way in the both cases.
    	msg.setOutboundHopCount(msg.getInboundHopCount());
        synchronized (overlay.getRepliesReceived()) {
            overlay.getRepliesReceived().notifyAll();
        }
    }

    public void makeDecision(StructuredOverlay overlay,
    		                    AnycastReply<D> msg) {
        PendingReplyEntry entry = overlay.getQueryManager().mergeResponseReceived(msg);
        @SuppressWarnings("unchecked")
		AnycastReply<D> response = (AnycastReply<D>) entry.getResponse();
        entry.incrementResponsesNumber(1);

        // we are on a synchronization point and all responses are received, 
		// we must ensure that query datastore is terminated before to send 
		// back the response.
		if (entry.getStatus() == PendingReplyEntry.Status.ALL_RESPONSES_RECEIVED) {
		    FutureTask<Object> pendingQueryResult = 
                ((SemanticQueryManager) overlay.getQueryManager())
                    .getPendingQueries().get(response.getID());
		    // pending query can be null if the response comes
		    // from a leaf peer. In this case the leaf has already
		    // queried its datastore, so we have nothing to do
		    if (pendingQueryResult != null) {
                // forces the query thread to terminate and merges 
		    	// the result returned by the query thread
                try {
					response.storeData(pendingQueryResult.get());
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					logger.error("Interrupted Exception", e);
				} catch (ExecutionException e) {
					logger.error("Error while querying the datastore on " + overlay, e);
				}
                // removes the pending query
                ((SemanticQueryManager) overlay.getQueryManager())
                	.getPendingQueries().remove(response.getID());
            }
			
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
				overlay.getQueryManager().getResponsesReceived().remove(response.getID());
				if (logger.isDebugEnabled()) {
					logger.debug("All responses received on " + overlay + " for msg " + msg.getID() + ".");
				}
			}
		}
    }
    
    /**
     * {@inheritDoc}
     */
    protected void performRoute(StructuredOverlay overlay, AnycastReply<D> msg) {
		AnycastRoutingEntry entry = msg.getAnycastRoutingList().removeLast();
		msg.incrementHopCount(1);
		entry.getPeerStub().route(msg);
		
		if (logger.isDebugEnabled()) {
			logger.debug(
					"On peer " + overlay + ", route response on peer " 
					+ entry.getPeerStub() + " validating constraints.");
		}
    }

}
