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
 * A SPARQL response that maintain information which are commons to any SPARQL
 * response.
 * 
 * @author lpellegr
 * 
 * @param <T>
 *            the result type.
 */
public abstract class SparqlResponse<T> implements Serializable {

    private static final long serialVersionUID = 160L;

    private final SparqlQueryStatistics stats;

    private final T result;

    public SparqlResponse(SparqlQueryStatistics stats, T result) {
        super();
        this.stats = stats;
        this.result = result;
    }

    /**
     * 
     * 
     * @return the stats
     */
    public SparqlQueryStatistics getStats() {
        return this.stats;
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
        return Objects.toStringHelper(this)
                .add("stats", this.stats.toString())
                .toString();
    }

}
