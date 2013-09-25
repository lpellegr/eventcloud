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
package fr.inria.eventcloud.webservices.pubsub;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.cxf.endpoint.Server;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.SubscribeResponse;
import org.objectweb.proactive.core.util.ProActiveInet;
import org.objectweb.proactive.extensions.p2p.structured.utils.ComponentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soceda.socialfilter.relationshipstrengthengine.RelationshipStrengthEngineManager;
import org.soceda.socialfilter.relationshipstrengthengine.RelationshipStrengthEngineManagerFactory;
import org.soceda.socialfilter.relationshipstrengthengine.Utils;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import fr.inria.eventcloud.EventCloudDescription;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.deployment.EventCloudDeploymentDescriptor;
import fr.inria.eventcloud.deployment.JunitEventCloudInfrastructureDeployer;
import fr.inria.eventcloud.pubsub.SubscriptionTestUtils;
import fr.inria.eventcloud.translators.wsn.WsnHelper;
import fr.inria.eventcloud.utils.Callback;
import fr.inria.eventcloud.utils.RDFReader;
import fr.inria.eventcloud.webservices.CompoundEventNotificationConsumer;
import fr.inria.eventcloud.webservices.WsTest;
import fr.inria.eventcloud.webservices.api.PublishWsnApi;
import fr.inria.eventcloud.webservices.api.SubscribeWsnApi;
import fr.inria.eventcloud.webservices.deployment.WsDeployer;
import fr.inria.eventcloud.webservices.deployment.WsnServiceInfo;
import fr.inria.eventcloud.webservices.factories.WsClientFactory;

/**
 * Test cases for {@link PublishWsnApi publish WS-Notification services} and
 * {@link SubscribeWsnApi subscribe WS-Notification services}.
 * 
 * @author bsauvan
 * @author lpellegr
 */
public class WsnPubSubTest extends WsTest {

    private static final Logger log =
            LoggerFactory.getLogger(WsnPubSubTest.class);

    private JunitEventCloudInfrastructureDeployer deployer;

    private EventCloudId id;

    private WsnServiceInfo subscribeWsnServiceInfo;

    private WsnServiceInfo publishWsnServiceInfo;

    private SubscribeWsnApi subscribeWsnClient;

    private PublishWsnApi publishWsnClient;

    private CompoundEventNotificationConsumer notificationConsumerService;

    private Server notificationConsumerServer;

    private String notificationConsumerEndpointUrl;

    @Before
    public void setUp() {
        EventCloudProperties.SOCIAL_FILTER_URL.setValue(null);
    }

    @Test(timeout = 180000)
    public void testPublishSubscribeWsnServices() throws Exception {
        // Initializes the EventCloud, web services and web service clients
        this.initEventCloudEnvironmentAndClients();

        // Creates the subscriber web service
        this.createNotificationConsumerWebService();

        int lastIndexOfSlash = this.id.getStreamUrl().lastIndexOf('/');

        // Extracts topic information
        QName topic =
                new QName(this.id.getStreamUrl().substring(
                        0, lastIndexOfSlash + 1), this.id.getStreamUrl()
                        .substring(lastIndexOfSlash + 1), "s");

        // Creates the subscribe request
        Subscribe subscribeRequest =
                WsnHelper.createSubscribeMessage(
                        this.notificationConsumerEndpointUrl, topic);

        // Subscribes for any events with topic TaxiUc
        SubscribeResponse subscribeResponse =
                this.subscribeWsnClient.subscribe(subscribeRequest);
        SubscriptionId subscriptionId =
                WsnHelper.getSubcriptionId(subscribeResponse);

        log.info("Subscription submitted, ID is " + subscriptionId);

        SubscriptionTestUtils.waitSubscriptionIndexation();

        // Creates the notify request
        Notify notifyRequest =
                WsnHelper.createNotifyMessage(
                        this.publishWsnServiceInfo.getWsEndpointUrl(), topic,
                        new CompoundEvent(this.read(
                                "/notification-01.trig", null)));

        // Publishes the event
        this.publishWsnClient.notify(notifyRequest);

        synchronized (this.notificationConsumerService.eventsReceived) {
            while (this.notificationConsumerService.eventsReceived.size() != 1) {
                this.notificationConsumerService.eventsReceived.wait();
            }
        }

        log.info("Compound event received!");

        // Unsubscribes
        this.subscribeWsnClient.unsubscribe(WsnHelper.createUnsubscribeRequest(subscriptionId));

        // Publishes a second event
        this.publishWsnClient.notify(notifyRequest);

        // Checks that no more events are received
        synchronized (this.notificationConsumerService.eventsReceived) {
            this.notificationConsumerService.eventsReceived.wait(4000);
            Assert.assertTrue(this.notificationConsumerService.eventsReceived.size() == 1);
        }
    }

    @Test(timeout = 180000)
    public void testPublishSubscribeWsnServicesWithSocialFilter()
            throws Exception {
        // Creates the subscriber web service
        this.createNotificationConsumerWebService();

        // Creates and initializes the social filter
        String source1 =
                replaceConsumerLoopbackIp("http://127.0.0.1:8891/source1");
        String source2 =
                replaceConsumerLoopbackIp("http://127.0.0.1:8891/source2");

        URL nodesUrl =
                Thread.currentThread().getContextClassLoader().getResource(
                        "social-filter-nodes.txt");
        URL relationshipsUrl =
                Thread.currentThread().getContextClassLoader().getResource(
                        "social-filter-relationships.txt");
        this.replaceConsumerLoopbackIpInSocialFilterFile(nodesUrl);
        this.replaceConsumerLoopbackIpInSocialFilterFile(relationshipsUrl);

        RelationshipStrengthEngineManager socialFilter =
                RelationshipStrengthEngineManagerFactory.newRelationshipStrengthEngineManager();

        Utils.deploySocialGraph(
                socialFilter, nodesUrl.toString(), relationshipsUrl.toString());
        EventCloudProperties.SOCIAL_FILTER_URL.setValue(Utils.getURI(socialFilter));

        // Initializes the EventCloud, web services and web service clients
        this.initEventCloudEnvironmentAndClients();

        int lastIndexOfSlash = this.id.getStreamUrl().lastIndexOf('/');

        // Extracts topic information
        QName topic =
                new QName(this.id.getStreamUrl().substring(
                        0, lastIndexOfSlash + 1), this.id.getStreamUrl()
                        .substring(lastIndexOfSlash + 1), "s");

        // Creates the subscribe request
        Subscribe subscribeRequest =
                WsnHelper.createSubscribeMessage(
                        this.notificationConsumerEndpointUrl, topic);

        // Subscribes for any events with topic TaxiUc
        this.subscribeWsnClient.subscribe(subscribeRequest);

        SubscriptionTestUtils.waitSubscriptionIndexation();

        // Creates the notify request emitted by source1
        Notify notifyRequest =
                WsnHelper.createNotifyMessage(
                        this.publishWsnServiceInfo.getWsEndpointUrl(), topic,
                        new CompoundEvent(this.read(
                                "/notification-01.trig", source1)));

        // Publishes the event
        this.publishWsnClient.notify(notifyRequest);

        synchronized (this.notificationConsumerService.eventsReceived) {
            while (this.notificationConsumerService.eventsReceived.size() != 1) {
                this.notificationConsumerService.eventsReceived.wait();
            }
        }

        log.info("Compound event received!");

        // Creates the notify request emitted by source2
        notifyRequest =
                WsnHelper.createNotifyMessage(
                        this.publishWsnServiceInfo.getWsEndpointUrl(), topic,
                        new CompoundEvent(this.read(
                                "/notification-01.trig", source2)));

        // Publishes the event
        this.publishWsnClient.notify(notifyRequest);

        synchronized (this.notificationConsumerService.eventsReceived) {
            this.notificationConsumerService.eventsReceived.wait(4000);
            Assert.assertTrue(this.notificationConsumerService.eventsReceived.size() == 1);
        }

        ComponentUtils.terminateComponent(socialFilter);
        EventCloudProperties.SOCIAL_FILTER_URL.setValue(null);
    }

    private void initEventCloudEnvironmentAndClients() {
        this.deployer = new JunitEventCloudInfrastructureDeployer();

        EventCloudDeploymentDescriptor deploymentDescriptor =
                new EventCloudDeploymentDescriptor();
        deploymentDescriptor.setComponentPoolManager(WsTest.COMPONENT_POOL_MANAGER);
        this.id =
                this.deployer.newEventCloud(
                        new EventCloudDescription(
                                "http://streams.event-processing.org/ids/TaxiUc"),
                        deploymentDescriptor, 1, 1);

        this.subscribeWsnServiceInfo =
                WsDeployer.deploySubscribeWsnService(
                        WsTest.COMPONENT_POOL_MANAGER,
                        this.deployer.getEventCloudsRegistryUrl(),
                        this.id.getStreamUrl(), "subscribe",
                        WsTest.WEBSERVICES_PORT);
        this.publishWsnServiceInfo =
                WsDeployer.deployPublishWsnService(
                        WsTest.COMPONENT_POOL_MANAGER,
                        this.deployer.getEventCloudsRegistryUrl(),
                        this.id.getStreamUrl(), "publish",
                        WsTest.WEBSERVICES_PORT);

        this.subscribeWsnClient =
                WsClientFactory.createWsClient(
                        SubscribeWsnApi.class,
                        this.subscribeWsnServiceInfo.getWsEndpointUrl());
        this.publishWsnClient =
                WsClientFactory.createWsClient(
                        PublishWsnApi.class,
                        this.publishWsnServiceInfo.getWsEndpointUrl());
    }

    private void createNotificationConsumerWebService() {
        this.notificationConsumerService =
                new CompoundEventNotificationConsumer();
        this.notificationConsumerServer =
                WsDeployer.deployWebService(
                        this.notificationConsumerService, "subscriber",
                        WsTest.WEBSERVICES_PORT);
        this.notificationConsumerEndpointUrl =
                this.notificationConsumerServer.getEndpoint()
                        .getEndpointInfo()
                        .getAddress();
    }

    private List<Quadruple> read(String uri, final String publicationSource) {
        final Builder<Quadruple> result = ImmutableList.builder();

        RDFReader.read(
                this.getClass().getResourceAsStream(uri),
                SerializationFormat.TriG, new Callback<Quadruple>() {

                    @Override
                    public void execute(Quadruple quadruple) {
                        if (publicationSource != null) {
                            quadruple.setPublicationSource(publicationSource);
                        }
                        result.add(quadruple);
                    }

                });

        return result.build();
    }

    private void replaceConsumerLoopbackIpInSocialFilterFile(URL fileUrl)
            throws URISyntaxException, IOException {

        String content = Resources.toString(fileUrl, Charsets.UTF_8);

        Files.write(
                replaceConsumerLoopbackIp(content).getBytes(Charsets.UTF_8),
                new File(fileUrl.toURI()));
    }

    private static String replaceConsumerLoopbackIp(String Url) {
        return Url.replaceAll(
                "127.0.0.1",
                ProActiveInet.getInstance().getInetAddress().getHostAddress())
                .replaceAll(":[0-9]{1,5}/", ":" + WsTest.WEBSERVICES_PORT + "/");
    }

    @After
    public void tearDown() {
        this.subscribeWsnServiceInfo.destroy();
        this.publishWsnServiceInfo.destroy();
        this.deployer.undeploy();
        this.notificationConsumerServer.destroy();
        WsTest.COMPONENT_POOL_MANAGER.stop();
    }

}
