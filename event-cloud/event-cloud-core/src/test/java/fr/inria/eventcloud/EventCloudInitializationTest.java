package fr.inria.eventcloud;

import org.junit.Test;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.EventCloud;
import fr.inria.eventcloud.api.EventCloudsRegistry;
import fr.inria.eventcloud.api.EventCloudsRegistryFactory;
import fr.inria.eventcloud.api.ProxyFactory;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.properties.UnalterableElaProperty;
import fr.inria.eventcloud.proxy.PutGetProxy;

/**
 * Shows how to instanciate and to use an EventCloud.
 * 
 * @author lpellegr
 */
public class EventCloudInitializationTest {

    @Test
    public void testEventCloudInstanciationAndUsage() {
        EventCloudsRegistry registry =
                EventCloudsRegistryFactory.newEventCloudsRegistry();

        // nodeProvider not yet designed!
        String nodeProviderURL = "http://nodeProviderURL.com";

        EventCloud eventCloud =
                EventCloud.create(
                        registry.getUrl(), nodeProviderURL,
                        new Collection<UnalterableElaProperty>(), 1, 3);
        registry.register(eventCloud);

        ProxyFactory factory =
                ProxyFactory.getInstance(registry.getUrl(), eventCloud.getId());

        PutGetProxy putGetProxy = factory.createPutGetProxy();
        putGetProxy.add(new Quadruple(
                Node.createURI("http://www.inria.fr"),
                Node.createURI("http://www.inria.fr"),
                Node.createURI("http://www.inria.fr"),
                Node.createURI("http://www.inria.fr")));

        // try {
        // putGetProxy.add(
        // new FileInputStream(
        // new File(
        // "/Users/lpellegr/Desktop/infobox_property_definitions_en.nq")),
        // SerializationFormat.NQuads);
        // } catch (FileNotFoundException e) {
        // e.printStackTrace();
        // }

        Collection<Quadruple> result = putGetProxy.find(QuadruplePattern.ANY);
        System.out.println("Quadruples that are contained by the Event-Cloud are the following");
        for (Quadruple quad : result) {
            System.out.println(quad);
        }
    }
}
