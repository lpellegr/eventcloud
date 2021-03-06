/**
 * Copyright (c) 2011-2014 INRIA.
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
package fr.inria.eventcloud.webservices.monitoring;

import java.io.IOException;

import javax.xml.namespace.QName;

import org.apache.cxf.endpoint.Server;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;
import org.objectweb.proactive.core.ProActiveException;
import org.objectweb.proactive.extensions.p2p.structured.utils.ComponentUtils;

import fr.inria.eventcloud.EventCloudsRegistry;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.Subscription;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.generators.CompoundEventGenerator;
import fr.inria.eventcloud.api.listeners.SignalNotificationListener;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.factories.EventCloudsRegistryFactory;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.proxies.PublishProxy;
import fr.inria.eventcloud.proxies.SubscribeProxy;
import fr.inria.eventcloud.pubsub.SubscriptionTestUtils;
import fr.inria.eventcloud.translators.wsn.WsnHelper;
import fr.inria.eventcloud.webservices.CompoundEventNotificationConsumer;
import fr.inria.eventcloud.webservices.WsTest;
import fr.inria.eventcloud.webservices.api.EventCloudsManagementWsnApi;
import fr.inria.eventcloud.webservices.api.PublishWsnApi;
import fr.inria.eventcloud.webservices.api.SubscribeWsnApi;
import fr.inria.eventcloud.webservices.deployment.WsDeployer;
import fr.inria.eventcloud.webservices.factories.WsClientFactory;

/**
 * Test cases for input/output monitoring.
 * 
 * @author lpellegr
 */
public class InputOutputMonitoringTest extends WsTest {

    private static final QName RAW_REPORT_QNAME = new QName(
            "http://www.petalslink.org/rawreport/1.0", "RawReportTopic", "bsm");

    private static final QName STREAM_QNAME = new QName(
            "http://example.org/", "MyTopic", "t");

    private static final String STREAM_URL = STREAM_QNAME.getNamespaceURI()
            + STREAM_QNAME.getLocalPart();

    private static final EventCloudId EVENTCLOUD_ID = new EventCloudId(
            STREAM_URL);

    private String registryUrl;

    private Server eventCloudsManagementServer;

    private EventCloudsManagementWsnApi eventCloudsManagementWsnClient;

    private PublishProxy publishProxy;

    private SubscribeProxy subscribeProxy;

    private String publishWsnServiceEndpointUrl;

    private String subscribeWsnServiceEndpointUrl;

    private PublishWsnApi publishWsnClient;

    private SubscribeWsnApi subscribeWsnClient;

    private BasicNotificationConsumer monitoringService;

    private Server monitoringServer;

    private CompoundEventNotificationConsumer notificationConsumerService;

    private Server notificationConsumerServer;

    private String notificationConsumerWsEndpointUrl;

    @Before
    public void setUp() throws ProActiveException, EventCloudIdNotManaged {
        this.initializeEventCloudsInfrastructure();
        this.initializeJavaProxies();
        this.initializeWebServices();
    }

    @Test(timeout = 180000)
    public void testInputOutputMonitoring() throws Exception {
        // Sends a subscribe request to enable input/output monitoring
        SubscribeResponse subscribeResponse =
                this.eventCloudsManagementWsnClient.subscribe(WsnHelper.createSubscribeMessage(
                        this.monitoringServer.getEndpoint()
                                .getEndpointInfo()
                                .getAddress(),
                        InputOutputMonitoringTest.RAW_REPORT_QNAME));
        SubscriptionId subscriptionId =
                WsnHelper.getSubcriptionId(subscribeResponse);

        // Subscribes with a Java proxy
        this.subscribeProxy.subscribe(
                new Subscription("SELECT ?g WHERE { GRAPH ?g { ?s ?p ?o } }"),
                new CustomSignalNotificationListener());

        // Subscribes with a WSN proxy
        this.subscribeWsnClient.subscribe(WsnHelper.createSubscribeMessage(
                this.notificationConsumerWsEndpointUrl,
                InputOutputMonitoringTest.STREAM_QNAME));

        SubscriptionTestUtils.waitSubscriptionIndexation();

        // Publishes a compound event through a ws publish proxy
        this.publishWsnClient.notify(WsnHelper.createNotifyMessage(
                this.publishWsnServiceEndpointUrl,
                InputOutputMonitoringTest.STREAM_QNAME,
                CompoundEventGenerator.random(
                        InputOutputMonitoringTest.STREAM_URL, 10)));

        // Publishes a compound event through a java publish proxy
        this.publishProxy.publish(CompoundEventGenerator.random(
                InputOutputMonitoringTest.STREAM_URL, 10));

        // We must receive one report for each subscription registered along
        // with a compound event notification listener, times the number of
        // publications matching the subscriptions
        synchronized (this.monitoringService.notificationsReceived) {
            while (this.monitoringService.notificationsReceived.size() != 2) {
                this.monitoringService.notificationsReceived.wait();
            }
        }

        // TODO: ideally we should check the content of the reports that are
        // received but I don't know how to say to CXF that it has to use the
        // easybox.petalslink.com.esrawreport._1.ObjectFactory to marshall the
        // any part of the notify message. Thus, currently, a call to
        // notify.getAny().get(0) returns null.

        // Sends an unsubscribe request to disable input/output monitoring
        this.eventCloudsManagementWsnClient.unsubscribe(WsnHelper.createUnsubscribeRequest(subscriptionId));

        // Publishes a compound event through a java publish proxy
        this.publishProxy.publish(CompoundEventGenerator.random(
                InputOutputMonitoringTest.STREAM_URL, 10));

        // Checks that no more reports are received
        synchronized (this.monitoringService.notificationsReceived) {
            this.monitoringService.notificationsReceived.wait(4000);
            Assert.assertTrue(this.monitoringService.notificationsReceived.size() == 2);
        }
    }

    private void initializeEventCloudsInfrastructure()
            throws ProActiveException {
        EventCloudsRegistry registry =
                EventCloudsRegistryFactory.newEventCloudsRegistry();
        this.registryUrl = registry.register("registry");

        this.eventCloudsManagementServer =
                WsDeployer.deployEventCloudsManagementService(
                        this.registryUrl, "management", WsTest.WEBSERVICES_PORT);
        this.eventCloudsManagementWsnClient =
                WsClientFactory.createWsClient(
                        EventCloudsManagementWsnApi.class,
                        this.eventCloudsManagementServer.getEndpoint()
                                .getEndpointInfo()
                                .getAddress());

        this.eventCloudsManagementWsnClient.createEventCloud(InputOutputMonitoringTest.STREAM_URL);
    }

    private void initializeJavaProxies() throws EventCloudIdNotManaged {
        this.publishProxy =
                (PublishProxy) ProxyFactory.newPublishProxy(
                        this.registryUrl,
                        InputOutputMonitoringTest.EVENTCLOUD_ID);
        this.subscribeProxy =
                (SubscribeProxy) ProxyFactory.newSubscribeProxy(
                        this.registryUrl,
                        InputOutputMonitoringTest.EVENTCLOUD_ID);
    }

    private void initializeWebServices() {
        this.publishWsnServiceEndpointUrl =
                this.eventCloudsManagementWsnClient.deployPublishWsnService(InputOutputMonitoringTest.STREAM_URL);
        this.subscribeWsnServiceEndpointUrl =
                this.eventCloudsManagementWsnClient.deploySubscribeWsnService(InputOutputMonitoringTest.STREAM_URL);

        this.publishWsnClient =
                WsClientFactory.createWsClient(
                        PublishWsnApi.class, this.publishWsnServiceEndpointUrl);
        this.subscribeWsnClient =
                WsClientFactory.createWsClient(
                        SubscribeWsnApi.class,
                        this.subscribeWsnServiceEndpointUrl);

        this.monitoringService = new BasicNotificationConsumer();
        this.monitoringServer =
                WsDeployer.deployWebService(
                        this.monitoringService, "monitoring",
                        WsTest.WEBSERVICES_PORT);

        this.notificationConsumerService =
                new CompoundEventNotificationConsumer();
        this.notificationConsumerServer =
                WsDeployer.deployWebService(
                        this.notificationConsumerService, "subscriber",
                        WsTest.WEBSERVICES_PORT);
        this.notificationConsumerWsEndpointUrl =
                this.notificationConsumerServer.getEndpoint()
                        .getEndpointInfo()
                        .getAddress();
    }

    private static class CustomSignalNotificationListener extends
            SignalNotificationListener {

        private static final long serialVersionUID = 160L;

        @Override
        public void onNotification(SubscriptionId id, String eventId) {
        }

    }

    @After
    public void tearDown() {
        try {
            EventCloudsRegistry registry =
                    EventCloudsRegistryFactory.lookupEventCloudsRegistry(this.registryUrl);
            registry.unregisterProxy(
                    InputOutputMonitoringTest.EVENTCLOUD_ID, this.publishProxy);
            registry.unregisterProxy(
                    InputOutputMonitoringTest.EVENTCLOUD_ID,
                    this.subscribeProxy);
        } catch (IOException e) {
            e.printStackTrace();
        }

        ComponentUtils.terminateComponent(this.publishProxy);
        ComponentUtils.terminateComponent(this.subscribeProxy);

        this.eventCloudsManagementWsnClient.destroyEventCloud(InputOutputMonitoringTest.STREAM_URL);
        this.eventCloudsManagementServer.destroy();
        this.monitoringServer.destroy();
        this.notificationConsumerServer.destroy();
    }

}
