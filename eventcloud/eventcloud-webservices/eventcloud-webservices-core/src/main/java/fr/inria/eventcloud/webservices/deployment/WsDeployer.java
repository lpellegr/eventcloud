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
package fr.inria.eventcloud.webservices.deployment;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.proactive.ActiveObjectCreationException;
import org.objectweb.proactive.core.node.NodeException;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.extensions.p2p.structured.deployment.DeploymentConfiguration;
import org.objectweb.proactive.extensions.p2p.structured.proxies.Proxy;
import org.objectweb.proactive.extensions.webservices.WSConstants;
import org.objectweb.proactive.extensions.webservices.component.Utils;
import org.objectweb.proactive.extensions.webservices.component.controller.PAWebServicesController;
import org.objectweb.proactive.extensions.webservices.exceptions.WebServicesException;

import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.PublishApi;
import fr.inria.eventcloud.api.PutGetApi;
import fr.inria.eventcloud.api.SubscribeApi;
import fr.inria.eventcloud.deployment.EventCloudComponentsManager;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.proxies.PublishProxy;
import fr.inria.eventcloud.proxies.PutGetProxy;
import fr.inria.eventcloud.proxies.SubscribeProxy;
import fr.inria.eventcloud.webservices.EventCloudsManagementServiceImpl;
import fr.inria.eventcloud.webservices.api.EventCloudsManagementWsnApi;
import fr.inria.eventcloud.webservices.api.PublishWsApi;
import fr.inria.eventcloud.webservices.api.PublishWsnApi;
import fr.inria.eventcloud.webservices.api.PutGetWsApi;
import fr.inria.eventcloud.webservices.api.SubscribeWsApi;
import fr.inria.eventcloud.webservices.api.SubscribeWsnApi;
import fr.inria.eventcloud.webservices.proxies.PublishWsProxyImpl;
import fr.inria.eventcloud.webservices.proxies.PutGetWsProxyImpl;
import fr.inria.eventcloud.webservices.proxies.SubscribeWsProxyImpl;
import fr.inria.eventcloud.webservices.wsn.PublishWsnServiceImpl;
import fr.inria.eventcloud.webservices.wsn.SubscribeWsnServiceImpl;

/**
 * Deployer to ease web service operations (deploy and undeploy).
 * 
 * @author bsauvan
 */
public class WsDeployer {

    /**
     * Deploys a new {@link EventCloudsManagementWsnApi EventClouds Management
     * Service}.
     * 
     * @param registryUrl
     *            the URL of the EventClouds registry to use to manage
     *            EventClouds.
     * @param urlSuffix
     *            the suffix appended to the end of the URL associated to the
     *            web service to be deployed.
     * @param port
     *            the port used to deploy the web service and which will also be
     *            used to deploy WS-Notification services.
     * 
     * @return the Server instance of the web service.
     */
    public static Server deployEventCloudsManagementService(String registryUrl,
                                                            String urlSuffix,
                                                            int port) {
        return deployWebService(
                EventCloudsManagementWsnApi.class,
                new EventCloudsManagementServiceImpl(registryUrl, port),
                urlSuffix, port);
    }

    /**
     * Deploys a new {@link PublishWsnApi publish WS-Notification service}.
     * 
     * @param componentPoolManager
     *            the component pool manager to be used for the deployment of
     *            the underlying publish proxy.
     * @param registryUrl
     *            the URL of the EventClouds registry to connect to in order to
     *            create the underlying publish proxy.
     * @param streamUrl
     *            the URL which identifies an EventCloud which is running.
     * @param urlSuffix
     *            the suffix appended to the end of the URL associated to the
     *            web service to be deployed.
     * @param port
     *            the port used to deploy the web service.
     * 
     * @return the WsnServiceInfo instance of the web service.
     */
    public static WsnServiceInfo deployPublishWsnService(EventCloudComponentsManager componentPoolManager,
                                                         String registryUrl,
                                                         String streamUrl,
                                                         String urlSuffix,
                                                         int port) {
        return deployPublishWsnService(
                componentPoolManager, null, registryUrl, streamUrl, urlSuffix,
                port);
    }

    /**
     * Deploys a new {@link PublishWsnApi publish WS-Notification service}.
     * 
     * @param componentPoolManager
     *            the component pool manager to be used for the deployment of
     *            the underlying publish proxy.
     * @param deploymentConfiguration
     *            the deployment configuration to use during the deployment of
     *            the underlying publish proxy.
     * @param registryUrl
     *            the URL of the EventClouds registry to connect to in order to
     *            create the underlying publish proxy.
     * @param streamUrl
     *            the URL which identifies an EventCloud which is running.
     * @param urlSuffix
     *            the suffix appended to the end of the URL associated to the
     *            web service to be deployed.
     * @param port
     *            the port used to deploy the web service.
     * 
     * @return the WsnServiceInfo instance of the web service.
     */
    public static WsnServiceInfo deployPublishWsnService(EventCloudComponentsManager componentPoolManager,
                                                         DeploymentConfiguration deploymentConfiguration,
                                                         String registryUrl,
                                                         String streamUrl,
                                                         String urlSuffix,
                                                         int port) {
        PublishWsnServiceImpl publishWsnService =
                new PublishWsnServiceImpl(
                        componentPoolManager, deploymentConfiguration,
                        registryUrl, streamUrl);

        Server publishWsnServer =
                deployWebService(
                        PublishWsnApi.class, publishWsnService, urlSuffix, port);

        return new WsnServiceInfo(
                streamUrl, publishWsnService, publishWsnServer);
    }

    /**
     * Deploys a new {@link SubscribeWsnApi subscribe service}.
     * 
     * @param componentPoolManager
     *            the component pool manager to be used for the deployment of
     *            the underlying subscribe proxy.
     * @param registryUrl
     *            the URL of the EventClouds registry to connect to in order to
     *            create the underlying subscribe proxy.
     * @param streamUrl
     *            the URL which identifies an EventCloud which is running.
     * @param urlSuffix
     *            the suffix appended to the end of the URL associated to the
     *            web service to be deployed.
     * @param port
     *            the port used to deploy the web service.
     * 
     * @return the WsnServiceInfo instance of the web service.
     */
    public static WsnServiceInfo deploySubscribeWsnService(EventCloudComponentsManager componentPoolManager,
                                                           String registryUrl,
                                                           String streamUrl,
                                                           String urlSuffix,
                                                           int port) {
        return deploySubscribeWsnService(
                componentPoolManager, null, registryUrl, streamUrl, urlSuffix,
                port);
    }

    /**
     * Deploys a new {@link SubscribeWsnApi subscribe service}.
     * 
     * @param componentPoolManager
     *            the component pool manager to be used for the deployment of
     *            the underlying subscribe proxy.
     * @param deploymentConfiguration
     *            the deployment configuration to use during the deployment of
     *            the underlying subcribe proxy.
     * @param registryUrl
     *            the URL of the EventClouds registry to connect to in order to
     *            create the underlying subscribe proxy.
     * @param streamUrl
     *            the URL which identifies an EventCloud which is running.
     * @param urlSuffix
     *            the suffix appended to the end of the URL associated to the
     *            web service to be deployed.
     * @param port
     *            the port used to deploy the web service.
     * 
     * @return the WsnServiceInfo instance of the web service.
     */
    public static WsnServiceInfo deploySubscribeWsnService(EventCloudComponentsManager componentPoolManager,
                                                           DeploymentConfiguration deploymentConfiguration,
                                                           String registryUrl,
                                                           String streamUrl,
                                                           String urlSuffix,
                                                           int port) {
        SubscribeWsnServiceImpl subscribeWsnService =
                new SubscribeWsnServiceImpl(
                        componentPoolManager, deploymentConfiguration,
                        registryUrl, streamUrl);

        Server subscribeWsnServer =
                deployWebService(
                        SubscribeWsnApi.class, subscribeWsnService, urlSuffix,
                        port);

        return new WsnServiceInfo(
                streamUrl, subscribeWsnService, subscribeWsnServer);
    }

    /**
     * Deploys a new web service.
     * 
     * @param service
     *            Service to expose as web service.
     * @param urlSuffix
     *            the suffix appended to the end of the URL associated to the
     *            web service to be deployed.
     * @param port
     *            the port used to deploy the web service.
     * 
     * @return the Server instance of the web service.
     */
    public static Server deployWebService(Object service, String urlSuffix,
                                          int port) {
        return deployWebService(service.getClass(), service, urlSuffix, port);
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
     *            web service to be deployed.
     * @param port
     *            the port used to deploy the web service.
     * 
     * @return the Server instance of the web service.
     */
    public static Server deployWebService(Class<?> serviceClass,
                                          Object service, String urlSuffix,
                                          int port) {
        // binds the web service to all interfaces by default
        StringBuilder address = new StringBuilder("http://");
        address.append(ProActiveInet.getInstance()
                .getInetAddress()
                .getHostAddress());
        address.append(':');
        address.append(port);
        address.append('/');
        if (urlSuffix != null) {
            address.append(urlSuffix);
        }

        JaxWsServerFactoryBean serverFactoryBean = new JaxWsServerFactoryBean();
        serverFactoryBean.setServiceClass(serviceClass);
        serverFactoryBean.setAddress(address.toString());
        serverFactoryBean.setServiceBean(service);

        LoggingInInterceptor in = new LoggingInInterceptor();
        serverFactoryBean.getInInterceptors().add(in);
        serverFactoryBean.getInFaultInterceptors().add(in);

        LoggingOutInterceptor out = new LoggingOutInterceptor();
        serverFactoryBean.getOutInterceptors().add(out);
        serverFactoryBean.getOutFaultInterceptors().add(out);

        return serverFactoryBean.create();
    }

    /**
     * Deploys a new {@link PublishWsApi publish web service proxy}.
     * 
     * @param componentPoolManager
     *            the component pool manager to be used for the deployment of
     *            the publish web service proxy.
     * @param registryUrl
     *            the URL of the EventClouds registry to connect to in order to
     *            create the publish web service proxy.
     * @param streamUrl
     *            the URL which identifies an EventCloud which is running.
     * @param proxyName
     *            the name of the publish web service proxy which will be used
     *            as part of the URL associated to the web service to be
     *            deployed.
     * 
     * @return the PublishWsProxyInfo instance of the web service.
     */
    public static PublishWsProxyInfo deployPublishWsProxy(EventCloudComponentsManager componentPoolManager,
                                                          String registryUrl,
                                                          String streamUrl,
                                                          String proxyName) {
        return deployPublishWsProxy(
                componentPoolManager, null, registryUrl, streamUrl, proxyName);
    }

    /**
     * Deploys a new {@link PublishWsApi publish web service proxy}.
     * 
     * @param componentPoolManager
     *            the component pool manager to be used for the deployment of
     *            the publish web service proxy.
     * @param deploymentConfiguration
     *            the deployment configuration to use during the deployment of
     *            the publish web service proxy.
     * @param registryUrl
     *            the URL of the EventClouds registry to connect to in order to
     *            create the publish web service proxy.
     * @param streamUrl
     *            the URL which identifies an EventCloud which is running.
     * @param proxyName
     *            the name of the publish web service proxy which will be used
     *            as part of the URL associated to the web service to be
     *            deployed.
     * 
     * @return the PublishWsProxyInfo instance of the web service.
     */
    public static PublishWsProxyInfo deployPublishWsProxy(EventCloudComponentsManager componentPoolManager,
                                                          DeploymentConfiguration deploymentConfiguration,
                                                          String registryUrl,
                                                          String streamUrl,
                                                          String proxyName) {
        try {
            PublishProxy publishProxy =
                    componentPoolManager.getPublishProxy(
                            deploymentConfiguration, registryUrl,
                            new EventCloudId(streamUrl));

            String endpointUrl =
                    exposePublishWebService(publishProxy, proxyName);

            return new PublishWsProxyInfo(
                    streamUrl, endpointUrl, componentPoolManager, registryUrl,
                    publishProxy, proxyName,
                    PublishWsProxyImpl.PUBLISH_WEBSERVICES_ITF);
        } catch (EventCloudIdNotManaged e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Deploys a new {@link SubscribeWsApi subscribe web service proxy}.
     * 
     * @param componentPoolManager
     *            the component pool manager to be used for the deployment of
     *            the subscribe web service proxy.
     * @param registryUrl
     *            the URL of the EventClouds registry to connect to in order to
     *            create the subscribe web service proxy.
     * @param streamUrl
     *            the URL which identifies an EventCloud which is running.
     * @param proxyName
     *            the name of the subscribe web service proxy which will be used
     *            as part of the URL associated to the web service to be
     *            deployed.
     * 
     * @return the SubscribeWsProxyInfo instance of the web service.
     */
    public static SubscribeWsProxyInfo deploySubscribeWsProxy(EventCloudComponentsManager componentPoolManager,
                                                              String registryUrl,
                                                              String streamUrl,
                                                              String proxyName) {
        return deploySubscribeWsProxy(
                componentPoolManager, null, registryUrl, streamUrl, proxyName);
    }

    /**
     * Deploys a new {@link SubscribeWsApi subscribe web service proxy}.
     * 
     * @param componentPoolManager
     *            the component pool manager to be used for the deployment of
     *            the subscribe web service proxy.
     * @param deploymentConfiguration
     *            the deployment configuration to use during the deployment of
     *            the subscribe web service proxy.
     * @param registryUrl
     *            the URL of the EventClouds registry to connect to in order to
     *            create the subscribe web service proxy.
     * @param streamUrl
     *            the URL which identifies an EventCloud which is running.
     * @param proxyName
     *            the name of the subscribe web service proxy which will be used
     *            as part of the URL associated to the web service to be
     *            deployed.
     * 
     * @return the SubscribeWsProxyInfo instance of the web service.
     */
    public static SubscribeWsProxyInfo deploySubscribeWsProxy(EventCloudComponentsManager componentPoolManager,
                                                              DeploymentConfiguration deploymentConfiguration,
                                                              String registryUrl,
                                                              String streamUrl,
                                                              String proxyName) {
        try {
            SubscribeProxy subscribeProxy =
                    componentPoolManager.getSubscribeProxy(
                            deploymentConfiguration, registryUrl,
                            new EventCloudId(streamUrl));

            String endpointUrl =
                    exposeSubscribeWebService(subscribeProxy, proxyName);

            return new SubscribeWsProxyInfo(
                    streamUrl, endpointUrl, componentPoolManager, registryUrl,
                    subscribeProxy, proxyName,
                    SubscribeWsProxyImpl.SUBSCRIBE_WEBSERVICES_ITF);
        } catch (EventCloudIdNotManaged e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Deploys a new {@link PutGetWsApi put/get web service proxy}.
     * 
     * @param componentPoolManager
     *            the component pool manager to be used for the deployment of
     *            the put/get web service proxy.
     * @param registryUrl
     *            the URL of the EventClouds registry to connect to in order to
     *            create the put/get web service proxy.
     * @param streamUrl
     *            the URL which identifies an EventCloud which is running.
     * @param proxyName
     *            the name of the put/get web service proxy which will be used
     *            as part of the URL associated to the web service to be
     *            deployed.
     * 
     * @return the PutGetWsProxyInfo instance of the web service.
     */
    public static PutGetWsProxyInfo deployPutGetWsProxy(EventCloudComponentsManager componentPoolManager,
                                                        String registryUrl,
                                                        String streamUrl,
                                                        String proxyName) {
        return deployPutGetWsProxy(
                componentPoolManager, null, registryUrl, streamUrl, proxyName);
    }

    /**
     * Deploys a new {@link PutGetWsApi put/get web service proxy}.
     * 
     * @param componentPoolManager
     *            the component pool manager to be used for the deployment of
     *            the put/get web service proxy.
     * @param deploymentConfiguration
     *            the deployment configuration to use during the deployment of
     *            the put/get web service proxy.
     * @param registryUrl
     *            the URL of the EventClouds registry to connect to in order to
     *            create the put/get web service proxy.
     * @param streamUrl
     *            the URL which identifies an EventCloud which is running.
     * @param proxyName
     *            the name of the put/get web service proxy which will be used
     *            as part of the URL associated to the web service to be
     *            deployed.
     * 
     * @return the PutGetWsProxyInfo instance of the web service.
     */
    public static PutGetWsProxyInfo deployPutGetWsProxy(EventCloudComponentsManager componentPoolManager,
                                                        DeploymentConfiguration deploymentConfiguration,
                                                        String registryUrl,
                                                        String streamUrl,
                                                        String proxyName) {
        try {
            PutGetProxy putgetProxy =
                    componentPoolManager.getPutGetProxy(
                            deploymentConfiguration, registryUrl,
                            new EventCloudId(streamUrl));

            String endpointUrl = exposePutGetWebService(putgetProxy, proxyName);

            return new PutGetWsProxyInfo(
                    streamUrl, endpointUrl, componentPoolManager, registryUrl,
                    putgetProxy, proxyName,
                    PutGetWsProxyImpl.PUTGET_WEBSERVICES_ITF);
        } catch (EventCloudIdNotManaged e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Exposes as a web service the {@code publish-webservices} interface of a
     * publish proxy component.
     * 
     * @param publishProxy
     *            the {@code publish-services} interface of the component owning
     *            the {@code publish-webservices} interface to be exposed as a
     *            web service.
     * @param proxyName
     *            the name of the publish web service proxy which will be used
     *            as part of the URL associated to the web service to be
     *            deployed.
     * 
     * @return the endpoint URL of the web service which has been exposed.
     */
    public static String exposePublishWebService(PublishApi publishProxy,
                                                 String proxyName) {
        return exposeWebService(
                (Proxy) publishProxy, proxyName,
                PublishWsProxyImpl.PUBLISH_WEBSERVICES_ITF);
    }

    /**
     * Exposes as a web service the {@code subscribe-webservices} interface of a
     * subscribe proxy component.
     * 
     * @param subscribeProxy
     *            the {@code subscribe-services} interface of the component
     *            owning the {@code subscribe-webservices} interface to be
     *            exposed as a web service.
     * @param proxyName
     *            the name of the subscribe web service proxy which will be used
     *            as part of the URL associated to the web service to be
     *            deployed.
     * 
     * @return the endpoint URL of the web service which has been exposed.
     */
    public static String exposeSubscribeWebService(SubscribeApi subscribeProxy,
                                                   String proxyName) {
        return exposeWebService(
                (Proxy) subscribeProxy, proxyName,
                SubscribeWsProxyImpl.SUBSCRIBE_WEBSERVICES_ITF);
    }

    /**
     * Exposes as a web service the {@code putget-webservices} interface of a
     * put/get proxy component.
     * 
     * @param putgetProxy
     *            the {@code putget-services} interface of the component owning
     *            the {@code putget-webservices} interface to be exposed as a
     *            web service.
     * @param proxyName
     *            the name of the put/get web service proxy which will be used
     *            as part of the URL associated to the web service to be
     *            deployed.
     * 
     * @return the endpoint URL of the web service which has been exposed.
     */
    public static String exposePutGetWebService(PutGetApi putgetProxy,
                                                String proxyName) {
        return exposeWebService(
                (Proxy) putgetProxy, proxyName,
                PutGetWsProxyImpl.PUTGET_WEBSERVICES_ITF);
    }

    /**
     * Exposes as a web service the specified interface of the specified proxy
     * component.
     * 
     * @param proxy
     *            the Proxy interface of the component owning the interface to
     *            be exposed as a web service.
     * @param proxyName
     *            the name of the web service proxy which will be used as part
     *            of the URL associated to the web service to be deployed.
     * @param interfaceName
     *            the name of the interface to expose as a web service.
     * 
     * @return the endpoint URL of the web service which has been exposed.
     */
    public static String exposeWebService(Proxy proxy, String proxyName,
                                          String interfaceName) {
        try {
            Component proxyComponent = ((Interface) proxy).getFcItfOwner();
            PAWebServicesController wsc =
                    Utils.getPAWebServicesController(proxyComponent);
            wsc.initServlet();
            wsc.exposeComponentAsWebService(
                    proxyName, new String[] {interfaceName});

            return getUrl(wsc.getUrl()) + WSConstants.SERVICES_PATH + proxyName
                    + "_" + interfaceName;
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

    /*
     * TODO: Remove when the new release of ProActive containing the fix for PROACTIVE-1239 will be deployed 
     */
    private static String getUrl(String localUrl) {
        String url = localUrl;

        if (url.contains("localhost")) {
            url =
                    url.replaceFirst("localhost", ProActiveInet.getInstance()
                            .getInetAddress()
                            .getHostAddress());
        }

        return url;
    }

    /**
     * Unexposes the {@code publish-webservices} interface exposed as a web
     * service of a publish proxy component.
     * 
     * @param publishProxy
     *            the {@code publish-services} interface of the component owning
     *            the {@code publish-webservices} interface exposed as a web
     *            service.
     * @param proxyName
     *            the name of the publish web service proxy which is used as
     *            part of the URL associated to the web service deployed.
     */
    public static void unexposePublishWebService(PublishApi publishProxy,
                                                 String proxyName) {
        unexposeWebService(
                (Proxy) publishProxy, proxyName,
                PublishWsProxyImpl.PUBLISH_WEBSERVICES_ITF);
    }

    /**
     * Unexposes the {@code subscribe-webservices} interface exposed as a web
     * service of a subscribe proxy component.
     * 
     * @param subscribeProxy
     *            the {@code subscribe-services} interface of the component
     *            owning the {@code subscribe-webservices} interface exposed as
     *            a web service.
     * @param proxyName
     *            the name of the subscribe web service proxy which is used as
     *            part of the URL associated to the web service deployed.
     */
    public static void unexposeSubscribeWebService(SubscribeApi subscribeProxy,
                                                   String proxyName) {
        unexposeWebService(
                (Proxy) subscribeProxy, proxyName,
                SubscribeWsProxyImpl.SUBSCRIBE_WEBSERVICES_ITF);
    }

    /**
     * Unexposes the {@code putget-webservices} interface exposed as a web
     * service of a put/get proxy component.
     * 
     * @param putgetProxy
     *            the {@code putget-services} interface of the component owning
     *            the {@code putget-webservices} interface exposed as a web
     *            service.
     * @param proxyName
     *            the name of the put/get web service proxy which is used as
     *            part of the URL associated to the web service deployed.
     */
    public static void unexposePutGetWebService(PutGetApi putgetProxy,
                                                String proxyName) {
        unexposeWebService(
                (Proxy) putgetProxy, proxyName,
                PutGetWsProxyImpl.PUTGET_WEBSERVICES_ITF);
    }

    /**
     * Unexposes the specified interface exposed as a web service of the
     * specified proxy component.
     * 
     * @param proxy
     *            the Proxy interface of the component owning the interface
     *            exposed as a web service.
     * @param proxyName
     *            the name of the web service proxy which is used as part of the
     *            URL associated to the web service deployed.
     * @param interfaceName
     *            the name of the interface exposed as a web service.
     */
    public static void unexposeWebService(Proxy proxy, String proxyName,
                                          String interfaceName) {
        try {
            Component proxyComponent = ((Interface) proxy).getFcItfOwner();
            PAWebServicesController wsc =
                    Utils.getPAWebServicesController(proxyComponent);
            wsc.unExposeComponentAsWebService(
                    proxyName, new String[] {interfaceName});
        } catch (NoSuchInterfaceException e) {
            throw new IllegalArgumentException(e);
        } catch (WebServicesException e) {
            throw new IllegalStateException(e);
        }
    }

}
