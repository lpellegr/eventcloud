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
package fr.inria.eventcloud.load_balancing.gossip;

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.MulticastRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.OptimalBroadcastRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.router.can.OptimalBroadcastRequestRouter;
import org.objectweb.proactive.extensions.p2p.structured.utils.SerializedValue;

import fr.inria.eventcloud.load_balancing.LoadReport;
import fr.inria.eventcloud.messages.request.StatelessQuadruplePatternRequest;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.overlay.can.SemanticCoordinate;

/**
 * Basic flooding strategy. The load is spread step by step to all peers by
 * using the optimal broadcast algorithm.
 * 
 * @author lpellegr
 */
public class FloodingStrategy implements GossipStrategy<LoadReport> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void push(SemanticCanOverlay overlay, LoadReport loadReport) {
        overlay.getStub().route(new FloodingLoadRequest(loadReport));
    }

    public static class FloodingLoadRequest extends
            OptimalBroadcastRequest<SemanticCoordinate> {

        private static final long serialVersionUID = 160L;

        private final SerializedValue<LoadReport> loadReport;

        public FloodingLoadRequest(LoadReport loadReport) {
            super(null);

            this.loadReport = SerializedValue.create(loadReport);
        }

        public void onPeerValidatingKeyConstraints(CanOverlay<SemanticCoordinate> overlay) {
            ((SemanticCanOverlay) overlay).getLoadBalancingManager().save(
                    this.loadReport.getValue());
        }

        @Override
        public OptimalBroadcastRequestRouter<StatelessQuadruplePatternRequest, SemanticCoordinate> getRouter() {
            return new OptimalBroadcastRequestRouter<StatelessQuadruplePatternRequest, SemanticCoordinate>() {
                @Override
                public void onPeerValidatingKeyConstraints(final CanOverlay<SemanticCoordinate> overlay,
                                                           final MulticastRequest<SemanticCoordinate> request) {
                    FloodingLoadRequest.this.onPeerValidatingKeyConstraints(overlay);
                }
            };
        }

    }

}
