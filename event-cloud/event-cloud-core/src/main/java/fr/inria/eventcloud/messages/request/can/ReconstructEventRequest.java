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
package fr.inria.eventcloud.messages.request.can;

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.utils.SerializedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.datastore.SynchronizedJenaDatasetGraph;
import fr.inria.eventcloud.messages.response.can.ReconstructEventResponse;
import fr.inria.eventcloud.proxies.SubscribeProxyImpl;
import fr.inria.eventcloud.utils.LongLong;

/**
 * This request is used to retrieve all the {@link Quadruple}s that match the
 * {@link QuadruplePattern} that is specified when the object is constructed and
 * that have a {@link Quadruple#hashValue()} which is not equals to a value
 * contained by the list of {@code quadHashesReceived}.
 * 
 * @see SubscribeProxyImpl#reconstructEvent(fr.inria.eventcloud.pubsub.Subscription,
 *      com.hp.hpl.jena.sparql.engine.binding.Binding)
 * 
 * @author lpellegr
 */
public class ReconstructEventRequest extends QuadruplePatternRequest {

    private static final long serialVersionUID = 1L;

    private static final Logger log =
            LoggerFactory.getLogger(ReconstructEventRequest.class);

    // the hash values associated to the quadruples which have been already
    // received by the seeker
    private SerializedValue<Collection<LongLong>> hashValuesReceived;

    // the meta graph URI used to lookup the quadruples
    private SerializedValue<String> metaGraphValue;

    /**
     * Creates a ReconstructEventRequest by using the specified
     * {@code quadruplePattern} to route the request, and the given set of hash
     * values in order to return only the quadruples which have not been yet
     * received.
     * 
     * @param quadruplePattern
     *            the quadruple pattern used to route the request.
     * @param hashValuesReceived
     *            the hash values associated to the quadruples which have been
     *            already received.
     */
    public ReconstructEventRequest(QuadruplePattern quadruplePattern,
            Collection<LongLong> hashValuesReceived) {
        super(quadruplePattern);

        this.hashValuesReceived =
                new SerializedValue<Collection<LongLong>>(hashValuesReceived);

        this.metaGraphValue =
                new SerializedValue<String>(
                        quadruplePattern.createMetaGraphNode().getURI());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReconstructEventResponse createResponse(StructuredOverlay overlay) {
        return new ReconstructEventResponse(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Collection<Quadruple> onPeerValidatingKeyConstraints(CanOverlay overlay,
                                                                AnycastRequest request,
                                                                QuadruplePattern quadruplePattern) {
        log.info("ReconstructEventRequest.onPeerValidatingKeyConstraints() step1 mGV="
                + this.metaGraphValue.getValue());

        Collection<LongLong> hashValues = this.hashValuesReceived.getValue();
        log.info("ReconstructEventRequest.onPeerValidatingKeyConstraints() step2");
        Collection<Quadruple> result = new Collection<Quadruple>();
        log.info("ReconstructEventRequest.onPeerValidatingKeyConstraints() step3");
        StringBuilder query =
                new StringBuilder(
                        "SELECT ?g ?s ?p ?o WHERE {\n    GRAPH ?g {\n         ?s ?p ?o . \n  } }");
        log.info("ReconstructEventRequest.onPeerValidatingKeyConstraints() step4");
        // query.append(this.metaGraphValue.getValue());
        // query.append("\" ||  sameTerm(?g, <" + this.metaGraphValue.getValue()
        // + ">)) } }");
        log.info("ReconstructEventRequest.onPeerValidatingKeyConstraints() step5");
        synchronized (overlay.getDatastore()) {
            ResultSet queryResult =
                    ((SynchronizedJenaDatasetGraph) overlay.getDatastore()).executeSparqlSelect(query.toString());
            log.info("ReconstructEventRequest.onPeerValidatingKeyConstraints() step6");
            while (queryResult.hasNext()) {
                QuerySolution solution = queryResult.next();

                log.info("ReconstructEventRequest.onPeerValidatingKeyConstraints() step7 --> "
                        + solution.get("g").asNode().getURI()
                        + " equals "
                        + this.metaGraphValue.getValue()
                        + "?"
                        + solution.get("g").asNode().getURI().equals(
                                this.metaGraphValue.getValue()));
                if (solution.get("g").asNode().getURI().equals(this.metaGraphValue.getValue())) {

                    Quadruple quad =
                            new Quadruple(
                                    solution.get("g").asNode(), solution.get(
                                            "s").asNode(), solution.get("p")
                                            .asNode(), solution.get("o")
                                            .asNode());
                    log.info("ReconstructEventRequest.onPeerValidatingKeyConstraints() step8");
                    log.info(
                            "RECONSTRUCTINGREQUEST quad={}, pubtime={}, contained={}",
                            new Object[] {
                                    quad, quad.getPublicationTime(),
                                    hashValues.contains(quad.hashValue())});

                    if (quad.getPublicationTime() != -1
                            && !hashValues.contains(quad.hashValue())) {
                        log.info("ReconstructEventRequest.onPeerValidatingKeyConstraints() step9");
                        result.add(quad);
                    }
                }
            }
        }

        log.info("ReconstructEventRequest.onPeerValidatingKeyConstraints() step10");
        return result;
    }

}
