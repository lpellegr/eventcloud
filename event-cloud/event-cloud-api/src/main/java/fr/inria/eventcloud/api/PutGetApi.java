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
package fr.inria.eventcloud.api;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.api.responses.SparqlAskResponse;
import fr.inria.eventcloud.api.responses.SparqlConstructResponse;
import fr.inria.eventcloud.api.responses.SparqlDescribeResponse;
import fr.inria.eventcloud.api.responses.SparqlResponse;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;

/**
 * Defines the synchronous operations that can be executed on an Event-Cloud.
 * The boolean value which is returned for some of the methods is used to ensure
 * that ProActive calls are synchronous.
 * 
 * @author lpellegr
 */
public interface PutGetApi {

    /**
     * Inserts the specified quadruple into the Event-Cloud.
     * 
     * @param quad
     *            the quadruple to insert into the Event-Cloud.
     * 
     * @return {@code true} if the operation has succeeded, {@code false}
     *         otherwise.
     */
    public boolean add(Quadruple quad);

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
    public boolean add(Collection<Quadruple> quads);

    /**
     * Publishes the quadruples that are read from the specified input stream.
     * The input stream is assumed to comply with the <a
     * href="http://www4.wiwiss.fu-berlin.de/bizer/TriG/">TriG</a> or <a
     * href="http://sw.deri.org/2008/07/n-quads/">N-Quads</a> syntax.
     * 
     * @param in
     *            the input stream from where the quadruples are read.
     * 
     * @param format
     *            the format that is used to read the data from the input
     *            stream.
     */
    public boolean add(InputStream in, SerializationFormat format);

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
    public boolean contains(Quadruple quad);

    /**
     * Deletes the specified quadruple from the Event-Cloud.
     * 
     * @param quad
     *            the quadruple to remove from the Event-Cloud.
     * 
     * @return {@code true} if the operation has succeeded, {@code false}
     *         otherwise.
     */
    public boolean delete(Quadruple quad);

    /**
     * Deletes the specified quadruples from the Event-Cloud.
     * 
     * @param quads
     *            the collection of quadruples to remove from the Event-Cloud.
     * 
     * @return {@code true} if the operation has succeeded, {@code false}
     *         otherwise.
     */
    public boolean delete(Collection<Quadruple> quads);

    /**
     * Deletes from the Event-Cloud the quadruples that match the specified
     * quadruple pattern.
     * 
     * @param quadPattern
     *            the quadruple pattern that is used to remove the quadruples.
     * 
     * @return the quadruples which have been removed from the Event-Cloud
     *         according to the quadruple pattern.
     */
    public List<Quadruple> delete(QuadruplePattern quadPattern);

    /**
     * Returns the number of quadruples that match the specified
     * {@code quadPattern}.
     * 
     * @param quadPattern
     *            the quadruple pattern to use.
     * 
     * @return the number of quadruples that match the specified
     *         {@code quadPattern}.
     */
    public long count(QuadruplePattern quadPattern);

    /**
     * Returns the number of solutions for the specified {@code sparqlQuery}.
     * 
     * @param sparqlQuery
     *            the sparqlQuery to execute.
     * 
     * @return the number of solutions for the specified {@code sparqlQuery}.
     */
    public long count(String sparqlQuery);

    /**
     * Finds all the quadruples that match the specified quadruple pattern.
     * 
     * @param quadPattern
     *            the quadruple pattern to be tested.
     * 
     * @return the quadruples that match the quadruple pattern that has been
     *         specified.
     */
    public List<Quadruple> find(QuadruplePattern quadPattern);

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
