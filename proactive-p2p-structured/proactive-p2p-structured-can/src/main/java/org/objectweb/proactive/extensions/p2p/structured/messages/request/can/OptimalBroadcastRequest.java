/**
 * Copyright (c) 2011-2014 INRIA.
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
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.points.Point;
import org.objectweb.proactive.extensions.p2p.structured.providers.ResponseProvider;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.router.can.OptimalBroadcastRequestRouter;
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
public class OptimalBroadcastRequest<E extends Coordinate> extends
        MulticastRequest<E> {

    private static final long serialVersionUID = 160L;

    // The identifier of the broadcast request.
    private MessageId originalMessageId;

    // Aims to know if the request must be forwarded again
    private boolean constraintReached;

    // The directions on which the broadcast request has to be propagated by the
    // peer receiving it.
    private byte[][] directions;

    // The coordinates that describe the splitting plan (the coordinates that
    // need to be contained by the neighbors that will receive the request).
    private Coordinate[] splitPlans;

    /**
     * Constructs a new message with the specified {@code validator} but with no
     * {@link ResponseProvider}. It means that this request is not supposed to
     * sent back a response.
     * 
     * @param validator
     *            the constraints validator to use for checking the constraints.
     */
    public OptimalBroadcastRequest(MulticastConstraintsValidator<E> validator) {
        super(validator);
    }

    /**
     * Constructs a new message with the specified {@code validator},
     * {@code responseProvider} and {@code responseProvider}.
     * 
     * @param validator
     *            the constraints validator to use for checking the constraints.
     * @param responseProvider
     *            the responseProvider to use when a response has to be created.
     */
    public OptimalBroadcastRequest(
            MulticastConstraintsValidator<E> validator,
            ResponseProvider<? extends MulticastResponse<E>, Point<E>> responseProvider) {
        super(validator, responseProvider);
    }

    public OptimalBroadcastRequest(
            MulticastConstraintsValidator<E> validator,
            ResponseProvider<? extends MulticastResponse<E>, Point<E>> provider,
            MessageId messageId, byte[][] directions, Coordinate[] splitPlans) {
        super(validator, provider);

        // if messageId==null then this is the request
        // received by the initiator.
        if (messageId == null) {
            this.originalMessageId = this.getId();
        } else {
            this.originalMessageId = messageId;
        }

        this.splitPlans = splitPlans;
        this.directions = directions;
        this.constraintReached = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Router<? extends Message<Point<E>>, Point<E>> getRouter() {
        return new OptimalBroadcastRequestRouter<OptimalBroadcastRequest<E>, E>();
    }

    /**
     * Changes the status of the request to inform that the constraint limits
     * have been reached. Call this to cut off the forwarding as soon as
     * possible.
     */
    public void turnConstraintReached() {
        this.constraintReached = true;
    }

    /**
     * Says whether the request has already reached the constraint that has been
     * given to it.
     * 
     * @return true if the request has already satisfied the constraint.
     */
    public boolean getConstraintReached() {
        return this.constraintReached;
    }

    public byte[][] getDirections() {
        return this.directions;
    }

    public byte getDirection(int i, int j) {
        return this.directions[i][j];
    }

    public Coordinate[] getSplitPlans() {
        return this.splitPlans;
    }

    public Coordinate getSplitPlan(int index) {
        return this.splitPlans[index];
    }

    public void setDirections(byte[][] directions) {
        this.directions = directions;
    }

    public void setSplitPlans(Coordinate[] splitPlans) {
        this.splitPlans = splitPlans;
    }

    public void setSplitPlan(int index, Coordinate splitPlan) {
        this.splitPlans[index] = splitPlan;
    }

    public MessageId getOriginalMessageId() {
        return this.originalMessageId;
    }

    public void setOriginalMessageId(MessageId originalMessageId) {
        this.originalMessageId = originalMessageId;
    }

}
