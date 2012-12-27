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
package fr.inria.eventcloud.pubsub.solutions;

import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;

import fr.inria.eventcloud.api.listeners.BindingNotificationListener;
import fr.inria.eventcloud.api.listeners.SignalNotificationListener;
import fr.inria.eventcloud.pubsub.PublishSubscribeUtils;

/**
 * A solution that embeds a {@link Binding} as the chunks associated to the
 * solution. It is useful for solutions matching a subscription registered with
 * a {@link SignalNotificationListener} or {@link BindingNotificationListener}.
 * 
 * @author lpellegr
 */
public class BindingSolution extends Solution<Binding> {

    private final int nbSubSolutionExpected;

    private int nbSubSolutionsReceived;

    /**
     * Constructs a new BindingSolution with the specified number of expected
     * sub solutions and the given sub solutions.
     * 
     * @param nbSubSolutionsExpected
     *            the expected number of sub-solutions.
     * @param chunk
     *            the first sub-solution that is received.
     */
    public BindingSolution(int nbSubSolutionsExpected, Binding chunk) {
        super(new PublishSubscribeUtils.BindingMap());

        this.add(chunk);

        this.nbSubSolutionExpected = nbSubSolutionsExpected;
    }

    /**
     * Merges a sub solution with the current ones.
     */
    @Override
    public synchronized void merge(Binding binding) {
        this.add(binding);
    }

    private void add(Binding binding) {
        ((BindingMap) super.chunks).addAll(binding);

        // does not add null bindings but increment sub-solution counters to
        // manage signal notification listener
        this.nbSubSolutionsReceived++;
    }

    /**
     * Returns the number of sub solutions that are expected.
     * 
     * @return the number of sub solutions that are expected.
     */
    public int getExpectedSubSolutions() {
        return this.nbSubSolutionExpected;
    }

    /**
     * Returns the number of sub solutions that have been received.
     * 
     * @return the number of sub solutions that have been received.
     */
    public int getReceivedSubSolutions() {
        return this.nbSubSolutionsReceived;
    }

    /**
     * Indicates whether the solution is ready for delivery (i.e. if the number
     * of sub solutions received is equals to the number of sub solutions
     * expected).
     * 
     * @return {@code true} if the the number of sub solutions received is
     *         equals to the number of sub solutions expected, {@code false}
     *         otherwise.
     */
    @Override
    public boolean isReady() {
        return this.nbSubSolutionsReceived == this.nbSubSolutionExpected;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Binding getChunks() {
        return super.chunks;
    }

}
