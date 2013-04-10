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
package fr.inria.eventcloud.messages.request.can;

import org.objectweb.proactive.extensions.p2p.structured.overlay.StructuredOverlay;
import org.objectweb.proactive.extensions.p2p.structured.utils.SerializedValue;

import fr.inria.eventcloud.api.CompoundEvent;
import fr.inria.eventcloud.api.Quadruple;
import fr.inria.eventcloud.delayers.ExtendedCompoundEvent;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;

/**
 * Publishes a full CompoundEvent into the network. The publish operation
 * consists in storing only one quadruple from the CE which is published, the
 * quadruple that is used to route the request. After that, an algorithm is
 * triggered by using the full CE to detect whether some subscriptions are
 * matched or not. This request is used by SBCE3.
 * 
 * @author lpellegr
 */
public class PublishCompoundEventRequest extends QuadrupleRequest {

    private static final long serialVersionUID = 140L;

    private SerializedValue<CompoundEvent> compoundEvent;

    private int indexQuadrupleUsedForIndexing;

    /**
     * Constructs a PublishCompoundEventRequest that is used to publish a
     * {@link CompoundEvent} as a whole according to the specified
     * {@code quadrupleUsedForIndexing}.
     * 
     * @param compoundEvent
     *            the compound event to use for the matching.
     * @param indexQuadrupleUsedForIndexing
     *            the index of the quadruple that is used for indexing the
     *            compound event.
     */
    public PublishCompoundEventRequest(CompoundEvent compoundEvent,
            int indexQuadrupleUsedForIndexing) {
        // TODO: avoid the copy of the quadruple used for indexing the request.
        // It is already embedded by the compoundEvent
        super(compoundEvent.get(indexQuadrupleUsedForIndexing), null);

        this.compoundEvent = SerializedValue.create(compoundEvent);
        this.indexQuadrupleUsedForIndexing = indexQuadrupleUsedForIndexing;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestinationReached(final StructuredOverlay overlay,
                                     final Quadruple quadruple) {

        ((SemanticCanOverlay) overlay).getPublishCompoundEventRequestDelayer()
                .receive(
                        new ExtendedCompoundEvent(
                                this.compoundEvent.getValue(),
                                this.indexQuadrupleUsedForIndexing));
    }

}
