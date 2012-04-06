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
import fr.inria.eventcloud.webservices.configuration.EventCloudWsProperties;
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
     * @return the URL of the web service which has been exposed.
     */
    public static String exposePublishWebService(PublishProxy proxy) {
        return exposeWebService(
                proxy,
                EventCloudWsProperties.PUBLISH_PROXY_WEBSERVICES_ITF.getValue());
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
                proxy,
                EventCloudWsProperties.PUBLISH_PROXY_WEBSERVICES_ITF.getValue());
    }

    /**
     * Exposes as a web service the {@code subscribe-webservices} interface of a
     * subscribe proxy component.
     * 
     * @param proxy
     *            the {@code subscribe-services} interface of the component
     *            owning the {@code subscribe-webservices} interface to be
     *            exposed as a web service.
     * @return the URL of the web service which has been exposed.
     */
    public static String exposeSubscribeWebService(SubscribeProxy proxy) {
        return exposeWebService(
                proxy,
                EventCloudWsProperties.SUBSCRIBE_PROXY_WEBSERVICES_ITF.getValue());
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
                proxy,
                EventCloudWsProperties.SUBSCRIBE_PROXY_WEBSERVICES_ITF.getValue());
    }

    /**
     * Exposes as a web service the {@code putget-webservices} interface of a
     * put/get proxy component.
     * 
     * @param proxy
     *            the {@code putget-services} interface of the component owning
     *            the {@code putget-webservices} interface to be exposed as a
     *            web service.
     * @return the URL of the web service which has been exposed.
     */
    public static String exposePutGetWebService(PutGetProxy proxy) {
        return exposeWebService(
                proxy,
                EventCloudWsProperties.PUTGET_PROXY_WEBSERVICES_ITF.getValue());
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
                proxy,
                EventCloudWsProperties.PUTGET_PROXY_WEBSERVICES_ITF.getValue());
    }

    /**
     * Exposes as a web service the specified interface of a proxy component.
     * 
     * @param proxy
     *            the {@code proxy} interface of the component owning the
     *            interface to be exposed as a web service.
     * @param interfaceName
     *            the name of the interface to be exposed as a web service.
     * @return the URL of the web service which has been exposed.
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
     * Deploys a new {@link EventCloudManagementServiceImpl}.
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
     * @param webServicePort
     *            the port used to deploy the web service.
     * 
     * @return a new {@link EventCloudManagementServiceImpl}.
     */
    public static Server deployEventCloudManagementWebService(String registryUrl,
                                                              int portLowerBound,
                                                              String urlSuffix,
                                                              int webServicePort) {
        return deployWebService(new EventCloudManagementServiceImpl(
                registryUrl, portLowerBound), urlSuffix, webServicePort);
    }

    public static Server deployPublishWebService(String registryUrl,
                                                 String eventCloudIdUrl,
                                                 String urlSuffix, int port) {
        return deployWebService(new PublishServiceImpl(
                registryUrl, eventCloudIdUrl), urlSuffix, port);
    }

    public static Server deploySubscribeWebService(String registryUrl,
                                                   String streamUrl,
                                                   String urlSuffix, int port) {
        return deployWebService(
                new SubscribeServiceImpl(registryUrl, streamUrl), urlSuffix,
                port);
    }

    public static Server deployPutGetWebService(String registryUrl,
                                                String streamUrl,
                                                String urlSuffix, int port) {
        return deployWebService(
                new PutGetServiceImpl(registryUrl, streamUrl), urlSuffix, port);
    }

    public static Server deploySubscriberWebService(String urlSuffix, int port) {
        return deployWebService(new SubscriberServiceImpl(), urlSuffix, port);
    }

    public static Server deployWebService(Object service, String addressSuffix,
                                          int port) {
        StringBuilder address = new StringBuilder("http://");
        try {
            address.append(java.net.InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        address.append(":");
        address.append(port);
        address.append("/");
        if (addressSuffix != null) {
            address.append(addressSuffix);
        }

        JaxWsServerFactoryBean svrFactory = new JaxWsServerFactoryBean();
        svrFactory.setServiceClass(service.getClass());
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
