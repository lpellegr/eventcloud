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

import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Collection;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.datastore.TransactionalTdbDatastore;
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
    public Collection<Quadruple> onPeerValidatingKeyConstraints(CanOverlay overlay,
                                                                             AnycastRequest request,
                                                                             QuadruplePattern quadruplePattern) {
        Collection<LongLong> hashValues = this.hashValuesReceived.getValue();
        Collection<Quadruple> result = new Collection<Quadruple>();

        TransactionalDatasetGraph txnGraph =
                ((TransactionalTdbDatastore) overlay.getDatastore()).begin(AccessMode.READ_ONLY);
        for (Quadruple quadruple : txnGraph.find(
                Node.createURI(this.metaGraphValue.getValue()), Node.ANY,
                Node.ANY, Node.ANY)) {
            if (quadruple.getPublicationTime() != -1
                    && !hashValues.contains(quadruple.hashValue())) {
                result.add(quadruple);
            }
        }
        txnGraph.close();

        return result;
    }

}
