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
package fr.inria.eventcloud.messages.request.can;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.utils.SerializedValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.hash.HashCode;
import com.hp.hpl.jena.graph.Node;

import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.datastore.AccessMode;
import fr.inria.eventcloud.datastore.QuadrupleIterator;
import fr.inria.eventcloud.datastore.TransactionalDatasetGraph;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.overlay.can.SemanticElement;
import fr.inria.eventcloud.proxies.SubscribeProxyImpl;

/**
 * This request is used to retrieve all the {@link Quadruple}s that match the
 * {@link QuadruplePattern} that is specified when the object is constructed and
 * that have a {@link Quadruple#hashValue()} which is not equals to a value
 * contained by the list of {@code quadHashesReceived}.
 * 
 * @see SubscribeProxyImpl#reconstructCompoundEvent(fr.inria.eventcloud.pubsub.notifications.NotificationId,
 *      fr.inria.eventcloud.api.SubscriptionId, Node)
 * 
 * @author lpellegr
 */
public class ReconstructCompoundEventRequest extends QuadruplePatternRequest {

    private static final long serialVersionUID = 140L;

    private static final Logger log =
            LoggerFactory.getLogger(ReconstructCompoundEventRequest.class);

    // the hash values associated to the quadruples which have been already
    // received by the seeker
    private SerializedValue<Set<HashCode>> hashValuesReceived;

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
    public ReconstructCompoundEventRequest(QuadruplePattern quadruplePattern,
            Set<HashCode> hashValuesReceived) {
        super(quadruplePattern);

        this.hashValuesReceived = SerializedValue.create(hashValuesReceived);

        this.metaGraphValue =
                SerializedValue.create(quadruplePattern.createMetaGraphNode()
                        .getURI());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Quadruple> onPeerValidatingKeyConstraints(CanOverlay<SemanticElement> overlay,
                                                          AnycastRequest<SemanticElement> request,
                                                          QuadruplePattern quadruplePattern) {
        Set<HashCode> hashValues = this.hashValuesReceived.getValue();
        List<Quadruple> result = new ArrayList<Quadruple>();

        TransactionalDatasetGraph txnGraph =
                ((SemanticCanOverlay) overlay).getMiscDatastore().begin(
                        AccessMode.READ_ONLY);

        try {
            // all events which belong to a compound event share the same graph
            // value and the same meta information (e.g. publication time,
            // source)
            QuadrupleIterator iterator =
                    txnGraph.find(
                            Node.createURI(this.metaGraphValue.getValue()),
                            Node.ANY, Node.ANY, Node.ANY);

            while (iterator.hasNext()) {
                Quadruple quadruple = iterator.next();

                if (quadruple.getPublicationTime() != -1
                        && !hashValues.contains(quadruple.hashValue())) {
                    result.add(quadruple);
                }
            }

            log.info(
                    "Retrieved {} new event(s) on {} for meta graph node {}",
                    new Object[] {
                            result.size(), overlay,
                            this.metaGraphValue.getValue()});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            txnGraph.end();
        }

        return result;
    }

}
