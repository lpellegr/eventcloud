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

import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.messages.AnycastRoutingEntry;
import org.objectweb.proactive.extensions.p2p.structured.messages.AnycastRoutingList;
import org.objectweb.proactive.extensions.p2p.structured.messages.RequestResponseMessage;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.ResponseProvider;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.can.AnycastResponse;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.Zone;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.elements.Element;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.router.can.OptimalBroadcastRequestRouter;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.AnycastConstraintsValidator;

/**
 * Message used to dispatch a request to all peers validating the specified
 * constraints (i.e. the coordinates to reach).
 * 
 * @param <E>
 *            the {@link Element}s type manipulated.
 * 
 * @author jrochas
 */
public class OptimalBroadcastRequest<E extends Element> extends
        AnycastRequest<E> {

    private static final long serialVersionUID = 150L;

    private boolean alreadyReceived = false;

    // The identifier of the broadcast request.
    private UUID originalMessageId;

    private AnycastRoutingList broadcastRoutingList = new AnycastRoutingList();
    // The directions on which the broadcast request has to be propagated by the
    // peer receiving it.
    private byte[][] directions;
    // The coordinates that describe the splitting plan (the coordinates that
    // need to be contained by the neighbors that will receive the request).
    private Element[] splitPlans;

    /**
     * Constructs a new message with the specified {@code validator} but with no
     * {@link ResponseProvider}. This means that this request is not supposed to
     * sent back a response.
     * 
     * @param validator
     *            the constraints validator to use for checking the constraints.
     */
    public OptimalBroadcastRequest(AnycastConstraintsValidator<E> validator) {
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
    public OptimalBroadcastRequest(
            AnycastConstraintsValidator<E> validator,
            ResponseProvider<? extends AnycastResponse<E>, Coordinate<E>> responseProvider) {
        super(validator, responseProvider);
    }

    public OptimalBroadcastRequest(
            AnycastConstraintsValidator<E> validator,
            ResponseProvider<? extends AnycastResponse<E>, Coordinate<E>> provider,
            UUID messageId, byte[][] directions, Element[] splitPlans) {
        super(validator, provider);
        // if messageId==null then this is the request received by the
        // initiator.
        if (messageId == null) {
            this.originalMessageId = this.getId();
        } else {
            this.originalMessageId = messageId;
        }

        this.splitPlans = splitPlans;
        this.directions = directions;
    }

    @Override
    public void markAsAlreadyReceived() {
        this.alreadyReceived = true;
    }

    @Override
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
    public AnycastRoutingList getBroadcastRoutingList() {
        return this.broadcastRoutingList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Router<? extends RequestResponseMessage<Coordinate<E>>, Coordinate<E>> getRouter() {
        return new OptimalBroadcastRequestRouter<OptimalBroadcastRequest<E>, E>();
    }

    @Override
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
        for (AnycastRoutingEntry entry : this.broadcastRoutingList) {
            buf.append("  - ");
            buf.append(entry.getPeerStub());
            buf.append('\n');
        }

        return buf.toString();
    }

    public byte[][] getDirections() {
        return this.directions;
    }

    public byte getDirection(int i, int j) {
        return this.directions[i][j];
    }

    public Element[] getSplitPlans() {
        return this.splitPlans;
    }

    public Element getSplitPlan(int index) {
        return this.splitPlans[index];
    }

    public void setDirections(byte[][] directions) {
        this.directions = directions;
    }

    public void setSplitPlans(Element[] splitPlans) {
        this.splitPlans = splitPlans;
    }

    public void setSplitPlan(int index, Element splitPlan) {
        this.splitPlans[index] = splitPlan;
    }

    public UUID getOriginalMessageId() {
        return this.originalMessageId;
    }

    public void setOriginalMessageId(UUID originalMessageId) {
        this.originalMessageId = originalMessageId;
    }

}
