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
import java.util.HashMap;

import org.etsi.uri.gcm.util.GCM;
import org.junit.Test;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.component.adl.FactoryFactory;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.webservices.WSConstants;
import org.objectweb.proactive.extensions.webservices.component.Utils;
import org.objectweb.proactive.extensions.webservices.component.controller.PAWebServicesController;
import org.openjena.atlas.lib.Sink;
import org.openjena.riot.RiotReader;
import org.openjena.riot.lang.LangRIOT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.sparql.core.Quad;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Event;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.deployment.JunitEventCloudInfrastructureDeployer;
import fr.inria.eventcloud.webservices.JaxWsCXFWSCaller;
import fr.inria.eventcloud.webservices.api.PublishWsApi;
import fr.inria.eventcloud.webservices.api.SubscribeInfos;
import fr.inria.eventcloud.webservices.api.SubscribeWsApi;
import fr.inria.eventcloud.webservices.deployment.WsProxyDeployer;

/**
 * Class used to test a subscribe proxy component and a publish proxy component
 * by using web services.
 * 
 * @author lpellegr
 * @author bsauvan
 */
public class PubSubTest {

    private static final Logger log = LoggerFactory.getLogger(PubSubTest.class);

    private static Factory factory;

    static {
        CentralPAPropertyRepository.GCM_PROVIDER.setValue(P2PStructuredProperties.GCM_PROVIDER.getValue());
        try {
            factory = FactoryFactory.getFactory();
        } catch (ADLException e) {
            e.printStackTrace();
        }
    }

    @Test(timeout = 90000)
    public void testPublishSubscribeWsProxies() throws Exception {
        JunitEventCloudInfrastructureDeployer deployer =
                new JunitEventCloudInfrastructureDeployer();

        EventCloudId ecId = deployer.createEventCloud(10);

        String subscribeWsUrl =
                WsProxyDeployer.deploySubscribeWebService(
                        deployer.getEventCloudsRegistryUrl(), ecId.toUrl(),
                        "subscribe", 8888);

        String publishWsUrl =
                WsProxyDeployer.deployPublishWebService(
                        deployer.getEventCloudsRegistryUrl(), ecId.toUrl(),
                        "publish", 8889);

        Component pubSubComponent =
                this.createPubSubComponent(subscribeWsUrl, publishWsUrl);
        String subscriberWsUrl = this.exposeSubscriberWs(pubSubComponent);

        SubscribeWsApi subscribeWs =
                (SubscribeWsApi) pubSubComponent.getFcInterface("subscribe-services");

        // Subscribe subscribeRequest = new Subscribe();
        // FilterType filterType = new FilterType();
        // TopicExpressionType tet = new TopicExpressionType();
        // tet.getContent().add("fireman_event:cardiacRythmFiremanTopic");
        // filterType.getAny().add(tet);
        // subscribeRequest.setFilter(filterType);
        //
        // W3CEndpointReferenceBuilder endPointReferenceBuilder =
        // new W3CEndpointReferenceBuilder();
        // endPointReferenceBuilder.address(subscriberWsUrl);
        // subscribeRequest.setConsumerReference(endPointReferenceBuilder.build());

        // subscribes for any events
        subscribeWs.subscribe(new SubscribeInfos(
                "SELECT ?g ?s ?p ?o WHERE { GRAPH ?g { <http://eventcloud.inria.fr/replace/me/with/a/correct/namespace/fireman_event:cardiacRythmFiremanTopic> ?p ?o } }",
                subscriberWsUrl));

        // waits a little to be sure that the subscription has been indexed
        Thread.sleep(2000);

        PublishWsApi publishWs =
                (PublishWsApi) pubSubComponent.getFcInterface("publish-services");

        Collection<Event> events = new Collection<Event>();
        events.add(new Event(read("/notification-01.trig")));
        publishWs.publish(events);

        PubSubStatus pubSubComponentStatus =
                (PubSubStatus) pubSubComponent.getFcInterface("status-services");

        while (!pubSubComponentStatus.hasReceivedEvent()) {
            log.info("Waiting for the reception of event");
            Thread.sleep(500);
        }

        deployer.undeploy();
    }

    private Component createPubSubComponent(String subscribeWsUrl,
                                            String publishWsUrl) {
        try {
            Component pubSubComponent =
                    (Component) factory.newComponent(
                            "fr.inria.eventcloud.webservices.pubsub.PubSubComponent",
                            new HashMap<String, Object>());

            GCM.getBindingController(pubSubComponent).bindFc(
                    PubSubComponentImpl.SUBSCRIBE_WEBSERVICES_NAME,
                    subscribeWsUrl + "(" + JaxWsCXFWSCaller.class.getName()
                            + ")");
            GCM.getBindingController(pubSubComponent)
                    .bindFc(
                            PubSubComponentImpl.PUBLISH_WEBSERVICES_NAME,
                            publishWsUrl + "("
                                    + JaxWsCXFWSCaller.class.getName() + ")");

            GCM.getGCMLifeCycleController(pubSubComponent).startFc();

            return pubSubComponent;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private String exposeSubscriberWs(Component pubSubComponent) {
        try {
            PAWebServicesController wsc =
                    Utils.getPAWebServicesController(pubSubComponent);

            wsc.initServlet();
            wsc.exposeComponentAsWebService(
                    "EventCloud", new String[] {"subscriber-webservices"});

            return wsc.getLocalUrl() + WSConstants.SERVICES_PATH
                    + "EventCloud_subscriber-webservices";
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
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
