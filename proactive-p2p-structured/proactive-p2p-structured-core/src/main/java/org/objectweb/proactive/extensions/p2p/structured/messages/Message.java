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
package org.objectweb.proactive.extensions.p2p.structured.messages;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.proxies.Proxy;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.utils.SerializedValue;
import org.objectweb.proactive.extensions.p2p.structured.validator.ConstraintsValidator;

/**
 * Class that all request/response messages must implement. It maintains some
 * standard information that may useful for any kind of message.
 * 
 * @author lpellegr
 */
public abstract class Message<K> implements Routable<K>, Serializable {

    private static final long serialVersionUID = 160L;

    /**
     * Universally unique identifier used in order to identify the message.
     */
    protected MessageId id;

    /**
     * Identifier used to combine several requests as one. This id identifier is
     * set and used when a set of requests are dispatched together with a call
     * from {@link Proxy#send(java.util.List, Serializable, ResponseCombiner)}
     */
    protected MessageId aggregationId;

    /**
     * Constraints validator used to make the routing decision possible.
     */
    protected transient ConstraintsValidator<K> constraintsValidator;

    /**
     * Reference to the requester that has to receive the final response.
     */
    protected SerializedValue<FinalResponseReceiver> responseDestination;

    /**
     * The number of hops between the source and the destination of the message.
     */
    protected int hopCount = 0;

    /**
     * Constructs a new RequestResponseMessage with the specified
     * {@code constraintsValidator}.
     * 
     * @param constraintsValidator
     *            the constraints validator to use for routing the message.
     */
    public Message(ConstraintsValidator<K> constraintsValidator) {
        this.constraintsValidator = constraintsValidator;
    }

    /**
     * Returns the constraints validator.
     * 
     * @return the constraints validator.
     */
    public ConstraintsValidator<K> getConstraintsValidator() {
        return this.constraintsValidator;
    }

    /**
     * Returns the timestamp of the creation of the message.
     * 
     * @return the timestamp of the creation of the message.
     */
    public abstract long getDispatchTimestamp();

    /**
     * Returns a reference to the requester.
     * 
     * @return the reference to the requester of the request.
     */
    public FinalResponseReceiver getResponseDestination() {
        return this.responseDestination.getValue();
    }

    /**
     * Returns the universally unique identifier which identifies the message.
     * 
     * @return the universally unique identifier which identifies the message.
     */
    public MessageId getId() {
        return this.id;
    }

    public MessageId getAggregationId() {
        return this.aggregationId;
    }

    /**
     * Returns the key to reach.
     * 
     * @return the key to reach.
     */
    public K getKey() {
        return this.constraintsValidator.getKey();
    }

    /**
     * Returns the number of hops between the source and the destination of this
     * message.
     * 
     * @return the number of hops between the source and the destination of this
     *         message.
     */
    public int getHopCount() {
        return this.hopCount;
    }

    /**
     * Increments the hop count (i.e. the counter counting the number of hops
     * between the source and the destination of this message) by the specified
     * {@code increment}.
     * 
     * @param increment
     *            the size of the increment.
     */
    public void incrementHopCount(int increment) {
        this.hopCount += increment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void route(StructuredOverlay overlay) {
        // this condition is useful to set an id to requests that are routed
        // directly without passing by a proxy. Requests allowed to bypass
        // proxies are those for which no response is sent back. However, to do
        // so is risked and we suppose that users know what they are doing.
        if (this.id == null) {
            this.id = overlay.newMessageId();
        }

        ((Router<Message<K>, K>) this.getRouter()).makeDecision(overlay, this);
    }

    /**
     * Sets a new value to the hop count counter.
     * 
     * @param value
     *            the new value to set.
     */
    public void setHopCount(int value) {
        this.hopCount = value;
    }

    /**
     * Indicates if the specified {@code overlay} validates the constraints
     * which are denoted by {@code key}.
     * 
     * @param overlay
     *            the overlay on which the constraints are checked.
     * 
     * @return {@code true} if the constraints are validated, {@code false}
     *         otherwise.
     */
    public boolean validatesKeyConstraints(StructuredOverlay overlay) {
        if (this.constraintsValidator == null) {
            return true;
        }

        return this.constraintsValidator.validatesKeyConstraints(overlay);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "RequestResponseMessage[id=" + this.id + ", aggregationId="
                + this.aggregationId + ", constraintsValidator="
                + this.constraintsValidator + ", hopCount=" + this.hopCount
                + "]";
    }

    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        stream.defaultReadObject();
        this.customReadObject(stream);
    }

    @SuppressWarnings("unchecked")
    protected void customReadObject(ObjectInputStream stream)
            throws ClassNotFoundException, IOException {
        this.constraintsValidator =
                (ConstraintsValidator<K>) stream.readObject();
    }

    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        this.customWriteObject(stream);
    }

    protected void customWriteObject(ObjectOutputStream stream)
            throws IOException {
        stream.writeObject(this.constraintsValidator);
    }

    @SuppressWarnings("unused")
    private boolean isCompatible(Message<K> other) {
        return true;
    }

}
