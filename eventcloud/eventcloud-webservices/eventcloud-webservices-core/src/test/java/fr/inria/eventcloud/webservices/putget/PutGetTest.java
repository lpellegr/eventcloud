/**
 * Copyright (c) 2011-2013 INRIA.
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
package fr.inria.eventcloud.webservices.putget;

import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
import fr.inria.eventcloud.deployment.JunitEventCloudInfrastructureDeployer;
import fr.inria.eventcloud.webservices.WsTest;
import fr.inria.eventcloud.webservices.api.PutGetWsApi;
import fr.inria.eventcloud.webservices.deployment.WsDeployer;
import fr.inria.eventcloud.webservices.deployment.WsProxyInfo;
import fr.inria.eventcloud.webservices.factories.WsClientFactory;

/**
 * Test cases for {@link PutGetWsApi put/get web service proxies}.
 * 
 * @author bsauvan
 */
public class PutGetTest extends WsTest {

    private static final Logger log = LoggerFactory.getLogger(PutGetTest.class);

    private JunitEventCloudInfrastructureDeployer deployer;

    private WsProxyInfo putgetWsProxyInfo;

    private PutGetWsApi putgetWsClient;

    @Before
    public void setUp() {
        this.initEventCloudEnvironmentAndClient();
    }

    @Test(timeout = 180000)
    public void testPutGetWsProxy() throws Exception {
        this.putgetWsClient.addQuadruple(new Quadruple(
                Node.createURI("http://sources.event-processing.org/ids/NiceWeatherStation01#source"),
                Node.createURI("http://www.nice.fr"),
                Node.createURI("http://france.meteofrance.com/france/meteo"),
                Node.createURI("http://france.meteofrance.com/france/meteo?PREVISIONS_PORTLET.path=previsionsville/060880")));

        Node expectedNodeResult =
                Node.createURI("http://france.meteofrance.com/france/meteo/max-temperature/08082011-2012/");
        this.putgetWsClient.addQuadruple(new Quadruple(
                Node.createURI("http://sources.event-processing.org/ids/NiceWeatherStation01#source"),
                Node.createURI("http://www.nice.fr"), expectedNodeResult,
                Node.createLiteral("29", XSDDatatype.XSDint)));

        this.putgetWsClient.addQuadruple(new Quadruple(
                Node.createURI("http://sources.event-processing.org/ids/NiceWeatherStation01#source"),
                Node.createURI("http://www.nice.fr"),
                Node.createURI("http://france.meteofrance.com/france/meteo/max-temperature/09082011-2012/"),
                Node.createLiteral("26", XSDDatatype.XSDint)));

        List<Quadruple> result =
                this.putgetWsClient.findQuadruplePattern(QuadruplePattern.ANY);
        log.info("Quadruples contained by the EventCloud:");
        for (Quadruple quad : result) {
            log.info(quad.toString());
        }
        Assert.assertEquals(3, result.size());

        String sparqlQuery =
                "SELECT ?day WHERE { GRAPH ?g { <http://www.nice.fr> ?day ?temp FILTER (?temp > 26) } }";
        SparqlSelectResponse response =
                this.putgetWsClient.executeSparqlSelect(sparqlQuery);
        Node resultNode =
                response.getResult().nextSolution().get("day").asNode();
        log.info("Answer for SPARQL query {}:", sparqlQuery);
        log.info(resultNode.toString());
        Assert.assertEquals(expectedNodeResult, resultNode);
    }

    private void initEventCloudEnvironmentAndClient() {
        this.deployer = new JunitEventCloudInfrastructureDeployer();

        EventCloudId id = this.deployer.newEventCloud(1, 1);

        this.putgetWsProxyInfo =
                WsDeployer.deployPutGetWsProxy(
                        LOCAL_NODE_PROVIDER,
                        this.deployer.getEventCloudsRegistryUrl(),
                        id.getStreamUrl(), "putget");

        this.putgetWsClient =
                WsClientFactory.createWsClient(
                        PutGetWsApi.class,
                        this.putgetWsProxyInfo.getWsEndpointUrl());

    }

    @After
    public void tearDown() {
        this.putgetWsProxyInfo.destroy();
        this.deployer.undeploy();
    }

}
