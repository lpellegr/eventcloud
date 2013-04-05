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

import java.util.concurrent.atomic.AtomicInteger;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.utils.RandomUtils;
import org.objectweb.proactive.extensions.p2p.structured.utils.UnicodeUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;
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
    public static CompoundEvent randomCompoundEvent(SemanticZone[] zones,
                                                    int nbQuadruples,
                                                    int nodeSize) {

        Builder<Quadruple> builder = ImmutableList.<Quadruple> builder();

        Node graphNode =
                randomGraphNode(
                        zones[RandomUtils.nextInt(zones.length)], nodeSize);

        for (int i = 0; i < nbQuadruples; i++) {
            builder.add(randomQuadruple(
                    zones[i % zones.length], graphNode, nodeSize));
        }

        return new CompoundEvent(builder.build());
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
        }

        String uri = "urn:" + UnicodeUtils.toString(result);

        if (sequenceNumber > 0) {
            uri = uri + sequenceNumber;
        }

        return Node.createURI(uri);
    }

    public static void main(String[] args) {
        P2PStructuredProperties.CAN_NB_DIMENSIONS.setValue((byte) 4);

        SemanticZone zone = new SemanticZone();

        for (int i = 0; i < 10; i++) {
            zone = (SemanticZone) zone.split((byte) 0).getSecond();
        }

        System.out.println(randomCompoundEvent(zone, 1, 10));
    }

}
