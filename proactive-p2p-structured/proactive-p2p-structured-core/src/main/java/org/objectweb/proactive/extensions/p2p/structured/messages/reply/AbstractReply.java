package org.objectweb.proactive.extensions.p2p.structured.messages.reply;

import org.objectweb.proactive.extensions.p2p.structured.api.messages.Reply;
import org.objectweb.proactive.extensions.p2p.structured.messages.RequestReplyMessage;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.AbstractRequest;

/**
 * An <code>AbstractReply</code> is an abstract response associated to
 * an abstract {@link AbstractQueryMessage}. This class contains several information
 * common to all responses like for example the latency, the hop count, etc.
 * 
 * @author Laurent Pellegrino
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
        super(response.getID(), keyToReach);
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
        super(request.getID(), keyToReach);
        this.dispatchTimestamp = request.getDispatchTimestamp();
        this.outboundHopCount = request.getHopCount();
    }

    /**
     * Converts the current response (from private API) to a response from the
     * public API.
     * 
     * @return a response from the public API.
     */
    public abstract Reply createResponse();

    /**
     * Returns the timestamp of the delivery.
     * 
     * @return the timestamp of the delivery.
     */
    public long getDeliveryTimestamp() {
        return this.deliveryTimestamp;
    }

    /**
     * Returns the timestamps of the dispatch of the initial query.
     * 
     * @return the timestamps of the dispatch of the initial query.
     */
    public long getDispatchTimestamp() {
        return this.dispatchTimestamp;
    }

    /**
     * Returns the latency (in milliseconds). It is the time between the moment
     * of the creation of the message and when the response has been received.
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
     * Returns the number of peers traversed by a response message 
     * between its source and destination.
     * 
     * @return the number of peers traversed by a response message 
     * 		   between its source and destination.
     */
    public int getInboundHopCount() {
        return super.getHopCount();
    }

    /**
     * Returns the number of peers traversed by a query message 
     * between its source and destination.
     * 
     * @return the number of peers traversed by a query message 
     * 		   between its source and destination.
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
            throw new IllegalStateException("Delivery time has already been set.");
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
        return this.getID().toString();
    }

}
