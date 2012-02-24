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
package fr.inria.eventcloud.webservices.api;

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * Defines the operations which are exposed as a web service for managing
 * eventclouds and their associated proxies.
 * 
 * @author lpellegr
 */
@WebService(serviceName = "EventCloudManagement", portName = "EventCloudManagementPort", targetNamespace = "http://webservices.eventcloud.inria.fr/", name = "EventCloudManagementPortType")
public interface EventCloudManagementWsApi {

    /**
     * Creates and deploys a new eventcloud for the specified {@code streamUrl}.
     * 
     * @param streamUrl
     *            an URL which identifies an eventcloud among an organization.
     * 
     * @return a boolean which indicates whether an eventcloud has been created
     *         for the specified {@code streamUrl} or not. In the former case,
     *         when the return value is {@code false}, this means that an
     *         eventcloud is already running for the specified {@code streamUrl}
     *         .
     */
    @WebMethod
    boolean createEventCloud(@WebParam(name = "streamUrl") String streamUrl);

    /**
     * Returns the endpoint URL associated to the eventclouds registry which
     * knows what are the eventclouds that are manageable.
     * 
     * @return the endpoint URL associated to the eventclouds registry which
     *         knows what are the eventclouds that are manageable.
     */
    @WebMethod
    String getRegistryEndpointUrl();

    /**
     * Returns a list that contains the URLs of the streams/eventclouds which
     * are managed by the eventclouds registry used by this webservice.
     * 
     * @return a list that contains the URLs of the streams which are managed by
     *         the eventclouds registry used by this webservice.
     */
    @WebMethod
    List<String> getEventCloudIds();

    /**
     * Creates, deploys and exposes as a web service a publish proxy for the
     * eventcloud identified by the specified {@code streamUrl}. When the call
     * succeeds, the endpoint URL to the publish web service is returned.
     * 
     * @param streamUrl
     *            an URL which identifies an eventcloud which is running.
     * 
     * @return the endpoint URL to the publish web service which is deployed or
     *         {@code null} if there was no existing eventcloud for the
     *         specified {@code streamUrl}.
     */
    @WebMethod
    String createPublishProxy(@WebParam(name = "streamUrl") String streamUrl);

    /**
     * Creates, deploys and exposes as a web service a subscribe proxy for the
     * eventcloud identified by the specified {@code eventcloudId}. When the
     * call succeeds, the endpoint URL to the subscribe web service is returned.
     * 
     * @param streamUrl
     *            an URL which identifies an eventcloud which is running.
     * 
     * @return the endpoint URL to the publish web service which is deployed or
     *         {@code null} if there was no existing eventcloud for the
     *         specified {@code streamUrl}.
     */
    @WebMethod
    String createSubscribeProxy(@WebParam(name = "streamUrl") String streamUrl);

    /**
     * Creates, deploys and exposes as a web service a putget proxy for the
     * eventcloud identified by the specified {@code eventcloudId}. When the
     * call succeeds, the endpoint URL to the putget web service is returned.
     * 
     * @param streamUrl
     *            an URL which identifies an eventcloud which is running.
     * 
     * @return the endpoint URL to the publish web service which is deployed or
     *         {@code null} if there was no existing eventcloud for the
     *         specified {@code streamUrl}.
     */
    @WebMethod
    String createPutGetProxy(@WebParam(name = "streamUrl") String streamUrl);

}
