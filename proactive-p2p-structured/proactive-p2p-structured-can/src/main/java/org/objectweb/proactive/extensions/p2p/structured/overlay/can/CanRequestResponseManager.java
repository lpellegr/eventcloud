package org.objectweb.proactive.extensions.p2p.structured.overlay.can;

import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListSet;

import org.objectweb.proactive.extensions.p2p.structured.overlay.RequestResponseManager;

/**
 * 
 * @author lpellegr
 */
public class CanRequestResponseManager extends RequestResponseManager {

	private static final long serialVersionUID = 1L;

	/*
	 * Used to maintain the list of requests which have been already 
	 * received when anycast requests are executed.
	 */
    private ConcurrentSkipListSet<UUID> requestsAlreadyReceived = new ConcurrentSkipListSet<UUID>();
	
    public void markRequestAsReceived(UUID requestId) {
    	this.requestsAlreadyReceived.add(requestId);
    }

	/**
	 * Indicates if the current peer has already received the request identified
	 * by the specified requestId.
	 * 
	 * @param requestId
	 * 
	 * @return {@code true} if the current peer has already received the
	 *         request, {@code false} otherwise.
	 */
    public boolean hasReceivedRequest(UUID requestId) {
    	return this.requestsAlreadyReceived.contains(requestId);
    }
    
}
