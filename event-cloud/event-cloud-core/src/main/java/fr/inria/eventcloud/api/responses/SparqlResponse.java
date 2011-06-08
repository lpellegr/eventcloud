/**
 * Copyright (c) 2011 INRIA.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.api.responses;

import java.io.Serializable;

/**
 * A SPARQL response that maintain information which are commons to any SPARQL
 * response.
 * 
 * @author lpellegr
 * 
 * @param <T>
 *            the result type.
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
