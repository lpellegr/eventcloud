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
package org.objectweb.proactive.extensions.p2p.structured.messages;

import java.io.Serializable;
import java.util.UUID;

import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.router.Router;
import org.objectweb.proactive.extensions.p2p.structured.validator.ConstraintsValidator;

/**
 * Class that all request/response messages must implement. It maintains some
 * standard information that may useful for any kind of message.
 * 
 * @author lpellegr
 */
public abstract class RequestResponseMessage<K> implements Routable<K>,
        Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Universally unique identifier used in order to identify the message.
     */
    protected final UUID uuid;

    /**
     * Constraints validator used to make the routing decision possible.
     */
    protected ConstraintsValidator<K> constraintsValidator;

    /**
     * The number of hops between the source and the destination of the message.
     */
    private int hopCount = 0;

    /**
     * Constructs a new RequestResponseMessage with the specified
     * {@code identifier} and {@code constraintsValidator}.
     * 
     * @param identifier
     *            the unique identifier associated to the message.
     * 
     * @param constraintsValidator
     *            the constraints validator to use for routing the message.
     */
    public RequestResponseMessage(UUID identifier,
            ConstraintsValidator<K> constraintsValidator) {
        this.uuid = identifier;
        this.constraintsValidator = constraintsValidator;
    }

    /**
     * Returns the timestamp of the creation of the message.
     * 
     * @return the timestamp of the creation of the message.
     */
    public abstract long getDispatchTimestamp();

    /**
     * Returns the universally unique identifier which identifies the message.
     * 
     * @return the universally unique identifier which identifies the message.
     */
    public UUID getId() {
        return this.uuid;
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
     * <code>increment</code>.
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
        ((Router<RequestResponseMessage<K>, K>) this.getRouter()).makeDecision(
                overlay, this);
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
     * @return <code>true</code> if the constraints are validated,
     *         <code>false</code> otherwise.
     */
    public boolean validatesKeyConstraints(StructuredOverlay overlay) {
        return this.constraintsValidator.validatesKeyConstraints(overlay);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "RequestResponseMessage [id=" + this.uuid
                + ", constraintsValidator=" + this.constraintsValidator
                + ", hopCount=" + this.hopCount + "]";
    }

}
