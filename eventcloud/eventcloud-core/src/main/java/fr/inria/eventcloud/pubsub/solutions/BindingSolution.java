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
package fr.inria.eventcloud.pubsub.solutions;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingMap;

import fr.inria.eventcloud.api.listeners.BindingNotificationListener;
import fr.inria.eventcloud.api.listeners.SignalNotificationListener;
import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.utils.SparqlResultSerializer;

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
        super(new SimpleBindingMap());

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

    private static class SimpleBindingMap implements BindingMap, Serializable {

        private static final long serialVersionUID = 130L;

        private Map<Var, Node> content = new HashMap<Var, Node>();

        public SimpleBindingMap() {

        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Iterator<Var> vars() {
            return this.content.keySet().iterator();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean contains(Var var) {
            return this.content.containsKey(var);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Node get(Var var) {
            return this.content.get(var);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int size() {
            return this.content.size();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isEmpty() {
            return this.content.isEmpty();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void add(Var var, Node node) {
            this.content.put(var, node);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void addAll(Binding binding) {
            Iterator<Var> varsIt = binding.vars();

            while (varsIt.hasNext()) {
                Var var = varsIt.next();
                this.content.put(var, binding.get(var));
            }
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();

            SparqlResultSerializer.serialize(
                    out, this, EventCloudProperties.COMPRESSION.getValue());
        }

        private void readObject(ObjectInputStream in) throws IOException,
                ClassNotFoundException {
            in.defaultReadObject();

            Binding binding =
                    SparqlResultSerializer.deserializeBinding(
                            in, EventCloudProperties.COMPRESSION.getValue());

            this.content = new HashMap<Var, Node>();
            Iterator<Var> it = binding.vars();

            while (it.hasNext()) {
                Var var = it.next();
                this.content.put(var, binding.get(var));
            }
        }

    }

}
