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
package org.objectweb.proactive.extensions.p2p.structured.messages.request.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.Request;
import org.objectweb.proactive.extensions.p2p.structured.messages.RequestResponseMessage;
import org.objectweb.proactive.extensions.p2p.structured.messages.ReversePathEntry;
import org.objectweb.proactive.extensions.p2p.structured.messages.ReversePathStack;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.MulticastResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;
import org.objectweb.proactive.extensions.p2p.structured.providers.ResponseProvider;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.router.can.FloodingBroadcastRequestRouter;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.MulticastConstraintsValidator;

/**
 * Message used to dispatch a request to all peers validating the specified
 * constraints (i.e. the coordinates to reach).
 * 
 * @param <E>
 *            the {@link Element}s type manipulated.
 * 
 * @author lpellegr
 */
public class MulticastRequest<E extends Element> extends Request<Coordinate<E>> {

    private static final long serialVersionUID = 160L;

    protected final ReversePathStack<E> reversePathStack;

    /**
     * Constructs a new message with the specified {@code validator} but with no
     * {@link ResponseProvider}. It means that this request is not supposed to
     * sent back a response.
     * 
     * @param validator
     *            the constraints validator to use for checking the constraints.
     */
    public MulticastRequest(MulticastConstraintsValidator<E> validator) {
        this(validator, null);
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
    public MulticastRequest(
            MulticastConstraintsValidator<E> validator,
            ResponseProvider<? extends MulticastResponse<E>, Coordinate<E>> responseProvider) {
        super(validator, responseProvider);

        this.reversePathStack = new ReversePathStack<E>();
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
    public Router<? extends RequestResponseMessage<Coordinate<E>>, Coordinate<E>> getRouter() {
        return new FloodingBroadcastRequestRouter<MulticastRequest<E>, E>();
    }

    public boolean validatesKeyConstraints(Zone<E> zone) {
        return ((MulticastConstraintsValidator<E>) super.constraintsValidator).validatesKeyConstraints(zone);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer();

        buf.append(this.getClass().getSimpleName());
        buf.append("[id=");
        buf.append(this.getId());
        buf.append(", stack:\n");

        for (ReversePathEntry<E> entry : this.reversePathStack) {
            buf.append("  - ");
            buf.append(entry.getPeerCoordinate());
            buf.append('\n');
        }

        buf.append("]");

        return buf.toString();
    }

}
