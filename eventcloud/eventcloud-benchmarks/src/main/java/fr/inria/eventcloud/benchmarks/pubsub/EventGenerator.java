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
package fr.inria.eventcloud.benchmarks.pubsub;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.operations.CanOperations;
import org.objectweb.proactive.extensions.p2p.structured.operations.can.GetIdAndZoneResponseOperation;
import org.objectweb.proactive.extensions.p2p.structured.overlay.Peer;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.utils.RandomUtils;
import org.objectweb.proactive.extensions.p2p.structured.utils.StringRepresentation;
import org.objectweb.proactive.extensions.p2p.structured.utils.UnicodeUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.EventCloudDescription;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.deployment.EventCloudDeployer;
import fr.inria.eventcloud.deployment.EventCloudDeploymentDescriptor;
import fr.inria.eventcloud.overlay.can.SemanticCoordinateFactory;
import fr.inria.eventcloud.overlay.can.SemanticElement;
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
                                                    int nodeSize) {

        Builder<Quadruple> builder = ImmutableList.<Quadruple> builder();

        Node graphNode =
                randomGraphNode(
                        zones.get(RandomUtils.nextInt(zones.size())), nodeSize);

        for (int i = 0; i < nbQuadruples; i++) {
            builder.add(randomQuadruple(
                    zones.get(i % zones.size()), graphNode, nodeSize));
        }

        return new CompoundEvent(builder.build());
    }

    public static CompoundEvent randomCompoundEvent(SemanticZone[] zones,
                                                    int nbQuadruples,
                                                    int nodeSize) {
        return randomCompoundEvent(
                ImmutableList.copyOf(zones), nbQuadruples, nodeSize);
    }

    public static CompoundEvent randomCompoundEventForRewriting(List<SemanticZone> zones,
                                                                int nbQuadruples,
                                                                int nodeSize,
                                                                int nbRewrites) {

        Builder<Quadruple> builder = ImmutableList.<Quadruple> builder();

        Node graph =
                randomGraphNode(
                        zones.get(RandomUtils.nextInt(zones.size())), nodeSize);
        Node subject = null;
        Node predicate = null;
        Node object = null;

        for (int i = 0; i < nbRewrites + 1; i++) {
            int zoneIndex = i % zones.size();

            SemanticZone zone = zones.get(zoneIndex);

            if (i == 0) {
                subject =
                        randomNode(
                                zone.getLowerBound((byte) 1),
                                zone.getUpperBound((byte) 2), -1, nodeSize);
            }

            object =
                    randomNode(
                            zone.getLowerBound((byte) 3),
                            zone.getUpperBound((byte) 3), -1, nodeSize);

            predicate =
                    randomNode(
                            zone.getLowerBound((byte) 2),
                            zone.getUpperBound((byte) 2), -1, nodeSize);

            builder.add(new Quadruple(
                    graph, subject, predicate, object, false, false));

            subject = object;
        }

        for (int i = 0; i < nbQuadruples - nbRewrites + 1; i++) {
            builder.add(randomQuadruple(zones.get((nbRewrites + 1 + i)
                    % zones.size()), graph, nodeSize));
        }

        return new CompoundEvent(builder.build());
    }

    public static CompoundEvent randomCompoundEventForRewriting(SemanticZone[] zones,
                                                                int nbQuadruples,
                                                                int nodeSize,
                                                                int nbRewrites) {
        return randomCompoundEvent(
                ImmutableList.copyOf(zones), nbQuadruples, nodeSize);
    }

    /**
     * Generates a compound event whose all the quadruples fit into the
     * specified zone.
     * 
     * @param zone
     *            the zone used to generate the quadruples contained by the
     *            compound event.
     * @param nbQuadruples
     *            the number of quadruples to generate for the compound event.
     * @param nodeSize
     *            the number of characters assigned to each RDF term of
     *            quadruple that is generated.
     * 
     * @return the generated compound event.
     */
    public static CompoundEvent randomCompoundEvent(SemanticZone zone,
                                                    int nbQuadruples,
                                                    int nodeSize) {
        Builder<Quadruple> builder = ImmutableList.<Quadruple> builder();

        Node graphNode = randomGraphNode(zone, nodeSize);

        for (int i = 0; i < nbQuadruples; i++) {
            builder.add(randomQuadruple(zone, graphNode, nodeSize));
        }

        return new CompoundEvent(builder.build());
    }

    private static Node randomGraphNode(SemanticZone zone, int nodeSize) {
        return randomNode(
                zone.getLowerBound((byte) 0), zone.getUpperBound((byte) 0),
                sequenceNumber.incrementAndGet(), nodeSize);
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

    private static int[] ucscpts = {
            45, 46, 48, 57, 65, 90, 95, 97, 122, 126, 160, 55295, 63744, 64975,
            65008, 65519, 65536, 131069, 131072, 196605, 196608, 262141,
            262144, 327677, 327680, 393213, 393216, 458749, 458752, 524285,
            524288, 589821, 589824, 655357, 655360, 720893, 720896, 786429,
            786432, 851965, 851968, 917501, 921600, 983037};

    private static boolean isucschar(int codepoint) {
        // Unicode characters I HATE you
        return codepoint == 45 || codepoint == 46 || codepoint == 95
                || codepoint == 126 || (codepoint >= 48 && codepoint <= 57)
                || (codepoint >= 65 && codepoint <= 90)
                || (codepoint >= 97 && codepoint <= 122)
                || (codepoint >= 160 && codepoint <= 55295)
                || (codepoint >= 63744 && codepoint <= 64975)
                || (codepoint >= 65008 && codepoint <= 65519)
                || (codepoint >= 65536 && codepoint <= 131069)
                || (codepoint >= 131072 && codepoint <= 196605)
                || (codepoint >= 196608 && codepoint <= 262141)
                || (codepoint >= 262144 && codepoint <= 327677)
                || (codepoint >= 327680 && codepoint <= 393213)
                || (codepoint >= 393216 && codepoint <= 458749)
                || (codepoint >= 458752 && codepoint <= 524285)
                || (codepoint >= 524288 && codepoint <= 589821)
                || (codepoint >= 589824 && codepoint <= 655357)
                || (codepoint >= 655360 && codepoint <= 720893)
                || (codepoint >= 720896 && codepoint <= 786429)
                || (codepoint >= 786432 && codepoint <= 851965)
                || (codepoint >= 851968 && codepoint <= 917501)
                || (codepoint >= 921600 && codepoint <= 983037);
    }

    private static int findBestSubstitue(int codepoint) {
        int bestCandidate = 0;
        int diff = Integer.MAX_VALUE;

        for (int cpt : ucscpts) {
            int tmpDiff = Math.abs(codepoint - cpt);
            if (tmpDiff < diff) {
                bestCandidate = cpt;
                diff = tmpDiff;
            }

            if (tmpDiff == 1) {
                break;
            }
        }

        return bestCandidate;
    }

    private static Node randomNode(SemanticElement lowerBoundElement,
                                   SemanticElement upperBoundElement,
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

            // if (!isucschar(result[i])) {
            // System.out.println("ILLEGAL CHARACTER " + result[i]);
            // result[i] = findBestSubstitue(result[i]);
            // System.out.println("NEW CANDIDATE " + result[i]);
            // }
        }

        String uri = "urn:" + UnicodeUtils.toString(result);

        if (sequenceNumber > 0) {
            uri = uri + sequenceNumber;
        }

        return Node.createURI(uri);
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
            GetIdAndZoneResponseOperation<SemanticElement> response =
                    CanOperations.getIdAndZoneResponseOperation(p);
            zones.add((SemanticZone) response.getPeerZone());
        }

        System.out.println("Zones are:");

        for (SemanticZone z : zones) {
            System.out.println("  " + z);
        }

        System.out.println("Generated compound event:");

        CompoundEvent ce = randomCompoundEventForRewriting(zones, 10, 10, 3);

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

            Coordinate<SemanticElement> sc =
                    SemanticCoordinateFactory.newSemanticCoordinate(q);

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
