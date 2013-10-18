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
package fr.inria.eventcloud.api;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.api.exceptions.MalformedSparqlQueryException;
import fr.inria.eventcloud.api.responses.SparqlAskResponse;
import fr.inria.eventcloud.api.responses.SparqlConstructResponse;
import fr.inria.eventcloud.api.responses.SparqlDescribeResponse;
import fr.inria.eventcloud.api.responses.SparqlResponse;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;

/**
 * Defines the synchronous operations that can be executed on an EventCloud. The
 * boolean value which is returned for some of the methods is used to ensure
 * that ProActive calls are synchronous.
 * 
 * @author lpellegr
 */
public interface PutGetApi {

    /**
     * Inserts the specified quadruple into the EventCloud.
     * 
     * @param quad
     *            the quadruple to insert into the EventCloud.
     * 
     * @return {@code true} if the operation has succeeded, {@code false}
     *         otherwise.
     */
    boolean add(Quadruple quad);

    /**
     * Loads the specified collection of quadruples into the EventCloud in
     * parallel.
     * 
     * @param quads
     *            the quadruples to insert into the EventCloud.
     * 
     * @return {@code true} if the operation has succeeded, {@code false}
     *         otherwise.
     */
    boolean add(Collection<Quadruple> quads);

    /**
     * Publishes the quadruples that are read from an input stream opened from
     * the specified URL. The input stream is assumed to comply with the <a
     * href="http://www4.wiwiss.fu-berlin.de/bizer/TriG/">TriG</a> or <a
     * href="http://sw.deri.org/2008/07/n-quads/">N-Quads</a> syntax.
     * 
     * @param url
     *            the URL from where the quadruples are read.
     * 
     * @param format
     *            the format that is used to read the data from the input
     *            stream.
     * @throws IOException
     */
    void add(URL url, SerializationFormat format) throws IOException;

    /**
     * Indicates whether the specified quadruples is contained by the
     * EventCloud.
     * 
     * @param quad
     *            the quadruple whose presence in EventCloud is to be tested.
     * 
     * @return {@code true} if the operation has succeeded, {@code false}
     *         otherwise.
     */
    boolean contains(Quadruple quad);

    /**
     * Deletes the specified quadruple from the EventCloud.
     * 
     * @param quad
     *            the quadruple to remove from the EventCloud.
     * 
     * @return {@code true} if the operation has succeeded, {@code false}
     *         otherwise.
     */
    boolean delete(Quadruple quad);

    /**
     * Deletes the specified quadruples from the EventCloud.
     * 
     * @param quads
     *            the collection of quadruples to remove from the EventCloud.
     * 
     * @return {@code true} if the operation has succeeded, {@code false}
     *         otherwise.
     */
    boolean delete(Collection<Quadruple> quads);

    /**
     * Deletes from the EventCloud the quadruples that match the specified
     * quadruple pattern.
     * 
     * @param quadPattern
     *            the quadruple pattern that is used to remove the quadruples.
     * 
     * @return the quadruples which have been removed from the EventCloud
     *         according to the quadruple pattern.
     */
    List<Quadruple> delete(QuadruplePattern quadPattern);

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
    long count(QuadruplePattern quadPattern);

    /**
     * Returns the number of solutions for the specified {@code sparqlQuery}.
     * 
     * @param sparqlQuery
     *            the sparqlQuery to execute.
     * 
     * @return the number of solutions for the specified {@code sparqlQuery}.
     */
    long count(String sparqlQuery) throws MalformedSparqlQueryException;

    /**
     * Finds all the quadruples that match the specified quadruple pattern.
     * 
     * @param quadPattern
     *            the quadruple pattern to be tested.
     * 
     * @return the quadruples that match the quadruple pattern that has been
     *         specified.
     */
    List<Quadruple> find(QuadruplePattern quadPattern);

    /**
     * Executes on the EventCloud the specified SPARQL query. This SPARQL query
     * can have any query form.
     * 
     * @param sparqlQuery
     *            the SPARQL query to execute.
     * 
     * @return a response according the query form that has been executed.
     */
    SparqlResponse<?> executeSparqlQuery(String sparqlQuery)
            throws MalformedSparqlQueryException;

    /**
     * Executes on the EventCloud the specified SPARQL query that uses a ASK
     * query form.
     * 
     * @param sparqlAskQuery
     *            the SPARQL query to execute.
     * 
     * @return a response according the query form that has been executed.
     */
    SparqlAskResponse executeSparqlAsk(String sparqlAskQuery)
            throws MalformedSparqlQueryException;

    /**
     * Executes on the EventCloud the specified SPARQL query that uses a
     * CONSTRUCT query form.
     * 
     * @param sparqlConstructQuery
     *            the SPARQL query to execute.
     * 
     * @return a response according the query form that has been executed.
     */
    SparqlConstructResponse executeSparqlConstruct(String sparqlConstructQuery)
            throws MalformedSparqlQueryException;

    /**
     * Executes on the EventCloud the specified SPARQL query that uses a
     * DESCRIBE query form.
     * 
     * @param sparqlDescribeQuery
     *            the SPARQL query to execute.
     * 
     * @return a response according the query form that has been executed.
     */
    SparqlDescribeResponse executeSparqlDescribe(String sparqlDescribeQuery)
            throws MalformedSparqlQueryException;

    /**
     * Executes on the EventCloud the specified SPARQL query that uses a SELECT
     * query form.
     * 
     * @param sparqlSelectQuery
     *            the SPARQL query to execute.
     * 
     * @return a response according the query form that has been executed.
     */
    SparqlSelectResponse executeSparqlSelect(String sparqlSelectQuery)
            throws MalformedSparqlQueryException;

}
