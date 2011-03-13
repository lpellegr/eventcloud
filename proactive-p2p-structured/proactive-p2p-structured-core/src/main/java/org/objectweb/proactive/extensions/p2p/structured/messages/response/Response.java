package org.objectweb.proactive.extensions.p2p.structured.messages.response;

import org.objectweb.proactive.extensions.p2p.structured.messages.RequestResponseMessage;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.Request;
import org.objectweb.proactive.extensions.p2p.structured.validator.ConstraintsValidator;

/**
 * A <code>Response</code> is the response message associated to a
 * {@link Request}. This class contains several information common to all
 * responses like for example the latency, the hop count, etc.
 * 
 * @author lpellegr
 */
public abstract class Response<K> extends RequestResponseMessage<K> {

    private static final long serialVersionUID = 1L;

    private long dispatchTimestamp;
    
    private long deliveryTimestamp;

    /**
     * The number of peers traversed by the query message between 
     * its source and destination which have been reached.
     */
    private int outboundHopCount;
    
    private int latency = -1;

    /**
     * Constructs a new response with the specified <code>request</code>
     * and <code>keyToReach</code>.
     * 
     * @param request
     *            the request associated to the response.
     * @param validator
     *            the key used in order to route the response to it recipient.
     */
    public Response(Request<K> request, ConstraintsValidator<K> validator) {
        super(request.getId(), validator);
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
    public long getDispatchTimestamp() {
        return this.dispatchTimestamp;
    }

    /**
     * Returns the latency (in milliseconds). It is the time between 
     * the creation of the message and when the response has been received.
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
                    "The response has not been receive from network after a query (latency=" + this.latency + ")");
        }
        return this.latency;
    }

    /**
     * Returns the number of peers traversed by a response
     * between its source and its destination.
     * 
     * @return the number of peers traversed by a response
     * 		   between its source and its destination.
     */
    public int getInboundHopCount() {
        return super.getHopCount();
    }

    /**
     * Returns the number of peers traversed by a response 
     * between its source and its destination.
     * 
     * @return the number of peers traversed by a response 
     * 		   between its source and its destination.
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
            throw new IllegalStateException("delivery timestamp is already set");
        }

        this.deliveryTimestamp = System.currentTimeMillis();
        this.latency = (int) (this.deliveryTimestamp - this.dispatchTimestamp);
    }

    /**
     * Sets the latency value.
     * 
     * @param duration 
     * 			the latency value to set.
     */
    public void setLatency(int duration) {
        this.latency = duration;
    }
    
    /**
     * Sets the number of peers traversed by a query message 
     * between its source and destination.
     * 
     * @param value
     * 			the new value to set.
     */
    public void setOutboundHopCount(int value) {
        this.outboundHopCount = value;
    }

	/**
	 * Returns a string containing a concise, human-readable description 
	 * of this object.
	 * 
	 * @return a string containing a concise, human-readable description 
	 * 		   of this object.
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
