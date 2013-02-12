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

import java.util.Map;

import fr.inria.eventcloud.api.wrappers.ResultSetWrapper;

/**
 * The response returned for a Select SPARQL query form that has been executed.
 * 
 * @author lpellegr
 * @author mantoine
 */
public class SparqlSelectResponse extends SparqlResponse<ResultSetWrapper> {

    private static final long serialVersionUID = 140L;

    // the following fields are used exclusively for benchmarking purposes
    private int nbIntermediateResults, nbSubQueries;
    private long timeToGetResult, sizeOfIntermediateResultsInBytes;
    private Map<String, Integer> mapSubQueryNbResults;

    public SparqlSelectResponse(long inboundHopCount, long outboundHopCount,
            long latency, long queryDatastoreTime, ResultSetWrapper result) {
        super(inboundHopCount, outboundHopCount, latency, queryDatastoreTime,
                result);
    }

    public int getNbIntermediateResults() {
        return this.nbIntermediateResults;
    }

    public void setNbIntermediateResults(int nbIntermediateResults) {
        this.nbIntermediateResults = nbIntermediateResults;
    }

    public long getTimeToGetResult() {
        return this.timeToGetResult;
    }

    public void setTimeToGetResult(long timeToGetResult) {
        this.timeToGetResult = timeToGetResult;
    }

    public int getNbSubQueries() {
        return this.nbSubQueries;
    }

    public void setNbSubQueries(int nbSubQueries) {
        this.nbSubQueries = nbSubQueries;
    }

    /**
     * Returns the size (in bytes) of all the quadruples contained in the
     * intermediate results for this SparqlSelectResponse.
     * 
     * @return the size (in bytes) of all the quadruples contained in the
     *         intermediate results for this SparqlSelectResponse.
     */
    public long getSizeOfIntermediateResultsInBytes() {
        return this.sizeOfIntermediateResultsInBytes;
    }

    public void setSizeOfIntermediateResultsInBytes(long sizeOfIntermediateResultsInBytes) {
        this.sizeOfIntermediateResultsInBytes =
                sizeOfIntermediateResultsInBytes;
    }

    /**
     * Returns the number of results for each subquery of the query for this
     * response.
     * 
     * @return gives the number of results for each subquery of the query for
     *         this response.
     */
    public Map<String, Integer> getMapSubQueryNbResults() {
        return this.mapSubQueryNbResults;
    }

    public void setMapSubQueryNbResults(Map<String, Integer> mapSubQueryNbResults) {
        this.mapSubQueryNbResults = mapSubQueryNbResults;
    }

}
