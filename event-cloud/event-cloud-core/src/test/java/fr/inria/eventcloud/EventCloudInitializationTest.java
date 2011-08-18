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
import org.objectweb.proactive.api.PAActiveObject;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.BindingNotificationListener;
import fr.inria.eventcloud.api.properties.UnalterableElaProperty;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.proxies.PublishProxy;
import fr.inria.eventcloud.proxies.SubscribeProxy;

/**
 * Shows how to instantiate and to use an EventCloud.
 * 
 * @author lpellegr
 */
public class EventCloudInitializationTest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Test
    public void testEventCloudInstantiationAndUsage() {
        // Creates an EnvetCloud registry in order to store and to retrieve
        // later the information about the EventCloud which have been created
        EventCloudsRegistry registry =
                EventCloudsRegistryFactory.newEventCloudsRegistry();

        String registryUrl = PAActiveObject.getUrl(registry);

        // Creates a new Event-Cloud
        EventCloud eventCloud =
                EventCloud.create(
                        registryUrl, "http://node.provider.not.yet.available",
                        new Collection<UnalterableElaProperty>(), 1, 3);

        System.out.println("Peers contained by the Event Cloud:");
        for (Peer p : eventCloud.getTrackers().iterator().next().getPeers()) {
            System.out.println(p.dump());
        }

        // Registers the Event-Cloud which has been previously created into the
        // registry. This is done in order to offer the possibility to any user
        // to interact with this Event-Cloud
        registry.register(eventCloud);

        // Retrieves a factory that is specialized to the previous Event-Cloud
        // for creating a proxy
        ProxyFactory factory =
                ProxyFactory.getInstance(registryUrl, eventCloud.getId());

        // From the factory we can get a PutGet proxy that is used to perform
        // some synchronous operations
        // PutGetProxy putGetProxy = factory.createPutGetProxy();
        //
        // putGetProxy.add(new Quadruple(
        // Node.createURI("http://www.inria.fr"),
        // Node.createURI("http://www.inria.fr"),
        // Node.createURI("http://www.inria.fr"),
        // Node.createURI("http://www.inria.fr")));

        // TODO make it work
        // try {
        // putGetProxy.add(
        // new FileInputStream(
        // new File(
        //
        // "/user/lpellegr/home/Desktop/infobox_property_definitions_en.nq")),
        // SerializationFormat.NQuads);
        // } catch (FileNotFoundException e) {
        // e.printStackTrace();
        // }

        // Finds all the quadruples that are contained by the Event-Cloud
        // Collection<Quadruple> result =
        // putGetProxy.find(QuadruplePattern.ANY);
        // System.out.println("Quadruples contained by the Event-Cloud "
        // + eventCloud.getId() + ":");
        // for (Quadruple quad : result) {
        // System.out.println(quad);
        // }
        //
        // // You can also use the find method with a quadruple pattern.
        // // The Node.ANY plays as a variable
        // putGetProxy.find(new QuadruplePattern(
        // Node.createURI("http://uri"), Node.ANY,
        // Node.createURI("http://uri2"), Node.createLiteral("a")));

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
                                System.out.println("EventCloudInitializationTest.handleNotification() "
                                        + solution);
                            }
                        });

        PublishProxy publishProxy = factory.createPublishProxy();

        System.out.println("Subscription with id " + id
                + " has been registered");

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

        // pubsubProxy.find(id);

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

        // pubsubProxy.publish(new Quadruple(
        // Node.createURI("https://plus.google.com/ee"),
        // Node.createURI("https://plus.google.com/107234124364605485774/"),
        // Node.createURI("http://xmlns.com/foaf/0.1/name"),
        // Node.createLiteral("Laurent Pellegrino")));
        //
        // pubsubProxy.publish(new Quadruple(
        // Node.createURI("https://plus.google.com/ee2"),
        // Node.createURI("https://plus.google.com/107234124364605485774/"),
        // Node.createURI("http://xmlns.com/foaf/0.1/name"),
        // Node.createLiteral("Laurent Pellegrino")));
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // TODO test has to be rewritten to detect termination cleanly
    }

}
