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
package fr.inria.eventcloud.overlay.can;

import java.util.HashSet;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.factories.SemanticFactory;
import fr.inria.eventcloud.initializers.EventCloudInitializer;
import fr.inria.eventcloud.operations.can.FindQuadruplesOperation;
import fr.inria.eventcloud.operations.can.FindQuadruplesResponseOperation;
import fr.inria.eventcloud.overlay.SemanticPeer;

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

    private static String[][] quadrupleValues = {
            {"http://A", "http://A", "http://A", "http://A"},
            {"http://U", "http://U", "http://U", "http://U"},
            {"http://Z", "http://Z", "http://Z", "http://Z"}};

    @Before
    public void setUp() {
        this.initializer = new EventCloudInitializer(1);
        this.initializer.setUp();
    }

    @Test
    public void testDataTransfert() {
        for (String[] stmt : quadrupleValues) {
            this.initializer.selectPeer().add(
                    new Quadruple(
                            Node.createURI(stmt[0]), Node.createURI(stmt[1]),
                            Node.createURI(stmt[2]), Node.createURI(stmt[3])));
        }

        SemanticPeer newPeer = SemanticFactory.newSemanticPeer();
        SemanticPeer oldPeer = this.initializer.selectPeer();

        Collection<Quadruple> oldPeerQuads = oldPeer.find(QuadruplePattern.ANY);
        int nbDataContainedByOldPeer = oldPeerQuads.size();

        logger.debug(
                "Before join operation on old peer, the old peer contains {} data",
                nbDataContainedByOldPeer);

        try {
            this.initializer.getRandomTracker().inject(newPeer);
        } catch (NetworkAlreadyJoinedException e) {
            e.printStackTrace();
        }

        logger.debug("Initial peer manages " + oldPeer);
        logger.debug("New peer manages " + newPeer);

        oldPeerQuads = findQuadruplesOperation(oldPeer, QuadruplePattern.ANY);
        nbDataContainedByOldPeer = oldPeerQuads.size();

        Collection<Quadruple> newPeerQuads =
                findQuadruplesOperation(newPeer, QuadruplePattern.ANY);
        int nbDataContainedByNewPeer = newPeerQuads.size();

        logger.debug(
                "After join operation on old peer with new peer, the old peer contains {} data whereas the new peer contains {} data",
                nbDataContainedByOldPeer, nbDataContainedByNewPeer);

        Assert.assertNotSame(0, Sets.intersection(new HashSet<Quadruple>(
                oldPeerQuads), new HashSet<Quadruple>(newPeerQuads)));
        Assert.assertEquals(quadrupleValues.length, nbDataContainedByOldPeer
                + nbDataContainedByNewPeer);
        Assert.assertTrue(nbDataContainedByOldPeer < quadrupleValues.length);
        Assert.assertTrue(nbDataContainedByOldPeer > 0);
    }

    @After
    public void tearDown() {
        this.initializer.tearDown();
    }

    private static Collection<Quadruple> findQuadruplesOperation(SemanticPeer peer,
                                                                 QuadruplePattern quadruplePattern) {
        return ((FindQuadruplesResponseOperation) PAFuture.getFutureValue(peer.receiveImmediateService(new FindQuadruplesOperation(
                quadruplePattern)))).getQuadruples();
    }

}
