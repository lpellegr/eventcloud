package fr.inria.eventcloud.api.responses;

import java.io.Serializable;

/**
 * 
 * @author lpellegr
 * 
 * @param <T>
 */
public abstract class SparqlResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final long inboundHopCount;

    private final long outboundHopCount;

    private final long latency;

    private final T result;

    public SparqlResponse(long inboundHopCount, long outboundHopCount,
            long latency, T result) {
        super();
        this.inboundHopCount = inboundHopCount;
        this.outboundHopCount = outboundHopCount;
        this.latency = latency;
        this.result = result;
    }

    public long getInboundHopCount() {
        return this.inboundHopCount;
    }

    public long getOutboundHopCount() {
        return this.outboundHopCount;
    }

    public long getLatency() {
        return this.latency;
    }

    public T getResult() {
        return this.result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "SparqlResponse [inboundHopCount=" + this.inboundHopCount
                + ", outboundHopCount=" + this.outboundHopCount + ", latency="
                + this.latency + "]";
    }

}
