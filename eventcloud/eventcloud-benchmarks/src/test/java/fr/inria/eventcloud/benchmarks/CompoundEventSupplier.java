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
package fr.inria.eventcloud.benchmarks;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Supplier;
import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;
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

    /**
     * Constructs a compound event supplier that builds compound events composed
     * of the specified number of quadruples.
     * 
     * @param size
     *            the number of quadruples contained by each compound event
     *            which is generated.
     */
    public CompoundEventSupplier(int size) {
        this.size = size;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CompoundEvent get() {
        List<Quadruple> quadruples = new ArrayList<Quadruple>();
        Node graph =
                Node.createURI(EventCloudProperties.EVENT_CLOUD_ID_PREFIX.getValue()
                        + UuidGenerator.randomUuid());

        for (int i = 0; i < this.size; i++) {
            quadruples.add(QuadrupleGenerator.random(graph));
        }

        return new CompoundEvent(quadruples);
    }

}
