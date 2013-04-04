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

import java.util.Random;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
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

    public static CompoundEvent randomCompoundEvent(SemanticZone zone,
                                                    int nbQuadruples,
                                                    int nodeSize) {
        Builder<Quadruple> builder = ImmutableList.<Quadruple> builder();

        for (int i = 0; i < nbQuadruples; i++) {
            builder.add(randomQuadruple(zone, nodeSize));
        }

        return new CompoundEvent(builder.build());
    }

    public static Quadruple randomQuadruple(SemanticZone zone, int nodeSize) {
        Node[] nodes =
                new Node[P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue()];

        for (byte i = 0; i < P2PStructuredProperties.CAN_NB_DIMENSIONS.getValue(); i++) {
            nodes[i] =
                    randomNode(
                            zone.getLowerBound(i), zone.getUpperBound(i),
                            nodeSize);
        }

        return new Quadruple(
                nodes[0], nodes[1], nodes[2], nodes[3], false, false);
    }

    private static Node randomNode(SemanticElement lowerBoundElement,
                                   SemanticElement upperBoundElement,
                                   int nodeSize) {

        int[] lbCodePoints =
                UnicodeUtils.toCodePointArray(lowerBoundElement.getValue());
        int[] upCodePoints =
                UnicodeUtils.toCodePointArray(upperBoundElement.getValue());

        int[] result = new int[nodeSize];

        Random rand = new Random();

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
                result[i] = lowerBound + rand.nextInt(diff);
            }
        }

        return Node.createURI("urn:" + UnicodeUtils.toString(result));
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
