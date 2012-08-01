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

import org.objectweb.proactive.extensions.p2p.structured.messages.ResponseEntry;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.Request;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.ForwardRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.Response;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;
import org.objectweb.proactive.extensions.p2p.structured.router.can.UnicastResponseRouter;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.UnicastConstraintsValidator;

/**
 * Response associated to {@link ForwardRequest}.
 * 
 * @param <E>
 *            the {@link Element}s type manipulated.
 * 
 * @author lpellegr
 */
public class ForwardResponse<E extends Element> extends Response<Coordinate<E>> {

    private static final long serialVersionUID = 1L;

    public ForwardResponse() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttributes(Request<Coordinate<E>> request,
                              StructuredOverlay overlay) {
        super.setAttributes(request, overlay);

        super.constraintsValidator =
                new UnicastConstraintsValidator<E>(
                        ((ForwardRequest<E>) request).getSenderCoordinate());
    }

    /**
     * Handles the last step for the current response using the specified
     * {@link StructuredOverlay}.
     * 
     * @param overlay
     *            the overlay to use in order to handle the response.
     */
    public void handle(StructuredOverlay overlay) {
        ResponseEntry entry = overlay.getResponseEntries().get(super.getId());

        synchronized (entry) {
            entry.incrementResponsesCount(1);
            entry.setResponse(this);
            entry.notifyAll();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UnicastResponseRouter<ForwardResponse<E>, E> getRouter() {
        return new UnicastResponseRouter<ForwardResponse<E>, E>();
    }

}
