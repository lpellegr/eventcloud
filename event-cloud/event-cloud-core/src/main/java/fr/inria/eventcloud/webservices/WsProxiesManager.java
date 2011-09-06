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
package fr.inria.eventcloud.webservices;

import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.extensions.webservices.WSConstants;
import org.objectweb.proactive.extensions.webservices.component.Utils;
import org.objectweb.proactive.extensions.webservices.component.controller.PAWebServicesController;
import org.objectweb.proactive.extensions.webservices.exceptions.WebServicesException;

import fr.inria.eventcloud.api.webservices.PublishWsApi;
import fr.inria.eventcloud.api.webservices.PutGetWsApi;
import fr.inria.eventcloud.api.webservices.SubscribeWsApi;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.proxies.Proxy;
import fr.inria.eventcloud.proxies.PublishProxy;
import fr.inria.eventcloud.proxies.PutGetProxy;
import fr.inria.eventcloud.proxies.SubscribeProxy;

/**
 * WsProxiesManager is used to ease web service operations (expose and unexpose)
 * for proxy components.
 * 
 * @author bsauvan
 */
public class WsProxiesManager {

    /**
     * Exposes as a web service the {@link PublishWsApi} interface of a publish
     * proxy component.
     * 
     * @param proxy
     *            the {@link PublishProxy} interface of the component owning the
     *            {@link PublishWsApi} interface to be exposed as a web service.
     * @return the URL of the web service which has been exposed.
     */
    public static String exposePublishWebService(PublishProxy proxy) {
        return exposeWebService(
                proxy,
                EventCloudProperties.PUBLISH_PROXY_WEBSERVICES_ITF.getValue());
    }

    /**
     * Unexposes the {@link PublishWsApi} web service of a publish proxy
     * component.
     * 
     * @param proxy
     *            the {@link PublishProxy} interface of the component owning the
     *            {@link PublishWsApi} interface exposed as a web service.
     */
    public static void unexposePublishWebService(PublishProxy proxy) {
        unexposeWebService(
                proxy,
                EventCloudProperties.PUBLISH_PROXY_WEBSERVICES_ITF.getValue());
    }

    /**
     * Exposes as a web service the {@link SubscribeWsApi} interface of a
     * subscribe proxy component.
     * 
     * @param proxy
     *            the {@link SubscribeProxy} interface of the component owning
     *            the {@link SubscribeWsApi} interface to be exposed as a web
     *            service.
     * @return the URL of the web service which has been exposed.
     */
    public static String exposeSubscribeWebService(SubscribeProxy proxy) {
        return exposeWebService(
                proxy,
                EventCloudProperties.SUBSCRIBE_PROXY_WEBSERVICES_ITF.getValue());
    }

    /**
     * Unexposes the {@link SubscribeWsApi} web service of a subscribe proxy
     * component.
     * 
     * @param proxy
     *            the {@link SubscribeProxy} interface of the component owning
     *            the {@link SubscribeWsApi} interface exposed as a web service.
     */
    public static void unexposeSubscribeWebService(SubscribeProxy proxy) {
        unexposeWebService(
                proxy,
                EventCloudProperties.SUBSCRIBE_PROXY_WEBSERVICES_ITF.getValue());
    }

    /**
     * Exposes as a web service the {@link PutGetWsApi} interface of a put/get
     * proxy component.
     * 
     * @param proxy
     *            the {@link PutGetProxy} interface of the component owning the
     *            {@link PutGetWsApi} interface to be exposed as a web service.
     * @return the URL of the web service which has been exposed.
     */
    public static String exposePutGetWebService(PutGetProxy proxy) {
        return exposeWebService(
                proxy,
                EventCloudProperties.PUTGET_PROXY_WEBSERVICES_ITF.getValue());
    }

    /**
     * Unexposes the {@link PutGetWsApi} web service of a put/get proxy
     * component.
     * 
     * @param proxy
     *            the {@link PutGetProxy} interface of the component owning the
     *            {@link PutGetWsApi} interface exposed as a web service.
     */
    public static void unexposePutGetWebService(PutGetProxy proxy) {
        unexposeWebService(
                proxy,
                EventCloudProperties.PUTGET_PROXY_WEBSERVICES_ITF.getValue());
    }

    /**
     * Exposes as a web service the specified interface of a proxy component.
     * 
     * @param proxy
     *            the {@link Proxy} interface of the component owning the
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
     *            the {@link Proxy} interface of the component owning the
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

}
