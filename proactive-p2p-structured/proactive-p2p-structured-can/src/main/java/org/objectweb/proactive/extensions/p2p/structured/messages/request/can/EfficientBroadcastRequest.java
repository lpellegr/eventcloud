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

import org.objectweb.proactive.extensions.p2p.structured.messages.Message;
import org.objectweb.proactive.extensions.p2p.structured.messages.MessageId;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.MulticastResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.points.Point;
import org.objectweb.proactive.extensions.p2p.structured.providers.ResponseProvider;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.router.can.EfficientBroadcastRequestRouter;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.MulticastConstraintsValidator;

/**
 * Message used to dispatch a request to all peers validating the specified
 * constraints (i.e. the coordinates to reach).
 * 
 * @param <E>
 *            the {@link Coordinate}s type manipulated.
 * 
 * @author jrochas
 */
public class EfficientBroadcastRequest<E extends Coordinate> extends
        MulticastRequest<E> {

    private static final long serialVersionUID = 160L;

    // The identifier of the broadcast request.
    private MessageId originalMessageId;

    // The directions on which the broadcast request has to be propagated by the
    // peer receiving it.
    private byte[][] directions;

    /**
     * Constructs a new message with the specified {@code validator} but with no
     * {@link ResponseProvider}. This means that this request is not supposed to
     * sent back a response.
     * 
     * @param validator
     *            the constraints validator to use for checking the constraints.
     */
    public EfficientBroadcastRequest(MulticastConstraintsValidator<E> validator) {
        super(validator);
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
    public EfficientBroadcastRequest(
            MulticastConstraintsValidator<E> validator,
            ResponseProvider<? extends MulticastResponse<E>, Point<E>> responseProvider) {
        super(validator, responseProvider);
    }

    public EfficientBroadcastRequest(
            MulticastConstraintsValidator<E> validator,
            ResponseProvider<? extends MulticastResponse<E>, Point<E>> provider,
            MessageId messageId, byte[][] directions, Coordinate[] splitPlans) {
        super(validator, provider);

        // if messageId==null then this is the request received by the
        // initiator.
        if (messageId == null) {
            this.originalMessageId = this.getId();
        } else {
            this.originalMessageId = messageId;
        }

        this.directions = directions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Router<? extends Message<Point<E>>, Point<E>> getRouter() {
        return new EfficientBroadcastRequestRouter<EfficientBroadcastRequest<E>, E>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validatesKeyConstraints(Zone<E> zone) {
        return ((MulticastConstraintsValidator<E>) super.constraintsValidator).validatesKeyConstraints(zone);
    }

    public byte[][] getDirections() {
        return this.directions;
    }

    public byte getDirection(int i, int j) {
        return this.directions[i][j];
    }

    public void setDirections(byte[][] directions) {
        this.directions = directions;
    }

    public MessageId getOriginalMessageId() {
        return this.originalMessageId;
    }

    public void setOriginalMessageId(MessageId originalMessageId) {
        this.originalMessageId = originalMessageId;
    }

}
