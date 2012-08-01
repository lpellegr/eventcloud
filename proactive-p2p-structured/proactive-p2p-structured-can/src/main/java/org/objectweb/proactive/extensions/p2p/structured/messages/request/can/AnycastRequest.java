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
package org.objectweb.proactive.extensions.p2p.structured.messages.request.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.AnycastRoutingEntry;
import org.objectweb.proactive.extensions.p2p.structured.messages.AnycastRoutingList;
import org.objectweb.proactive.extensions.p2p.structured.messages.RequestResponseMessage;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.Request;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.ResponseProvider;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.AnycastResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.router.can.AnycastRequestRouter;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.AnycastConstraintsValidator;

/**
 * Message used to dispatch a request to all peers validating the specified
 * constraints (i.e. the coordinates to reach).
 * 
 * @param <E>
 *            the {@link Element}s type manipulated.
 * 
 * @author lpellegr
 */
public class AnycastRequest<E extends Element> extends Request<Coordinate<E>> {

    private static final long serialVersionUID = 1L;

    private AnycastRoutingList anycastRoutingList = new AnycastRoutingList();

    private boolean alreadyReceived = false;

    /**
     * Constructs a new message with the specified {@code validator} but with no
     * {@link ResponseProvider}. This means that this request is not supposed to
     * sent back a response.
     * 
     * @param validator
     *            the constraints validator to use for checking the constraints.
     */
    public AnycastRequest(AnycastConstraintsValidator<E> validator) {
        super(validator, null);
    }

    /**
     * Constructs a new message with the specified {@code validator} and
     * {@code responseProvider}.
     * 
     * @param validator
     *            the constraints validator to use for checking the constraints.
     * @param responseProvider
     *            the responseProvider to use when a response has to be created.
     */
    public AnycastRequest(
            AnycastConstraintsValidator<E> validator,
            ResponseProvider<? extends AnycastResponse<E>, Coordinate<E>> responseProvider) {
        super(validator, responseProvider);
    }

    public void markAsAlreadyReceived() {
        this.alreadyReceived = true;
    }

    public boolean isAlreadyReceived() {
        return this.alreadyReceived;
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
    public Router<? extends RequestResponseMessage<Coordinate<E>>, Coordinate<E>> getRouter() {
        return new AnycastRequestRouter<AnycastRequest<E>, E>();
    }

    public boolean validatesKeyConstraints(Zone<E> zone) {
        return ((AnycastConstraintsValidator<E>) super.constraintsValidator).validatesKeyConstraints(zone);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer("AnycastQueryMessage ID=");
        buf.append(this.getId());

        buf.append("\nStack: \n");
        for (AnycastRoutingEntry entry : this.anycastRoutingList) {
            buf.append("  - ");
            buf.append(entry.getPeerStub());
            buf.append('\n');
        }

        return buf.toString();
    }

}
