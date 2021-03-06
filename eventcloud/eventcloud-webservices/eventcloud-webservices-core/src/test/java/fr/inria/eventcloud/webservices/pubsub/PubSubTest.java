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
package fr.inria.eventcloud.webservices.pubsub;

import org.apache.cxf.endpoint.Server;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.NodeFactory;

import fr.inria.eventcloud.EventCloudDescription;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.generators.CompoundEventGenerator;
import fr.inria.eventcloud.deployment.EventCloudComponentsManager;
import fr.inria.eventcloud.deployment.EventCloudDeploymentDescriptor;
import fr.inria.eventcloud.deployment.JunitEventCloudInfrastructureDeployer;
import fr.inria.eventcloud.pubsub.SubscriptionTestUtils;
import fr.inria.eventcloud.runners.EachPublishSubscribeAlgorithm;
import fr.inria.eventcloud.webservices.WsTest;
import fr.inria.eventcloud.webservices.api.PublishWsApi;
import fr.inria.eventcloud.webservices.api.SubscribeWsApi;
import fr.inria.eventcloud.webservices.api.subscribers.BindingSubscriberWsApi;
import fr.inria.eventcloud.webservices.api.subscribers.CompoundEventSubscriberWsApi;
import fr.inria.eventcloud.webservices.api.subscribers.SignalSubscriberWsApi;
import fr.inria.eventcloud.webservices.deployment.PublishWsProxyInfo;
import fr.inria.eventcloud.webservices.deployment.SubscribeWsProxyInfo;
import fr.inria.eventcloud.webservices.deployment.WsDeployer;
import fr.inria.eventcloud.webservices.factories.WsClientFactory;

/**
 * Test cases for {@link PublishWsApi publish web service proxies} and
 * {@link SubscribeWsApi subscribe web service proxies}.
 * 
 * @author bsauvan
 * @author lpellegr
 */
@RunWith(EachPublishSubscribeAlgorithm.class)
public class PubSubTest extends WsTest {

    private static final Logger LOG = LoggerFactory.getLogger(PubSubTest.class);

    private JunitEventCloudInfrastructureDeployer deployer;

    private EventCloudId id;

    private SubscribeWsProxyInfo subscribeWsProxyInfo;

    private PublishWsProxyInfo publishWsProxyInfo;

    private SubscribeWsApi subscribeWsClient;

    private PublishWsApi publishWsClient;

    private BasicSubscriberWs subscriberService;

    private Server signalSubscriberServer;

    private Server bindingSubscriberServer;

    private Server eventSubscriberServer;

    private String signalSubscriberEndpointUrl;

    private String bindingSubscriberEndpointUrl;

    private String eventSubscriberEndpointUrl;

    private EventCloudComponentsManager componentsManager;

    @Before
    public void setUp() {
        this.initEventCloudEnvironmentAndClients();
    }

    @Test(timeout = 180000)
    public void testSubscribeSignalNotificationListener() throws Exception {
        // Subscribes for any quadruples
        String subscriptionId =
                this.subscribeWsClient.subscribeSignal(
                        "SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { ?s ?p ?o } }",
                        this.signalSubscriberEndpointUrl);

        SubscriptionTestUtils.waitSubscriptionIndexation();

        // Publishes an event
        CompoundEvent event = CompoundEventGenerator.random(1);
        this.publishWsClient.publish(event);

        synchronized (this.subscriberService.signalsReceived) {
            while (this.subscriberService.signalsReceived.getValue() != 1) {
                this.subscriberService.signalsReceived.wait();
            }
        }

        // Unsubscribes
        this.subscribeWsClient.unsubscribe(subscriptionId);

        // Publishes a second event
        event = CompoundEventGenerator.random(1);
        this.publishWsClient.publish(event);

        // Checks that no more events are received
        synchronized (this.subscriberService.signalsReceived) {
            this.subscriberService.signalsReceived.wait(4000);
            Assert.assertTrue(this.subscriberService.signalsReceived.getValue() == 1);
        }
    }

    @Test(timeout = 180000)
    public void testSubscribeBindingNotificationListener() throws Exception {
        // Subscribes for any quadruples
        String subscriptionId =
                this.subscribeWsClient.subscribeBinding(
                        "PREFIX foaf: <http://xmlns.com/foaf/0.1/> SELECT ?name ?email ?g WHERE { GRAPH ?g { ?id foaf:name ?name . ?id foaf:email ?email } }",
                        this.bindingSubscriberEndpointUrl);

        SubscriptionTestUtils.waitSubscriptionIndexation();

        // Publishes 5 events
        long publicationTime = System.currentTimeMillis();

        Quadruple q1 =
                new Quadruple(
                        NodeFactory.createURI("https://plus.google.com/825349613"),
                        NodeFactory.createURI("https://plus.google.com/107234124364605485774"),
                        NodeFactory.createURI("http://xmlns.com/foaf/0.1/email"),
                        NodeFactory.createLiteral("user1@company.com"));
        q1.setPublicationTime(publicationTime);
        this.publishWsClient.publish(q1);

        Quadruple q2 =
                new Quadruple(
                        NodeFactory.createURI("https://plus.google.com/825349613"),
                        NodeFactory.createURI("https://plus.google.com/107234124364605485774"),
                        NodeFactory.createURI("http://xmlns.com/foaf/0.1/name"),
                        NodeFactory.createLiteral("User1"));
        q2.setPublicationTime(publicationTime);
        this.publishWsClient.publish(q2);

        publicationTime = System.currentTimeMillis();

        Quadruple q3 =
                new Quadruple(
                        NodeFactory.createURI("https://plus.google.com/3283940594/2011-2012-08-30-18:13:05"),
                        NodeFactory.createURI("https://plus.google.com/107545688688906540962"),
                        NodeFactory.createURI("http://xmlns.com/foaf/0.1/email"),
                        NodeFactory.createLiteral("user2@company.com"));
        q3.setPublicationTime(publicationTime);
        this.publishWsClient.publish(q3);

        Quadruple q4 =
                new Quadruple(
                        NodeFactory.createURI("https://plus.google.com/124324034/2011-2012-08-30-19:04:54"),
                        NodeFactory.createURI("https://plus.google.com/14023231238123495031/"),
                        NodeFactory.createURI("http://xmlns.com/foaf/0.1/name"),
                        NodeFactory.createLiteral("User 3"));
        q4.setPublicationTime();
        this.publishWsClient.publish(q4);

        Quadruple q5 =
                new Quadruple(
                        NodeFactory.createURI("https://plus.google.com/3283940594/2011-2012-08-30-18:13:05"),
                        NodeFactory.createURI("https://plus.google.com/107545688688906540962"),
                        NodeFactory.createURI("http://xmlns.com/foaf/0.1/name"),
                        NodeFactory.createLiteral("User 2"));
        q5.setPublicationTime(publicationTime);
        this.publishWsClient.publish(q5);

        synchronized (this.subscriberService.bindingsReceived) {
            while (this.subscriberService.bindingsReceived.size() != 2) {
                this.subscriberService.bindingsReceived.wait();
            }
        }

        LOG.info("Bindings received!");

        // Unsubscribes
        this.subscribeWsClient.unsubscribe(subscriptionId);

        Thread.sleep(2000);

        // Publishes a 6th quadruple
        Quadruple q6 =
                new Quadruple(
                        NodeFactory.createURI("https://plus.google.com/124324034/2011-2012-08-30-19:04:54"),
                        NodeFactory.createURI("https://plus.google.com/14023231238123495031/"),
                        NodeFactory.createURI("http://xmlns.com/foaf/0.1/email"),
                        NodeFactory.createLiteral("user3@company.com"));
        q6.setPublicationTime(publicationTime);
        this.publishWsClient.publish(q6);

        // Checks that no more events are received
        synchronized (this.subscriberService.bindingsReceived) {
            this.subscriberService.bindingsReceived.wait(4000);
            Assert.assertTrue(this.subscriberService.bindingsReceived.size() == 2);
        }
    }

    @Test(timeout = 180000)
    public void testSubscribeCompoundEventNotificationListener()
            throws Exception {
        // Subscribes for any quadruples
        String subscriptionId =
                this.subscribeWsClient.subscribeCompoundEvent(
                        "SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { ?s ?p ?o } }",
                        this.eventSubscriberEndpointUrl);

        SubscriptionTestUtils.waitSubscriptionIndexation();

        // Publishes an event
        this.publishWsClient.publish(CompoundEventGenerator.random(4));

        synchronized (this.subscriberService.eventsReceived) {
            while (this.subscriberService.eventsReceived.size() != 1) {
                this.subscriberService.eventsReceived.wait();
            }
        }

        LOG.info("Compound event received!");

        // Unsubscribes
        this.subscribeWsClient.unsubscribe(subscriptionId);

        // Publishes a second event
        this.publishWsClient.publish(CompoundEventGenerator.random(4));

        // Checks that no more events are received
        synchronized (this.subscriberService.eventsReceived) {
            this.subscriberService.eventsReceived.wait(4000);
            Assert.assertTrue(this.subscriberService.eventsReceived.size() == 1);
        }
    }

    private void initEventCloudEnvironmentAndClients() {
        this.deployer = new JunitEventCloudInfrastructureDeployer();

        EventCloudDeploymentDescriptor deploymentDescriptor =
                new EventCloudDeploymentDescriptor();

        this.componentsManager = WsTest.createAndStartComponentsManager();

        this.id =
                this.deployer.newEventCloud(
                        new EventCloudDescription(
                                "http://streams.event-processing.org/ids/TaxiUc"),
                        deploymentDescriptor, this.componentsManager, 1, 1);

        this.subscribeWsProxyInfo =
                WsDeployer.deploySubscribeWsProxy(
                        this.componentsManager,
                        this.deployer.getEventCloudsRegistryUrl(),
                        this.id.getStreamUrl(), "subscribe");
        this.publishWsProxyInfo =
                WsDeployer.deployPublishWsProxy(
                        this.componentsManager,
                        this.deployer.getEventCloudsRegistryUrl(),
                        this.id.getStreamUrl(), "publish");

        this.subscribeWsClient =
                WsClientFactory.createWsClient(
                        SubscribeWsApi.class,
                        this.subscribeWsProxyInfo.getWsEndpointUrl());
        this.publishWsClient =
                WsClientFactory.createWsClient(
                        PublishWsApi.class,
                        this.publishWsProxyInfo.getWsEndpointUrl());

        this.subscriberService = new BasicSubscriberWs();
        this.signalSubscriberServer =
                WsDeployer.deployWebService(
                        SignalSubscriberWsApi.class, this.subscriberService,
                        "signal-subscriber", WsTest.WEBSERVICES_PORT);
        this.signalSubscriberEndpointUrl =
                this.signalSubscriberServer.getEndpoint()
                        .getEndpointInfo()
                        .getAddress();
        this.bindingSubscriberServer =
                WsDeployer.deployWebService(
                        BindingSubscriberWsApi.class, this.subscriberService,
                        "binding-subscriber", WsTest.WEBSERVICES_PORT);
        this.bindingSubscriberEndpointUrl =
                this.bindingSubscriberServer.getEndpoint()
                        .getEndpointInfo()
                        .getAddress();
        this.eventSubscriberServer =
                WsDeployer.deployWebService(
                        CompoundEventSubscriberWsApi.class,
                        this.subscriberService, "event-subscriber",
                        WsTest.WEBSERVICES_PORT);
        this.eventSubscriberEndpointUrl =
                this.eventSubscriberServer.getEndpoint()
                        .getEndpointInfo()
                        .getAddress();
    }

    @After
    public void tearDown() {
        this.subscribeWsProxyInfo.destroy();
        this.publishWsProxyInfo.destroy();

        this.deployer.undeploy();

        this.signalSubscriberServer.destroy();
        this.bindingSubscriberServer.destroy();
        this.eventSubscriberServer.destroy();

        WsTest.stopAndTerminateComponentsManager(this.componentsManager);
    }

}
