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

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.BindingNotificationListener;
import fr.inria.eventcloud.deployment.JunitEventCloudInfrastructureDeployer;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.proxies.PublishProxy;
import fr.inria.eventcloud.proxies.PutGetProxy;
import fr.inria.eventcloud.proxies.SubscribeProxy;

/**
 * The purpose of this test is juste to show how to instantiate and to use an
 * Event Cloud.
 * 
 * @author lpellegr
 */
public class EventCloudUsageTest implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger log =
            LoggerFactory.getLogger(EventCloudUsageTest.class);

    @Test
    public void testEventCloudInstantiationAndUsage() {
        JunitEventCloudInfrastructureDeployer deployer =
                new JunitEventCloudInfrastructureDeployer();
        EventCloudId eventCloudId = deployer.createEventCloud(15);

        // Retrieves a factory that is specialized to the previous Event-Cloud
        // for creating a proxy
        ProxyFactory factory =
                ProxyFactory.getInstance(
                        deployer.getEventCloudsRegistryUrl(), eventCloudId);

        // From the factory we can get a PutGet proxy that is used to perform
        // some synchronous operations
        PutGetProxy putGetProxy = factory.createPutGetProxy();

        putGetProxy.add(new Quadruple(
                Node.createURI("http://www.inria.fr"),
                Node.createURI("http://www.inria.fr"),
                Node.createURI("http://www.inria.fr"),
                Node.createURI("http://www.inria.fr")));

        // TODO make it work
        // try {
        // putGetProxy.add(
        // new FileInputStream(
        // new File(
        // "/user/lpellegr/home/Desktop/infobox_property_definitions_en.nq")),
        // SerializationFormat.NQuads);
        // } catch (FileNotFoundException e) {
        // e.printStackTrace();
        // }

        // Finds all the quadruples that are contained by the Event-Cloud
        Collection<Quadruple> result = putGetProxy.find(QuadruplePattern.ANY);
        log.info("Quadruples contained by the Event-Cloud {}", eventCloudId);
        for (Quadruple quad : result) {
            log.info(quad.toString());
        }

        // You can also use the find method with a quadruple pattern.
        // The Node.ANY plays as a variable
        putGetProxy.find(new QuadruplePattern(
                Node.createURI("http://uri"), Node.ANY,
                Node.createURI("http://uri2"), Node.createLiteral("a")));

        // Creates and retrieved a publish subscribe proxy
        final SubscribeProxy subscribeProxy = factory.createSubscribeProxy();

        SubscriptionId id =
                subscribeProxy.subscribe(
                        "PREFIX foaf: <http://xmlns.com/foaf/0.1/> SELECT ?name ?email ?g WHERE { GRAPH ?g { ?id foaf:name ?name . ?id foaf:email ?email } }",
                        new BindingNotificationListener() {
                            private static final long serialVersionUID = 1L;

                            @Override
                            public void onNotification(SubscriptionId id,
                                                       Binding solution) {
                                log.info("Solution received: {}", solution);
                            }
                        });

        PublishProxy publishProxy = factory.createPublishProxy();

        log.info("Subscription with id {} has been registered", id);

        publishProxy.publish(new Quadruple(
                Node.createURI("https://plus.google.com/"),
                Node.createURI("https://plus.google.com/107234124364605485774/"),
                Node.createURI("http://xmlns.com/foaf/0.1/name"),
                Node.createLiteral("Laurent Pellegrino")));

        publishProxy.publish(new Quadruple(
                Node.createURI("https://plus.google.com/"),
                Node.createURI("https://plus.google.com/107234124364605485774/"),
                Node.createURI("http://xmlns.com/foaf/0.1/email"),
                Node.createLiteral("laurent.pellegrino@gmail.com")));

        publishProxy.publish(new Quadruple(
                Node.createURI("https://plus.google.com/"),
                Node.createURI("https://plus.google.com/107234124364605485774/"),
                Node.createURI("http://xmlns.com/foaf/0.1/email"),
                Node.createLiteral("laurent.pellegrino@gmail.com[chronicle context]")));

        publishProxy.publish(new Quadruple(
                Node.createURI("https://plus.google.com/"),
                Node.createURI("https://plus.google.com/48798548797974/"),
                Node.createURI("http://xmlns.com/foaf/0.1/email"),
                Node.createLiteral("firstname.lastname@gmail.com")));

        publishProxy.publish(new Quadruple(
                Node.createURI("https://plus.google.com/"),
                Node.createURI("https://plus.google.com/746668964541235679/"),
                Node.createURI("http://xmlns.com/foaf/0.1/email"),
                Node.createLiteral("frederic.dupont@gmail.com")));

        publishProxy.publish(new Quadruple(
                Node.createURI("https://plus.google.com/"),
                Node.createURI("https://plus.google.com/746668964541235679/"),
                Node.createURI("http://xmlns.com/foaf/0.1/name"),
                Node.createLiteral("Frederic Dupont")));

        publishProxy.publish(new Quadruple(
                Node.createURI("https://plus.google.com/"),
                Node.createURI("https://plus.google.com/48798548797974/"),
                Node.createURI("http://xmlns.com/foaf/0.1/name"),
                Node.createLiteral("Firstname Lastname")));

        // 4 notifications are expected

        /*
         * Waits some time because Junit does not wait that internal threads end
         */
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
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

        subscribeProxy.unsubscribe(id);

        // System.err.println(">>> UNSUBSCRIBE REQUEST SENT!");

        publishProxy.publish(new Quadruple(
                Node.createURI("https://plus.google.com/"),
                Node.createURI("https://plus.google.com/48798548797974/"),
                Node.createURI("http://xmlns.com/foaf/0.1/name"),
                Node.createLiteral("Firstname Lastname2")));

        // just to test that no notification is delivered

        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // System.err.println(">>> UNSUBSCRIBE REQUEST ASSUMED TO BE EXECUTED!");
        //
        // try {
        // deployer.getRandomSemanticPeer(eventCloudId).send(
        // new StatelessQuadruplePatternRequest(QuadruplePattern.ANY) {
        //
        // private static final long serialVersionUID = 1L;
        //
        // @Override
        // public void onPeerValidatingKeyConstraints(CanOverlay overlay,
        // QuadruplePattern quadruplePattern) {
        // System.err.println("$B$B$ Peer " + overlay
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
        //
        // try {
        // Thread.sleep(15000);
        // } catch (InterruptedException e) {
        // e.printStackTrace();
        // }

        // TODO test has to be rewritten to detect termination cleanly
    }

}
