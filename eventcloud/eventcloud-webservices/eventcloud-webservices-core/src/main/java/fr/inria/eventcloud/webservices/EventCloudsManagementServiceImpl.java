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
package fr.inria.eventcloud.webservices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.xml.namespace.QName;

import org.etsi.uri.gcm.util.GCM;
import org.oasis_open.docs.wsn.b_2.GetCurrentMessage;
import org.oasis_open.docs.wsn.b_2.GetCurrentMessageResponse;
import org.oasis_open.docs.wsn.b_2.Renew;
import org.oasis_open.docs.wsn.b_2.RenewResponse;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;
import org.oasis_open.docs.wsn.b_2.Unsubscribe;
import org.oasis_open.docs.wsn.b_2.UnsubscribeResponse;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalContentException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.component.PAInterface;
import org.objectweb.proactive.core.component.Utils;
import org.objectweb.proactive.core.component.control.PAMembraneController;
import org.objectweb.proactive.core.component.exceptions.NoSuchComponentException;
import org.objectweb.proactive.extensions.webservices.WSConstants;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Maps;

import fr.inria.eventcloud.EventCloudDescription;
import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.deployment.EventCloudDeployer;
import fr.inria.eventcloud.deployment.EventCloudDeploymentDescriptor;
import fr.inria.eventcloud.factories.EventCloudsRegistryFactory;
import fr.inria.eventcloud.providers.SemanticPersistentOverlayProvider;
import fr.inria.eventcloud.proxies.AbstractProxy;
import fr.inria.eventcloud.proxies.SubscribeProxy;
import fr.inria.eventcloud.translators.wsn.WsnHelper;
import fr.inria.eventcloud.webservices.api.EventCloudsManagementWsnApi;
import fr.inria.eventcloud.webservices.deployment.WsDeployer;
import fr.inria.eventcloud.webservices.deployment.WsInfo;
import fr.inria.eventcloud.webservices.deployment.WsProxyInfo;
import fr.inria.eventcloud.webservices.deployment.WsnServiceInfo;
import fr.inria.eventcloud.webservices.factories.ProxyMonitoringManagerFactory;
import fr.inria.eventcloud.webservices.monitoring.ProxyMonitoringManager;
import fr.inria.eventcloud.webservices.monitoring.ProxyMonitoringManagerImpl;

/**
 * Concrete implementation of {@link EventCloudsManagementWsnApi}.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public class EventCloudsManagementServiceImpl implements
        EventCloudsManagementWsnApi {

    private static final String WSN_SERVICE_ID = "wsn-service-";

    private static final String WS_PROXY_ID = "ws-proxy-";

    private static final String RAW_REPORT_TOPIC =
            "http://www.petalslink.org/rawreport/1.0/RawReportTopic";

    private final String registryUrl;

    private EventCloudsRegistry registry;

    private final int wsnServicePort;

    // web service endpoint URL -> one WsInfo instance
    private final Map<String, WsInfo> wsInfos;

    // stream URL -> one or several number ID which are already assigned
    private final ListMultimap<String, Integer> assignedNumberIds;

    // stream URL -> one or several publish WSN service endpoint URL
    private final ListMultimap<String, String> publishWsnServiceEndpointUrls;

    // stream URL -> one or several subscribe WSN service endpoint URL
    private final ListMultimap<String, String> subscribeWsnServiceEndpointUrls;

    // stream URL -> one or several publish WS proxy endpoint URL
    private final ListMultimap<String, String> publishWsProxyEndpointUrls;

    // stream URL -> one or several subscribe WS proxy endpoint URL
    private final ListMultimap<String, String> subscribeWsProxyEndpointUrls;

    // stream URL -> one or several put/get WS proxy endpoint URL
    private final ListMultimap<String, String> putgetWsProxyEndpointUrls;

    /**
     * Creates a {@link EventCloudsManagementServiceImpl}.
     * 
     * @param registryUrl
     *            the URL of the EventClouds registry to use to manage
     *            EventClouds.
     * @param wsnServicePort
     *            the port on which to deploy the WS-Notification services.
     */
    public EventCloudsManagementServiceImpl(String registryUrl,
            int wsnServicePort) {
        this.registryUrl = registryUrl;
        this.wsnServicePort = wsnServicePort;
        this.wsInfos = Maps.newHashMap();
        this.assignedNumberIds = ArrayListMultimap.create();
        this.publishWsnServiceEndpointUrls = ArrayListMultimap.create();
        this.subscribeWsnServiceEndpointUrls = ArrayListMultimap.create();
        this.publishWsProxyEndpointUrls = ArrayListMultimap.create();
        this.subscribeWsProxyEndpointUrls = ArrayListMultimap.create();
        this.putgetWsProxyEndpointUrls = ArrayListMultimap.create();
    }

    /**
     * Initializes the EventClouds management service.
     */
    @PostConstruct
    public void init() {
        try {
            this.registry =
                    EventCloudsRegistryFactory.lookupEventCloudsRegistry(this.registryUrl);
        } catch (IOException ioe) {
            throw new IllegalStateException(ioe);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getRegistryEndpointUrl() {
        return this.registryUrl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean createEventCloud(String streamUrl) {
        EventCloudId id = new EventCloudId(streamUrl);

        if (!this.registry.contains(id)) {
            EventCloudDescription description = new EventCloudDescription(id);

            EventCloudDeployer deployer =
                    new EventCloudDeployer(
                            description, new EventCloudDeploymentDescriptor(
                                    new SemanticPersistentOverlayProvider()));

            deployer.deploy(1, 1);

            return this.registry.register(deployer);
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCreated(String streamUrl) {
        return this.registry.contains(new EventCloudId(streamUrl));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getEventCloudIds() {
        Set<EventCloudId> ids = this.registry.listEventClouds();
        List<String> result = new ArrayList<String>(ids.size());

        for (EventCloudId id : ids) {
            result.add(id.getStreamUrl());
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean destroyEventCloud(String streamUrl) {
        EventCloudId id = new EventCloudId(streamUrl);

        if (this.registry.contains(id)) {
            boolean result = true;

            result &=
                    this.destroyWebServices(
                            streamUrl, this.publishWsnServiceEndpointUrls);
            result &=
                    this.destroyWebServices(
                            streamUrl, this.subscribeWsnServiceEndpointUrls);
            result &=
                    this.destroyWebServices(
                            streamUrl, this.publishWsProxyEndpointUrls);
            result &=
                    this.destroyWebServices(
                            streamUrl, this.subscribeWsProxyEndpointUrls);
            result &=
                    this.destroyWebServices(
                            streamUrl, this.putgetWsProxyEndpointUrls);

            return result && this.registry.undeploy(id);
        }

        return false;
    }

    private boolean destroyWebServices(String streamUrl,
                                       ListMultimap<String, String> wsEndpointUrls) {
        boolean result = true;

        for (String wsEndpointUrl : ImmutableList.copyOf(wsEndpointUrls.get(streamUrl))) {
            result &= this.destroyWebService(wsEndpointUrl, wsEndpointUrls);
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String deployPublishWsnService(String streamUrl) {
        this.checkEventCloudId(streamUrl);

        int numberId = this.lockUnassignedNumberId(streamUrl);
        WsnServiceInfo publishWsnServiceInfo =
                WsDeployer.deployPublishWsnService(
                        this.registryUrl, streamUrl, WSConstants.SERVICES_PATH
                                + "eventclouds/" + this.getTopicName(streamUrl)
                                + "/" + WSN_SERVICE_ID + numberId
                                + "_publish-webservices", this.wsnServicePort);

        return this.storeAndReturnProxyAddress(
                publishWsnServiceInfo, this.publishWsnServiceEndpointUrls);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String deploySubscribeWsnService(String streamUrl) {
        this.checkEventCloudId(streamUrl);

        int numberId = this.lockUnassignedNumberId(streamUrl);
        WsnServiceInfo subscribeWsnServiceInfo =
                WsDeployer.deploySubscribeWsnService(
                        this.registryUrl, streamUrl, WSConstants.SERVICES_PATH
                                + "eventclouds/" + this.getTopicName(streamUrl)
                                + "/" + WSN_SERVICE_ID + numberId
                                + "_subscribe-webservices", this.wsnServicePort);

        return this.storeAndReturnProxyAddress(
                subscribeWsnServiceInfo, this.subscribeWsnServiceEndpointUrls);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String deployPublishWsProxy(String streamUrl) {
        this.checkEventCloudId(streamUrl);

        int numberId = this.lockUnassignedNumberId(streamUrl);
        WsProxyInfo publishWsProxyInfo =
                WsDeployer.deployPublishWsProxy(
                        this.registryUrl, streamUrl, "eventclouds/"
                                + this.getTopicName(streamUrl) + "/"
                                + WS_PROXY_ID + numberId);

        return this.storeAndReturnProxyAddress(
                publishWsProxyInfo, this.publishWsProxyEndpointUrls);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String deploySubscribeWsProxy(String streamUrl) {
        this.checkEventCloudId(streamUrl);

        int numberId = this.lockUnassignedNumberId(streamUrl);
        WsProxyInfo subscribeWsProxyInfo =
                WsDeployer.deploySubscribeWsProxy(
                        this.registryUrl, streamUrl, "eventclouds/"
                                + this.getTopicName(streamUrl) + "/"
                                + WS_PROXY_ID + numberId);

        return this.storeAndReturnProxyAddress(
                subscribeWsProxyInfo, this.subscribeWsProxyEndpointUrls);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String deployPutGetWsProxy(String streamUrl) {
        this.checkEventCloudId(streamUrl);

        int numberId = this.lockUnassignedNumberId(streamUrl);
        WsProxyInfo putgetWsProxyInfo =
                WsDeployer.deployPutGetWsProxy(
                        this.registryUrl, streamUrl, "eventclouds/"
                                + this.getTopicName(streamUrl) + "/"
                                + WS_PROXY_ID + numberId);

        return this.storeAndReturnProxyAddress(
                putgetWsProxyInfo, this.putgetWsProxyEndpointUrls);
    }

    private void checkEventCloudId(String streamUrl) {
        EventCloudId id = new EventCloudId(streamUrl);

        if (!this.registry.contains(id)) {
            throw new IllegalArgumentException("No EventCloud running for "
                    + streamUrl);
        }
    }

    private String getTopicName(String streamUrl) {
        return streamUrl.substring(streamUrl.lastIndexOf('/') + 1);
    }

    private int lockUnassignedNumberId(String streamUrl) {
        int numberId = 1;

        synchronized (this.assignedNumberIds) {
            while (true) {
                if (!this.assignedNumberIds.containsEntry(streamUrl, numberId)) {
                    this.assignedNumberIds.put(streamUrl, numberId);

                    return numberId;
                }
                numberId++;
            }
        }
    }

    private String storeAndReturnProxyAddress(WsInfo wsInfo,
                                              ListMultimap<String, String> wsEndpointUrls) {
        wsEndpointUrls.put(wsInfo.getStreamUrl(), wsInfo.getWsEndpointUrl());
        this.wsInfos.put(wsInfo.getWsEndpointUrl(), wsInfo);

        return wsInfo.getWsEndpointUrl();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getPublishWsnServiceEndpointUrls(String streamUrl) {
        return this.publishWsnServiceEndpointUrls.get(streamUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getSubscribeWsnServiceEndpointUrls(String streamUrl) {
        return this.subscribeWsnServiceEndpointUrls.get(streamUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getPublishWsProxyEndpointUrls(String streamUrl) {
        return this.publishWsProxyEndpointUrls.get(streamUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getSubscribeWsProxyEndpointUrls(String streamUrl) {
        return this.subscribeWsProxyEndpointUrls.get(streamUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getPutGetWsProxyEndpointUrls(String streamUrl) {
        return this.putgetWsProxyEndpointUrls.get(streamUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean destroyPublishWsnService(String publishWsnEndpointUrl) {
        return this.destroyWebService(
                publishWsnEndpointUrl, this.publishWsnServiceEndpointUrls);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean destroySubscribeWsnService(String subscribeWsnEndpointUrl) {
        return this.destroyWebService(
                subscribeWsnEndpointUrl, this.subscribeWsnServiceEndpointUrls);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean destroyPublishWsProxy(String publishWsProxyEndpointUrl) {
        return this.destroyWebService(
                publishWsProxyEndpointUrl, this.publishWsProxyEndpointUrls);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean destroySubscribeWsProxy(String subscribeWsProxyEndpointUrl) {
        return this.destroyWebService(
                subscribeWsProxyEndpointUrl, this.subscribeWsProxyEndpointUrls);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean destroyPutGetWsProxy(String putgetWsProxyEndpointUrl) {
        return this.destroyWebService(
                putgetWsProxyEndpointUrl, this.putgetWsProxyEndpointUrls);
    }

    private boolean destroyWebService(String wsEndpointUrl,
                                      ListMultimap<String, String> wsEndpointUrls) {
        WsInfo wsInfo = this.wsInfos.remove(wsEndpointUrl);

        if (wsInfo != null) {
            wsInfo.destroy();
            wsEndpointUrls.remove(wsInfo.getStreamUrl(), wsEndpointUrl);
            this.assignedNumberIds.remove(
                    wsInfo.getStreamUrl(), this.getNumberId(wsInfo));

            return true;
        }

        return false;
    }

    private int getNumberId(WsInfo wsInfo) {
        String wsEndpointUrl = wsInfo.getWsEndpointUrl();
        int beginNumberIdIndex = 0;
        int endNumberIdIndex = wsEndpointUrl.lastIndexOf("_");

        if (wsInfo instanceof WsnServiceInfo) {
            beginNumberIdIndex =
                    wsEndpointUrl.lastIndexOf(WSN_SERVICE_ID)
                            + WSN_SERVICE_ID.length();
        } else if (wsInfo instanceof WsProxyInfo) {
            beginNumberIdIndex =
                    wsEndpointUrl.lastIndexOf(WS_PROXY_ID)
                            + WS_PROXY_ID.length();
        } else {
            throw new IllegalStateException(
                    "Unknow web service information type: " + wsInfo.getClass());
        }

        return Integer.parseInt(wsEndpointUrl.substring(
                beginNumberIdIndex, endNumberIdIndex));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GetCurrentMessageResponse getCurrentMessage(GetCurrentMessage getCurrentMessageRequest) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SubscribeResponse subscribe(Subscribe subscribe) {
        SubscriptionId subscriptionId = new SubscriptionId();
        String consumerReference =
                WsnHelper.getAddress(subscribe.getConsumerReference());
        QName topic = WsnHelper.getTopic(subscribe);

        if ((topic.getNamespaceURI() + topic.getLocalPart()).equals(RAW_REPORT_TOPIC)) {
            Set<EventCloudId> ids = this.registry.listEventClouds();

            // enable input/output monitoring for all EventClouds
            for (EventCloudId id : ids) {
                for (SubscribeProxy proxy : this.registry.getSubscribeProxies(id)) {
                    ProxyMonitoringManager proxyMonitoringManagerInterface =
                            this.getProxyMonitoringManagerInterface(proxy);

                    if (proxyMonitoringManagerInterface != null) {
                        proxyMonitoringManagerInterface.enableInputOutputMonitoring(
                                subscriptionId, consumerReference);
                    }
                }
            }
        }

        return WsnHelper.createSubscribeResponse(
                subscriptionId, consumerReference);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RenewResponse renew(Renew renewRequest) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UnsubscribeResponse unsubscribe(Unsubscribe unsubscribeRequest) {
        SubscriptionId subscriptionId =
                WsnHelper.getSubcriptionId(unsubscribeRequest);
        Set<EventCloudId> ids = this.registry.listEventClouds();

        // disable input/output monitoring for all EventClouds
        for (EventCloudId id : ids) {
            for (SubscribeProxy proxy : this.registry.getSubscribeProxies(id)) {
                ProxyMonitoringManager proxyMonitoringManagerInterface =
                        this.getProxyMonitoringManagerInterface(proxy);

                if (proxyMonitoringManagerInterface != null) {
                    proxyMonitoringManagerInterface.disableInputOutputMonitoring(subscriptionId);
                }
            }
        }

        return new UnsubscribeResponse();
    }

    private ProxyMonitoringManager getProxyMonitoringManagerInterface(Object proxyStub) {
        try {
            Component proxy = ((PAInterface) proxyStub).getFcItfOwner();
            PAMembraneController membraneController =
                    Utils.getPAMembraneController(proxy);
            Component proxyMonitoringManager =
                    membraneController.nfGetFcSubComponent(ProxyMonitoringManagerImpl.COMPONENT_NAME);

            if (proxyMonitoringManager == null) {
                return this.addProxyMonitoringManager(proxy, membraneController);
            } else {
                return (ProxyMonitoringManager) proxyMonitoringManager.getFcInterface(ProxyMonitoringManagerImpl.MONITORING_SERVICES_ITF);
            }
        } catch (NoSuchInterfaceException nsie) {
            nsie.printStackTrace();
        } catch (NoSuchComponentException nsce) {
            nsce.printStackTrace();
        } catch (IllegalContentException ice) {
            ice.printStackTrace();
        } catch (IllegalLifeCycleException ilce) {
            ilce.printStackTrace();
        } catch (IllegalBindingException ibe) {
            ibe.printStackTrace();
        }

        return null;
    }

    private ProxyMonitoringManager addProxyMonitoringManager(Component proxy,
                                                             PAMembraneController membraneController)
            throws NoSuchInterfaceException, IllegalLifeCycleException,
            IllegalContentException, IllegalBindingException,
            NoSuchComponentException {
        ProxyMonitoringManager stub =
                ProxyMonitoringManagerFactory.newProxyMonitoringManager();
        Component proxyMonitoringManager = ((PAInterface) stub).getFcItfOwner();

        GCM.getNameController(proxyMonitoringManager).setFcName(
                ProxyMonitoringManagerImpl.COMPONENT_NAME);

        Utils.getPAGCMLifeCycleController(proxy).stopFc();
        membraneController.stopMembrane();

        membraneController.nfAddFcSubComponent(proxyMonitoringManager);
        membraneController.nfBindFc(
                AbstractProxy.MONITORING_SERVICES_CONTROLLER_ITF,
                ProxyMonitoringManagerImpl.COMPONENT_NAME + "."
                        + ProxyMonitoringManagerImpl.MONITORING_SERVICES_ITF);

        membraneController.startMembrane();
        Utils.getPAGCMLifeCycleController(proxy).startFc();

        return stub;
    }

}