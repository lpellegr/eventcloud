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

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.JaxWsClientFactoryBean;
import org.junit.Test;
import org.oasis_open.docs.wsn.b_2.FilterType;
import org.oasis_open.docs.wsn.b_2.Notify;
import org.oasis_open.docs.wsn.b_2.Subscribe;
import org.oasis_open.docs.wsn.b_2.TopicExpressionType;
import org.oasis_open.docs.wsn.bw_2.NotificationConsumer;
import org.oasis_open.docs.wsn.bw_2.NotificationProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.deployment.JunitEventCloudInfrastructureDeployer;
import fr.inria.eventcloud.parsers.RdfParser;
import fr.inria.eventcloud.translators.wsnotif.notify.EventToNotificationMessageTranslator;
import fr.inria.eventcloud.utils.Callback;
import fr.inria.eventcloud.webservices.deployment.WebServiceDeployer;
import fr.inria.eventcloud.webservices.services.SubscriberServiceImpl;

/**
 * Class used to test a subscribe proxy component and a publish proxy component
 * by using web services.
 * 
 * @author lpellegr
 * @author bsauvan
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

    @Test(timeout = 90000)
    public void testPublishSubscribeWsProxies() throws Exception {
        JunitEventCloudInfrastructureDeployer deployer =
                new JunitEventCloudInfrastructureDeployer();

        EventCloudId ecId = deployer.createEventCloud(10);

        // Web services which are deployed
        String subscribeWsUrl =
                WebServiceDeployer.deploySubscribeWebService(
                        deployer.getEventCloudsRegistryUrl(), ecId.toUrl(),
                        "subscribe", 8889);

        String publishWsUrl =
                WebServiceDeployer.deployPublishWebService(
                        deployer.getEventCloudsRegistryUrl(), ecId.toUrl(),
                        "publish", 8890);

        SubscriberServiceImpl subscriberService = new SubscriberServiceImpl();
        String subscriberWsUrl =
                WebServiceDeployer.deployWebService(
                        subscriberService, "subscriber", 8891);

        // Clients associated to Web services
        Client subscribeClient =
                createWsClient(NotificationProducer.class, subscribeWsUrl);

        Client publishClient =
                createWsClient(NotificationConsumer.class, publishWsUrl);

        // Creates the subscribe request
        Subscribe subscribeRequest = new Subscribe();
        FilterType filterType = new FilterType();
        TopicExpressionType topicExpressionType = new TopicExpressionType();
        topicExpressionType.getContent().add(
                "fireman_event:cardiacRythmFiremanTopic");

        JAXBElement<TopicExpressionType> jaxbElement =
                new JAXBElement<TopicExpressionType>(
                        new QName(
                                "http://docs.oasis-open.org/wsn/b-2",
                                "TopicExpression"), TopicExpressionType.class,
                        topicExpressionType);
        filterType.getAny().add(jaxbElement);
        subscribeRequest.setFilter(filterType);

        W3CEndpointReferenceBuilder endPointReferenceBuilder =
                new W3CEndpointReferenceBuilder();
        endPointReferenceBuilder.address(subscriberWsUrl);
        subscribeRequest.setConsumerReference(endPointReferenceBuilder.build());

        // Subscribes for any events with topic
        // fireman_event:cardiacRythmFiremanTopic
        subscribeClient.invoke("Subscribe", subscribeRequest);

        // Creates the notify request
        Notify notifyRequest = new Notify();
        Collection<CompoundEvent> events = new Collection<CompoundEvent>();
        events.add(new CompoundEvent(read("/notification-01.trig")));
        EventToNotificationMessageTranslator translator =
                new EventToNotificationMessageTranslator();
        for (CompoundEvent event : events) {
            notifyRequest.getNotificationMessage().add(
                    translator.translate(event));
        }
        publishClient.invoke("Notify", notifyRequest);

        while (subscriberService.eventsReceived.size() == 0) {
            log.info("Waiting for the reception of compound events");
            Thread.sleep(500);
        }

        deployer.undeploy();
    }

    private static Collection<Quadruple> read(String file) {
        final Collection<Quadruple> quadruples = new Collection<Quadruple>();

        RdfParser.parse(
                inputStreamFrom(file), SerializationFormat.TriG,
                new Callback<Quadruple>() {
                    @Override
                    public void execute(Quadruple quadruple) {
                        quadruples.add(quadruple);
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
