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
package fr.inria.eventcloud.webservices.pubsub;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.cxf.endpoint.Server;
import org.etsi.uri.gcm.util.GCM;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.bw_2.NotificationConsumer;
import org.oasis_open.docs.wsn.bw_2.NotificationProducer;
import org.objectweb.fractal.api.Interface;
import org.objectweb.proactive.extensions.p2p.structured.utils.ComponentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.soceda.socialfilter.relationshipstrengthengine.RelationshipStrengthEngine;
import org.soceda.socialfilter.relationshipstrengthengine.RelationshipStrengthEngineManager;
import org.soceda.socialfilter.relationshipstrengthengine.RelationshipStrengthEngineManagerAttributeController;
import org.soceda.socialfilter.relationshipstrengthengine.RelationshipStrengthEngineManagerFactory;
import org.soceda.socialfilter.socialnetwork.SocialNetwork;

import fr.inria.eventcloud.EventCloudDescription;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.deployment.JunitEventCloudInfrastructureDeployer;
import fr.inria.eventcloud.parsers.RdfParser;
import fr.inria.eventcloud.translators.wsn.WsnHelper;
import fr.inria.eventcloud.utils.Callback;
import fr.inria.eventcloud.webservices.deployment.ServiceInformation;
import fr.inria.eventcloud.webservices.deployment.WebServiceDeployer;
import fr.inria.eventcloud.webservices.factories.WsClientFactory;
import fr.inria.eventcloud.webservices.services.SubscriberServiceImpl;

/**
 * Class used to test a subscribe proxy component and a publish proxy component
 * by using web services.
 * 
 * @author bsauvan
 * @author lpellegr
 */
public class PubSubTest {

    private static final Logger log = LoggerFactory.getLogger(PubSubTest.class);

    private JunitEventCloudInfrastructureDeployer deployer;

    private EventCloudId ecId;

    private ServiceInformation subscribeServiceInformation;

    private ServiceInformation publishServiceInformation;

    private NotificationProducer subscribeClient;

    private NotificationConsumer publishClient;

    private SubscriberServiceImpl subscriberService;

    private Server subscriberWsServer;

    private String subscriberWsEndpointUrl;

    @Before
    public void init() {
        EventCloudProperties.SOCIAL_FILTER_URL.setValue(null);
    }

    @Test(timeout = 180000)
    public void testPublishSubscribeWsProxies() throws Exception {
        // Initializes the Event Cloud, proxies and web service clients
        this.initEventCloudAndProxiesAndClients();

        // Creates the subscriber web service
        this.createSubscriberWebService();

        int lastIndexOfSlash = this.ecId.getStreamUrl().lastIndexOf('/');

        // Extracts topic information
        QName topic =
                new QName(this.ecId.getStreamUrl().substring(
                        0, lastIndexOfSlash + 1), this.ecId.getStreamUrl()
                        .substring(lastIndexOfSlash + 1), "s");

        // Creates the subscribe request
        Subscribe subscribeRequest =
                WsnHelper.createSubscribeMessage(
                        this.subscriberWsEndpointUrl, topic);

        // Subscribes for any events with topic TaxiUc
        this.subscribeClient.subscribe(subscribeRequest);

        // Creates the notify request
        Notify notifyRequest =
                WsnHelper.createNotifyMessage(
                        this.publishServiceInformation.getServer()
                                .getEndpoint()
                                .toString(), topic, new CompoundEvent(
                                this.read(
                                        "/notification-01.trig",
                                        SerializationFormat.TriG, null)));

        // Publishes the event
        this.publishClient.notify(notifyRequest);

        synchronized (this.subscriberService.eventsReceived) {
            while (this.subscriberService.eventsReceived.size() != 1) {
                this.subscriberService.eventsReceived.wait();
            }
        }

        log.info("Compound event received!");
    }

    @Test(timeout = 180000)
    public void testPublishSubscribeWsProxiesWithSocialFilter()
            throws Exception {
        // Creates the subscriber web service
        this.createSubscriberWebService();

        // Creates and initializes the social filter
        String source1 = "http://127.0.0.1:8891/source1";
        String source2 = "http://127.0.0.1:8891/source2";
        SocialNetwork socialNetwork = new SocialNetwork();
        socialNetwork.add_node(source1);
        socialNetwork.add_node(source2);
        socialNetwork.add_node(this.subscriberWsEndpointUrl);
        socialNetwork.add_relationship(source1, this.subscriberWsEndpointUrl);
        socialNetwork.add_relationship(source2, this.subscriberWsEndpointUrl);
        socialNetwork.get_relationship(source1, this.subscriberWsEndpointUrl)
                .set_trust((float) 0.8);
        socialNetwork.get_relationship(source2, this.subscriberWsEndpointUrl)
                .set_trust((float) 0.4);
        RelationshipStrengthEngineManager socialFilter =
                RelationshipStrengthEngineManagerFactory.newRelationshipStrengthEngineManager(new RelationshipStrengthEngine(
                        socialNetwork));
        String socialFilterUri =
                ((RelationshipStrengthEngineManagerAttributeController) GCM.getAttributeController(((Interface) socialFilter).getFcItfOwner())).getComponentURI();
        EventCloudProperties.SOCIAL_FILTER_URL.setValue(socialFilterUri);

        // Initializes the Event Cloud, proxies and web service clients
        this.initEventCloudAndProxiesAndClients();

        int lastIndexOfSlash = this.ecId.getStreamUrl().lastIndexOf('/');

        // Extracts topic information
        QName topic =
                new QName(this.ecId.getStreamUrl().substring(
                        0, lastIndexOfSlash + 1), this.ecId.getStreamUrl()
                        .substring(lastIndexOfSlash + 1), "s");

        // Creates the subscribe request
        Subscribe subscribeRequest =
                WsnHelper.createSubscribeMessage(
                        this.subscriberWsEndpointUrl, topic);

        // Subscribes for any events with topic TaxiUc
        this.subscribeClient.subscribe(subscribeRequest);

        // Creates the notify request emitted by source1
        Notify notifyRequest =
                WsnHelper.createNotifyMessage(
                        this.publishServiceInformation.getServer()
                                .getEndpoint()
                                .toString(), topic, new CompoundEvent(
                                this.read(
                                        "/notification-01.trig",
                                        SerializationFormat.TriG, source1)));

        // Publishes the event
        this.publishClient.notify(notifyRequest);

        synchronized (this.subscriberService.eventsReceived) {
            while (this.subscriberService.eventsReceived.size() != 1) {
                this.subscriberService.eventsReceived.wait();
            }
        }

        log.info("Compound event received!");

        // Creates the notify request emitted by source2
        notifyRequest =
                WsnHelper.createNotifyMessage(
                        this.publishServiceInformation.getServer()
                                .getEndpoint()
                                .toString(), topic, new CompoundEvent(
                                this.read(
                                        "/notification-01.trig",
                                        SerializationFormat.TriG, source2)));

        // Publishes the event
        this.publishClient.notify(notifyRequest);

        synchronized (this.subscriberService.eventsReceived) {
            this.subscriberService.eventsReceived.wait(4000);
            Assert.assertTrue(this.subscriberService.eventsReceived.size() == 1);
        }

        ComponentUtils.terminateComponent(socialFilter);
        EventCloudProperties.SOCIAL_FILTER_URL.setValue(null);
    }

    private void initEventCloudAndProxiesAndClients() {
        this.deployer = new JunitEventCloudInfrastructureDeployer();

        this.ecId =
                this.deployer.newEventCloud(
                        new EventCloudDescription(
                                "http://streams.event-processing.org/ids/TaxiUc"),
                        1, 10);

        // Web services which are deployed
        this.subscribeServiceInformation =
                WebServiceDeployer.deploySubscribeWebService(
                        this.deployer.getEventCloudsRegistryUrl(),
                        this.ecId.getStreamUrl(), "subscribe", 8889);

        this.publishServiceInformation =
                WebServiceDeployer.deployPublishWebService(
                        this.deployer.getEventCloudsRegistryUrl(),
                        this.ecId.getStreamUrl(), "publish", 8890);

        // Clients associated to Web services
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
    }

    private void createSubscriberWebService() {
        this.subscriberService = new SubscriberServiceImpl();
        this.subscriberWsServer =
                WebServiceDeployer.deployWebService(
                        this.subscriberService, "subscriber", 8891);
        this.subscriberWsEndpointUrl =
                this.subscriberWsServer.getEndpoint()
                        .getEndpointInfo()
                        .getAddress();
    }

    private List<Quadruple> read(String file, SerializationFormat format,
                                 final String publicationSource) {
        final List<Quadruple> quadruples = new ArrayList<Quadruple>();

        RdfParser.parse(
                this.inputStreamFrom(file), format, new Callback<Quadruple>() {
                    @Override
                    public void execute(Quadruple quad) {
                        if (publicationSource != null) {
                            quad.setPublicationSource(publicationSource);
                        }
                        quadruples.add(quad);
                    }

                });

        return quadruples;
    }

    private InputStream inputStreamFrom(String file) {
        InputStream is = null;

        if (file != null) {
            is = PubSubTest.class.getResourceAsStream(file);
        }

        return is;
    }

    @After
    public void tearDown() {
        this.subscribeServiceInformation.destroy();
        this.publishServiceInformation.destroy();
        this.deployer.undeploy();
        this.subscriberWsServer.destroy();
    }

}
