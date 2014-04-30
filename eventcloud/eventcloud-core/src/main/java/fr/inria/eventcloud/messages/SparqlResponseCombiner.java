/**
 * Copyright (c) 2011-2014 INRIA.
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
package fr.inria.eventcloud.messages;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.proactive.extensions.p2p.structured.configuration.P2PStructuredProperties;
import org.objectweb.proactive.extensions.p2p.structured.messages.RequestResponseManager;
import org.objectweb.proactive.extensions.p2p.structured.messages.Response;
import org.objectweb.proactive.extensions.p2p.structured.messages.ResponseCombiner;
import org.objectweb.proactive.extensions.p2p.structured.utils.converters.ObjectToByteConverter;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;

import fr.inria.eventcloud.api.responses.SparqlAskResponse;
import fr.inria.eventcloud.api.responses.SparqlConstructResponse;
import fr.inria.eventcloud.api.responses.SparqlQueryStatistics;
import fr.inria.eventcloud.api.responses.SparqlResponse;
import fr.inria.eventcloud.api.responses.SparqlSelectResponse;
import fr.inria.eventcloud.api.wrappers.ModelWrapper;
import fr.inria.eventcloud.api.wrappers.ResultSetWrapper;
import fr.inria.eventcloud.messages.response.QuadruplePatternResponse;
import fr.inria.eventcloud.overlay.SemanticRequestResponseManager;

/**
 * Defines how to combine responses received for a SPARQL query.
 * 
 * @author lpellegr
 */
public class SparqlResponseCombiner implements ResponseCombiner {

    private static final long serialVersionUID = 160L;

    /**
     * {@inheritDoc}
     */
    @Override
    public Serializable combine(List<? extends Response<?>> responses,
                                RequestResponseManager messageManager,
                                Serializable context) {

        SemanticRequestResponseManager semanticMessageManager =
                (SemanticRequestResponseManager) messageManager;

        SparqlMessageContext semanticContext = (SparqlMessageContext) context;

        @SuppressWarnings("unchecked")
        List<QuadruplePatternResponse> semanticResponses =
                (List<QuadruplePatternResponse>) responses;

        long[] measurements = this.aggregateMeasurements(semanticResponses);

        SparqlResponse<?> result = null;

        switch (semanticContext.getQueryType()) {
            case ASK:
                long beginFiltering = System.currentTimeMillis();
                Boolean answer =
                        semanticMessageManager.getColander().filterSparqlAsk(
                                semanticContext.getQuery(), semanticResponses);
                long endFiltering = System.currentTimeMillis();

                SparqlQueryStatistics stats =
                        new SparqlQueryStatistics(
                                semanticResponses.size(), measurements[0],
                                measurements[1], measurements[2],
                                measurements[3], endFiltering - beginFiltering,
                                endFiltering - measurements[4]);

                result = new SparqlAskResponse(stats, answer);
                break;
            case CONSTRUCT:
                beginFiltering = System.currentTimeMillis();
                Model model =
                        semanticMessageManager.getColander()
                                .filterSparqlConstruct(
                                        semanticContext.getQuery(),
                                        semanticResponses);
                endFiltering = System.currentTimeMillis();

                stats =
                        new SparqlQueryStatistics(
                                semanticResponses.size(), measurements[0],
                                measurements[1], measurements[2],
                                measurements[3], endFiltering - beginFiltering,
                                endFiltering - measurements[4]);

                result =
                        new SparqlConstructResponse(stats, new ModelWrapper(
                                model));
                break;

            case SELECT:
                beginFiltering = System.currentTimeMillis();
                ResultSet selectResultSet =
                        semanticMessageManager.getColander()
                                .filterSparqlSelect(
                                        semanticContext.getQuery(),
                                        semanticResponses);
                endFiltering = System.currentTimeMillis();

                stats =
                        new SparqlQueryStatistics(
                                semanticResponses.size(), measurements[0],
                                measurements[1], measurements[2],
                                measurements[3], endFiltering - beginFiltering,
                                endFiltering - measurements[4]);

                SparqlSelectResponse sparqlSelectResponse =
                        new SparqlSelectResponse(stats, new ResultSetWrapper(
                                selectResultSet));

                if (P2PStructuredProperties.ENABLE_BENCHMARKS_INFORMATION.getValue()) {
                    Map<String, Integer> mapSubQueryNbResults =
                            new HashMap<String, Integer>();
                    long responsesSizeInBytes = 0;
                    int nbIntermediateResults = 0;

                    for (int i = 0; i < responses.size(); i++) {
                        QuadruplePatternResponse response =
                                semanticResponses.get(i);

                        mapSubQueryNbResults.put(
                                semanticContext.getQuery(),
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

                result = sparqlSelectResponse;

                break;
            default:
                throw new UnsupportedOperationException(
                        "Query type not allowed: "
                                + semanticContext.getQueryType());
        }

        return result;
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
        long dispatchTimestamp = 0;

        for (QuadruplePatternResponse response : responses) {
            if (response.getLatency() > latency) {
                latency = response.getLatency();
            }

            inboundHopCount += response.getInboundHopCount();
            outboundHopCount += response.getOutboundHopCount();
            queryDatastoreTime += response.getActionTime();

            if (dispatchTimestamp == 0
                    || response.getDispatchTimestamp() < dispatchTimestamp) {
                dispatchTimestamp = response.getDispatchTimestamp();
            }
        }

        return new long[] {
                inboundHopCount, outboundHopCount, latency, queryDatastoreTime,
                dispatchTimestamp};
    }

    public static SparqlResponseCombiner getInstance() {
        return Singleton.instance;
    }

    private static class Singleton {

        private static final SparqlResponseCombiner instance =
                new SparqlResponseCombiner();

    }

}
