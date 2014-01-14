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

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.MulticastRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.points.Point;
import org.objectweb.proactive.extensions.p2p.structured.providers.ResponseProvider;

import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.benchmarks.pubsub.StorageTimes;
import fr.inria.eventcloud.benchmarks.pubsub.overlay.CustomSemanticOverlay;
import fr.inria.eventcloud.messages.request.StatefulQuadruplePatternRequest;
import fr.inria.eventcloud.overlay.can.SemanticCoordinate;

public class RetrieveStorageEndTimesRequest extends
        StatefulQuadruplePatternRequest<StorageTimes> {

    private static final long serialVersionUID = 160L;

    public RetrieveStorageEndTimesRequest() {
        super(
                QuadruplePattern.ANY,
                new ResponseProvider<RetrieveStorageEndTimesResponse, Point<SemanticCoordinate>>() {
                    private static final long serialVersionUID = 160L;

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
    public StorageTimes onPeerValidatingKeyConstraints(CanOverlay<SemanticCoordinate> overlay,
                                                       MulticastRequest<SemanticCoordinate> request,
                                                       QuadruplePattern quadruplePattern) {
        CustomSemanticOverlay customOverlay = (CustomSemanticOverlay) overlay;

        return new StorageTimes(
                customOverlay.publicationsStorageEndTime,
                customOverlay.subscriptionsStorageEndTime);

    }

}
