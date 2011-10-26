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

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
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
import org.openjena.atlas.lib.Sink;
import org.openjena.riot.RiotReader;
import org.openjena.riot.lang.LangRIOT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.core.Quad;
import com.petalslink.wsn.service.wsnproducer.NotificationProducer;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.deployment.JunitEventCloudInfrastructureDeployer;
import fr.inria.eventcloud.translators.wsnotif.notify.EventToNotificationMessageTranslator;
import fr.inria.eventcloud.webservices.deployment.WsProxyDeployer;
import fr.inria.eventcloud.webservices.services.ISubscriberService;
import fr.inria.eventcloud.webservices.services.SubscriberService;

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

    @Test
    public void testPublishSubscribeWsProxies() throws Exception {
        JunitEventCloudInfrastructureDeployer deployer =
                new JunitEventCloudInfrastructureDeployer();

        EventCloudId ecId = deployer.createEventCloud(10);

        // Web services which are deployed
        String subscribeWsUrl =
                WsProxyDeployer.deploySubscribeWebService(
                        deployer.getEventCloudsRegistryUrl(), ecId.toUrl(),
                        "subscribe", 8889);

        String publishWsUrl =
                WsProxyDeployer.deployPublishWebService(
                        deployer.getEventCloudsRegistryUrl(), ecId.toUrl(),
                        "publish", 8890);

        SubscriberService subscriberService = new SubscriberService();

        String subscriberWsUrl =
                WsProxyDeployer.deployWebService(
                        subscriberService, "subscriber", 8891);

        // WsProxyDeployer.deploySubscriberWebService("subscriber", 8891);

        // Clients associated to Web services
        // Client subscribeClient =
        // createWsClient(NotificationProducer.class, subscribeWsUrl);
        //
        // Client publishClient =
        // createWsClient(NotificationConsumer.class, publishWsUrl);

        Client subscriberClient =
                createWsClient(ISubscriberService.class, subscriberWsUrl);

        // Creates the subscribe request
        // Subscribe subscribeRequest = new Subscribe();
        // FilterType filterType = new FilterType();
        // TopicExpressionType tet = new TopicExpressionType();
        // tet.getContent().add("fireman_event:cardiacRythmFiremanTopic");
        //
        // JAXBElement<TopicExpressionType> jaxbElement =
        // new JAXBElement<TopicExpressionType>(
        // new QName(
        // "http://docs.oasis-open.org/wsn/b-2",
        // "TopicExpressionType"),
        // TopicExpressionType.class, tet);
        // filterType.getAny().add(jaxbElement);
        // subscribeRequest.setFilter(filterType);
        //
        // W3CEndpointReferenceBuilder endPointReferenceBuilder =
        // new W3CEndpointReferenceBuilder();
        // endPointReferenceBuilder.address(subscriberWsUrl);
        // subscribeRequest.setConsumerReference(endPointReferenceBuilder.build());
        //
        // // subscribes for any events with topic
        // // fireman_event:cardiacRythmFiremanTopic
        // subscribeClient.invoke("Subscribe", subscribeRequest);

        // waits a little to be sure that the subscription has been indexed
        // Thread.sleep(2000);
        //
        // Collection<Event> events = new Collection<Event>();
        // events.add(new Event(read("/notification-01.trig")));
        // EventToNotificationMessageTranslator translator =
        // new EventToNotificationMessageTranslator();
        //
        // // creates the notify request
        // Notify notifyRequest = new Notify();
        // for (Event event : events) {
        // notifyRequest.getNotificationMessage().add(
        // translator.translate(event));
        // }
        // publishClient.invoke("Notify", notifyRequest);

        while (subscriberService.eventsReceived.size() == 0) {
            log.info("Waiting for the reception of event");
            Thread.sleep(500);
        }

        deployer.undeploy();
    }

    private static Collection<Quadruple> read(String file) {
        final Collection<Quadruple> quadruples = new Collection<Quadruple>();

        Sink<Quad> sink = new Sink<Quad>() {
            @Override
            public void send(final Quad quad) {
                quadruples.add(new Quadruple(
                        quad.getGraph(), quad.getSubject(),
                        quad.getPredicate(), quad.getObject()));
            }

            @Override
            public void close() {
            }

            @Override
            public void flush() {
            }

        };

        LangRIOT parser =
                RiotReader.createParserTriG(inputStreamFrom(file), null, sink);

        parser.parse();
        sink.close();

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
