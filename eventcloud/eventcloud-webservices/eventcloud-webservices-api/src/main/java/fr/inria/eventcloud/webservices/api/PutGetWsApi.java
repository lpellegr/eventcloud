/**
 * Copyright (c) 2011-2012 INRIA.
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
package fr.inria.eventcloud.webservices.api;

import java.util.Collection;
import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.responses.SparqlAskResponse;
import fr.inria.eventcloud.api.responses.SparqlConstructResponse;
import fr.inria.eventcloud.api.responses.SparqlDescribeResponse;
import fr.inria.eventcloud.api.responses.SparqlResponse;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
import fr.inria.eventcloud.webservices.api.adapters.QuadrupleAdapter;
import fr.inria.eventcloud.webservices.api.adapters.QuadrupleCollectionAdapter;
import fr.inria.eventcloud.webservices.api.adapters.QuadruplePatternAdapter;
import fr.inria.eventcloud.webservices.api.adapters.SparqlAskResponseAdapter;
import fr.inria.eventcloud.webservices.api.adapters.SparqlConstructResponseAdapter;
import fr.inria.eventcloud.webservices.api.adapters.SparqlDescribeResponseAdapter;
import fr.inria.eventcloud.webservices.api.adapters.SparqlResponseAdapter;
import fr.inria.eventcloud.webservices.api.adapters.SparqlSelectResponseAdapter;

/**
 * Defines the synchronous operations that can be executed on an Event-Cloud and
 * can be exposed as web services by a put/get proxy component.
 * 
 * @author lpellegr
 * @author bsauvan
 */
@WebService(serviceName = "EventCloudPutGet", portName = "EventCloudPutGetPort", name = "EventCloudPutGetPortType", targetNamespace = "http://webservices.eventcloud.inria.fr/")
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
    @WebMethod
    public boolean addQuadruple(@WebParam(name = "quad") @XmlJavaTypeAdapter(QuadrupleAdapter.class) Quadruple quad);

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
    @WebMethod
    public boolean addQuadrupleCollection(@WebParam(name = "quads") @XmlJavaTypeAdapter(QuadrupleCollectionAdapter.class) Collection<Quadruple> quads);

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
    @WebMethod
    public boolean containsQuadruple(@WebParam(name = "quad") @XmlJavaTypeAdapter(QuadrupleAdapter.class) Quadruple quad);

    /**
     * Deletes the specified quadruple from the Event-Cloud.
     * 
     * @param quad
     *            the quadruple to remove from the Event-Cloud.
     * 
     * @return {@code true} if the operation has succeeded, {@code false}
     *         otherwise.
     */
    @WebMethod
    public boolean deleteQuadruple(@WebParam(name = "quad") @XmlJavaTypeAdapter(QuadrupleAdapter.class) Quadruple quad);

    /**
     * Deletes the specified quadruples from the Event-Cloud.
     * 
     * @param quads
     *            the collection of quadruples to remove from the Event-Cloud.
     * 
     * @return {@code true} if the operation has succeeded, {@code false}
     *         otherwise.
     */
    @WebMethod
    public boolean deleteQuadrupleCollection(@WebParam(name = "quads") @XmlJavaTypeAdapter(QuadrupleCollectionAdapter.class) Collection<Quadruple> quads);

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
    @WebMethod
    @XmlJavaTypeAdapter(QuadrupleCollectionAdapter.class)
    public List<Quadruple> deleteQuadruplePattern(@WebParam(name = "quadPattern") @XmlJavaTypeAdapter(QuadruplePatternAdapter.class) QuadruplePattern quadPattern);

    /**
     * Finds all the quadruples that match the specified quadruple pattern.
     * 
     * @param quadPattern
     *            the quadruple pattern to be tested.
     * 
     * @return the quadruples that match the quadruple pattern that has been
     *         specified.
     */
    @WebMethod
    @XmlJavaTypeAdapter(QuadrupleCollectionAdapter.class)
    public List<Quadruple> findQuadruplePattern(@WebParam(name = "quadPattern") @XmlJavaTypeAdapter(QuadruplePatternAdapter.class) QuadruplePattern quadPattern);

    /**
     * Executes on the Event-Cloud the specified SPARQL query. This SPARQL query
     * can have any query form.
     * 
     * @param sparqlQuery
     *            the SPARQL query to execute.
     * 
     * @return a response according the query form that has been executed.
     */
    @WebMethod
    @XmlJavaTypeAdapter(SparqlResponseAdapter.class)
    public SparqlResponse<?> executeSparqlQuery(@WebParam(name = "sparqlQuery") String sparqlQuery);

    /**
     * Executes on the Event-Cloud the specified SPARQL query that uses a ASK
     * query form.
     * 
     * @param sparqlAskQuery
     *            the SPARQL query to execute.
     * 
     * @return a response according the query form that has been executed.
     */
    @WebMethod
    @XmlJavaTypeAdapter(SparqlAskResponseAdapter.class)
    public SparqlAskResponse executeSparqlAsk(@WebParam(name = "sparqlAskQuery") String sparqlAskQuery);

    /**
     * Executes on the Event-Cloud the specified SPARQL query that uses a
     * CONSTRUCT query form.
     * 
     * @param sparqlConstructQuery
     *            the SPARQL query to execute.
     * 
     * @return a response according the query form that has been executed.
     */
    @WebMethod
    @XmlJavaTypeAdapter(SparqlConstructResponseAdapter.class)
    public SparqlConstructResponse executeSparqlConstruct(@WebParam(name = "sparqlConstructQuery") String sparqlConstructQuery);

    /**
     * Executes on the Event-Cloud the specified SPARQL query that uses a
     * DESCRIBE query form.
     * 
     * @param sparqlDescribeQuery
     *            the SPARQL query to execute.
     * 
     * @return a response according the query form that has been executed.
     */
    @WebMethod
    @XmlJavaTypeAdapter(SparqlDescribeResponseAdapter.class)
    public SparqlDescribeResponse executeSparqlDescribe(@WebParam(name = "sparqlDescribeQuery") String sparqlDescribeQuery);

    /**
     * Executes on the Event-Cloud the specified SPARQL query that uses a SELECT
     * query form.
     * 
     * @param sparqlSelectQuery
     *            the SPARQL query to execute.
     * 
     * @return a response according the query form that has been executed.
     */
    @WebMethod
    @XmlJavaTypeAdapter(SparqlSelectResponseAdapter.class)
    public SparqlSelectResponse executeSparqlSelect(@WebParam(name = "sparqlSelectQuery") String sparqlSelectQuery);

}