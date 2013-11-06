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

import com.google.common.base.Objects;

/**
 * Store statistics about the execution of a SPARQL query.
 * 
 * @author lpellegr
 */
public class SparqlQueryStatistics implements Serializable {

    private static final long serialVersionUID = 160L;

    private final int nbSubQueries;

    private final long cumulativeInboundHopCount;

    private final long cumulativeOutboundHopCount;

    private final long cumulativeTimeToExecuteSubQueries;

    private final long cumulativeTimeToQueryDatastores;

    private final long timeToCombineSubQueries;

    private final long networkExecutionTime;

    /**
     * Constructor.
     * 
     * @param nbSubQueries
     *            the number of sub queries executed.
     * @param cumulativeInboundHopCount
     *            the cumulative number of peers traversed by the sub queries in
     *            the forward direction.
     * @param cumulativeOutboundHopCount
     *            the cumulative number of peers traversed by the sub queries in
     *            the backward direction.
     * @param cumulativeTimeToExecuteSubQueries
     *            the cumulative time (in ms) to execute sub queries.
     * @param cumulativetimeToQueryDatastores
     *            the cumulative time (in nanoseconds) to query all the peers
     * @param timeToCombineSubQueries
     *            time (in ms) to combine all sub queries with a final filtering
     * @param networkExecutionTime
     *            the time the time taken (in ms) to execute the SPARQL query on
     *            the EC.
     */
    public SparqlQueryStatistics(int nbSubQueries,
            long cumulativeInboundHopCount, long cumulativeOutboundHopCount,
            long cumulativeTimeToExecuteSubQueries,
            long cumulativetimeToQueryDatastores, long timeToCombineSubQueries,
            long networkExecutionTime) {
        this.nbSubQueries = nbSubQueries;
        this.cumulativeInboundHopCount = cumulativeInboundHopCount;
        this.cumulativeOutboundHopCount = cumulativeOutboundHopCount;
        this.cumulativeTimeToExecuteSubQueries =
                cumulativeTimeToExecuteSubQueries;
        this.cumulativeTimeToQueryDatastores = cumulativetimeToQueryDatastores;
        this.timeToCombineSubQueries = timeToCombineSubQueries;
        this.networkExecutionTime = networkExecutionTime;
    }

    /**
     * Returns the number of sub queries executed.
     * 
     * @return the number of sub queries executed.
     */
    public int getNbSubQueries() {
        return this.nbSubQueries;
    }

    /**
     * Returns the cumulative number of peers traversed by the sub queries in
     * the forward direction.
     * 
     * @return the number of peers traversed by the sub queries in the forward
     *         direction.
     */
    public long getCumulativeInboundHopCount() {
        return this.cumulativeInboundHopCount;
    }

    /**
     * Returns the cumulative number of peers traversed by the sub queries in
     * the backward direction.
     * 
     * @return the number of peers traversed by the sub queries in the backward
     *         direction.
     */
    public long getCumulativeOutboundHopCount() {
        return this.cumulativeOutboundHopCount;
    }

    /**
     * Returns the cumulative time (in ms) to execute sub queries.
     * 
     * @return the cumulative time (in ms) to execute sub queries.
     */
    public long getCumulativeTimeToExecuteSubQueries() {
        return this.cumulativeTimeToExecuteSubQueries;
    }

    /**
     * Returns the cumulative time (in nanoseconds) to query all the peers. This
     * value is the sum of the time taken to execute the query on each peer.
     * Hence, if you have a disjunctive query, it is possible to have a
     * {@code queryDatastoreTime} greater than the query latency because a
     * disjunction is decomposed into sub-queries and each sub-query is handled
     * in parallel.
     * 
     * @return the time taken to query all the peers.
     */
    public long getCumulativeTimeToQueryDatastores() {
        return this.cumulativeTimeToQueryDatastores;
    }

    /**
     * The time (in ms) to combine all sub queries with a final filtering.
     * 
     * @return the time (in ms) to combine all sub queries with a final
     *         filtering.
     */
    public long getTimeToCombineSubQueries() {
        return this.timeToCombineSubQueries;
    }

    /**
     * Returns the time taken (in ms) to execute the SPARQL query on the EC.
     * 
     * @return the time the time taken (in ms) to execute the SPARQL query on
     *         the EC.
     */
    public long getNetworkExecutionTime() {
        return this.networkExecutionTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("nbSubQueries", this.nbSubQueries)
                .add(
                        "cumulativeInboundHopCount",
                        this.cumulativeInboundHopCount)
                .add(
                        "cumulativeOutboundHopCount",
                        this.cumulativeOutboundHopCount)
                .add(
                        "cumulativeTimeToExecuteSubQueries",
                        this.cumulativeTimeToExecuteSubQueries)
                .add(
                        "cumulativeTimeToQueryDatastores",
                        this.cumulativeTimeToQueryDatastores)
                .add("timeToCombineSubQueries", this.timeToCombineSubQueries)
                .add("networkExecutionTime", this.networkExecutionTime)
                .toString();
    }

}
