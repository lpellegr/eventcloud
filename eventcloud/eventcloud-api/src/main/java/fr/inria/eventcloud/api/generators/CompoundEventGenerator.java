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
package fr.inria.eventcloud.api.generators;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;

/**
 * Utility class that defines some convenient methods to create arbitrary
 * {@link CompoundEvent}s.
 * 
 * @author lpellegr
 */
public class CompoundEventGenerator {

    /**
     * Creates a random compound event composed of the specified number of
     * quadruples.
     * 
     * @param nbQuadruples
     *            the number of quadruples to generate randomly.
     * 
     * @return a random compound event composed of the specified number of
     *         quadruples.
     */
    public static CompoundEvent random(int nbQuadruples) {
        return random(null, nbQuadruples);
    }

    /**
     * Creates a random node composed of the specified number of quadruples. It
     * uses the specified {@code streamUrl} for the graph value shared by all
     * the quadruples.
     * 
     * @param streamUrl
     *            the streamUrl to set.
     * @param nbQuadruples
     *            the number of quadruples to generate randomly.
     * 
     * @return a random compound event composed of the specified number of
     *         quadruples.
     */
    public static CompoundEvent random(String streamUrl, int nbQuadruples) {
        return random(streamUrl, nbQuadruples, Generator.DEFAULT_LENGTH);
    }

    /**
     * Creates a random node composed of the specified number of quadruples. It
     * uses the specified {@code streamUrl} for the graph value shared by all
     * the quadruples.
     * 
     * @param streamUrl
     *            the streamUrl to set.
     * @param nbQuadruples
     *            the number of quadruples to generate randomly.
     * @param nodeSize
     *            the number of characters used for each node generated.
     * 
     * @return a random compound event composed of the specified number of
     *         quadruples.
     */
    public static CompoundEvent random(String streamUrl, int nbQuadruples,
                                       int nodeSize) {
        Node graphNode = NodeGenerator.randomUri();

        if (streamUrl != null) {
            nbQuadruples = nbQuadruples - 1;
        }

        Builder<Quadruple> builder = new ImmutableList.Builder<Quadruple>();
        for (int i = 0; i < nbQuadruples; i++) {
            builder.add(QuadrupleGenerator.random(graphNode, nodeSize));
        }

        if (streamUrl != null) {
            builder.add(new Quadruple(
                    graphNode,
                    Node.createURI(graphNode.getURI() + "#event"),
                    Node.createURI("http://events.event-processing.org/types/stream"),
                    Node.createURI(streamUrl + "#stream"), false, true));
        }

        return new CompoundEvent(builder.build());
    }

}
