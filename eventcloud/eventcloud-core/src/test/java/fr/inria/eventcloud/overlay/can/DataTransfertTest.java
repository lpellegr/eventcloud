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
package fr.inria.eventcloud.overlay.can;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.objectweb.proactive.api.PAFuture;
import org.objectweb.proactive.extensions.p2p.structured.exceptions.NetworkAlreadyJoinedException;
import org.objectweb.proactive.extensions.p2p.structured.operations.CanOperations;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.GetIdAndZoneResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.UnicodeZone;
import org.objectweb.proactive.extensions.p2p.structured.utils.HomogenousPair;
import org.objectweb.proactive.extensions.p2p.structured.utils.UnicodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.deployment.JunitByClassEventCloudDeployer;
import fr.inria.eventcloud.factories.SemanticFactory;
import fr.inria.eventcloud.operations.can.FindQuadruplesOperation;
import fr.inria.eventcloud.operations.can.FindQuadruplesResponseOperation;
import fr.inria.eventcloud.overlay.SemanticPeer;
import fr.inria.eventcloud.providers.SemanticInMemoryOverlayProvider;

/**
 * Test the data transfer during a join operation with a {@link CanOverlay}.
 * 
 * @author lpellegr
 */
public class DataTransfertTest extends JunitByClassEventCloudDeployer {

    private static final Logger log =
            LoggerFactory.getLogger(DataTransfertTest.class);

    public DataTransfertTest() {
        super(1, 1);
    }

    @Test
    public void testDataTransfert() {
        SemanticPeer firstPeer = super.getRandomSemanticPeer();
        SemanticPeer secondPeer =
                SemanticFactory.newSemanticPeer(new SemanticInMemoryOverlayProvider());

        GetIdAndZoneResponseOperation<SemanticElement> response =
                CanOperations.<SemanticElement> getIdAndZoneResponseOperation(firstPeer);

        UnicodeZone<SemanticElement> zone =
                new SemanticZone(
                        response.getPeerZone().getLowerBound(),
                        response.getPeerZone().getUpperBound());

        byte dimensionSplit = 0;

        // we compute the value of the split which will be done on the next join
        // from the third peer in order to create data that will be transfered
        // from a peer to an another
        HomogenousPair<UnicodeZone<SemanticElement>> res =
                zone.split(dimensionSplit);

        // the next two elements will be contained by two different peers on the
        // fourth dimension: one on peer one and one on peer five.
        String elt1 =
                res.getFirst()
                        .getLowerBound()
                        .getElement(dimensionSplit)
                        .getUnicodeRepresentation();

        String elt2 =
                res.getSecond()
                        .getLowerBound()
                        .getElement(dimensionSplit)
                        .getUnicodeRepresentation()
                        + "a";

        log.debug(
                "Element1={}, size ={}", UnicodeUtil.makePrintable(elt1),
                elt1.length());
        log.debug(
                "Element2={}, size ={}", UnicodeUtil.makePrintable(elt2),
                elt2.length());

        Node node1 = Node.createURI(elt1);
        Node node2 = Node.createURI(elt2);

        Quadruple quad1 =
                new Quadruple(
                        node1, Node.createURI(res.getFirst().getLowerBound(
                                (byte) 1).getUnicodeRepresentation()),
                        Node.createURI(res.getFirst()
                                .getLowerBound((byte) 2)
                                .getUnicodeRepresentation()),
                        Node.createURI(res.getFirst()
                                .getLowerBound((byte) 3)
                                .getUnicodeRepresentation()));

        Quadruple quad2 =
                new Quadruple(
                        node2, Node.createURI(res.getSecond().getLowerBound(
                                (byte) 1).getUnicodeRepresentation()),
                        Node.createURI(res.getSecond()
                                .getLowerBound((byte) 2)
                                .getUnicodeRepresentation()),
                        Node.createURI(res.getSecond()
                                .getLowerBound((byte) 3)
                                .getUnicodeRepresentation()));

        log.debug("Quadruple1={}", quad1);
        log.debug("Quadruple2={}", quad2);

        // add two quadruples whose one must be conveyed to the second peer when
        // it joins the first peer
        firstPeer.add(quad1);
        firstPeer.add(quad2);

        Assert.assertEquals(2, findQuadruplesOperation(
                firstPeer, QuadruplePattern.ANY).size());

        try {
            secondPeer.join(firstPeer);
        } catch (NetworkAlreadyJoinedException e) {
            throw new IllegalStateException(e);
        }

        log.debug("First peer manages {}", firstPeer);
        log.debug("Second peer manages {}", secondPeer);

        List<Quadruple> firstPeerResult =
                findQuadruplesOperation(firstPeer, QuadruplePattern.ANY);
        List<Quadruple> secondPeerResult =
                findQuadruplesOperation(secondPeer, QuadruplePattern.ANY);

        Assert.assertEquals(1, firstPeerResult.size());
        Assert.assertEquals(1, secondPeerResult.size());
    }

    private static List<Quadruple> findQuadruplesOperation(SemanticPeer peer,
                                                           QuadruplePattern quadruplePattern) {
        return ((FindQuadruplesResponseOperation) PAFuture.getFutureValue(peer.receiveImmediateService(new FindQuadruplesOperation(
                quadruplePattern)))).getQuadruples();
    }

}
