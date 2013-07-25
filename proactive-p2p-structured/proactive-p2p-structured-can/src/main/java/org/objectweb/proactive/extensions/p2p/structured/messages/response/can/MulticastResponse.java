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
package org.objectweb.proactive.extensions.p2p.structured.messages.response.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.Request;
import org.objectweb.proactive.extensions.p2p.structured.messages.Response;
import org.objectweb.proactive.extensions.p2p.structured.messages.ReversePathEntry;
import org.objectweb.proactive.extensions.p2p.structured.messages.ReversePathStack;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.MulticastRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.router.can.BroadcastResponseRouter;
import org.objectweb.proactive.extensions.p2p.structured.validator.ConstraintsValidator;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.UnicastConstraintsValidator;

/**
 * Response associated to {@link MulticastRequest}. This kind of response will
 * use the same path as the initial request for its routing. The concrete
 * implementation has to override {@link #mergeAttributes(MulticastResponse)} if
 * it is supposed to sent back a response.
 * 
 * @param <E>
 *            the {@link Element}s type manipulated.
 * 
 * @author lpellegr
 */
public class MulticastResponse<E extends Element> extends
        Response<Coordinate<E>> {

    private static final long serialVersionUID = 151L;

    private ReversePathStack<E> reversePathStack;

    private ConstraintsValidator<Coordinate<E>> requestConstraintValidator;

    private boolean isEmpty = false;

    public MulticastResponse() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void setAttributes(Request<Coordinate<E>> request,
                              StructuredOverlay overlay) {
        super.setAttributes(request, overlay);

        MulticastRequest<E> anycastRequest = (MulticastRequest<E>) request;

        this.requestConstraintValidator =
                anycastRequest.getConstraintsValidator();
        this.reversePathStack = anycastRequest.getReversePathStack();
        this.constraintsValidator =
                new UnicastConstraintsValidator<E>(
                        ((CanOverlay<E>) overlay).getZone().getLowerBound());
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
     * validate the constraints. To check if the peer is validating the
     * constraints you can use
     * {@link #validatesRequestKeyConstraints(StructuredOverlay)}.
     * 
     * @param overlay
     *            the overlay on which this method is called.
     */
    public void beforeSendingBackResponse(StructuredOverlay overlay) {
        // to be overriden if necessary
    }

    public boolean isEmpty() {
        return this.isEmpty;
    }

    /**
     * Returns the {@link ReversePathStack} containing the
     * {@link ReversePathEntry entries} to use in order to route the response.
     * 
     * @return the {@link ReversePathStack} containing the
     *         {@link ReversePathEntry entries} to use in order to route the
     *         response.
     */
    public ReversePathStack<E> getReversePathStack() {
        return this.reversePathStack;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Router<? extends MulticastResponse<E>, Coordinate<E>> getRouter() {
        return new BroadcastResponseRouter<MulticastResponse<E>, E>();
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
    public void mergeAttributes(MulticastResponse<E> responseReceived) {
        // to be overridden if necessary
    }

    /**
     * Merges two responses on a synchronization point. To merge information
     * which are specific to the type of response, you have to override
     * {@link #mergeAttributes(MulticastResponse)}.
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
     * @see #mergeAttributes(MulticastResponse)
     */
    public static <E extends Element> MulticastResponse<E> merge(MulticastResponse<E> localResponse,
                                                                 MulticastResponse<E> responseReceived) {
        if (localResponse == null) {
            return responseReceived;
        } else {
            localResponse.incrementHopCount(responseReceived.getInboundHopCount());
            localResponse.mergeAttributes(responseReceived);
            return localResponse;
        }
    }

    public void setConstraintsValidator(ConstraintsValidator<Coordinate<E>> constraintsValidator) {
        super.constraintsValidator = constraintsValidator;
    }

    public void setIsEmpty(boolean value) {
        this.isEmpty = value;
    }

    public boolean validatesRequestKeyConstraints(StructuredOverlay overlay) {
        return this.requestConstraintValidator.validatesKeyConstraints(overlay);
    }

}
