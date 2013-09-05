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
package fr.inria.eventcloud.messages.request;

import java.io.Serializable;
import java.util.concurrent.Callable;

import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.MulticastRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.providers.ResponseProvider;
import org.objectweb.proactive.extensions.p2p.structured.router.can.OptimalBroadcastRequestRouter;
import org.objectweb.proactive.extensions.p2p.structured.utils.SerializedValue;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.MulticastConstraintsValidator;

import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.messages.response.StatefulQuadruplePatternResponse;
import fr.inria.eventcloud.overlay.SemanticRequestResponseManager;
import fr.inria.eventcloud.overlay.can.SemanticElement;

/**
 * StatelessQuadruplePatternRequest is a {@link QuadruplePattern} query that is
 * distributed among all the peers that match the values contained by the
 * quadruple pattern which is specified when the request is created. This
 * request is said stateful because it maintains a state about the action which
 * is executed by overriding
 * {@link #onPeerValidatingKeyConstraints(CanOverlay, MulticastRequest, QuadruplePattern)}
 * . This state can be retrieved later when the response is routed back.
 * 
 * @author lpellegr
 * 
 * @param <T>
 *            the type of the state that is maintained.
 */
public abstract class StatefulQuadruplePatternRequest<T> extends
        StatelessQuadruplePatternRequest {

    private static final long serialVersionUID = 160L;

    public StatefulQuadruplePatternRequest(
            QuadruplePattern quadPattern,
            ResponseProvider<? extends StatefulQuadruplePatternResponse<T>, Coordinate<SemanticElement>> responseProvider) {
        super(quadPattern, responseProvider);
    }

    public StatefulQuadruplePatternRequest(
            MulticastConstraintsValidator<SemanticElement> constraintsValidator,
            QuadruplePattern quadPattern,
            ResponseProvider<? extends StatefulQuadruplePatternResponse<T>, Coordinate<SemanticElement>> responseProvider) {
        super(constraintsValidator, quadPattern, responseProvider);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final void onPeerValidatingKeyConstraints(CanOverlay<SemanticElement> overlay,
                                                     QuadruplePattern quadruplePattern) {
        throw new UnsupportedOperationException();
    }

    /**
     * This methods is executed in a new thread on each peer which validates the
     * constraints. The value that is returned is stored into a table to be
     * retrieved later (e.g. when the response is routed back).
     * <p>
     * The object which is returned must be {@link Serializable}.
     */
    public abstract T onPeerValidatingKeyConstraints(CanOverlay<SemanticElement> overlay,
                                                     MulticastRequest<SemanticElement> request,
                                                     QuadruplePattern quadruplePattern);

    /**
     * {@inheritDoc}
     */
    @Override
    public OptimalBroadcastRequestRouter<StatelessQuadruplePatternRequest, SemanticElement> getRouter() {
        return new OptimalBroadcastRequestRouter<StatelessQuadruplePatternRequest, SemanticElement>() {
            @Override
            public void onPeerValidatingKeyConstraints(final CanOverlay<SemanticElement> overlay,
                                                       final MulticastRequest<SemanticElement> request) {
                final SemanticRequestResponseManager messagingManager =
                        (SemanticRequestResponseManager) overlay.getRequestResponseManager();

                messagingManager.getPendingResults()
                        .put(
                                request.getId(),
                                messagingManager.threadPool.submit(new Callable<StatefulRequestAction<T>>() {
                                    @Override
                                    public StatefulRequestAction<T> call() {
                                        long start = System.nanoTime();

                                        T actionResult =
                                                StatefulQuadruplePatternRequest.this.onPeerValidatingKeyConstraints(
                                                        overlay,
                                                        request,
                                                        StatefulQuadruplePatternRequest.this.quadruplePattern.getValue());

                                        return new StatefulRequestAction<T>(
                                                System.nanoTime() - start,
                                                actionResult);
                                    }
                                }));
            }
        };
    }

    @Override
    public SerializedValue<QuadruplePattern> getQuadruplePattern() {
        return this.quadruplePattern;
    }

}
