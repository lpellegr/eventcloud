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
package fr.inria.eventcloud.api.wrappers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.utils.SparqlResultSerializer;

/**
 * BindingWrapper is used to make a Jena {@link Binding} serializable.
 * 
 * @author lpellegr
 */
public class BindingWrapper extends SparqlResultWrapper<Binding> implements
        Binding {

    private static final long serialVersionUID = 1L;

    /**
     * Creates a {@link BindingWrapper} from the specified {@link Binding}.
     * 
     * @param binding
     *            the binding to make serializable.
     */
    public BindingWrapper(Binding binding) {
        super(binding);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void internalWriteObject(ObjectOutputStream out)
            throws IOException {
        SparqlResultSerializer.serialize(
                out, super.object, EventCloudProperties.COMPRESSION.getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void internalReadObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        super.object =
                SparqlResultSerializer.deserializeBinding(
                        in, EventCloudProperties.COMPRESSION.getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Binding getParent() {
        return super.object.getParent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void add(Var var, Node node) {
        super.object.add(var, node);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Var> vars() {
        return super.object.vars();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Var var) {
        return super.object.contains(var);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Node get(Var var) {
        return super.object.get(var);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int size() {
        return super.object.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        return super.object.isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addAll(Binding key) {
        super.object.addAll(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("(");
        Iterator<Var> varsIt = this.vars();

        int i = 0;
        while (varsIt.hasNext()) {
            Var var = varsIt.next();
            buf.append(var);
            buf.append("=");
            buf.append(this.get(var));
            buf.append(", ");
            i++;
        }

        if (i > 0) {
            buf = buf.delete(buf.length() - 2, buf.length());
        }
        buf.append(")");
        
        return buf.toString();
    }

}
