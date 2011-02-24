package org.objectweb.proactive.extensions.p2p.structured.messages.reply.can;

import org.objectweb.proactive.core.body.reply.Reply;
import org.objectweb.proactive.extensions.p2p.structured.messages.PendingReplyEntry;
import org.objectweb.proactive.extensions.p2p.structured.messages.reply.AbstractReply;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.ForwardRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.router.can.UnicastResponseRouter;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.UnicastConstraintsValidator;

/**
 * Response associated to {@link ForwardRequest}.
 * 
 * @author lpellegr
 */
public class ForwardReply extends AbstractReply<Coordinate> {

    private static final long serialVersionUID = 1L;
    
    public ForwardReply(ForwardRequest query) {
        super(query, query.getSenderCoordinate());
    }

    /**
     * Handles the last step for the current response using the specified
     * {@link StructuredOverlay}.
     * 
     * @param overlay
     *            the overlay to use in order to handle the response.
     */
    public void handle(StructuredOverlay overlay) {
        PendingReplyEntry entry = 
        	overlay.getRepliesReceived().get(super.getId());
        entry.incrementResponsesNumber(1);
        entry.setResponse(this);

        synchronized (overlay.getRepliesReceived()) {
            overlay.getRepliesReceived().notifyAll();
        }
    }

    /**
     * {@inheritDoc}
     */
    public UnicastResponseRouter<ForwardReply> getRouter() {
        return new UnicastResponseRouter<ForwardReply>(
        							new UnicastConstraintsValidator());
    }

    /**
     * Nothing to do because LookupResponseMessage is not accessible from the
     * public API.  
     */
    public Reply createResponse() {
        return null;
    }


}
