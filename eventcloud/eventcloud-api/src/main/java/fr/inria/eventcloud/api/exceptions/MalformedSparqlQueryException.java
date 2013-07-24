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
package fr.inria.eventcloud.api.exceptions;

/**
 * Exception thrown when a malformed SPARQL query is detected.
 * 
 * @author lpellegr
 */
public class MalformedSparqlQueryException extends Exception {

    private static final long serialVersionUID = 160L;

    private final String sparqlQuery;

    public MalformedSparqlQueryException(String sparqlQuery) {
        this(sparqlQuery, null);
    }

    public MalformedSparqlQueryException(String sparqlQuery, Throwable cause) {
        super("Malformed SPARQL query: " + sparqlQuery, cause);
        this.sparqlQuery = sparqlQuery;
    }

    /**
     * Returns the malformed SPARQL query.
     * 
     * @return the malformed SPARQL query.
     */
    public String getSparqlQuery() {
        return this.sparqlQuery;
    }

}
