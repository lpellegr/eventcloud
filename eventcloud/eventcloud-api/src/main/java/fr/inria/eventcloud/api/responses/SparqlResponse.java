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

    private static final long serialVersionUID = 140L;

    private final long inboundHopCount;

    private final long outboundHopCount;

    private final long latency;

    private final long queryDatastoreTime;

    private final T result;

    public SparqlResponse(long inboundHopCount, long outboundHopCount,
            long latency, long queryDatastoreTime, T result) {
        super();
        this.inboundHopCount = inboundHopCount;
        this.outboundHopCount = outboundHopCount;
        this.latency = latency;
        this.queryDatastoreTime = queryDatastoreTime;
        this.result = result;
    }

    /**
     * Returns the number of peers traversed by the query in the forward
     * direction.
     * 
     * @return the number of peers traversed by the query in the forward
     *         direction.
     */
    public long getInboundHopCount() {
        return this.inboundHopCount;
    }

    /**
     * Returns the number of peers traversed by the query in the backward
     * direction.
     * 
     * @return the number of peers traversed by the query in the backward
     *         direction.
     */
    public long getOutboundHopCount() {
        return this.outboundHopCount;
    }

    /**
     * Returns the time taken (in nanoseconds) to query all the peers. This
     * value is the sum of the time taken to execute the query on each peer.
     * Hence, if you have a disjunctive query, it is possible to have a
     * {@code queryDatastoreTime} greater than the query latency because a
     * disjunction is decomposed into sub-queries and each sub-query is handled
     * in parallel.
     * 
     * @return the time taken to query all the peers
     */
    public long getQueryDatastoreTime() {
        return this.queryDatastoreTime;
    }

    /**
     * Returns the take taken (in ms) to send the request and to receive the
     * response.
     * 
     * @return the take taken (in ms) to send the request and to receive the
     *         response.
     */
    public long getLatency() {
        return this.latency;
    }

    /**
     * Returns the result associated to the initial request.
     * 
     * @return the result associated to the initial request.
     */
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
