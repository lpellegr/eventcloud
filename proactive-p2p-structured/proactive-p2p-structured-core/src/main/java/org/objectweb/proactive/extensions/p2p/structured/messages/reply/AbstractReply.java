package org.objectweb.proactive.extensions.p2p.structured.messages.reply;

import org.objectweb.proactive.extensions.p2p.structured.messages.RequestReplyMessage;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.AbstractRequest;

/**
 * An <code>AbstractReply</code> is an responses associated to an 
 * {@link AbstractRequest}. This class contains several information
 * common to all replies like for example the latency, the hop count, etc.
 * 
 * @author lpellegr
 */
public abstract class AbstractReply<K> extends RequestReplyMessage<K> {

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
     * Constructs a new response message with the specified <code>query</code>
     * and <code>keyToReach</code>.
     * 
     * @param response
     *            the response to merge with.
     * @param keyToReach
     *            the key used in order to route the response to it recipient.
     */
    public AbstractReply(AbstractReply<K> response, K keyToReach) {
        super(response.getId(), keyToReach);
        this.dispatchTimestamp = response.getDispatchTimestamp();
        this.outboundHopCount = response.getOutboundHopCount();
        super.incrementHopCount(response.getInboundHopCount());
    }

    /**
     * Constructs a new reply with the specified <code>request</code>
     * and <code>keyToReach</code>.
     * 
     * @param request
     *            the query which creates the response.
     * @param keyToReach
     *            the key used in order to route the response to it recipient.
     */
    public AbstractReply(AbstractRequest<K> request, K keyToReach) {
        super(request.getId(), keyToReach);
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
     * Returns the number of peers traversed by a reply
     * between its source and its destination.
     * 
     * @return the number of peers traversed by a reply
     * 		   between its source and its destination.
     */
    public int getInboundHopCount() {
        return super.getHopCount();
    }

    /**
     * Returns the number of peers traversed by a request 
     * between its source and its destination.
     * 
     * @return the number of peers traversed by a request
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
     * Returns a string containing a concise, human-readable description of this
     * object.
     * 
     * @return a string containing a concise, human-readable description of this
     *         object.
     */
    public String toString() {
        return this.getId().toString();
    }

}
