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

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.OptimalBroadcastRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.router.can.OptimalBroadcastRequestRouter;
import org.objectweb.proactive.extensions.p2p.structured.utils.SerializedValue;

import fr.inria.eventcloud.load_balancing.LoadReport;
import fr.inria.eventcloud.messages.request.can.StatelessQuadruplePatternRequest;
import fr.inria.eventcloud.overlay.SemanticCanOverlay;
import fr.inria.eventcloud.overlay.can.SemanticElement;

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
        overlay.getStub().sendv(new FloodingLoadRequest(loadReport));
    }

    public static class FloodingLoadRequest extends
            OptimalBroadcastRequest<SemanticElement> {

        private static final long serialVersionUID = 150L;

        private final SerializedValue<LoadReport> loadReport;

        public FloodingLoadRequest(LoadReport loadReport) {
            super(null);

            this.loadReport = SerializedValue.create(loadReport);
        }

        public void onPeerValidatingKeyConstraints(CanOverlay<SemanticElement> overlay) {
            ((SemanticCanOverlay) overlay).getLoadBalancingManager().save(
                    this.loadReport.getValue());
        }

        @Override
        public OptimalBroadcastRequestRouter<StatelessQuadruplePatternRequest, SemanticElement> getRouter() {
            return new OptimalBroadcastRequestRouter<StatelessQuadruplePatternRequest, SemanticElement>() {
                @Override
                public void onPeerValidatingKeyConstraints(final CanOverlay<SemanticElement> overlay,
                                                           final AnycastRequest<SemanticElement> request) {
                    FloodingLoadRequest.this.onPeerValidatingKeyConstraints(overlay);
                }
            };
        }

    }

}