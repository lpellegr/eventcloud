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
package fr.inria.eventcloud.webservices.monitoring;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.cxf.endpoint.Server;
import org.junit.Before;
import org.junit.Test;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.bw_2.NotificationConsumer;
import org.oasis_open.docs.wsn.bw_2.NotificationProducer;
import org.objectweb.proactive.core.ProActiveException;

import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.PublishApi;
import fr.inria.eventcloud.api.SubscribeApi;
import fr.inria.eventcloud.api.Subscription;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.generators.CompoundEventGenerator;
import fr.inria.eventcloud.api.listeners.SignalNotificationListener;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.factories.EventCloudsRegistryFactory;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.translators.wsn.WsnHelper;
import fr.inria.eventcloud.webservices.BasicNotificationConsumer;
import fr.inria.eventcloud.webservices.api.EventCloudManagementWsServiceApi;
import fr.inria.eventcloud.webservices.deployment.ServiceInformation;
import fr.inria.eventcloud.webservices.deployment.WebServiceDeployer;
import fr.inria.eventcloud.webservices.services.SubscriberServiceImpl;
import fr.inria.eventcloud.webservices.utils.WsClientFactory;

/**
 * Test cases for input/output monitoring.
 * 
 * @author lpellegr
 */
public class InputOutputMonitoringTest {

    private static final QName RAW_REPORT_QNAME = new QName(
            "http://www.petalslink.org/rawreport/1.0", "RawReportTopic", "bsm");

    private static final QName STREAM_QNAME = new QName(
            "http://example.org/", "MyTopic", "t");

    private static final String STREAM_URL = STREAM_QNAME.getNamespaceURI()
            + STREAM_QNAME.getLocalPart();

    private static final EventCloudId EVENTCLOUD_ID = new EventCloudId(
            STREAM_URL);

    private String registryUrl;

    private Server eventCloudManagementWebServer;

    private EventCloudManagementWsServiceApi eventCloudManagementClient;

    private PublishApi publishProxy;

    private SubscribeApi subscribeProxy;

    private ServiceInformation subscribeServiceInformation;

    private ServiceInformation publishServiceInformation;

    private NotificationProducer subscribeClient;

    private NotificationConsumer publishClient;

    private BasicNotificationConsumer monitoringService;

    private Server monitoringServer;

    private SubscriberServiceImpl subscriberService;

    private Server subscriberWsServer;

    private String subscriberWsEndpointUrl;

    @Before
    public void setUp() throws ProActiveException, EventCloudIdNotManaged {
        this.initializeEventCloudsInfrastructure();
        this.initializeJavaProxies();
        this.initializeWsProxies();
    }

    @Test(timeout = 180000)
    public void testInputOutputMonitoring() throws Exception {

        // sends a subscribe request to enable input/output monitoring
        this.eventCloudManagementClient.subscribe(WsnHelper.createSubscribeMessage(
                this.monitoringServer.getEndpoint()
                        .getEndpointInfo()
                        .getAddress(), RAW_REPORT_QNAME));

        // subscribes with a Java proxy
        this.subscribeProxy.subscribe(
                new Subscription("SELECT ?g WHERE { GRAPH ?g { ?s ?p ?o } }"),
                new CustomSignalNotificationListener());

        // subscribes with a WSN proxy
        this.subscribeClient.subscribe(WsnHelper.createSubscribeMessage(
                this.subscriberWsEndpointUrl, STREAM_QNAME));

        // publishes a compound event through a ws publish proxy
        this.publishClient.notify(WsnHelper.createNotifyMessage(
                STREAM_QNAME, CompoundEventGenerator.random(STREAM_URL, 10)));

        // publishes a compound event through a java publish proxy
        this.publishProxy.publish(CompoundEventGenerator.random(STREAM_URL, 10));

        List<Notify> monitoringReportsReceived =
                this.monitoringService.getNotificationsReceived();

        // we must receive one report for each subscribe proxy times the number
        // of publications matching the subscriptions
        synchronized (monitoringReportsReceived) {
            while (monitoringReportsReceived.size() != 4) {
                try {
                    monitoringReportsReceived.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        // TODO: ideally we should check the content of the reports that are
        // received but I don't know how to say to CXF that it has to use the
        // easybox.petalslink.com.esrawreport._1.ObjectFactory to marshall the
        // any part of the notify message. Thus, currently, a call to
        // notify.getAny().get(0) returns null.
    }

    private void initializeEventCloudsInfrastructure()
            throws ProActiveException {
        EventCloudsRegistry registry =
                EventCloudsRegistryFactory.newEventCloudsRegistry();
        this.registryUrl = registry.register("registry");

        this.eventCloudManagementWebServer =
                WebServiceDeployer.deployEventCloudManagementWebService(
                        this.registryUrl, 22004, "management", 22005);

        this.eventCloudManagementClient =
                WsClientFactory.createWsClient(
                        EventCloudManagementWsServiceApi.class,
                        this.eventCloudManagementWebServer.getEndpoint()
                                .getEndpointInfo()
                                .getAddress());

        this.eventCloudManagementClient.createEventCloud(STREAM_URL);
    }

    private void initializeJavaProxies() throws EventCloudIdNotManaged {
        this.subscribeProxy =
                ProxyFactory.newSubscribeProxy(this.registryUrl, EVENTCLOUD_ID);
        this.publishProxy =
                ProxyFactory.newPublishProxy(this.registryUrl, EVENTCLOUD_ID);
    }

    private void initializeWsProxies() {

        this.monitoringService = new BasicNotificationConsumer();
        this.monitoringServer =
                WebServiceDeployer.deployWebService(
                        this.monitoringService, "monitoring", 22000);

        // web services which are deployed
        this.subscribeServiceInformation =
                WebServiceDeployer.deploySubscribeWebService(
                        this.registryUrl, STREAM_URL, "subscribe", 22001);

        this.publishServiceInformation =
                WebServiceDeployer.deployPublishWebService(
                        this.registryUrl, STREAM_URL, "publish", 22002);

        // clients associated to web services
        this.subscribeClient =
                WsClientFactory.createWsClient(
                        NotificationProducer.class,
                        this.subscribeServiceInformation.getServer()
                                .getEndpoint()
                                .getEndpointInfo()
                                .getAddress());

        this.publishClient =
                WsClientFactory.createWsClient(
                        NotificationConsumer.class,
                        this.publishServiceInformation.getServer()
                                .getEndpoint()
                                .getEndpointInfo()
                                .getAddress());

        this.subscriberService = new SubscriberServiceImpl();
        this.subscriberWsServer =
                WebServiceDeployer.deployWebService(
                        this.subscriberService, "subscriber", 22003);
        this.subscriberWsEndpointUrl =
                this.subscriberWsServer.getEndpoint()
                        .getEndpointInfo()
                        .getAddress();
    }

    private static class CustomSignalNotificationListener extends
            SignalNotificationListener {

        private static final long serialVersionUID = 1L;

        @Override
        public void onNotification(SubscriptionId id) {
        }

    }

}
