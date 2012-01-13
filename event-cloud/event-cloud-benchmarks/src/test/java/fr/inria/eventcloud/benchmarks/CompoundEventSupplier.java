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
package fr.inria.eventcloud.benchmarks;

import java.util.UUID;

import org.objectweb.proactive.core.util.ProActiveRandom;

import com.google.common.base.Supplier;
import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.generators.QuadrupleGenerator;
import fr.inria.eventcloud.configuration.EventCloudProperties;

/**
 * This class provides a randomly generated {@link CompoundEvent} each time a call to
 * {@link CompoundEventSupplier#get()} is performed.
 * 
 * @author lpellegr
 */
public class CompoundEventSupplier implements Supplier<CompoundEvent> {

    private final int minSize;

    private final int maxSize;

    /**
     * Constructs an Event supplier that builds events composed of the specified
     * number of quadruples.
     * 
     * @param size
     *            the number of quadruples contained by each event which are
     *            generated.
     */
    public CompoundEventSupplier(int size) {
        this(size, size);
    }

    /**
     * Constructs an Event supplier that builds events composed of a number
     * quadruples which is between {@code minSize} (inclusive) and
     * {@code maxSize} (exclusive).
     * 
     * @param minSize
     *            the minimum number of quadruples contained by an event which
     *            is generated.
     * 
     * @param maxSize
     *            the maximum number of quadruples contained by an event which
     *            is generated.
     */
    public CompoundEventSupplier(int minSize, int maxSize) {
        this.minSize = minSize;
        this.maxSize = maxSize;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompoundEvent get() {
        Collection<Quadruple> quadruples = new Collection<Quadruple>();
        Node graph =
                Node.createURI(EventCloudProperties.EVENT_CLOUD_ID_PREFIX.getValue()
                        + UUID.randomUUID().toString());

        for (int i = 0; i < this.minSize
                + ProActiveRandom.nextInt(this.maxSize - this.minSize); i++) {
            quadruples.add(QuadrupleGenerator.create(graph));
        }

        return new CompoundEvent(quadruples);
    }

}
