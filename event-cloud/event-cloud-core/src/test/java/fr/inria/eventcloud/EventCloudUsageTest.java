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
package fr.inria.eventcloud;

import java.io.Serializable;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.Subscription;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.BindingNotificationListener;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
import fr.inria.eventcloud.deployment.JunitEventCloudInfrastructureDeployer;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.proxies.PublishProxy;
import fr.inria.eventcloud.proxies.PutGetProxy;
import fr.inria.eventcloud.proxies.SubscribeProxy;

/**
 * The purpose of this test is just to show how to instantiate and to use an
 * Event Cloud.
 * 
 * @author lpellegr
 */
public class EventCloudUsageTest implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger log =
            LoggerFactory.getLogger(EventCloudUsageTest.class);

    private static final Collection<Binding> bindingsReceived =
            new Collection<Binding>();

    @Test(timeout = 180000)
    public void testEventCloudInstantiationAndUsage()
            throws InterruptedException {
        // Creates and deploy an EventCloudsRegistry locally
        JunitEventCloudInfrastructureDeployer deployer =
                new JunitEventCloudInfrastructureDeployer();

        // Creates and deploy an Event Cloud composed of 10 peers
        EventCloudId eventCloudId = deployer.createEventCloud(10);

        // Retrieves a proxy factory which is specialized to create
        // proxies for the Event Cloud which has been previously created
        ProxyFactory proxyFactory =
                ProxyFactory.getInstance(
                        deployer.getEventCloudsRegistryUrl(), eventCloudId);

        // From the proxy factory we can create a PutGet proxy whose the
        // purpose is to work with historical semantic data
        PutGetProxy putGetProxy = proxyFactory.createPutGetProxy();

        // By using the PutGetProxy we can publish synchronously some
        // historical quadruples (although these quadruples may trigger
        // a notification)
        putGetProxy.add(new Quadruple(
                Node.createURI("http://sources.event-processing.org/ids/NiceWeatherStation01#source"),
                Node.createURI("http://www.nice.fr"),
                Node.createURI("http://france.meteofrance.com/france/meteo"),
                Node.createURI("http://france.meteofrance.com/france/meteo?PREVISIONS_PORTLET.path=previsionsville/060880")));

        Node expectedNodeResult =
                Node.createURI("http://france.meteofrance.com/france/meteo/max-temperature/08082011/");
        putGetProxy.add(new Quadruple(
                Node.createURI("http://sources.event-processing.org/ids/NiceWeatherStation01#source"),
                Node.createURI("http://www.nice.fr"), expectedNodeResult,
                Node.createLiteral("29", XSDDatatype.XSDint)));

        putGetProxy.add(new Quadruple(
                Node.createURI("http://sources.event-processing.org/ids/NiceWeatherStation01#source"),
                Node.createURI("http://www.nice.fr"),
                Node.createURI("http://france.meteofrance.com/france/meteo/max-temperature/09082011/"),
                Node.createLiteral("26", XSDDatatype.XSDint)));

        // Once a quadruple is inserted, it is possible to retrieve some
        // quadruples (historical data) by using a quadruple pattern.
        // Hereafter, any quadruple which has any value as graph, subject,
        // predicate and object value is returned
        Collection<Quadruple> result = putGetProxy.find(QuadruplePattern.ANY);
        log.info("Quadruples contained by the Event-Cloud {}", eventCloudId);
        for (Quadruple quad : result) {
            log.info(quad.toString());
        }

        Assert.assertEquals(3, result.size());

        // Or more complex queries may be formulated by using a SPARQL query.
        String sparqlQuery =
                "SELECT ?day WHERE { GRAPH ?g { <http://www.nice.fr> ?day ?temp FILTER (?temp > 26) } }";
        SparqlSelectResponse response =
                putGetProxy.executeSparqlSelect(sparqlQuery);

        Assert.assertTrue(response.getResult().hasNext());

        Node resultNode =
                response.getResult().nextSolution().get("day").asNode();
        log.info("Answer for SPARQL query {}:", sparqlQuery);
        log.info(resultNode.toString());

        Assert.assertEquals(expectedNodeResult, resultNode);

        // Then, it is possible to create a SubscribeProxy to
        // subscribe to some interest and to be notified asynchronously
        final SubscribeProxy subscribeProxy =
                proxyFactory.createSubscribeProxy();

        // Once a subscription is created, a SubscriptionId can be retrived from
        // the subscription object to have the possibility to perform an
        // unsubscribe operation
        Subscription subscription =
                new Subscription(
                        "PREFIX foaf: <http://xmlns.com/foaf/0.1/> SELECT ?name ?email ?g WHERE { GRAPH ?g { ?id foaf:name ?name . ?id foaf:email ?email } }");

        subscribeProxy.subscribe(
                subscription, new BindingNotificationListener() {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void onNotification(SubscriptionId id,
                                               Binding solution) {
                        synchronized (bindingsReceived) {
                            bindingsReceived.add(solution);
                            bindingsReceived.notifyAll();
                        }
                        log.info("Solution received:\n{}", solution);
                    }
                });
        log.info(
                "Subscription with id {} has been registered",
                subscription.getId());

        // Finally, we can simulate an event source by creating a PublishProxy
        PublishProxy publishProxy = proxyFactory.createPublishProxy();

        long publicationTime = System.currentTimeMillis();

        // From the publish proxy it is possible to publish quadruples (events)
        Quadruple q1 =
                new Quadruple(
                        Node.createURI("https://plus.google.com/825349613"),
                        Node.createURI("https://plus.google.com/107234124364605485774"),
                        Node.createURI("http://xmlns.com/foaf/0.1/email"),
                        Node.createLiteral("user1@company.com"));
        q1.setPublicationTime(publicationTime);
        publishProxy.publish(q1);

        Quadruple q2 =
                new Quadruple(
                        Node.createURI("https://plus.google.com/825349613"),
                        Node.createURI("https://plus.google.com/107234124364605485774"),
                        Node.createURI("http://xmlns.com/foaf/0.1/name"),
                        Node.createLiteral("User1"));
        q2.setPublicationTime(publicationTime);
        publishProxy.publish(q2);

        // this quadruple shows chronicle context property because it is
        // delivered by reconsuming the first quadruple which was published
        Quadruple q3 =
                new Quadruple(
                        Node.createURI("https://plus.google.com/825349613"),
                        Node.createURI("https://plus.google.com/107234124364605485774"),
                        Node.createURI("http://xmlns.com/foaf/0.1/email"),
                        Node.createLiteral("user1.new.email@company.com"));
        q3.setPublicationTime(publicationTime);
        publishProxy.publish(q3);

        publicationTime = System.currentTimeMillis();

        Quadruple q4 =
                new Quadruple(
                        Node.createURI("https://plus.google.com/3283940594/2011-08-30-18:13:05"),
                        Node.createURI("https://plus.google.com/107545688688906540962"),
                        Node.createURI("http://xmlns.com/foaf/0.1/email"),
                        Node.createLiteral("user2@company.com"));
        q4.setPublicationTime(publicationTime);
        publishProxy.publish(q4);

        Quadruple q5 =
                new Quadruple(
                        Node.createURI("https://plus.google.com/124324034/2011-08-30-19:04:54"),
                        Node.createURI("https://plus.google.com/14023231238123495031/"),
                        Node.createURI("http://xmlns.com/foaf/0.1/name"),
                        Node.createLiteral("User 3"));
        q5.setPublicationTime();
        publishProxy.publish(q5);

        Quadruple q6 =
                new Quadruple(
                        Node.createURI("https://plus.google.com/3283940594/2011-08-30-18:13:05"),
                        Node.createURI("https://plus.google.com/107545688688906540962"),
                        Node.createURI("http://xmlns.com/foaf/0.1/name"),
                        Node.createLiteral("User 2"));
        q6.setPublicationTime(publicationTime);
        publishProxy.publish(q6);

        // 3 notifications are expected
        synchronized (bindingsReceived) {
            while (bindingsReceived.size() != 3) {
                try {
                    bindingsReceived.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        // try {
        // deployer.getRandomSemanticPeer(eventCloudId).send(
        // new StatelessQuadruplePatternRequest(QuadruplePattern.ANY) {
        //
        // private static final long serialVersionUID = 1L;
        //
        // @Override
        // public void onPeerValidatingKeyConstraints(CanOverlay overlay,
        // QuadruplePattern quadruplePattern) {
        // System.err.println("$A$A$ Peer " + overlay
        // + " contains:");
        // for (Quadruple quad : ((SynchronizedJenaDatasetGraph)
        // overlay.getDatastore()).find(QuadruplePattern.ANY)) {
        // log.debug(quad.toString());
        // }
        // }
        // });
        // } catch (DispatchException e) {
        // e.printStackTrace();
        // }

        // Unsubscribes. Once this step is done no notification related to the
        // subscription must be received.
        subscribeProxy.unsubscribe(subscription.getId());

        int currentNbNotifications = bindingsReceived.size();

        // one new quadruple that matches the subscription is published
        publishProxy.publish(new Quadruple(
                Node.createURI("https://plus.google.com/"),
                Node.createURI("https://plus.google.com/4879854879797418743/"),
                Node.createURI("http://xmlns.com/foaf/0.1/name"),
                Node.createLiteral("Firstname Lastname")));

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // no new notification should be received
        Assert.assertTrue(currentNbNotifications == bindingsReceived.size());
    }

    @Test
    public void testEventCloudsInitializationWith2EventClouds() {
        JunitEventCloudInfrastructureDeployer deployer =
                new JunitEventCloudInfrastructureDeployer();

        EventCloudId ecId1 = deployer.createEventCloud(1);

        EventCloudId ecId2 = deployer.createEventCloud(1);

        Assert.assertFalse(
                "Two Event Clouds created at two different time have the same identifier",
                ecId1.equals(ecId2));

        deployer.undeploy();
    }

}
