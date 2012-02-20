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
package fr.inria.eventcloud.pubsub;

import java.io.Serializable;
import java.util.Iterator;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.proxies.SubscribeProxy;

/**
 * A solution is a value that is delivered to a user who has subscribed with a
 * query which is matched by the solution. It contains a collection of
 * sub-solutions that are received by a {@link SubscribeProxy} asynchronously. A
 * solution also knows what is the number of sub solutions expected and what is
 * the current number of sub solution that are received. A sub solution is
 * represented by using a {@link Binding}.
 * 
 * @author lpellegr
 */
public final class Solution implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int expectedSubSolutions;

    private int receivedSubSolutions;

    private Collection<Binding> bindings;

    /**
     * Constructs a new Solution from the specified number of sub-solutions
     * expected and the specified sub-solution.
     * 
     * @param expectedSubSolutions
     *            the expected number of sub-solutions.
     * @param binding
     *            the first sub-solution that is received.
     */
    public Solution(int expectedSubSolutions, Binding binding) {
        this.expectedSubSolutions = expectedSubSolutions;
        if (binding == null) {
            this.bindings = new Collection<Binding>();
        } else {
            this.bindings = new Collection<Binding>(binding);
        }
        this.receivedSubSolutions = 1;
    }

    /**
     * Merges the sub-solutions and returns the final solution. The result of
     * this operation is not put in a cache. Hence, you have to take care when
     * calling this method because the merge will be computed on each call.
     * 
     * @return the solution resulting from the merge of sub-solutions.
     */
    public Binding getSolution() {
        BindingMap solution = BindingFactory.create();
        for (Binding b : this.bindings) {
            Iterator<Var> varsIt = b.vars();
            while (varsIt.hasNext()) {
                Var var = varsIt.next();
                solution.add(var, b.get(var));
            }
        }

        return solution;
    }

    /**
     * Adds the specified sub-solution and increments the number of
     * sub-solutions that have been received.
     * 
     * @param binding
     *            the sub-solution to add.
     */
    public synchronized void addSubSolution(Binding binding) {
        if (binding != null) {
            this.bindings.add(binding);
        }

        // does not add null bindings but increment sub-solution counters to
        // manage signal notification listener
        this.receivedSubSolutions++;
    }

    /**
     * Returns the number of sub-solutions that are expected.
     * 
     * @return the number of sub-solutions that are expected.
     */
    public int getExpectedSubSolutions() {
        return this.expectedSubSolutions;
    }

    /**
     * Returns the number of sub-solutions that have been received.
     * 
     * @return the number of sub-solutions that have been received.
     */
    public int getReceivedSubSolutions() {
        return this.receivedSubSolutions;
    }

    /**
     * Returns the sub-solutions that have been received.
     * 
     * @return the sub-solutions that have been received.
     */
    public Collection<Binding> getSubSolutions() {
        return this.bindings;
    }

    /**
     * Indicates whether the solution is ready for delivery (i.e. if the number
     * of sub-solutions received is equals to the number of sub-solutions
     * expected).
     * 
     * @return {@code true} if the the number of sub-solutions received is
     *         equals to the number of sub-solutions expected, {@code false}
     *         otherwise.
     */
    public boolean isReady() {
        return this.receivedSubSolutions == this.expectedSubSolutions;
    }

}
