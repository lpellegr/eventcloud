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
package org.objectweb.proactive.extensions.p2p.structured.messages.response.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.AnycastRoutingEntry;
import org.objectweb.proactive.extensions.p2p.structured.messages.AnycastRoutingList;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.Request;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.Response;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.router.can.AnycastResponseRouter;

/**
 * Response associated to {@link AnycastRequest}. This kind of response will use
 * the same path as the initial request for its routing. The concrete
 * implementation has to override {@link #mergeAttributes(AnycastResponse)} if
 * it is supposed to sent back a response.
 * 
 * @param <E>
 *            the {@link Element}s type manipulated.
 * 
 * @author lpellegr
 */
public class AnycastResponse<E extends Element> extends Response<Coordinate<E>> {

    private static final long serialVersionUID = 1L;

    private AnycastRoutingList anycastRoutingList = new AnycastRoutingList();

    /* a response is set as dummy if it comes from a request which has already been received */
    private boolean isDummy = false;

    public AnycastResponse() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttributes(Request<Coordinate<E>> request,
                              StructuredOverlay overlay) {
        super.setAttributes(request, overlay);

        if (!((AnycastRequest<E>) request).isAlreadyReceived()) {
            this.anycastRoutingList =
                    ((AnycastRequest<E>) request).getAnycastRoutingList();
            this.constraintsValidator = request.getConstraintsValidator();
        } else {
            this.isDummy = true;
        }
    }

    /**
     * This method is called on the specified {@code overlay} once a
     * synchronization point is removed but just before to route the current
     * response to the next synchronization point.
     * <p>
     * Warning, this method may be called on a peer that does not validate the
     * constraints. Indeed, before to reach the peers that validate the
     * constraints, the request may go through some peers that does not validate
     * the constraints. Thus, because the responses follow the reverse path, it
     * is possible to have a synchronization point on a peer that does not
     * validate the constraints. To detect if the current peer on which a
     * synchronization point is unlocked, look at
     * {@link AnycastResponse#validatesKeyConstraints(StructuredOverlay)}.
     * 
     * @param overlay
     *            the overlay on which this method is called.
     */
    public void synchronizationPointUnlocked(StructuredOverlay overlay) {
        // to be overriden if necessary
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
     * {@inheritDoc}
     */
    @Override
    public Router<? extends AnycastResponse<E>, Coordinate<E>> getRouter() {
        return new AnycastResponseRouter<AnycastResponse<E>, E>();
    }

    /**
     * Merges the specified {@code responseReceived} with the current one. This
     * method has to be overridden to merge the data received as result with an
     * another response. When the merge operation is terminated, the specified
     * response is discarded.
     * 
     * @param responseReceived
     *            the response to merge with the current one.
     */
    public void mergeAttributes(AnycastResponse<E> responseReceived) {
        // to be overridden if necessary
    }

    /**
     * Merges two responses on a synchronization point. To merge information
     * which is specific to the type of response, you have to override
     * {@link #mergeAttributes(AnycastResponse)}.
     * 
     * @param localResponse
     *            the response which has already been received and which is
     *            resulting from a previous merge. If no response has been yet
     *            received, the response may be {@code null}.
     * @param responseReceived
     *            the non {@code null} response which has been received.
     * 
     * @return a new response merging {@code localResponse} and
     *         {@code responseReceived} responses.
     * 
     * @see #mergeAttributes(AnycastResponse)
     */
    public static <E extends Element> AnycastResponse<E> merge(AnycastResponse<E> localResponse,
                                                               AnycastResponse<E> responseReceived) {
        if (responseReceived.isDummy) {
            return localResponse;
        }

        if (localResponse == null) {
            return responseReceived;
        }

        localResponse.incrementHopCount(responseReceived.getOutboundHopCount());
        localResponse.mergeAttributes(responseReceived);

        return localResponse;
    }

    /**
     * Returns {@code true} if the response is dummy (i.e. this is due to a
     * request which has already been received), {@code false} otherwise.
     * 
     * @return {@code true} if the response is dummy (i.e. this is due to a
     *         request which has already been received), {@code false}
     *         otherwise.
     */
    public boolean isDummy() {
        return this.isDummy;
    }

}
