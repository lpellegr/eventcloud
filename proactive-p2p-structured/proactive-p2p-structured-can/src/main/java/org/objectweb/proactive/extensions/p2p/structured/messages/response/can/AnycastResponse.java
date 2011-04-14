package org.objectweb.proactive.extensions.p2p.structured.messages.response.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.AnycastRoutingEntry;
import org.objectweb.proactive.extensions.p2p.structured.messages.AnycastRoutingList;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.Response;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.StringCoordinate;

/**
 * Response associated to {@link AnycastRequest}. This kind of response will use
 * the same path as the initial request for its routing.
 * 
 * @author lpellegr
 */
public abstract class AnycastResponse extends Response<StringCoordinate> {

    private static final long serialVersionUID = 1L;

    private AnycastRoutingList anycastRoutingList = new AnycastRoutingList();

    public AnycastResponse(AnycastRequest request) {
        super(request, null);
        this.anycastRoutingList = request.getAnycastRoutingList();
    }

    /**
     * Returns the {@link AnycastRoutingList} containing the
     * {@link AnycastRoutingEntry} to use in order to route the response.
     * 
     * @return the {@link AnycastRoutingList} containing the
     *         {@link AnycastRoutingEntry} to use in order to route the
     *         response.
     */
    public AnycastRoutingList getAnycastRoutingList() {
        return this.anycastRoutingList;
    }

	/**
	 * Merges the specified {@link AnycastResponse} with the current one. This
	 * methods have to be overridden to merge some results data that are conveyed.
	 * When the merge operation is terminated, the specified response is discarded.
	 * 
	 * @param subResponse
	 *            the subResponse to merge into the current one.
	 */
    public synchronized void merge(AnycastResponse subResponse) {
    	// to be overridden
    }

}
