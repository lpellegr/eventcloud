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
package org.objectweb.proactive.extensions.p2p.structured.messages.response;

import org.objectweb.proactive.extensions.p2p.structured.messages.RequestResponseMessage;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.Request;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.validator.ConstraintsValidator;

/**
 * A Response is the response message associated to a {@link Request}. This
 * class contains several information common to all responses (e.g. the latency,
 * the hop count, etc.).
 * 
 * @author lpellegr
 */
public abstract class Response<K> extends RequestResponseMessage<K> {

    private static final long serialVersionUID = 140L;

    private long dispatchTimestamp;

    private long deliveryTimestamp;

    /*
     * The number of peers traversed by the request associated to 
     * this response between its source and its destination.
     */
    private int outboundHopCount;

    private int latency = -1;

    /**
     * Constructs a response with a {@link ConstraintsValidator} that is set to
     * {@code null}.
     */
    public Response() {
        super(null);
    }

    /**
     * Constructs a response with the specified {@code validator}.
     * 
     * @param validator
     *            the constraints validator to use.
     */
    public Response(ConstraintsValidator<K> validator) {
        super(validator);
    }

    /**
     * Sets some attributes of the current response from the specified
     * {@code request}. It is useful to pass values from a request to a response
     * because it is not possible to give these values to the constructor (due
     * to {@link ResponseProvider}). When it is overridden, the parent
     * definition must always be called in first.
     * 
     * @param request
     *            the request associated to the response.
     * @param overlay
     *            the overlay on which the response is created and where this
     *            method is called.
     */
    public void setAttributes(Request<K> request, StructuredOverlay overlay) {
        super.uuid = request.getId();
        this.dispatchTimestamp = request.getDispatchTimestamp();
        this.outboundHopCount = request.getHopCount();
    }

    /**
     * Returns the timestamp associated to the delivery of the message.
     * 
     * @return the timestamp associated to the delivery of the message.
     */
    public long getDeliveryTimestamp() {
        return this.deliveryTimestamp;
    }

    /**
     * Returns the timestamp associated to the dispatch of the initial query.
     * 
     * @return the timestamp associated the dispatch of the initial query.
     */
    @Override
    public long getDispatchTimestamp() {
        return this.dispatchTimestamp;
    }

    /**
     * Returns the latency (in milliseconds). It is the time between the
     * creation of the message and when the response has been received.
     * 
     * @return the latency between the moment of the creation of the message
     *         (dispatch) and when the response has been received (delivery).
     * 
     * @see #getDeliveryTimestamp()
     * @see #getDispatchTimestamp()
     */
    public int getLatency() {
        if (this.latency < 0) {
            throw new IllegalStateException(
                    "The response has not been receive from network after a query (latency="
                            + this.latency + ")");
        }
        return this.latency;
    }

    /**
     * Returns the number of peers traversed by the response between its source
     * and its destination.
     * 
     * @return the number of peers traversed by the response between its source
     *         and its destination.
     */
    public int getInboundHopCount() {
        return super.getHopCount();
    }

    /**
     * Returns the number of peers traversed by the request associated to this
     * response between its source and its destination.
     * 
     * @return the number of peers traversed by the request associated to this
     *         response between its source and its destination.
     */
    public int getOutboundHopCount() {
        return this.outboundHopCount;
    }

    /**
     * Sets the delivery time of the response (i.e. when the response has been
     * received). At this step, the latency is automatically calculated.
     */
    public void setDeliveryTime() {
        if (this.latency != -1) {
            throw new IllegalStateException(
                    "Delivery timestamp is already set: " + this.latency);
        }

        this.deliveryTimestamp = System.currentTimeMillis();
        this.latency = (int) (this.deliveryTimestamp - this.dispatchTimestamp);
    }

    /**
     * Sets the latency value.
     * 
     * @param duration
     *            the latency value to set.
     */
    public void setLatency(int duration) {
        this.latency = duration;
    }

    /**
     * Sets the number of peers traversed by the request associated to this
     * response between its source and destination.
     * 
     * @param value
     *            the new value to set.
     */
    public void setOutboundHopCount(int value) {
        this.outboundHopCount = value;
    }

    /**
     * Returns a string containing a concise, human-readable description of this
     * object.
     * 
     * @return a string containing a concise, human-readable description of this
     *         object.
     */
    @Override
    public String toString() {
        return "Response [id=" + super.getId() + ", dispatchTimestamp="
                + this.dispatchTimestamp + ", deliveryTimestamp="
                + this.deliveryTimestamp + ", inboundHopCount="
                + super.getHopCount() + ", outboundHopCount="
                + this.outboundHopCount + ", latency=" + this.latency + "]";
    }

}
