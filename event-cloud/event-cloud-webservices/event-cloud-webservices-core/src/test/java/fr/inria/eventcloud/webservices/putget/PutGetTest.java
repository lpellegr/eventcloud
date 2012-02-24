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
package fr.inria.eventcloud.webservices.putget;

import java.util.HashMap;

import org.etsi.uri.gcm.util.GCM;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.fractal.adl.ADLException;
import org.objectweb.fractal.adl.Factory;
import org.objectweb.fractal.api.Component;
import org.objectweb.proactive.core.component.adl.FactoryFactory;
import org.objectweb.proactive.core.config.CentralPAPropertyRepository;
import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
import fr.inria.eventcloud.deployment.JunitEventCloudInfrastructureDeployer;
import fr.inria.eventcloud.webservices.JaxWsCXFWSCaller;
import fr.inria.eventcloud.webservices.api.PutGetWsApi;
import fr.inria.eventcloud.webservices.deployment.WebServiceDeployer;

/**
 * Class used to test a put/get proxy component by using web services.
 * 
 * @author bsauvan
 */
public class PutGetTest {

    private static final Logger log = LoggerFactory.getLogger(PutGetTest.class);

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
    public void testPutGetWsProxy() throws Exception {
        JunitEventCloudInfrastructureDeployer deployer =
                new JunitEventCloudInfrastructureDeployer();

        EventCloudId ecId = deployer.createEventCloud(10);

        String putGetWsUrl =
                WebServiceDeployer.deployPutGetWebService(
                        deployer.getEventCloudsRegistryUrl(),
                        ecId.getStreamUrl(), "putget", 8889);

        Component putGetComponent = this.createPutGetComponent(putGetWsUrl);

        PutGetWsApi putGetWs =
                (PutGetWsApi) putGetComponent.getFcInterface("putget-services");

        putGetWs.addQuadruple(new Quadruple(
                Node.createURI("http://sources.event-processing.org/ids/NiceWeatherStation01#source"),
                Node.createURI("http://www.nice.fr"),
                Node.createURI("http://france.meteofrance.com/france/meteo"),
                Node.createURI("http://france.meteofrance.com/france/meteo?PREVISIONS_PORTLET.path=previsionsville/060880")));

        Node expectedNodeResult =
                Node.createURI("http://france.meteofrance.com/france/meteo/max-temperature/08082011/");
        putGetWs.addQuadruple(new Quadruple(
                Node.createURI("http://sources.event-processing.org/ids/NiceWeatherStation01#source"),
                Node.createURI("http://www.nice.fr"), expectedNodeResult,
                Node.createLiteral("29", XSDDatatype.XSDint)));

        putGetWs.addQuadruple(new Quadruple(
                Node.createURI("http://sources.event-processing.org/ids/NiceWeatherStation01#source"),
                Node.createURI("http://www.nice.fr"),
                Node.createURI("http://france.meteofrance.com/france/meteo/max-temperature/09082011/"),
                Node.createLiteral("26", XSDDatatype.XSDint)));

        Collection<Quadruple> result =
                putGetWs.findQuadruplePattern(QuadruplePattern.ANY);
        log.info("Quadruples contained by the Event-Cloud {}", ecId);
        for (Quadruple quad : result) {
            log.info(quad.toString());
        }
        Assert.assertEquals(3, result.size());

        String sparqlQuery =
                "SELECT ?day WHERE { GRAPH ?g { <http://www.nice.fr> ?day ?temp FILTER (?temp > 26) } }";
        SparqlSelectResponse response =
                putGetWs.executeSparqlSelect(sparqlQuery);
        Node resultNode =
                response.getResult().nextSolution().get("day").asNode();
        log.info("Answer for SPARQL query {}:", sparqlQuery);
        log.info(resultNode.toString());
        Assert.assertEquals(expectedNodeResult, resultNode);

        deployer.undeploy();
    }

    private Component createPutGetComponent(String putGetWsUrl) {
        try {
            Component putGetComponent =
                    (Component) factory.newComponent(
                            "fr.inria.eventcloud.webservices.putget.PutGetComponent",
                            new HashMap<String, Object>());

            GCM.getBindingController(putGetComponent).bindFc(
                    PutGetComponentImpl.PUTGET_WEBSERVICES_NAME,
                    putGetWsUrl + "(" + JaxWsCXFWSCaller.class.getName() + ")");

            GCM.getGCMLifeCycleController(putGetComponent).startFc();

            return putGetComponent;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
