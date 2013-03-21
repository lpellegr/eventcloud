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
package fr.inria.eventcloud.benchmarks.pubsub.suppliers;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.generators.NodeGenerator;
import fr.inria.eventcloud.api.generators.QuadrupleGenerator;
import fr.inria.eventcloud.api.generators.UuidGenerator;
import fr.inria.eventcloud.configuration.EventCloudProperties;

/**
 * This class provides a randomly generated {@link CompoundEvent} each time a
 * call to {@link CompoundEventSupplier#get()} is performed.
 * 
 * @author lpellegr
 */
public class CompoundEventSupplier implements Supplier<CompoundEvent> {

    private final int size;

    private final int rewritingLevel;

    /**
     * Constructs a compound event supplier that builds compound events randomly
     * by using the specified number of quadruples.
     * 
     * @param size
     *            the number of quadruples contained by each compound event
     *            which is generated.
     * @param rewritingLevel
     */
    public CompoundEventSupplier(int size, int rewritingLevel) {
        if (size < 1) {
            throw new IllegalArgumentException(
                    "The size of a compound event must be greater than one");
        }

        if (size <= rewritingLevel) {
            throw new IllegalArgumentException(
                    "The size of a compound event must be greater than the rewriting level");
        }

        this.size = size;
        this.rewritingLevel = rewritingLevel;
    }

    public CompoundEventSupplier(int size) {
        this(size, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompoundEvent get() {
        Builder<Quadruple> builder = new ImmutableList.Builder<Quadruple>();

        Node graph =
                Node.createURI(EventCloudProperties.EVENTCLOUD_ID_PREFIX.getValue()
                        + UuidGenerator.randomUuid());

        Node[] randomlyGenerateNodes = new Node[this.size];
        for (int i = 0; i < randomlyGenerateNodes.length; i++) {
            randomlyGenerateNodes[i] = NodeGenerator.randomUri();
        }

        for (int i = 1; i <= this.size; i++) {
            if (this.rewritingLevel == 0) {
                builder.add(QuadrupleGenerator.random(graph));
            } else {
                Node subject;

                if (i == 1) {
                    subject = NodeGenerator.randomUri();
                } else {
                    subject = randomlyGenerateNodes[i - 2];
                }

                builder.add(new Quadruple(
                        graph, subject, Node.createURI("urn:p" + i),
                        randomlyGenerateNodes[i - 1], false, false));
            }
        }

        return new CompoundEvent(builder.build());
    }

}
