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
package fr.inria.eventcloud;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.PublishApi;
import fr.inria.eventcloud.api.PutGetApi;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.SubscribeApi;
import fr.inria.eventcloud.api.Subscription;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.exceptions.MalformedSparqlQueryException;
import fr.inria.eventcloud.api.listeners.BindingNotificationListener;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
import fr.inria.eventcloud.deployment.JunitEventCloudInfrastructureDeployer;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.pubsub.SubscriptionTestUtils;

/**
 * The purpose of this test is just to show how to instantiate and to use an
 * EventCloud.
 * 
 * @author lpellegr
 */
public class EventCloudUsageTest implements Serializable {

    private static final long serialVersionUID = 160L;

    private static final Logger LOG =
            LoggerFactory.getLogger(EventCloudUsageTest.class);

    private static final List<Binding> bindingsReceived =
            new ArrayList<Binding>();

    @Test(timeout = 180000)
    public void testEventCloudInstantiationAndUsage()
            throws EventCloudIdNotManaged, MalformedSparqlQueryException {
        // Creates and deploys an EventCloudsRegistry locally
        JunitEventCloudInfrastructureDeployer deployer =
                new JunitEventCloudInfrastructureDeployer();

        // Creates and deploys an EventCloud composed of 1 tracker and 10 peers
        EventCloudId eventCloudId = deployer.newEventCloud(1, 10);

        // Creates a PutGet proxy whose the
        // purpose is to work with historical semantic data
        PutGetApi putGetProxy =
                ProxyFactory.newPutGetProxy(
                        deployer.getEventCloudsRegistryUrl(), eventCloudId);

        // By using the PutGetProxy we can publish synchronously some
        // historical quadruples (although these quadruples may trigger
        // a notification)
        putGetProxy.add(new Quadruple(
                NodeFactory.createURI("http://sources.event-processing.org/ids/NiceWeatherStation01#source"),
                NodeFactory.createURI("http://www.nice.fr"),
                NodeFactory.createURI("http://france.meteofrance.com/france/meteo"),
                NodeFactory.createURI("http://france.meteofrance.com/france/meteo?PREVISIONS_PORTLET.path=previsionsville/060880")));

        Node expectedNodeResult =
                NodeFactory.createURI("http://france.meteofrance.com/france/meteo/max-temperature/08082011-2012/");
        putGetProxy.add(new Quadruple(
                NodeFactory.createURI("http://sources.event-processing.org/ids/NiceWeatherStation01#source"),
                NodeFactory.createURI("http://www.nice.fr"),
                expectedNodeResult, NodeFactory.createLiteral(
                        "29", XSDDatatype.XSDint)));

        putGetProxy.add(new Quadruple(
                NodeFactory.createURI("http://sources.event-processing.org/ids/NiceWeatherStation01#source"),
                NodeFactory.createURI("http://www.nice.fr"),
                NodeFactory.createURI("http://france.meteofrance.com/france/meteo/max-temperature/09082011-2012/"),
                NodeFactory.createLiteral("26", XSDDatatype.XSDint)));

        // Once a quadruple is inserted, it is possible to retrieve some
        // quadruples (historical data) by using a quadruple pattern.
        // Hereafter, any quadruple which has any value as graph, subject,
        // predicate and object value is returned
        List<Quadruple> result = putGetProxy.find(QuadruplePattern.ANY);
        LOG.info("Quadruples contained by the EventCloud {}", eventCloudId);
        for (Quadruple quad : result) {
            LOG.info(quad.toString());
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
        LOG.info("Answer for SPARQL query {}:", sparqlQuery);
        LOG.info(resultNode.toString());

        Assert.assertEquals(expectedNodeResult, resultNode);

        // Then, it is possible to create a SubscribeProxy to
        // subscribe to some interest and to be notified asynchronously
        final SubscribeApi subscribeProxy =
                ProxyFactory.newSubscribeProxy(
                        deployer.getEventCloudsRegistryUrl(), eventCloudId);

        // Once a subscription is created, a SubscriptionId can be retrieved
        // from the subscription object to have the possibility to perform an
        // unsubscribe operation
        Subscription subscription =
                new Subscription(
                        "PREFIX foaf: <http://xmlns.com/foaf/0.1/> SELECT ?name ?email ?g WHERE { GRAPH ?g { ?id foaf:name ?name . ?id foaf:email ?email } }");

        subscribeProxy.subscribe(
                subscription, new BindingNotificationListener() {
                    private static final long serialVersionUID = 160L;

                    @Override
                    public void onNotification(SubscriptionId id,
                                               Binding solution) {
                        synchronized (bindingsReceived) {
                            bindingsReceived.add(solution);
                            bindingsReceived.notifyAll();
                        }
                        LOG.info("Solution received:\n{}", solution);
                    }
                });
        LOG.info(
                "Subscription with id {} has been registered",
                subscription.getId());

        SubscriptionTestUtils.waitSubscriptionIndexation();

        // Finally, we can simulate an event source by creating a PublishProxy
        PublishApi publishProxy =
                ProxyFactory.newPublishProxy(
                        deployer.getEventCloudsRegistryUrl(), eventCloudId);

        long publicationTime = System.currentTimeMillis();

        // From the publish proxy it is possible to publish quadruples (events)
        Quadruple q1 =
                new Quadruple(
                        NodeFactory.createURI("https://plus.google.com/825349613"),
                        NodeFactory.createURI("https://plus.google.com/107234124364605485774"),
                        NodeFactory.createURI("http://xmlns.com/foaf/0.1/email"),
                        NodeFactory.createLiteral("user1@company.com"));
        q1.setPublicationTime(publicationTime);
        publishProxy.publish(q1);

        Quadruple q2 =
                new Quadruple(
                        NodeFactory.createURI("https://plus.google.com/825349613"),
                        NodeFactory.createURI("https://plus.google.com/107234124364605485774"),
                        NodeFactory.createURI("http://xmlns.com/foaf/0.1/name"),
                        NodeFactory.createLiteral("User1"));
        q2.setPublicationTime(publicationTime);
        publishProxy.publish(q2);

        publicationTime = System.currentTimeMillis();

        Quadruple q3 =
                new Quadruple(
                        NodeFactory.createURI("https://plus.google.com/3283940594/2011-2012-08-30-18:13:05"),
                        NodeFactory.createURI("https://plus.google.com/107545688688906540962"),
                        NodeFactory.createURI("http://xmlns.com/foaf/0.1/email"),
                        NodeFactory.createLiteral("user2@company.com"));
        q3.setPublicationTime(publicationTime);
        publishProxy.publish(q3);

        Quadruple q4 =
                new Quadruple(
                        NodeFactory.createURI("https://plus.google.com/124324034/2011-2012-08-30-19:04:54"),
                        NodeFactory.createURI("https://plus.google.com/14023231238123495031/"),
                        NodeFactory.createURI("http://xmlns.com/foaf/0.1/name"),
                        NodeFactory.createLiteral("User 3"));
        q4.setPublicationTime();
        publishProxy.publish(q4);

        Quadruple q5 =
                new Quadruple(
                        NodeFactory.createURI("https://plus.google.com/3283940594/2011-2012-08-30-18:13:05"),
                        NodeFactory.createURI("https://plus.google.com/107545688688906540962"),
                        NodeFactory.createURI("http://xmlns.com/foaf/0.1/name"),
                        NodeFactory.createLiteral("User 2"));
        q5.setPublicationTime(publicationTime);
        publishProxy.publish(q5);

        // 3 notifications are expected
        synchronized (bindingsReceived) {
            while (bindingsReceived.size() != 2) {
                try {
                    bindingsReceived.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        // Unsubscribes. Once this step is done no notification related to the
        // subscription must be received.
        subscribeProxy.unsubscribe(subscription.getId());

        int currentNbNotifications = bindingsReceived.size();

        // one new quadruple that matches the subscription is published
        publishProxy.publish(new Quadruple(
                NodeFactory.createURI("https://plus.google.com/"),
                NodeFactory.createURI("https://plus.google.com/4879854879797418743/"),
                NodeFactory.createURI("http://xmlns.com/foaf/0.1/name"),
                NodeFactory.createLiteral("Firstname Lastname")));

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

        EventCloudId id1 = deployer.newEventCloud(1, 1);

        EventCloudId id2 = deployer.newEventCloud(1, 1);

        Assert.assertFalse(
                "Two EventClouds created at two different time have the same identifier",
                id1.equals(id2));

        deployer.undeploy();
    }

}
