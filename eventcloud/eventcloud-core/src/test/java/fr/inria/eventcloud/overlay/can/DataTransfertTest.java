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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.EventCloudId;
import fr.inria.eventcloud.api.PublishApi;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.SubscribeApi;
import fr.inria.eventcloud.api.Subscription;
import fr.inria.eventcloud.api.SubscriptionId;
import fr.inria.eventcloud.api.listeners.BindingNotificationListener;
import fr.inria.eventcloud.deployment.JunitEventCloudInfrastructureDeployer;
import fr.inria.eventcloud.exceptions.EventCloudIdNotManaged;
import fr.inria.eventcloud.factories.ProxyFactory;
import fr.inria.eventcloud.factories.SemanticFactory;
import fr.inria.eventcloud.formatters.QuadruplesFormatter;
import fr.inria.eventcloud.operations.can.Operations;
import fr.inria.eventcloud.overlay.SemanticPeer;
import fr.inria.eventcloud.providers.SemanticInMemoryOverlayProvider;

/**
 * Test the data transfer during a join operation with a {@link CanOverlay}.
 * 
 * @author lpellegr
 */
public class DataTransfertTest {

    private static final Logger log =
            LoggerFactory.getLogger(DataTransfertTest.class);

    private JunitEventCloudInfrastructureDeployer deployer;

    private EventCloudId eventCloudId;

    @Before
    public void setUp() {
        this.deployer = new JunitEventCloudInfrastructureDeployer();
    }

    @After
    public void tearDown() {
        this.deployer.undeploy();
    }

    @Test
    public void testMiscDataTransfert() throws NetworkAlreadyJoinedException {
        this.eventCloudId = this.deployer.newEventCloud(1, 1);

        SemanticPeer firstPeer =
                this.deployer.getRandomSemanticPeer(this.eventCloudId);
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
        // fourth dimension: one on peer one and one on peer two.
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
                "Element1={}, length={}", UnicodeUtil.makePrintable(elt1),
                elt1.length());
        log.debug(
                "Element2={}, length={}", UnicodeUtil.makePrintable(elt2),
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

        Assert.assertEquals(2, Operations.findQuadruplesOperation(
                firstPeer, QuadruplePattern.ANY).size());

        secondPeer.join(firstPeer);

        log.debug("First peer manages {}", firstPeer);
        log.debug("Second peer manages {}", secondPeer);

        List<Quadruple> firstPeerResult =
                Operations.findQuadruplesOperation(
                        firstPeer, QuadruplePattern.ANY);
        List<Quadruple> secondPeerResult =
                Operations.findQuadruplesOperation(
                        secondPeer, QuadruplePattern.ANY);

        Assert.assertEquals(1, firstPeerResult.size());
        Assert.assertEquals(1, secondPeerResult.size());
    }

    @Test
    public void testSubscriptionsTransfert() throws EventCloudIdNotManaged,
            InterruptedException, NetworkAlreadyJoinedException {
        this.eventCloudId = this.deployer.newEventCloud(1, 2);

        SemanticPeer firstPeer =
                this.deployer.getRandomSemanticPeer(this.eventCloudId);

        SubscribeApi subscribeProxy =
                ProxyFactory.newSubscribeProxy(
                        this.deployer.getEventCloudsRegistryUrl(),
                        this.eventCloudId);

        PublishApi publishProxy =
                ProxyFactory.newPublishProxy(
                        this.deployer.getEventCloudsRegistryUrl(),
                        this.eventCloudId);

        GetIdAndZoneResponseOperation<SemanticElement> response =
                CanOperations.<SemanticElement> getIdAndZoneResponseOperation(firstPeer);

        UnicodeZone<SemanticElement> zone =
                new SemanticZone(
                        response.getPeerZone().getLowerBound(),
                        response.getPeerZone().getUpperBound());

        // split dimension
        byte dimension =
                (byte) (this.deployer.getRandomSemanticTracker(
                        this.eventCloudId).getPeers().size() - 1);

        // we compute the value of the split which will be done on the next join
        // from the first peer in order to create data that will be transfered
        // from a peer to an another
        HomogenousPair<UnicodeZone<SemanticElement>> res =
                zone.split(dimension);

        // the next two elements will be contained by two different peers on the
        // fourth dimension: one on peer one and one on peer two.
        String bound1 =
                res.getSecond()
                        .getLowerBound()
                        .getElement(dimension)
                        .getUnicodeRepresentation();

        // do not use the first zone because low characters are forbidden in
        // IRIs
        String bound2 = "" + ((char) (bound1.charAt(0) - 1));

        Subscription s1 =
                new Subscription(createSubscription(dimension, bound2));
        Subscription s2 =
                new Subscription(createSubscription(dimension, bound1));

        log.debug("First subscription:\n{}", s1.getSparqlQuery());
        log.debug("Second subscription:\n{}", s2.getSparqlQuery());

        subscribeProxy.subscribe(s1, new CustomNotificationListener());
        subscribeProxy.subscribe(s2, new CustomNotificationListener());

        SemanticPeer thirdPeer =
                SemanticFactory.newSemanticPeer(new SemanticInMemoryOverlayProvider());

        Node uri1 = Node.createURI(createDummyUri(bound1));
        Node uri2 = Node.createURI(createDummyUri(bound2));

        publishProxy.publish(new CompoundEvent(new Quadruple(
                uri1, uri1, uri1, uri1)));
        publishProxy.publish(new CompoundEvent(new Quadruple(
                uri2, uri2, uri2, uri2)));

        // to ensure that the subscriptions have been indexed
        Thread.sleep(3000);

        List<Quadruple> subscriptions =
                Operations.findQuadruplesOperation(
                        firstPeer, QuadruplePattern.ANY, true);
        int nbSubscriptionQuadsFirstPeerBeforeJoin = subscriptions.size();

        log.debug(
                "Subscriptions for first peer before join:\n{}",
                QuadruplesFormatter.toString(subscriptions));

        thirdPeer.join(firstPeer);

        subscriptions =
                Operations.findQuadruplesOperation(
                        firstPeer, QuadruplePattern.ANY, true);
        // int nbSubscriptionQuadsFirstPeerAfterJoin = subscriptions.size();

        log.debug(
                "Subscriptions for first peer after join:\n{}",
                QuadruplesFormatter.toString(subscriptions));

        subscriptions =
                Operations.findQuadruplesOperation(
                        thirdPeer, QuadruplePattern.ANY, true);

        log.debug(
                "Subscriptions for third peer after join:\n{}",
                QuadruplesFormatter.toString(subscriptions));

        // Assert.assertTrue(nbSubscriptionQuadsFirstPeerBeforeJoin >
        // nbSubscriptionQuadsFirstPeerAfterJoin);

        Assert.assertTrue(nbSubscriptionQuadsFirstPeerBeforeJoin > subscriptions.size());

        // Assert.assertEquals(
        // nbSubscriptionQuadsFirstPeerBeforeJoin,
        // nbSubscriptionQuadsFirstPeerAfterJoin + subscriptions.size());
    }

    private static String createSubscription(byte dimension, String rdfTerm) {
        char[] vars = {'g', 's', 'p', 'o'};

        StringBuilder result = new StringBuilder("SELECT ?");
        result.append(vars[dimension + 1 % vars.length]);
        result.append(" ?");
        result.append(vars[0]);
        result.append(" WHERE { GRAPH ");
        append(result, dimension, (byte) 0, rdfTerm, vars);
        result.append(" { ");

        for (byte i = 1; i < vars.length; i++) {
            append(result, dimension, i, rdfTerm, vars);
        }

        result.append(" . ?s ?p <");
        result.append(createDummyUri(rdfTerm));
        result.append("> }} ");

        return result.toString();
    }

    private static final String createDummyUri(String localPart) {
        return "http://namespace.org/" + localPart;
    }

    private static void append(StringBuilder result, byte dimension,
                               byte index, String rdfTerm, char[] vars) {
        if (dimension == index) {
            result.append('<');
            result.append(createDummyUri(rdfTerm));
            result.append("> ");
        } else {
            result.append('?');
            result.append(vars[index]);
            result.append(' ');
        }
    }

    private static class CustomNotificationListener extends
            BindingNotificationListener {

        private static final long serialVersionUID = 1L;

        /**
         * {@inheritDoc}
         */
        @Override
        public void onNotification(SubscriptionId id, Binding solution) {
        }

    }

}