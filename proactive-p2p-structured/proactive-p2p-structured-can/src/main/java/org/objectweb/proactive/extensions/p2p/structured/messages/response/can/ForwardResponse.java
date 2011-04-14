package org.objectweb.proactive.extensions.p2p.structured.messages.response.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.ResponseEntry;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.ForwardRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.Response;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;
import org.objectweb.proactive.extensions.p2p.structured.router.can.UnicastResponseRouter;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.UnicastConstraintsValidator;

/**
 * Response associated to {@link ForwardRequest}.
 * 
 * @author lpellegr
 */
public class ForwardResponse extends Response<StringCoordinate> {

    private static final long serialVersionUID = 1L;
    
    public ForwardResponse(ForwardRequest query) {
        super(query, new UnicastConstraintsValidator(query.getSenderCoordinate()));
    }

    /**
     * Handles the last step for the current response using the specified
     * {@link StructuredOverlay}.
     * 
     * @param overlay
     *            the overlay to use in order to handle the response.
     */
    public void handle(StructuredOverlay overlay) {
        ResponseEntry entry = 
        	overlay.getResponseEntries().get(super.getId());
        
        synchronized (entry) {
        	entry.incrementResponsesCount(1);
        	entry.setResponse(this);
        	entry.notifyAll();
        }
    }

    /**
     * {@inheritDoc}
     */
    public UnicastResponseRouter<ForwardResponse> getRouter() {
        return new UnicastResponseRouter<ForwardResponse>();
    }

}
