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
package fr.inria.eventcloud.api.wrappers;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.resultset.ResultSetMem;

import fr.inria.eventcloud.configuration.EventCloudProperties;
import fr.inria.eventcloud.utils.SparqlResultSerializer;

/**
 * ResultSetWrapper is used to make a Jena {@link ResultSet} serializable.
 * 
 * @author lpellegr
 */
public class ResultSetWrapper extends SparqlResultWrapper<ResultSet> implements
        ResultSet {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a wrapper for the specified {@code resultSet}.
     * 
     * @param resultSet
     *            the result set to wrap.
     */
    public ResultSetWrapper(ResultSet resultSet) {
        // puts the content of the resultset into memory if the specified
        // resultset is not an in-memory resultset.
        super(new ResultSetMem(resultSet));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void internalWriteObject(ObjectOutputStream out)
            throws IOException {
        SparqlResultSerializer.serialize(
                out, this, EventCloudProperties.COMPRESSION.getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void internalReadObject(ObjectInputStream in) throws IOException {
        this.object =
                SparqlResultSerializer.deserializeResultSet(
                        in, EventCloudProperties.COMPRESSION.getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void remove() {
        super.object.remove();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        return super.object.hasNext();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QuerySolution next() {
        return super.object.next();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QuerySolution nextSolution() {
        return super.object.nextSolution();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Binding nextBinding() {
        return super.object.nextBinding();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRowNumber() {
        return super.object.getRowNumber();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getResultVars() {
        return super.object.getResultVars();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Model getResourceModel() {
        return super.object.getResourceModel();
    }

}
