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
package fr.inria.eventcloud.overlay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.objectweb.proactive.extensions.p2p.structured.exceptions.DispatchException;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanRequestResponseManager;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.responses.SparqlAskResponse;
import fr.inria.eventcloud.api.responses.SparqlConstructResponse;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
import fr.inria.eventcloud.api.wrappers.ModelWrapper;
import fr.inria.eventcloud.api.wrappers.ResultSetWrapper;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.messages.request.can.SparqlAtomicRequest;
import fr.inria.eventcloud.messages.response.can.SparqlAtomicResponse;
import fr.inria.eventcloud.reasoner.SparqlColander;
import fr.inria.eventcloud.reasoner.SparqlReasoner;

/**
 * {@link SemanticRequestResponseManager} is an implementation of
 * {@link CanRequestResponseManager} for managing SPARQL queries over a CAN
 * network.
 * 
 * @author lpellegr
 */
public class SemanticRequestResponseManager extends CanRequestResponseManager {

    private static final long serialVersionUID = 1L;

    private SparqlReasoner reasoner;

    private SparqlColander colander;

    private final ConcurrentHashMap<UUID, Future<? extends Object>> pendingResults;

    private ExecutorService threadPool;

    public SemanticRequestResponseManager(TransactionalTdbDatastore datastore) {
        super();
        this.colander = new SparqlColander(datastore);
        this.pendingResults =
                new ConcurrentHashMap<UUID, Future<? extends Object>>();
        this.reasoner = new SparqlReasoner();
        // TODO choose the optimal size to use for the thread-pool
        this.threadPool = Executors.newFixedThreadPool(30);
    }

    /**
     * Dispatches a SPARQL Ask query over the overlay network.
     * 
     * @param sparqlAskQuery
     *            the SPARQL ASK query to execute.
     * 
     * @return a response corresponding to the type of the query dispatched.
     */
    public SparqlAskResponse executeSparqlAsk(String sparqlAskQuery) {
        List<SparqlAtomicResponse> responses =
                this.dispatch(this.getReasoner().parseSparql(sparqlAskQuery));

        boolean result =
                this.getColander().filterSparqlAsk(
                        sparqlAskQuery, extractQuadruples(responses));

        long[] measurements = this.aggregateMeasurements(responses);

        return new SparqlAskResponse(
                measurements[0], measurements[1], measurements[2],
                measurements[3], result);
    }

    /**
     * Dispatches a SPARQL Construct query over the overlay network.
     * 
     * @param sparqlConstructQuery
     *            the SPARQL CONSTRUCT query to execute.
     * 
     * @return a response corresponding to the type of the query dispatched.
     */
    public SparqlConstructResponse executeSparqlConstruct(String sparqlConstructQuery) {
        List<SparqlAtomicResponse> responses =
                this.dispatch(this.getReasoner().parseSparql(
                        sparqlConstructQuery));

        Model result =
                this.getColander().filterSparqlConstruct(
                        sparqlConstructQuery, extractQuadruples(responses));

        long[] measurements = this.aggregateMeasurements(responses);

        return new SparqlConstructResponse(
                measurements[0], measurements[1], measurements[2],
                measurements[3], new ModelWrapper(result));
    }

    /**
     * Dispatches a SPARQL SELECT query over the overlay network.
     * 
     * @param sparqlSelectQuery
     *            the SPARQL SELECT query to execute.
     * @return a response corresponding to the type of the query dispatched.
     */
    public SparqlSelectResponse executeSparqlSelect(String sparqlSelectQuery) {
        List<SparqlAtomicResponse> responses =
                this.dispatch(this.getReasoner().parseSparql(sparqlSelectQuery));

        ResultSet result =
                this.getColander().filterSparqlSelect(
                        sparqlSelectQuery, extractQuadruples(responses));

        long[] measurements = this.aggregateMeasurements(responses);

        return new SparqlSelectResponse(
                measurements[0], measurements[1], measurements[2],
                measurements[3], new ResultSetWrapper(result));
    }

    private static List<Quadruple> extractQuadruples(List<SparqlAtomicResponse> responses) {
        List<Quadruple> quadruples = new ArrayList<Quadruple>();
        for (SparqlAtomicResponse response : responses) {
            quadruples.addAll(response.getResult());
        }

        return quadruples;
    }

    /**
     * Returns the measurements after having merged the intermediate results.
     * 
     * @param responses
     *            the list of responses containing the measurements.
     * 
     * @return the measurements after having merged the intermediate results.
     *         The measurements which are returned are respectively the
     *         {@code inboundHopCount}, the {@code outboundHopCount}, the
     *         {@code latency} and the {@code queryDatastoreTime}.
     */
    private long[] aggregateMeasurements(List<SparqlAtomicResponse> responses) {
        long outboundHopCount = 0;
        long latency = 0;
        long queryDatastoreTime = 0;

        for (SparqlAtomicResponse response : responses) {
            if (response.getLatency() > latency) {
                latency = response.getLatency();
            }

            outboundHopCount += response.getOutboundHopCount();
            queryDatastoreTime += response.getStateActionTime();
        }

        // inboundHopCount = outboundHopCount
        return new long[] {
                outboundHopCount, outboundHopCount, latency, queryDatastoreTime};
    }

    public List<SparqlAtomicResponse> dispatch(List<SparqlAtomicRequest> requests) {
        final List<SparqlAtomicResponse> replies =
                Collections.synchronizedList(new ArrayList<SparqlAtomicResponse>(
                        requests.size()));
        final CountDownLatch doneSignal = new CountDownLatch(requests.size());

        for (final SparqlAtomicRequest request : requests) {
            this.getThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        replies.add((SparqlAtomicResponse) SemanticRequestResponseManager.super.dispatch(request));
                    } catch (DispatchException e) {
                        e.printStackTrace();
                    } finally {
                        doneSignal.countDown();
                    }
                }
            });
        }

        try {
            doneSignal.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return replies;
    }

    public ConcurrentHashMap<UUID, Future<? extends Object>> getPendingResults() {
        return this.pendingResults;
    }

    public ExecutorService getThreadPool() {
        return this.threadPool;
    }

    private synchronized SparqlReasoner getReasoner() {
        return this.reasoner;
    }

    public SparqlColander getColander() {
        return this.colander;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws IOException {
        this.colander.close();
    }

}
