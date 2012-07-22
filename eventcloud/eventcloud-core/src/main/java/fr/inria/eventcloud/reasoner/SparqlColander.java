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
package fr.inria.eventcloud.reasoner;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.responses.SparqlResponse;
import fr.inria.eventcloud.api.wrappers.ResultSetWrapper;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.messages.response.can.QuadruplePatternResponse;

/**
 * SparqlColander is used to filter the results from a set of
 * {@link SparqlResponse} by using the original SPARQL request. Indeed, due to
 * the infrastructure, we can route only a sub-set of SPARQL queries. This mean
 * that the {@link SparqlResponse} does not contains the final result. To get
 * the final result we have to use a {@link SparqlColander} to remove some extra
 * values.
 * <p>
 * TODO provide a SemanticDatastorePool in order to have the possibility to
 * filter several queries in parallel. TODO 2 compute filter time and add it to
 * the response.
 * 
 * @author lpellegr
 */
public class SparqlColander implements Closeable {

    private TransactionalTdbDatastore datastore;

    public SparqlColander(TransactionalTdbDatastore datastore) {
        this.datastore = datastore;
        this.datastore.open();
    }

    /**
     * Filters a set of {@link Quadruple}s with the specified
     * {@code sparqlAskQuery}.
     * 
     * @param sparqlAskQuery
     *            the SPARQL query to use for filtering quadruples.
     * @param quadruplePatternResponses
     *            the quadruples to filter.
     * 
     * @return {@code true} if there are some values matching the
     *         {@code sparqlAskQuery}, {@code false} otherwise.
     */
    public synchronized boolean filterSparqlAsk(String sparqlAskQuery,
                                                List<QuadruplePatternResponse> quadruplePatternResponses) {
        this.cleanAndFill(this.datastore, quadruplePatternResponses);

        boolean result = false;

        TransactionalDatasetGraph txnGraph =
                this.datastore.begin(AccessMode.READ_ONLY);

        QueryExecution qExec = null;
        try {
            qExec =
                    QueryExecutionFactory.create(
                            sparqlAskQuery, txnGraph.toDataset());
            result = qExec.execAsk();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (qExec != null) {
                qExec.close();
            }
            txnGraph.end();
        }

        return result;
    }

    /**
     * Filters a set of {@link Quadruple}s with the specified
     * {@code sparqlConstructQuery}.
     * 
     * @param sparqlConstructQuery
     *            the SPARQL query to use for filtering quadruples.
     * @param quadruplePatternResponses
     *            the quadruples to filter.
     * 
     * @return {@code true} if there are some values matching the
     *         {@code sparqlConstructQuery}, {@code false} otherwise.
     */
    public synchronized Model filterSparqlConstruct(String sparqlConstructQuery,
                                                    List<QuadruplePatternResponse> quadruplePatternResponses) {
        this.cleanAndFill(this.datastore, quadruplePatternResponses);

        Model result = null;

        TransactionalDatasetGraph txnGraph =
                this.datastore.begin(AccessMode.READ_ONLY);

        QueryExecution qExec = null;
        try {
            qExec =
                    QueryExecutionFactory.create(
                            sparqlConstructQuery, txnGraph.toDataset());
            result = qExec.execConstruct();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (qExec != null) {
                qExec.close();
            }
            txnGraph.end();
        }

        return result;
    }

    /**
     * Filters a set of {@link Quadruple}s with the specified
     * {@code sparqlSelectQuery}.
     * 
     * @param sparqlSelectQuery
     *            the SPARQL query to use for filtering quadruples.
     * @param quadruplePatternResponses
     *            the quadruples to filter.
     * 
     * @return {@code true} if there are some values matching the
     *         {@code sparqlSelectQuery}, {@code false} otherwise.
     */
    public synchronized ResultSet filterSparqlSelect(String sparqlSelectQuery,
                                                     List<QuadruplePatternResponse> quadruplePatternResponses) {
        this.cleanAndFill(this.datastore, quadruplePatternResponses);

        ResultSet result = null;

        TransactionalDatasetGraph txnGraph =
                this.datastore.begin(AccessMode.READ_ONLY);

        QueryExecution qExec = null;
        try {
            qExec =
                    QueryExecutionFactory.create(
                            sparqlSelectQuery, txnGraph.toDataset());
            result = new ResultSetWrapper(qExec.execSelect());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (qExec != null) {
                qExec.close();
            }
            txnGraph.end();
        }

        return result;
    }

    private void cleanAndFill(TransactionalTdbDatastore datastore,
                              List<QuadruplePatternResponse> quadruplePatternResponses) {
        TransactionalDatasetGraph txnGraph = datastore.begin(AccessMode.WRITE);

        try {
            txnGraph.delete(Node.ANY, Node.ANY, Node.ANY, Node.ANY);
            for (QuadruplePatternResponse qpResponse : quadruplePatternResponses) {
                for (Quadruple quad : qpResponse.getResult()) {
                    // returns by default the graph value as it is contained by
                    // the Jena datastores
                    Node graph = quad.createMetaGraphNode();

                    txnGraph.add(
                            graph, quad.getSubject(), quad.getPredicate(),
                            quad.getObject());
                }
            }
            txnGraph.commit();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            txnGraph.end();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        this.datastore.close();
    }

}
