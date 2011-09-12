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

    private SerializedValue<Collection<LongLong>> quadHashesReceived;

    private SerializedValue<QuadruplePattern> timestampedQuadruplePattern;

    public ReconstructEventRequest(QuadruplePattern quadruplePattern,
            Collection<LongLong> quadHashesReceived) {
        super(QuadruplePattern.removeTimestampFromGraphValue(quadruplePattern));

        this.timestampedQuadruplePattern =
                new SerializedValue<QuadruplePattern>(quadruplePattern);
        this.quadHashesReceived =
                new SerializedValue<Collection<LongLong>>(quadHashesReceived);
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
        Collection<Quadruple> result = new Collection<Quadruple>();

        // TODO: use a quadruple pattern with the graph value fixed to the
        // eventId
        for (Quadruple q : ((SynchronizedJenaDatasetGraph) overlay.getDatastore()).find(QuadruplePattern.ANY)) {
            if (q.getGraph().equals(
                    this.timestampedQuadruplePattern.getValue().getGraph())
                    && !quadHashesReceived.getValue().contains(q.hashValue())) {
                result.add(q);
            }
        }

        return result;
    }

}
