/**
 * Copyright (c) 2011-2014 INRIA.
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
package fr.inria.eventcloud.benchmarks.pubsub;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.operations.CanOperations;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.GetIdAndZoneResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.points.Point;
import org.objectweb.proactive.extensions.p2p.structured.utils.RandomUtils;
import org.objectweb.proactive.extensions.p2p.structured.utils.StringRepresentation;
import org.objectweb.proactive.extensions.p2p.structured.utils.UnicodeUtils;

import com.google.common.collect.ImmutableList;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;

import fr.inria.eventcloud.EventCloudDescription;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.deployment.EventCloudDeployer;
import fr.inria.eventcloud.deployment.EventCloudDeploymentDescriptor;
import fr.inria.eventcloud.overlay.can.SemanticCoordinate;
import fr.inria.eventcloud.overlay.can.SemanticPointFactory;
import fr.inria.eventcloud.overlay.can.SemanticZone;

/**
 * Generates quadruples or compound events so that quadruples are managed by the
 * specified {@link SemanticZone}.
 * 
 * @author lpellegr
 */
public class EventGenerator {

    private static AtomicInteger sequenceNumber = new AtomicInteger();

    /**
     * Reset the event generator. It resets essentially the sequence number to
     * its default value.
     */
    public static void reset() {
        sequenceNumber.set(0);
    }

    /**
     * Generates a compound event so that each quadruple from the compound event
     * is uniformly distributed to the specified zones.
     * 
     * @param zones
     *            the zones used to generate the quadruples contained by the
     *            compound event.
     * @param nbQuadruples
     *            the number of quadruples to generate for the compound event.
     * @param nodeSize
     *            the number of characters assigned to each RDF term of
     *            quadruple that is generated.
     * 
     * @return the generated compound event.
     */
    public static CompoundEvent randomCompoundEvent(List<SemanticZone> zones,
                                                    int nbQuadruples,
                                                    int nodeSize,
                                                    boolean shuffle) {

        List<Quadruple> quadruples = new ArrayList<Quadruple>(nbQuadruples);

        Node graphNode =
                randomGraphNode(
                        zones.get(RandomUtils.nextInt(zones.size())), nodeSize);

        for (int i = 0; i < nbQuadruples; i++) {
            quadruples.add(randomQuadruple(
                    zones.get(i % zones.size()), graphNode, nodeSize));
        }

        if (shuffle) {
            // shuffle elements, randomness is sometimes good :)
            Collections.shuffle(quadruples, RandomUtils.JVM_RANDOM);
        }

        return new CompoundEvent(quadruples);
    }

    public static CompoundEvent randomCompoundEvent(SemanticZone[] zones,
                                                    int nbQuadruples,
                                                    int nodeSize,
                                                    boolean shuffle) {
        return randomCompoundEvent(
                ImmutableList.copyOf(zones), nbQuadruples, nodeSize, shuffle);
    }

    public static CompoundEvent randomCompoundEventForRewriting(List<SemanticZone> zones,
                                                                Node[] fixedPredicateNodes,
                                                                int eventIndex,
                                                                int nbQuadruples,
                                                                int nodeSize,
                                                                int objectSize,
                                                                int nbRewrites,
                                                                boolean shuffle) {

        SemanticZone zone = zones.get(eventIndex % zones.size());

        Node graph =
                randomGraphNode(
                        zones.get(RandomUtils.nextInt(zones.size())), nodeSize);

        Node subject = null;
        Node predicate = null;
        Node object = null;

        List<Quadruple> quadruples = new ArrayList<Quadruple>(nbQuadruples);

        for (int i = 0; i < nbRewrites + 1; i++) {
            int zoneIndex = RandomUtils.nextInt(zones.size());

            zone = zones.get(zoneIndex);

            if (i == 0) {
                subject =
                        randomNode(
                                zone.getLowerBound((byte) 1),
                                zone.getUpperBound((byte) 1), -1, nodeSize);
            }

            byte dimensionIndex = 3;

            object =
                    randomNode(
                            zone.getLowerBound(dimensionIndex),
                            zone.getUpperBound(dimensionIndex), -1, objectSize);

            if (fixedPredicateNodes != null && fixedPredicateNodes.length > 0) {
                predicate = fixedPredicateNodes[i];
            } else {
                predicate =
                        randomNode(
                                zone.getLowerBound((byte) 2),
                                zone.getUpperBound((byte) 2), -1, nodeSize);
            }

            quadruples.add(new Quadruple(
                    graph, subject, predicate, object, false, false));

            subject = object;
        }

        for (int i = 0; i < nbQuadruples - nbRewrites - 1; i++) {
            quadruples.add(randomQuadruple(zones.get((nbRewrites + 1 + i)
                    % zones.size()), graph, nodeSize));
        }

        if (shuffle) {
            // shuffle elements, randomness is sometimes good :)
            Collections.shuffle(quadruples, RandomUtils.JVM_RANDOM);
        }

        return new CompoundEvent(quadruples);
    }

    public static CompoundEvent randomCompoundEventForRewriting(SemanticZone[] zones,
                                                                Node[] fixedPredicateNodes,
                                                                int eventIndex,
                                                                int nbQuadruples,
                                                                int nodeSize,
                                                                int objectSize,
                                                                int nbRewrites,
                                                                boolean shuffle) {
        return randomCompoundEventForRewriting(
                ImmutableList.copyOf(zones), fixedPredicateNodes, eventIndex,
                nbQuadruples, nodeSize, objectSize, nbRewrites, shuffle);
    }

    private static Node randomGraphNode(SemanticZone zone, int nodeSize) {
        return randomNode(
                zone.getLowerBound((byte) 0), zone.getUpperBound((byte) 0),
                sequenceNumber.incrementAndGet(), nodeSize);
    }

    public static Quadruple randomQuadruple(SemanticZone zone, int nodeSize) {
        Node[] nodes =
                new Node[P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue()];

        for (byte i = 0; i < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); i++) {
            nodes[i] =
                    randomNode(
                            zone.getLowerBound(i), zone.getUpperBound(i), -1,
                            nodeSize);
        }

        return new Quadruple(
                nodes[0], nodes[1], nodes[2], nodes[3], false, false);
    }

    public static Quadruple randomQuadruple(SemanticZone zone, Node graphNode,
                                            int nodeSize) {
        Node[] nodes =
                new Node[P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue() - 1];

        for (byte i = 1; i < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); i++) {
            nodes[i - 1] =
                    randomNode(
                            zone.getLowerBound(i), zone.getUpperBound(i), -1,
                            nodeSize);
        }

        return new Quadruple(
                graphNode, nodes[0], nodes[1], nodes[2], false, false);
    }

    public static Node randomNode(SemanticCoordinate lowerBoundElement,
                                  SemanticCoordinate upperBoundElement,
                                  int sequenceNumber, int nodeSize) {

        int[] lbCodePoints =
                UnicodeUtils.toCodePointArray(lowerBoundElement.getValue());
        int[] upCodePoints =
                UnicodeUtils.toCodePointArray(upperBoundElement.getValue());

        int[] result = new int[nodeSize];

        for (int i = 0; i < nodeSize; i++) {
            int lowerBound = P2PStructuredProperties.CAN_LOWER_BOUND.getValue();
            int upperBound = P2PStructuredProperties.CAN_UPPER_BOUND.getValue();

            if (i < lbCodePoints.length) {
                lowerBound = lbCodePoints[i];
            }

            if (i < upCodePoints.length) {
                upperBound = upCodePoints[i];
            }

            if (lowerBound > upperBound) {
                int tmp = lowerBound;
                lowerBound = upperBound;
                upperBound = tmp;
            }

            int diff = upperBound - lowerBound;

            if (diff == 0) {
                result[i] = lowerBound;
            } else {
                result[i] = lowerBound + RandomUtils.nextInt(diff);
            }

            // replaces illegal characters by legal ones
            if (!IRIFixer.isUnreserved(result[i])) {
                result[i] = IRIFixer.findBestSubstitute(result[i]);
            }
        }

        String uri = "urn:" + UnicodeUtils.toString(result);

        if (sequenceNumber > 0) {
            String sequenceNumberAsString = Integer.toString(sequenceNumber);
            if ('0' < P2PStructuredProperties.CAN_LOWER_BOUND.getValue()) {
                sequenceNumberAsString =
                        UnicodeUtils.translate(
                                sequenceNumberAsString,
                                P2PStructuredProperties.CAN_LOWER_BOUND.getValue() - '0');
            }

            uri = uri + sequenceNumberAsString;
        }

        return NodeFactory.createURI(uri);
    }

    public static void main(String[] args) {
        P2PStructuredProperties.CAN_NB_DIMENSIONS.setValue((byte) 4);

        EventCloudDeployer deployer =
                new EventCloudDeployer(
                        new EventCloudDescription(),
                        new EventCloudDeploymentDescriptor());
        deployer.deploy(10);

        List<SemanticZone> zones = new ArrayList<SemanticZone>();

        for (Peer p : deployer.getRandomSemanticTracker().getPeers()) {
            GetIdAndZoneResponseOperation<SemanticCoordinate> response =
                    CanOperations.getIdAndZoneResponseOperation(p);
            zones.add((SemanticZone) response.getPeerZone());
        }

        System.out.println("Zones are:");

        for (SemanticZone z : zones) {
            System.out.println("  " + z);
        }

        System.out.println("Generated compound event:");

        CompoundEvent ce =
                randomCompoundEventForRewriting(
                        zones, new Node[0], 0, 10, 10, 10, 3, false);

        for (Quadruple q : ce) {
            System.out.println(q.toString(StringRepresentation.UTF_16));
        }

        /*
        * Simple scenario to check that each quadruple that is
        * generated belongs to the zone of one peer only
        */
        for (Quadruple q : ce) {
            System.out.println("  "
                    + q.toString(StringRepresentation.CODE_POINTS));

            Point<SemanticCoordinate> sc =
                    SemanticPointFactory.newSemanticCoordinate(q);

            for (SemanticZone z : zones) {
                if (z.contains(sc)) {
                    System.out.println("     contained by " + z);
                }
            }
        }

        System.out.println("end");

        deployer.undeploy();

        System.exit(0);
    }

}
