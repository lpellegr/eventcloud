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
package fr.inria.eventcloud.webservices.pubsub;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.JaxWsClientFactoryBean;
import org.junit.Test;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.bw_2.NotificationConsumer;
import org.oasis_open.docs.wsn.bw_2.NotificationProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.deployment.JunitEventCloudInfrastructureDeployer;
import fr.inria.eventcloud.parsers.RdfParser;
import fr.inria.eventcloud.translators.wsn.WsNotificationMessageBuilder;
import fr.inria.eventcloud.utils.Callback;
import fr.inria.eventcloud.webservices.deployment.WebServiceDeployer;
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

    private static <T> Client createWsClient(Class<T> serviceClass,
                                             String serviceAddress) {
        JaxWsClientFactoryBean factory = new JaxWsClientFactoryBean();
        factory.setServiceClass(serviceClass);
        factory.setAddress(serviceAddress);
        return factory.create();
    }

    @Test(timeout = 180000)
    public void testPublishSubscribeWsProxies() throws Exception {
        JunitEventCloudInfrastructureDeployer deployer =
                new JunitEventCloudInfrastructureDeployer();

        EventCloudId ecId = deployer.createEventCloud(10);

        // Web services which are deployed
        String subscribeWsUrl =
                WebServiceDeployer.deploySubscribeWebService(
                        deployer.getEventCloudsRegistryUrl(),
                        ecId.getStreamUrl(), "subscribe", 8889);

        String publishWsUrl =
                WebServiceDeployer.deployPublishWebService(
                        deployer.getEventCloudsRegistryUrl(),
                        ecId.getStreamUrl(), "publish", 8890);

        SubscriberServiceImpl subscriberService = new SubscriberServiceImpl();
        String subscriberWsUrl =
                WebServiceDeployer.deployWebService(
                        subscriberService, "subscriber", 8891);

        // Clients associated to Web services
        Client subscribeClient =
                createWsClient(NotificationProducer.class, subscribeWsUrl);

        Client publishClient =
                createWsClient(NotificationConsumer.class, publishWsUrl);

        String topicNamespace = "http://example.org/namespace/";
        String topicNsPrefix = "ns";
        String topicExpression = "TaxiUc";

        // Creates the subscribe request
        Subscribe subscribeRequest =
                WsNotificationMessageBuilder.createSubscribeMessage(
                        subscriberWsUrl, topicNamespace, topicNsPrefix,
                        topicExpression);

        // Subscribes for any events with topic TaxiUc
        subscribeClient.invoke("Subscribe", subscribeRequest);

        // Creates the notify request
        Notify notifyRequest =
                WsNotificationMessageBuilder.createNotifyMessage(
                        topicNamespace, topicNsPrefix, topicExpression,
                        new CompoundEvent(read(
                                "/notification-01.trig",
                                SerializationFormat.TriG)));

        publishClient.invoke("Notify", notifyRequest);

        synchronized (subscriberService.eventsReceived) {
            while (subscriberService.eventsReceived.size() != 1) {
                subscriberService.eventsReceived.wait();
            }
        }

        log.info("Compound event received!");

        deployer.undeploy();
    }

    private static List<Quadruple> read(String file, SerializationFormat format) {
        final List<Quadruple> quadruples = new ArrayList<Quadruple>();

        RdfParser.parse(
                inputStreamFrom(file), format, new Callback<Quadruple>() {
                    public void execute(Quadruple quad) {
                        quadruples.add(quad);
                    }

                });

        return quadruples;
    }

    private static InputStream inputStreamFrom(String file) {
        InputStream is = null;

        if (file != null) {
            is = PubSubTest.class.getResourceAsStream(file);
        }

        return is;
    }

}
