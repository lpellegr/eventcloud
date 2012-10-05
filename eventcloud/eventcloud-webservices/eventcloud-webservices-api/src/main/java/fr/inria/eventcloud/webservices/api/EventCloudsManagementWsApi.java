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

import java.util.List;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 * Defines the operations which are exposed as a web service for managing
 * EventClouds and their associated services and proxies.
 * 
 * @author lpellegr
 * @author bsauvan
 */
@WebService(serviceName = "EventCloudManagement", portName = "EventCloudManagementPort", targetNamespace = "http://webservices.eventcloud.inria.fr/", name = "EventCloudManagementPortType")
public interface EventCloudsManagementWsApi {

    /**
     * Returns the endpoint URL associated to the EventClouds registry which
     * knows what are the EventClouds that are manageable.
     * 
     * @return the endpoint URL associated to the EventClouds registry which
     *         knows what are the EventClouds that are manageable.
     */
    @WebMethod
    public String getRegistryEndpointUrl();

    /**
     * Creates a new EventCloud for the specified {@code streamUrl}.
     * 
     * @param streamUrl
     *            an URL which identifies an EventCloud among an organization.
     * 
     * @return a boolean which indicates whether an EventCloud has been created
     *         for the specified {@code streamUrl} or not. In the former case,
     *         when the return value is {@code false}, this means that an
     *         EventCloud is already running for the specified {@code streamUrl}
     *         .
     */
    @WebMethod
    public boolean createEventCloud(@WebParam(name = "streamUrl") String streamUrl);

    /**
     * Indicates whether an EventCloud exists or not according to its streamUrl.
     * 
     * @param streamUrl
     *            an URL which identifies an EventCloud among an organization.
     * 
     * @return {@code true} if the EventCloud identified by the specified
     *         streamUrl is already created, {@code false} otherwise.
     */
    @WebMethod
    public boolean isCreated(@WebParam(name = "streamUrl") String streamUrl);

    /**
     * Returns a list that contains the URLs of the streams/EventClouds which
     * are managed by the EventClouds registry used by this web service.
     * 
     * @return a list that contains the URLs of the streams which are managed by
     *         the EventClouds registry used by this web service.
     */
    @WebMethod
    public List<String> getEventCloudIds();

    /**
     * Detroys the EventCloud identified by {@code streamUrl}.
     * 
     * @param streamUrl
     *            an URL which identifies an EventCloud among an organization.
     * 
     * @return a boolean which indicates whether the EventCloud has been
     *         destroyed or not. A return value equals to {@code false} probably
     *         means that there was no EventCloud found with the specified
     *         {@code streamUrl}.
     */
    @WebMethod
    public boolean destroyEventCloud(@WebParam(name = "streamUrl") String streamUrl);

    /**
     * Deploys a new {@link PublishWsnApi publish WS-Notification service} for
     * the EventCloud identified by the specified {@code streamUrl}. When the
     * call succeeds, the endpoint URL to the {@link PublishWsnApi publish
     * WS-Notification service} is returned.
     * 
     * @param streamUrl
     *            an URL which identifies an EventCloud which is running.
     * 
     * @return the endpoint URL to the {@link PublishWsnApi publish
     *         WS-Notification service} which is deployed or {@code null} if
     *         there was no existing EventCloud for the specified
     *         {@code streamUrl}.
     */
    @WebMethod
    public String deployPublishWsnService(@WebParam(name = "streamUrl") String streamUrl);

    /**
     * Deploys a new {@link SubscribeWsnApi subscribe WS-Notification service}
     * for the EventCloud identified by the specified {@code streamUrl}. When
     * the call succeeds, the endpoint URL to the {@link SubscribeWsnApi
     * subscribe WS-Notification service} is returned.
     * 
     * @param streamUrl
     *            an URL which identifies an EventCloud which is running.
     * 
     * @return the endpoint URL to the {@link SubscribeWsnApi subscribe
     *         WS-Notification service} which is deployed or {@code null} if
     *         there was no existing EventCloud for the specified
     *         {@code streamUrl}.
     */
    @WebMethod
    public String deploySubscribeWsnService(@WebParam(name = "streamUrl") String streamUrl);

    /**
     * Deploys a new {@link PublishWsApi publish web service proxy} for the
     * EventCloud identified by the specified {@code streamUrl}. When the call
     * succeeds, the endpoint URL to the {@link PublishWsApi publish web service
     * proxy} is returned.
     * 
     * @param streamUrl
     *            an URL which identifies an EventCloud which is running.
     * 
     * @return the endpoint URL to the {@link PublishWsApi publish web service
     *         proxy} which is deployed or {@code null} if there was no existing
     *         EventCloud for the specified {@code streamUrl}.
     */
    @WebMethod
    public String deployPublishWsProxy(@WebParam(name = "streamUrl") String streamUrl);

    /**
     * Deploys a new {@link SubscribeWsApi subscribe web service proxy} for the
     * EventCloud identified by the specified {@code streamUrl}. When the call
     * succeeds, the endpoint URL to the {@link SubscribeWsApi subscribe web
     * service proxy} is returned.
     * 
     * @param streamUrl
     *            an URL which identifies an EventCloud which is running.
     * 
     * @return the endpoint URL to the {@link SubscribeWsApi subscribe web
     *         service proxy} which is deployed or {@code null} if there was no
     *         existing EventCloud for the specified {@code streamUrl}.
     */
    @WebMethod
    public String deploySubscribeWsProxy(@WebParam(name = "streamUrl") String streamUrl);

    /**
     * Deploys a new {@link PutGetWsApi put/get web service proxy} for the
     * EventCloud identified by the specified {@code streamUrl}. When the call
     * succeeds, the endpoint URL to the {@link PutGetWsApi put/get web service
     * proxy} is returned.
     * 
     * @param streamUrl
     *            an URL which identifies an EventCloud which is running.
     * 
     * @return the endpoint URL to the {@link PutGetWsApi put/get web service
     *         proxy} which is deployed or {@code null} if there was no existing
     *         EventCloud for the specified {@code streamUrl}.
     */
    @WebMethod
    public String deployPutGetWsProxy(@WebParam(name = "streamUrl") String streamUrl);

    /**
     * Returns the endpoint URLs for the {@link PublishWsnApi publish
     * WS-Notification services} which have been created for the specified
     * {@code streamUrl}.
     * 
     * @param streamUrl
     *            an URL which identifies an EventCloud which is running.
     * 
     * @return the endpoint URLs for the {@link PublishWsnApi publish
     *         WS-Notification services} which have been created for the
     *         specified {@code streamUrl}.
     */
    @WebMethod
    public List<String> getPublishWsnServiceEndpointUrls(@WebParam(name = "streamUrl") String streamUrl);

    /**
     * Returns the endpoint URLs for the {@link SubscribeWsnApi subscribe
     * WS-Notification services} which have been created for the specified
     * {@code streamUrl}.
     * 
     * @param streamUrl
     *            an URL which identifies an EventCloud which is running.
     * 
     * @return the endpoint URLs for the {@link SubscribeWsnApi subscribe
     *         WS-Notification services} which have been created for the
     *         specified {@code streamUrl}.
     */
    @WebMethod
    public List<String> getSubscribeWsnServiceEndpointUrls(@WebParam(name = "streamUrl") String streamUrl);

    /**
     * Returns the endpoint URLs for the {@link PublishWsApi publish web service
     * proxies} which have been created for the specified {@code streamUrl}.
     * 
     * @param streamUrl
     *            an URL which identifies an EventCloud which is running.
     * 
     * @return the endpoint URLs for the {@link PublishWsApi publish web service
     *         proxies} which have been created for the specified
     *         {@code streamUrl}.
     */
    @WebMethod
    public List<String> getPublishWsProxyEndpointUrls(@WebParam(name = "streamUrl") String streamUrl);

    /**
     * Returns the endpoint URLs for the {@link SubscribeWsApi subscribe web
     * service proxy} which have been created for the specified
     * {@code streamUrl}.
     * 
     * @param streamUrl
     *            an URL which identifies an EventCloud which is running.
     * 
     * @return the endpoint URLs for the {@link SubscribeWsApi subscribe web
     *         service proxy} which have been created for the specified
     *         {@code streamUrl}.
     */
    @WebMethod
    public List<String> getSubscribeWsProxyEndpointUrls(@WebParam(name = "streamUrl") String streamUrl);

    /**
     * Returns the endpoint URLs for the {@link PutGetWsApi put/get web service
     * proxies} which have been created for the specified {@code streamUrl}.
     * 
     * @param streamUrl
     *            an URL which identifies an EventCloud which is running.
     * 
     * @return the endpoint URLs for the {@link PutGetWsApi put/get web service
     *         proxies} which have been created for the specified
     *         {@code streamUrl}.
     */
    @WebMethod
    public List<String> getPutGetWsProxyEndpointUrls(@WebParam(name = "streamUrl") String streamUrl);

    /**
     * Destroys the {@link PublishWsnApi publish WS-Notification service}
     * identified by {@code publishWsnEndpointUrl}.
     * 
     * @param publishWsnEndpointUrl
     *            the endpoint URL of the {@link PublishWsnApi publish
     *            WS-Notification service} to destroy.
     * 
     * @return {@code true} if the {@link PublishWsnApi publish WS-Notification
     *         service} has been destroyed, {@code false} otherwise.
     */
    @WebMethod
    public boolean destroyPublishWsnService(@WebParam(name = "publishWsnEndpointUrl") String publishWsnEndpointUrl);

    /**
     * Destroys the {@link SubscribeWsnApi subscribe WS-Notification service}
     * identified by {@code subscribeWsnEndpointUrl}.
     * 
     * @param subscribeWsnEndpointUrl
     *            the endpoint URL of the {@link SubscribeWsnApi subscribe
     *            WS-Notification service} to destroy.
     * 
     * @return {@code true} if the {@link SubscribeWsnApi subscribe
     *         WS-Notification service} has been destroyed, {@code false}
     *         otherwise.
     */
    @WebMethod
    public boolean destroySubscribeWsnService(@WebParam(name = "subscribeWsnEndpointUrl") String subscribeWsnEndpointUrl);

    /**
     * Destroys the {@link PublishWsApi publish web service proxy} identified by
     * {@code publishWsProxyEndpointUrl}.
     * 
     * @param publishWsProxyEndpointUrl
     *            the endpoint URL of the {@link PublishWsApi publish web
     *            service proxy} to destroy.
     * 
     * @return {@code true} if the {@link PublishWsApi publish web service
     *         proxy} has been destroyed, {@code false} otherwise.
     */
    @WebMethod
    public boolean destroyPublishWsProxy(@WebParam(name = "publishWsProxyEndpointUrl") String publishWsProxyEndpointUrl);

    /**
     * Destroys the {@link SubscribeWsApi subscribe web service proxy}
     * identified by {@code subscribeWsProxyEndpointUrl}.
     * 
     * @param subscribeWsProxyEndpointUrl
     *            the endpoint URL of the {@link SubscribeWsApi subscribe web
     *            service proxy} to destroy.
     * 
     * @return {@code true} if the {@link SubscribeWsApi subscribe web service
     *         proxy} has been destroyed, {@code false} otherwise.
     */
    @WebMethod
    public boolean destroySubscribeWsProxy(@WebParam(name = "subscribeWsProxyEndpointUrl") String subscribeWsProxyEndpointUrl);

    /**
     * Destroys the {@link PutGetWsApi put/get web service proxy} identified by
     * {@code putgetWsProxyEndpointUrl}.
     * 
     * @param putgetWsProxyEndpointUrl
     *            the endpoint URL of the {@link PutGetWsApi put/get web service
     *            proxy} to destroy.
     * 
     * @return {@code true} if the {@link PutGetWsApi put/get web service proxy}
     *         has been destroyed, {@code false} otherwise.
     */
    @WebMethod
    public boolean destroyPutGetWsProxy(@WebParam(name = "publishProxyEndpoint") String putgetWsProxyEndpointUrl);

}
