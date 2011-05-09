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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>
 **/
package fr.inria.eventcloud.overlay.can;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.ontoware.rdf2go.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import fr.inria.eventcloud.api.SemanticFactory;
import fr.inria.eventcloud.config.EventCloudProperties;
import fr.inria.eventcloud.initializers.EventCloudInitializer;
import fr.inria.eventcloud.operations.can.SparqlConstructOperation;
import fr.inria.eventcloud.operations.can.SparqlConstructResponseOperation;
import fr.inria.eventcloud.overlay.SemanticPeer;
import fr.inria.eventcloud.util.RDF2GoBuilder;
import fr.inria.eventcloud.util.SemanticHelper;

/**
 * Test the data transfert during a join operation for
 * {@link SemanticSpaceCANOverlay}.
 * 
 * @author lpellegr
 */
public class DataTransfertTest {

    private final static Logger logger =
            LoggerFactory.getLogger(DataTransfertTest.class);

    private EventCloudInitializer initializer;

    private static String[][] statementsToAdd = {
            {"http://A", "http://A", "http://A"},
            {"http://U", "http://U", "http://U"},
            {"http://Z", "http://Z", "http://Z"}};

    @Before
    public void setUp() {
        this.initializer = new EventCloudInitializer();
        this.initializer.setUpNetworkOnLocalMachine(1);
    }

    @Test
    public void testDataTransfert() {
        for (String[] stmt : statementsToAdd) {
            this.initializer.getRandomPeer().addStatement(
                    EventCloudProperties.DEFAULT_CONTEXT,
                    RDF2GoBuilder.createStatementInternal(
                            stmt[0], stmt[1], stmt[2]));
        }

        SemanticPeer newPeer = SemanticFactory.newActiveSemanticPeer();
        SemanticPeer oldPeer = this.initializer.getRandomPeer();

        String query = "CONSTRUCT { ?s ?p ?o } WHERE { ?s ?p ?o }";

        Set<Statement> dataContainedByOldPeer =
                SemanticHelper.asSet(((SparqlConstructResponseOperation) PAFuture.getFutureValue(oldPeer.receiveImmediateService(new SparqlConstructOperation(
                        EventCloudProperties.DEFAULT_CONTEXT, query)))).getResult()
                        .toRDF2Go());

        logger.debug(
                "Before join operation on old peer, the old peer contains {} data",
                dataContainedByOldPeer.size());

        try {
            this.initializer.getRandomTracker().addOnNetwork(newPeer);
        } catch (NetworkAlreadyJoinedException e) {
            e.printStackTrace();
        }

        logger.debug("Initial peer manages "
                + this.initializer.getRandomTracker().getRandomPeer());
        logger.debug("New peer manages " + newPeer);

        dataContainedByOldPeer =
                SemanticHelper.asSet(((SparqlConstructResponseOperation) PAFuture.getFutureValue(oldPeer.receiveImmediateService(new SparqlConstructOperation(
                        EventCloudProperties.DEFAULT_CONTEXT, query)))).getResult()
                        .toRDF2Go());

        Set<Statement> dataContainedByNewPeer =
                SemanticHelper.asSet(((SparqlConstructResponseOperation) PAFuture.getFutureValue(newPeer.receiveImmediateService(new SparqlConstructOperation(
                        EventCloudProperties.DEFAULT_CONTEXT, query)))).getResult()
                        .toRDF2Go());

        logger.debug(
                "After join operation on old peer with new peer, the old peer contains {} data whereas the new peer contains {} data",
                dataContainedByOldPeer.size(), dataContainedByNewPeer.size());

        assertEquals(0, Sets.intersection(
                dataContainedByOldPeer, dataContainedByNewPeer).size());
        assertEquals(statementsToAdd.length, dataContainedByOldPeer.size()
                + dataContainedByNewPeer.size());
        assertTrue(dataContainedByOldPeer.size() < statementsToAdd.length);
        assertTrue(dataContainedByOldPeer.size() > 0);
    }

    @After
    public void tearDown() {
        this.initializer.tearDownNetwork();
    }

}
