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
package fr.inria.eventcloud.adapters.rdf2go;

import java.io.InputStream;
import java.util.Iterator;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.sparql.core.Quad;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.tdb.store.DatasetGraphTDB;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.PutGetApi;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.Quadruple.SerializationFormat;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.responses.SparqlAskResponse;
import fr.inria.eventcloud.api.responses.SparqlConstructResponse;
import fr.inria.eventcloud.api.responses.SparqlDescribeResponse;
import fr.inria.eventcloud.api.responses.SparqlResponse;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
import fr.inria.eventcloud.api.wrappers.ModelWrapper;
import fr.inria.eventcloud.api.wrappers.ResultSetWrapper;
import fr.inria.eventcloud.proxies.Proxy;
import fr.inria.eventcloud.proxies.Proxy.QuadrupleAction;

/**
 * This class is assumed to play the role of a mock PutGetProxy in order to test
 * if the translation between RDF2Go and Jena objects work.
 * 
 * @author lpellegr
 */
public class MockPutGetProxy implements PutGetApi {

    private DatasetGraphTDB dataset;

    public MockPutGetProxy() {
        this.dataset = TDBFactory.createDatasetGraph();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(Quadruple quad) {
        this.dataset.add(toJenaQuad(quad));
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(Collection<Quadruple> quads) {
        for (Quadruple quad : quads) {
            this.add(quad);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(InputStream in, SerializationFormat format) {
        Proxy.read(in, format, new QuadrupleAction() {
            @Override
            public void performAction(Quadruple quad) {
                return;
            }
        });

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Quadruple quad) {
        return this.dataset.contains(toJenaQuad(quad));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete(Quadruple quad) {
        this.dataset.delete(toJenaQuad(quad));
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete(Collection<Quadruple> quads) {
        for (Quadruple quad : quads) {
            this.delete(quad);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Quadruple> delete(QuadruplePattern quadPattern) {
        Collection<Quadruple> result = this.find(quadPattern);
        this.dataset.deleteAny(
                quadPattern.getGraph(), quadPattern.getSubject(),
                quadPattern.getPredicate(), quadPattern.getObject());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Quadruple> find(QuadruplePattern quadPattern) {
        Iterator<Quad> it =
                this.dataset.findNG(
                        quadPattern.getGraph(), quadPattern.getSubject(),
                        quadPattern.getPredicate(), quadPattern.getObject());

        Collection<Quadruple> quads = new Collection<Quadruple>();
        while (it.hasNext()) {
            quads.add(toQuadruple(it.next()));
        }

        return quads;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SparqlResponse<?> executeSparqlQuery(String sparqlQuery) {
        // do nothing, we do not have to test it because nothing is delegated to
        // it from the RDF2Go adapter
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SparqlAskResponse executeSparqlAsk(String sparqlAskQuery) {
        Query query = QueryFactory.create(sparqlAskQuery);
        QueryExecution qe =
                QueryExecutionFactory.create(query, dataset.toDataset());

        return new SparqlAskResponse(0, 0, 0, 0, qe.execAsk());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SparqlConstructResponse executeSparqlConstruct(String sparqlConstructQuery) {
        Query query = QueryFactory.create(sparqlConstructQuery);
        QueryExecution qe =
                QueryExecutionFactory.create(query, dataset.toDataset());

        return new SparqlConstructResponse(0, 0, 0, 0, new ModelWrapper(
                qe.execConstruct()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SparqlDescribeResponse executeSparqlDescribe(String sparqlDescribeQuery) {
        throw new UnsupportedOperationException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SparqlSelectResponse executeSparqlSelect(String sparqlSelectQuery) {
        Query query = QueryFactory.create(sparqlSelectQuery);
        QueryExecution qe =
                QueryExecutionFactory.create(query, dataset.toDataset());

        return new SparqlSelectResponse(0, 0, 0, 0, new ResultSetWrapper(
                qe.execSelect()));
    }

    private static final Quad toJenaQuad(Quadruple quad) {
        return new Quad(
                quad.getGraph(), quad.getSubject(), quad.getPredicate(),
                quad.getObject());
    }

    private static final Quadruple toQuadruple(Quad quad) {
        return new Quadruple(
                quad.getGraph(), quad.getSubject(), quad.getPredicate(),
                quad.getObject());
    }

}
