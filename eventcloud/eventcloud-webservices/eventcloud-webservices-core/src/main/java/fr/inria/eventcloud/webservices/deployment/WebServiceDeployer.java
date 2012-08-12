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
package fr.inria.eventcloud.webservices.deployment;

import java.net.UnknownHostException;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.oasis_open.docs.wsn.bw_2.NotificationConsumer;
import org.oasis_open.docs.wsn.bw_2.NotificationProducer;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.webservices.WSConstants;
import org.objectweb.proactive.extensions.webservices.component.Utils;
import org.objectweb.proactive.extensions.webservices.component.controller.PAWebServicesController;
import org.objectweb.proactive.extensions.webservices.exceptions.WebServicesException;

import fr.inria.eventcloud.proxies.Proxy;
import fr.inria.eventcloud.proxies.PublishProxy;
import fr.inria.eventcloud.proxies.PutGetProxy;
import fr.inria.eventcloud.proxies.SubscribeProxy;
import fr.inria.eventcloud.webservices.api.EventCloudManagementServiceApi;
import fr.inria.eventcloud.webservices.api.PublishServiceApi;
import fr.inria.eventcloud.webservices.api.PutGetWsApi;
import fr.inria.eventcloud.webservices.api.SubscribeServiceApi;
import fr.inria.eventcloud.webservices.proxies.PublishWsProxyImpl;
import fr.inria.eventcloud.webservices.proxies.PutGetWsProxyImpl;
import fr.inria.eventcloud.webservices.proxies.SubscribeWsProxyImpl;
import fr.inria.eventcloud.webservices.services.EventCloudManagementServiceImpl;
import fr.inria.eventcloud.webservices.services.PublishServiceImpl;
import fr.inria.eventcloud.webservices.services.PutGetServiceImpl;
import fr.inria.eventcloud.webservices.services.SubscribeServiceImpl;
import fr.inria.eventcloud.webservices.services.SubscriberServiceImpl;

/**
 * WsProxyDeployer is used to ease web service operations (expose and unexpose)
 * for proxy components.
 * 
 * @author bsauvan
 */
public class WebServiceDeployer {

    /**
     * Exposes as a web service the {@code publish-webservices} interface of a
     * publish proxy component.
     * 
     * @param proxy
     *            the {@code publish-services} interface of the component owning
     *            the {@code publish-webservices} interface to be exposed as a
     *            web service.
     * @return the endpoint URL of the web service which has been exposed.
     */
    public static String exposePublishWebService(PublishProxy proxy) {
        return exposeWebService(
                (Proxy) proxy, PublishWsProxyImpl.PUBLISH_WEBSERVICES_ITF);
    }

    /**
     * Unexposes the {@code publish-webservices} web service of a publish proxy
     * component.
     * 
     * @param proxy
     *            the {@code publish-services} interface of the component owning
     *            the {@code publish-webservices} interface exposed as a web
     *            service.
     */
    public static void unexposePublishWebService(PublishProxy proxy) {
        unexposeWebService(
                (Proxy) proxy, PublishWsProxyImpl.PUBLISH_WEBSERVICES_ITF);
    }

    /**
     * Exposes as a web service the {@code subscribe-webservices} interface of a
     * subscribe proxy component.
     * 
     * @param proxy
     *            the {@code subscribe-services} interface of the component
     *            owning the {@code subscribe-webservices} interface to be
     *            exposed as a web service.
     * @return the endpoint URL of the web service which has been exposed.
     */
    public static String exposeSubscribeWebService(SubscribeProxy proxy) {
        return exposeWebService(
                (Proxy) proxy, SubscribeWsProxyImpl.SUBSCRIBE_WEBSERVICES_ITF);
    }

    /**
     * Unexposes the {@code subscribe-webservices} web service of a subscribe
     * proxy component.
     * 
     * @param proxy
     *            the {@code subscribe-services} interface of the component
     *            owning the {@code subscribe-webservices} interface exposed as
     *            a web service.
     */
    public static void unexposeSubscribeWebService(SubscribeProxy proxy) {
        unexposeWebService(
                (Proxy) proxy, SubscribeWsProxyImpl.SUBSCRIBE_WEBSERVICES_ITF);
    }

    /**
     * Exposes as a web service the {@code putget-webservices} interface of a
     * put/get proxy component.
     * 
     * @param proxy
     *            the {@code putget-services} interface of the component owning
     *            the {@code putget-webservices} interface to be exposed as a
     *            web service.
     * @return the endpoint URL of the web service which has been exposed.
     */
    public static String exposePutGetWebService(PutGetProxy proxy) {
        return exposeWebService(
                (Proxy) proxy, PutGetWsProxyImpl.PUTGET_WEBSERVICES_ITF);
    }

    /**
     * Unexposes the {@code putget-webservices} web service of a put/get proxy
     * component.
     * 
     * @param proxy
     *            the {@code putget-services} interface of the component owning
     *            the {@code putget-webservices} interface exposed as a web
     *            service.
     */
    public static void unexposePutGetWebService(PutGetProxy proxy) {
        unexposeWebService(
                (Proxy) proxy, PutGetWsProxyImpl.PUTGET_WEBSERVICES_ITF);
    }

    /**
     * Exposes as a web service the specified interface of a proxy component.
     * 
     * @param proxy
     *            the {@code proxy} interface of the component owning the
     *            interface to be exposed as a web service.
     * @param interfaceName
     *            the name of the interface to be exposed as a web service.
     * @return the endpoint URL of the web service which has been exposed.
     */
    private static String exposeWebService(Proxy proxy, String interfaceName) {
        try {
            Component proxyComponent = ((Interface) proxy).getFcItfOwner();
            PAWebServicesController wsc =
                    Utils.getPAWebServicesController(proxyComponent);
            wsc.initServlet();
            wsc.exposeComponentAsWebService(
                    "EventCloud", new String[] {interfaceName});

            return wsc.getLocalUrl() + WSConstants.SERVICES_PATH
                    + "EventCloud_" + interfaceName;
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
        } catch (ActiveObjectCreationException e) {
            e.printStackTrace();
        } catch (NodeException e) {
            e.printStackTrace();
        } catch (WebServicesException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Unexposes the web service of a proxy component.
     * 
     * @param proxy
     *            the {@code proxy} interface of the component owning the
     *            interface exposed as a web service.
     * @param interfaceName
     *            the name of the interface exposed as a web service.
     */
    private static void unexposeWebService(Proxy proxy, String interfaceName) {
        try {
            Component proxyComponent = ((Interface) proxy).getFcItfOwner();
            PAWebServicesController wsc =
                    Utils.getPAWebServicesController(proxyComponent);
            wsc.unExposeComponentAsWebService(
                    "EventCloud", new String[] {interfaceName});
        } catch (NoSuchInterfaceException e) {
            e.printStackTrace();
        } catch (WebServicesException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deploys a new {@link EventCloudManagementServiceApi}.
     * 
     * @param registryUrl
     *            the registry to connect to in order to retrieve information
     *            about eventclouds running.
     * @param portLowerBound
     *            the port lower used to deploy proxies which are created
     *            through the service.
     * @param urlSuffix
     *            the suffix appended to the end of the URL associated to the
     *            service deployed.
     * @param port
     *            the port used to deploy the web service.
     * 
     * @return a new {@link EventCloudManagementServiceApi}.
     */
    public static Server deployEventCloudManagementWebService(String registryUrl,
                                                              int portLowerBound,
                                                              String urlSuffix,
                                                              int port) {
        return deployWebService(
                EventCloudManagementServiceApi.class,
                new EventCloudManagementServiceImpl(registryUrl, portLowerBound),
                urlSuffix, port);
    }

    /**
     * Deploys a new {@link PublishServiceApi}.
     * 
     * @param registryUrl
     *            the registry to connect to in order to retrieve information
     *            about eventclouds running.
     * @param streamUrl
     *            an URL which identifies an eventcloud which is running.
     * @param urlSuffix
     *            the suffix appended to the end of the URL associated to the
     *            service deployed.
     * @param port
     *            the port used to deploy the web service.
     * 
     * @return a new {@link PublishServiceApi}.
     */
    public static ServiceInformation deployPublishWebService(String registryUrl,
                                                             String streamUrl,
                                                             String urlSuffix,
                                                             int port) {
        PublishServiceImpl publishService =
                new PublishServiceImpl(registryUrl, streamUrl);

        Server publishServer =
                deployWebService(
                        PublishServiceApi.class, publishService, urlSuffix,
                        port);

        return new ServiceInformation(
                publishService, publishServer, streamUrl, port);
    }

    /**
     * Deploys a new {@link SubscribeServiceApi}.
     * 
     * @param registryUrl
     *            the registry to connect to in order to retrieve information
     *            about eventclouds running.
     * @param streamUrl
     *            an URL which identifies an eventcloud which is running.
     * @param urlSuffix
     *            the suffix appended to the end of the URL associated to the
     *            service deployed.
     * @param port
     *            the port used to deploy the web service.
     * 
     * @return a new {@link SubscribeServiceApi}.
     */
    public static ServiceInformation deploySubscribeWebService(String registryUrl,
                                                               String streamUrl,
                                                               String urlSuffix,
                                                               int port) {
        SubscribeServiceImpl subscribeService =
                new SubscribeServiceImpl(registryUrl, streamUrl);

        return new ServiceInformation(
                subscribeService, deployWebService(
                        SubscribeServiceApi.class, subscribeService, urlSuffix,
                        port), streamUrl, port);
    }

    /**
     * Deploys a new {@link PutGetWsApi}.
     * 
     * @param registryUrl
     *            the registry to connect to in order to retrieve information
     *            about eventclouds running.
     * @param streamUrl
     *            an URL which identifies an eventcloud which is running.
     * @param urlSuffix
     *            the suffix appended to the end of the URL associated to the
     *            service deployed.
     * @param port
     *            the port used to deploy the web service.
     * 
     * @return a new {@link PutGetWsApi}.
     */
    public static ServiceInformation deployPutGetWebService(String registryUrl,
                                                            String streamUrl,
                                                            String urlSuffix,
                                                            int port) {
        PutGetServiceImpl putGetService =
                new PutGetServiceImpl(registryUrl, streamUrl);

        return new ServiceInformation(
                putGetService, deployWebService(
                        PutGetWsApi.class, putGetService, urlSuffix, port),
                streamUrl, port);
    }

    /**
     * Deploys a new {@link NotificationConsumer}.
     * 
     * @param urlSuffix
     *            the suffix appended to the end of the URL associated to the
     *            service deployed.
     * @param port
     *            the port used to deploy the web service.
     * 
     * @return a new {@link NotificationConsumer}.
     */
    public static Server deploySubscriberWebService(String urlSuffix, int port) {
        return deployWebService(
                NotificationProducer.class, new SubscriberServiceImpl(),
                urlSuffix, port);
    }

    /**
     * Deploys a new web service.
     * 
     * @param service
     *            Service to expose as web service.
     * @param urlSuffix
     *            the suffix appended to the end of the URL associated to the
     *            service deployed.
     * @param port
     *            the port used to deploy the web service.
     * 
     * @return a new web service.
     */
    public static Server deployWebService(Object service, String addressSuffix,
                                          int port) {
        return deployWebService(
                service.getClass(), service, addressSuffix, port);
    }

    /**
     * Deploys a new web service.
     * 
     * @param serviceClass
     *            Class defining the services to expose as web services.
     * @param service
     *            Service to expose as web service.
     * @param urlSuffix
     *            the suffix appended to the end of the URL associated to the
     *            service deployed.
     * @param port
     *            the port used to deploy the web service.
     * 
     * @return a new web service.
     */
    public static Server deployWebService(Class<?> serviceClass,
                                          Object service, String addressSuffix,
                                          int port) {
        // binds the webservice to all interfaces by default
        StringBuilder address = new StringBuilder("http://0.0.0.0");
        address.append(':');
        address.append(port);
        address.append('/');
        if (addressSuffix != null) {
            address.append(addressSuffix);
        }

        JaxWsServerFactoryBean svrFactory = new JaxWsServerFactoryBean();
        svrFactory.setServiceClass(serviceClass);
        svrFactory.setAddress(address.toString());
        svrFactory.setServiceBean(service);

        LoggingInInterceptor in = new LoggingInInterceptor();
        svrFactory.getInInterceptors().add(in);
        svrFactory.getInFaultInterceptors().add(in);

        LoggingOutInterceptor out = new LoggingOutInterceptor();
        svrFactory.getOutInterceptors().add(out);
        svrFactory.getOutFaultInterceptors().add(out);

        return svrFactory.create();
    }
}
