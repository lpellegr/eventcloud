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
package fr.inria.eventcloud.overlay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanRequestResponseManager;
import org.objectweb.proactive.extensions.p2p.structured.utils.converters.ObjectToByteConverter;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.api.exceptions.MalformedSparqlQueryException;
import fr.inria.eventcloud.api.responses.SparqlAskResponse;
import fr.inria.eventcloud.api.responses.SparqlConstructResponse;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
import fr.inria.eventcloud.api.wrappers.ModelWrapper;
import fr.inria.eventcloud.api.wrappers.ResultSetWrapper;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
import fr.inria.eventcloud.messages.request.can.SparqlAtomicRequest;
import fr.inria.eventcloud.messages.response.can.QuadruplePatternResponse;
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

    private static final long serialVersionUID = 150L;

    private SparqlColander colander;

    private final ConcurrentHashMap<UUID, Future<? extends Object>> pendingResults;

    // this thread pool is used to execute an atomic query on the underlying
    // semantic datastore while the request continue to be forwarded to
    // others peers. The thread that is used is joined once a response is routed
    // back through this peer to retrieve the results
    public ExecutorService threadPool;

    public SemanticRequestResponseManager(
            TransactionalTdbDatastore colanderDatastore) {
        super();

        this.colander = new SparqlColander(colanderDatastore);

        this.pendingResults =
                new ConcurrentHashMap<UUID, Future<? extends Object>>(
                        16, 0.75f,
                        P2PStructuredProperties.MAO_SOFT_LIMIT_PEERS.getValue());

        this.threadPool =
                Executors.newFixedThreadPool(Runtime.getRuntime()
                        .availableProcessors());
    }

    /**
     * Dispatches a SPARQL ASK query over the overlay network.
     * 
     * @param sparqlAskQuery
     *            the SPARQL ASK query to execute.
     * @param overlay
     *            the overlay from where the request is sent.
     * 
     * @return a response corresponding to the type of the query dispatched.
     */
    public SparqlAskResponse executeSparqlAsk(String sparqlAskQuery,
                                              StructuredOverlay overlay)
            throws MalformedSparqlQueryException {
        List<SparqlAtomicRequest> parsingResult =
                SparqlReasoner.parse(sparqlAskQuery);

        List<QuadruplePatternResponse> responses =
                this.dispatch(parsingResult, overlay);

        boolean result =
                this.getColander().filterSparqlAsk(sparqlAskQuery, responses);

        long[] measurements = this.aggregateMeasurements(responses);

        return new SparqlAskResponse(
                measurements[0], measurements[1], measurements[2],
                measurements[3], result);
    }

    /**
     * Dispatches a SPARQL CONSTRUCT query over the overlay network.
     * 
     * @param sparqlConstructQuery
     *            the SPARQL CONSTRUCT query to execute.
     * @param overlay
     *            the overlay from where the request is sent.
     * 
     * @return a response corresponding to the type of the query dispatched.
     */
    public SparqlConstructResponse executeSparqlConstruct(String sparqlConstructQuery,
                                                          StructuredOverlay overlay)
            throws MalformedSparqlQueryException {
        List<SparqlAtomicRequest> parsingResult =
                SparqlReasoner.parse(sparqlConstructQuery);

        List<QuadruplePatternResponse> responses =
                this.dispatch(parsingResult, overlay);

        Model result =
                this.getColander().filterSparqlConstruct(
                        sparqlConstructQuery, responses);

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
     * @param overlay
     *            the overlay from where the request is sent.
     * 
     * @return a response corresponding to the type of the query dispatched.
     */
    public SparqlSelectResponse executeSparqlSelect(String sparqlSelectQuery,
                                                    StructuredOverlay overlay)
            throws MalformedSparqlQueryException {
        List<SparqlAtomicRequest> parsingResult =
                SparqlReasoner.parse(sparqlSelectQuery);

        List<QuadruplePatternResponse> responses =
                this.dispatch(parsingResult, overlay);

        ResultSet result =
                this.getColander().filterSparqlSelect(
                        sparqlSelectQuery, responses);

        long[] measurements = this.aggregateMeasurements(responses);

        SparqlSelectResponse sparqlSelectResponse =
                new SparqlSelectResponse(
                        measurements[0], measurements[1], measurements[2],
                        measurements[3], new ResultSetWrapper(result));

        if (P2PStructuredProperties.ENABLE_BENCHMARKS_INFORMATION.getValue()) {
            Map<String, Integer> mapSubQueryNbResults =
                    new HashMap<String, Integer>();
            long responsesSizeInBytes = 0;
            int nbIntermediateResults = 0;

            for (int i = 0; i < responses.size(); i++) {
                QuadruplePatternResponse response = responses.get(i);

                mapSubQueryNbResults.put(
                        response.getInitialRequestForThisResponse(),
                        response.getResult().size());

                nbIntermediateResults += response.getResult().size();

                for (int j = 0; j < response.getResult().size(); j++) {
                    try {
                        responsesSizeInBytes +=
                                ObjectToByteConverter.convert(response.getResult()
                                        .get(j)).length;
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }

            sparqlSelectResponse.setMapSubQueryNbResults(mapSubQueryNbResults);
            sparqlSelectResponse.setNbIntermediateResults(nbIntermediateResults);
            sparqlSelectResponse.setSizeOfIntermediateResultsInBytes(responsesSizeInBytes);
        }

        return sparqlSelectResponse;
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
    private long[] aggregateMeasurements(List<QuadruplePatternResponse> responses) {
        long inboundHopCount = 0;
        long outboundHopCount = 0;
        long latency = 0;
        long queryDatastoreTime = 0;

        for (QuadruplePatternResponse response : responses) {
            if (response.getLatency() > latency) {
                latency = response.getLatency();
            }

            inboundHopCount += response.getInboundHopCount();
            outboundHopCount += response.getOutboundHopCount();
            queryDatastoreTime += response.getActionTime();
        }

        return new long[] {
                inboundHopCount, outboundHopCount, latency, queryDatastoreTime};
    }

    private List<QuadruplePatternResponse> dispatch(final List<SparqlAtomicRequest> requests,
                                                    final StructuredOverlay overlay) {
        final List<QuadruplePatternResponse> replies =
                new ArrayList<QuadruplePatternResponse>(requests.size());

        // dispatch each request asynchronously
        for (SparqlAtomicRequest request : requests) {
            this.dispatchv(request, overlay);
        }

        // wait for responses
        for (SparqlAtomicRequest request : requests) {
            QuadruplePatternResponse resp =
                    (QuadruplePatternResponse) super.pullResponse(request.getId());

            if (P2PStructuredProperties.ENABLE_BENCHMARKS_INFORMATION.getValue()) {
                resp.setInitialRequestForThisResponse(request.getQuery());
            }
            replies.add(resp);
        }

        return replies;
    }

    public ConcurrentHashMap<UUID, Future<? extends Object>> getPendingResults() {
        return this.pendingResults;
    }

    public SparqlColander getColander() {
        return this.colander;
    }

    @Override
    public void clear() {
        super.clear();

        this.pendingResults.clear();

        TransactionalDatasetGraph txnGraph =
                this.colander.getDatastore().begin(AccessMode.WRITE);
        txnGraph.delete(QuadruplePattern.ANY);
        txnGraph.commit();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        super.close();

        try {
            this.colander.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            this.threadPool.shutdown();

            try {
                this.threadPool.awaitTermination(2, TimeUnit.MINUTES);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
