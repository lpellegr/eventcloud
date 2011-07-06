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

import org.junit.Test;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.properties.UnalterableElaProperty;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.proxies.PutGetProxy;

/**
 * Shows how to instantiate and to use an EventCloud.
 * 
 * @author lpellegr
 */
public class EventCloudInitializationTest {

    @Test
    public void testEventCloudInstantiationAndUsage() {
        // Creates an EnvetCloud registry in order to store and to retrieve
        // later the information about the EventCloud which have been created
        EventCloudsRegistry registry =
                EventCloudsRegistryFactory.newEventCloudsRegistry();

        // Creates a new Event-Cloud
        EventCloud eventCloud =
                EventCloud.create(
                        registry.getUrl(),
                        "http://node.provider.not.yet.available",
                        new Collection<UnalterableElaProperty>(), 1, 3);

        // Registers the Event-Cloud which has been previously created into the
        // registry. This is done in order to offer the possibility to any user
        // to interact with this Event-Cloud
        registry.register(eventCloud);

        // Retrieves a factory that is specialized to the previous Event-Cloud
        // for creating a proxy
        ProxyFactory factory =
                ProxyFactory.getInstance(registry.getUrl(), eventCloud.getId());

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
        // "/Users/lpellegr/rdf-data/infobox_property_definitions_en.nq")),
        // SerializationFormat.NQuads);
        // } catch (FileNotFoundException e) {
        // e.printStackTrace();
        // }

        Collection<Quadruple> result = putGetProxy.find(QuadruplePattern.ANY);
        System.out.println("Quadruples contained by the Event-Cloud "
                + eventCloud.getId() + ":");
        for (Quadruple quad : result) {
            System.out.println(quad);
        }
    }

}
