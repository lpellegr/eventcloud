/**
 * Copyright (c) 2011-2012 INRIA.
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
import java.util.List;

import org.etsi.uri.gcm.util.GCM;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.fractal.api.Component;
import org.objectweb.fractal.api.Interface;
import org.objectweb.fractal.api.NoSuchInterfaceException;
import org.objectweb.fractal.api.control.IllegalBindingException;
import org.objectweb.fractal.api.control.IllegalLifeCycleException;
import org.objectweb.proactive.core.component.webservices.JaxWsCXFWSCaller;
import org.objectweb.proactive.extensions.p2p.structured.utils.ComponentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
import fr.inria.eventcloud.deployment.JunitEventCloudInfrastructureDeployer;
import fr.inria.eventcloud.webservices.api.PutGetWsApi;
import fr.inria.eventcloud.webservices.deployment.WebServiceDeployer;

/**
 * Class used to test a put/get proxy component by using web services.
 * 
 * @author bsauvan
 */
public class PutGetTest {

    private static final Logger log = LoggerFactory.getLogger(PutGetTest.class);

    @Test(timeout = 90000)
    public void testPutGetWsProxy() throws Exception {
        JunitEventCloudInfrastructureDeployer deployer =
                new JunitEventCloudInfrastructureDeployer();

        EventCloudId ecId = deployer.newEventCloud(1, 10);

        String putGetWsEndpointUrl =
                WebServiceDeployer.deployPutGetWebService(
                        deployer.getEventCloudsRegistryUrl(),
                        ecId.getStreamUrl(), "putget", 8889)
                        .getEndpoint()
                        .getEndpointInfo()
                        .getAddress();

        PutGetWsApi putGetCaller = this.createPutGetCaller(putGetWsEndpointUrl);

        putGetCaller.addQuadruple(new Quadruple(
                Node.createURI("http://sources.event-processing.org/ids/NiceWeatherStation01#source"),
                Node.createURI("http://www.nice.fr"),
                Node.createURI("http://france.meteofrance.com/france/meteo"),
                Node.createURI("http://france.meteofrance.com/france/meteo?PREVISIONS_PORTLET.path=previsionsville/060880")));

        Node expectedNodeResult =
                Node.createURI("http://france.meteofrance.com/france/meteo/max-temperature/08082011-2012/");
        putGetCaller.addQuadruple(new Quadruple(
                Node.createURI("http://sources.event-processing.org/ids/NiceWeatherStation01#source"),
                Node.createURI("http://www.nice.fr"), expectedNodeResult,
                Node.createLiteral("29", XSDDatatype.XSDint)));

        putGetCaller.addQuadruple(new Quadruple(
                Node.createURI("http://sources.event-processing.org/ids/NiceWeatherStation01#source"),
                Node.createURI("http://www.nice.fr"),
                Node.createURI("http://france.meteofrance.com/france/meteo/max-temperature/09082011-2012/"),
                Node.createLiteral("26", XSDDatatype.XSDint)));

        List<Quadruple> result =
                putGetCaller.findQuadruplePattern(QuadruplePattern.ANY);
        log.info("Quadruples contained by the EventCloud {}", ecId);
        for (Quadruple quad : result) {
            log.info(quad.toString());
        }
        Assert.assertEquals(3, result.size());

        String sparqlQuery =
                "SELECT ?day WHERE { GRAPH ?g { <http://www.nice.fr> ?day ?temp FILTER (?temp > 26) } }";
        SparqlSelectResponse response =
                putGetCaller.executeSparqlSelect(sparqlQuery);
        Node resultNode =
                response.getResult().nextSolution().get("day").asNode();
        log.info("Answer for SPARQL query {}:", sparqlQuery);
        log.info(resultNode.toString());
        Assert.assertEquals(expectedNodeResult, resultNode);

        deployer.undeploy();
        ComponentUtils.terminateComponent(putGetCaller);
    }

    private PutGetWsApi createPutGetCaller(String putGetWsEndpointUrl) {

        try {
            PutGetWsApi putGetCaller =
                    ComponentUtils.createComponentAndGetInterface(
                            "fr.inria.eventcloud.webservices.putget.PutGetComponent",
                            new HashMap<String, Object>(), "putget-services",
                            PutGetWsApi.class, false);
            Component putGetComponent =
                    ((Interface) putGetCaller).getFcItfOwner();

            GCM.getBindingController(putGetComponent).bindFc(
                    PutGetComponentImpl.PUTGET_WEBSERVICES_NAME,
                    putGetWsEndpointUrl + "("
                            + JaxWsCXFWSCaller.class.getName() + ")");

            GCM.getGCMLifeCycleController(putGetComponent).startFc();

            return putGetCaller;
        } catch (NoSuchInterfaceException e) {
            throw new IllegalStateException(e);
        } catch (IllegalBindingException e) {
            throw new IllegalStateException(e);
        } catch (IllegalLifeCycleException e) {
            throw new IllegalStateException(e);
        }
    }

}
