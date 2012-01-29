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

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * Defines the operations which are exposed as a web service for managing
 * eventclouds.
 * 
 * @author lpellegr
 */
@WebService(serviceName = "EventCloudManagement", portName = "EventCloudManagementPort", targetNamespace = "http://webservices.eventcloud.inria.fr/", name = "EventCloudManagementPortType")
public interface EventCloudManagementWsApi {

    /**
     * Creates and deploys an eventcloud. Once it is running, an URL identifying
     * the eventcloud which has been created is returned.
     * 
     * @return an URL identifying the eventcloud which has been created and
     *         deployed.
     */
    @WebMethod
    String createEventCloud();

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
     * Creates, deploys and exposes as a web service a publish proxy for the
     * eventcloud identified by the specified {@code eventcloudId}. When the
     * call succeeds, the endpoint URL to the publish web service is returned.
     * 
     * @param eventcloudIdUrl
     *            an URL identifying an eventcloud which is running and for
     *            which a publish proxy will be created and deployed.
     * 
     * @return the endpoint URL to the publish web service which is deployed.
     */
    @WebMethod
    String createPublishProxy(@WebParam(name = "eventcloudIdUrl") String eventcloudIdUrl);

    /**
     * Creates, deploys and exposes as a web service a subscribe proxy for the
     * eventcloud identified by the specified {@code eventcloudId}. When the
     * call succeeds, the endpoint URL to the subscribe web service is returned.
     * 
     * @param eventcloudIdUrl
     *            an URL identifying an eventcloud which is running and for
     *            which a subscribe proxy will be created and deployed.
     * 
     * @return the endpoint URL to the subscribe web service which is deployed.
     */
    @WebMethod
    String createSubscribeProxy(@WebParam(name = "eventcloudIdUrl") String eventcloudIdUrl);

    /**
     * Creates, deploys and exposes as a web service a putget proxy for the
     * eventcloud identified by the specified {@code eventcloudId}. When the
     * call succeeds, the endpoint URL to the putget web service is returned.
     * 
     * @param eventcloudIdUrl
     *            an URL identifying an eventcloud which is running and for
     *            which a putget proxy will be created and deployed.
     * 
     * @return the endpoint URL to the putget web service which is deployed.
     */
    @WebMethod
    String createPutGetProxy(@WebParam(name = "eventcloudIdUrl") String eventcloudIdUrl);

}
