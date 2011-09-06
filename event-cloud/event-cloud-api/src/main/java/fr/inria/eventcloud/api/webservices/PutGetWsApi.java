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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.api.webservices;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.responses.SparqlAskResponse;
import fr.inria.eventcloud.api.responses.SparqlConstructResponse;
import fr.inria.eventcloud.api.responses.SparqlDescribeResponse;
import fr.inria.eventcloud.api.responses.SparqlResponse;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;

/**
 * Defines the synchronous operations that can be executed on an Event-Cloud and
 * can be exposed as web services by a put/get proxy component.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public interface PutGetWsApi {

    /**
     * Inserts the specified quadruple into the Event-Cloud.
     * 
     * @param quad
     *            the quadruple to insert into the Event-Cloud.
     * 
     * @return {@code true} if the operation has succeeded, {@code false}
     *         otherwise.
     */
    public boolean addQuadruple(Quadruple quad);

    /**
     * Loads the specified collection of quadruples into the Event-Cloud in
     * parallel.
     * 
     * @param quads
     *            the quadruples to insert into the Event-Cloud.
     * 
     * @return {@code true} if the operation has succeeded, {@code false}
     *         otherwise.
     */
    public boolean addQuadrupleCollection(Collection<Quadruple> quads);

    /**
     * Indicates whether the specified quadruples is contained by the
     * Event-Cloud.
     * 
     * @param quad
     *            the quadruple whose presence in Event-Cloud is to be tested.
     * 
     * @return {@code true} if the operation has succeeded, {@code false}
     *         otherwise.
     */
    public boolean containsQuadruple(Quadruple quad);

    /**
     * Deletes the specified quadruple from the Event-Cloud.
     * 
     * @param quad
     *            the quadruple to remove from the Event-Cloud.
     * 
     * @return {@code true} if the operation has succeeded, {@code false}
     *         otherwise.
     */
    public boolean deleteQuadruple(Quadruple quad);

    /**
     * Deletes the specified quadruples from the Event-Cloud.
     * 
     * @param quads
     *            the collection of quadruples to remove from the Event-Cloud.
     * 
     * @return {@code true} if the operation has succeeded, {@code false}
     *         otherwise.
     */
    public boolean deleteQuadrupleCollection(Collection<Quadruple> quads);

    /**
     * Deletes from the Event-Cloud the quadruples that match the specified
     * quadruple pattern.
     * 
     * @param quadPattern
     *            the quadruple pattern that is used to remove the quadruples.
     * 
     * @return the quadruples that have been removed from the Event-Cloud
     *         according to the quadruple pattern.
     */
    public Collection<Quadruple> deleteQuadruplePattern(QuadruplePattern quadPattern);

    /**
     * Finds all the quadruples that match the specified quadruple pattern.
     * 
     * @param quadPattern
     *            the quadruple pattern to be tested.
     * 
     * @return the quadruples that match the quadruple pattern that has been
     *         specified.
     */
    public Collection<Quadruple> findQuadruplePattern(QuadruplePattern quadPattern);

    /**
     * Executes on the Event-Cloud the specified SPARQL query. This SPARQL query
     * can have any query form.
     * 
     * @param sparqlQuery
     *            the SPARQL query to execute.
     * 
     * @return a response according the query form that has been executed.
     */
    public SparqlResponse<?> executeSparqlQuery(String sparqlQuery);

    /**
     * Executes on the Event-Cloud the specified SPARQL query that uses a ASK
     * query form.
     * 
     * @param sparqlAskQuery
     *            the SPARQL query to execute.
     * 
     * @return a response according the query form that has been executed.
     */
    public SparqlAskResponse executeSparqlAsk(String sparqlAskQuery);

    /**
     * Executes on the Event-Cloud the specified SPARQL query that uses a
     * CONSTRUCT query form.
     * 
     * @param sparqlConstructQuery
     *            the SPARQL query to execute.
     * 
     * @return a response according the query form that has been executed.
     */
    public SparqlConstructResponse executeSparqlConstruct(String sparqlConstructQuery);

    /**
     * Executes on the Event-Cloud the specified SPARQL query that uses a
     * DESCRIBE query form.
     * 
     * @param sparqlDescribeQuery
     *            the SPARQL query to execute.
     * 
     * @return a response according the query form that has been executed.
     */
    public SparqlDescribeResponse executeSparqlDescribe(String sparqlDescribeQuery);

    /**
     * Executes on the Event-Cloud the specified SPARQL query that uses a SELECT
     * query form.
     * 
     * @param sparqlSelectQuery
     *            the SPARQL query to execute.
     * 
     * @return a response according the query form that has been executed.
     */
    public SparqlSelectResponse executeSparqlSelect(String sparqlSelectQuery);

}
