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
package fr.inria.eventcloud.benchmarks.pubsub.messages;

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.response.ResponseProvider;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;

import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.benchmarks.pubsub.StorageTimes;
import fr.inria.eventcloud.benchmarks.pubsub.overlay.CustomSemanticOverlay;
import fr.inria.eventcloud.messages.request.can.StatefulQuadruplePatternRequest;
import fr.inria.eventcloud.overlay.can.SemanticElement;

public class RetrieveStorageEndTimesRequest extends
        StatefulQuadruplePatternRequest<StorageTimes> {

    private static final long serialVersionUID = 150L;

    public RetrieveStorageEndTimesRequest() {
        super(
                QuadruplePattern.ANY,
                new ResponseProvider<RetrieveStorageEndTimesResponse, Coordinate<SemanticElement>>() {
                    private static final long serialVersionUID = 150L;

                    @Override
                    public RetrieveStorageEndTimesResponse get() {
                        return new RetrieveStorageEndTimesResponse();
                    }
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public StorageTimes onPeerValidatingKeyConstraints(CanOverlay<SemanticElement> overlay,
                                                       AnycastRequest<SemanticElement> request,
                                                       QuadruplePattern quadruplePattern) {
        CustomSemanticOverlay customOverlay = (CustomSemanticOverlay) overlay;

        return new StorageTimes(
                customOverlay.publicationsStorageEndTime,
                customOverlay.subscriptionsStorageEndTime);

    }
}
