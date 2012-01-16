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

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;

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
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastoreMem;
import fr.inria.eventcloud.parsers.RdfParser;
import fr.inria.eventcloud.utils.Callback;

/**
 * This class is assumed to play the role of a mock PutGetProxy in order to test
 * if the translation between RDF2Go and Jena objects work.
 * 
 * @author lpellegr
 */
public class MockPutGetProxy implements PutGetApi {

    private TransactionalTdbDatastore datastore;

    public MockPutGetProxy() {
        this.datastore = new TransactionalTdbDatastoreMem();
        this.datastore.open();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(Quadruple quad) {
        TransactionalDatasetGraph txnGraph =
                this.datastore.begin(AccessMode.WRITE);
        txnGraph.add(quad);
        txnGraph.commit();
        txnGraph.close();

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(Collection<Quadruple> quads) {
        TransactionalDatasetGraph txnGraph =
                this.datastore.begin(AccessMode.WRITE);
        for (Quadruple quad : quads) {
            txnGraph.add(quad);
        }
        txnGraph.commit();
        txnGraph.close();

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean add(InputStream in, SerializationFormat format) {
        final Collection<Quadruple> quadruples = new Collection<Quadruple>();

        RdfParser.parse(in, format, new Callback<Quadruple>() {
            @Override
            public void execute(Quadruple quad) {
                quadruples.add(quad);
            }
        });

        this.add(quadruples);

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Quadruple quad) {
        boolean result = false;

        TransactionalDatasetGraph txnGraph =
                this.datastore.begin(AccessMode.READ_ONLY);
        result = txnGraph.contains(quad);
        txnGraph.commit();
        txnGraph.close();

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean delete(Quadruple quad) {
        TransactionalDatasetGraph txnGraph =
                this.datastore.begin(AccessMode.WRITE);
        txnGraph.delete(quad);
        txnGraph.commit();
        txnGraph.close();

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
        Collection<Quadruple> result;

        TransactionalDatasetGraph txnGraph =
                this.datastore.begin(AccessMode.READ_ONLY);
        result = Collection.from(txnGraph.find(quadPattern));
        txnGraph.close();

        txnGraph = this.datastore.begin(AccessMode.WRITE);
        txnGraph.delete(quadPattern);
        txnGraph.commit();
        txnGraph.close();

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Quadruple> find(QuadruplePattern quadPattern) {
        Collection<Quadruple> result;

        TransactionalDatasetGraph txnGraph =
                this.datastore.begin(AccessMode.READ_ONLY);
        result = Collection.from(txnGraph.find(quadPattern));
        txnGraph.close();

        return result;
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
        boolean result = false;

        TransactionalDatasetGraph txnGraph =
                this.datastore.begin(AccessMode.READ_ONLY);
        QueryExecution queryExecution =
                QueryExecutionFactory.create(
                        sparqlAskQuery, txnGraph.toDataset());
        result = queryExecution.execAsk();
        queryExecution.close();
        txnGraph.close();

        return new SparqlAskResponse(0, 0, 0, 0, result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SparqlConstructResponse executeSparqlConstruct(String sparqlConstructQuery) {
        ModelWrapper result = null;

        TransactionalDatasetGraph txnGraph =
                this.datastore.begin(AccessMode.READ_ONLY);
        QueryExecution queryExecution =
                QueryExecutionFactory.create(
                        sparqlConstructQuery, txnGraph.toDataset());
        result = new ModelWrapper(queryExecution.execConstruct());
        queryExecution.close();
        txnGraph.close();

        return new SparqlConstructResponse(0, 0, 0, 0, result);
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
        ResultSetWrapper result = null;

        TransactionalDatasetGraph txnGraph =
                this.datastore.begin(AccessMode.READ_ONLY);
        QueryExecution queryExecution =
                QueryExecutionFactory.create(
                        sparqlSelectQuery, txnGraph.toDataset());
        result = new ResultSetWrapper(queryExecution.execSelect());
        queryExecution.close();
        txnGraph.close();

        return new SparqlSelectResponse(0, 0, 0, 0, result);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long count(QuadruplePattern quadPattern) {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long count(String sparqlQuery) {
        return 0;
    }

}
