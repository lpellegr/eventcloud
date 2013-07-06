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

import org.objectweb.proactive.extensions.p2p.structured.messages.ResponseProvider;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.AnycastRequest;
import org.objectweb.proactive.extensions.p2p.structured.messages.request.can.OptimalBroadcastRequest;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.CanOverlay;
import org.objectweb.proactive.extensions.p2p.structured.overlay.can.zone.coordinates.Coordinate;
import org.objectweb.proactive.extensions.p2p.structured.router.can.OptimalBroadcastRequestRouter;
import org.objectweb.proactive.extensions.p2p.structured.utils.SerializedValue;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.AnycastConstraintsValidator;
import org.objectweb.proactive.extensions.p2p.structured.validator.can.DefaultAnycastConstraintsValidator;

import fr.inria.eventcloud.api.QuadruplePattern;
import fr.inria.eventcloud.messages.response.can.StatelessQuadruplePatternResponse;
import fr.inria.eventcloud.overlay.can.SemanticCoordinateFactory;
import fr.inria.eventcloud.overlay.can.SemanticElement;

/**
 * StatelessQuadruplePatternRequest is a {@link QuadruplePattern} that is
 * distributed among all the peers that match the values contained by the
 * quadruple pattern which is specified when the request is created. This
 * request is said stateless because it does not maintain any state about the
 * action which is executed by overriding
 * {@link #onPeerValidatingKeyConstraints(CanOverlay, QuadruplePattern)}.
 * 
 * @author lpellegr
 */
public abstract class StatelessQuadruplePatternRequest extends
        OptimalBroadcastRequest<SemanticElement> {

    private static final long serialVersionUID = 150L;

    protected SerializedValue<QuadruplePattern> quadruplePattern;

    public StatelessQuadruplePatternRequest(QuadruplePattern quadPattern) {
        this(
                quadPattern,
                new ResponseProvider<StatelessQuadruplePatternResponse, Coordinate<SemanticElement>>() {
                    private static final long serialVersionUID = 150L;

                    @Override
                    public StatelessQuadruplePatternResponse get() {
                        return new StatelessQuadruplePatternResponse();
                    }
                });
    }

    public StatelessQuadruplePatternRequest(
            QuadruplePattern quadPattern,
            ResponseProvider<? extends StatelessQuadruplePatternResponse, Coordinate<SemanticElement>> responseProvider) {
        super(new DefaultAnycastConstraintsValidator<SemanticElement>(
                SemanticCoordinateFactory.newSemanticCoordinate(quadPattern)),
                responseProvider);
        this.quadruplePattern = SerializedValue.create(quadPattern);
    }

    public StatelessQuadruplePatternRequest(
            AnycastConstraintsValidator<SemanticElement> validator,
            QuadruplePattern quadPattern,
            ResponseProvider<? extends StatelessQuadruplePatternResponse, Coordinate<SemanticElement>> responseProvider) {
        super(validator, responseProvider);
        this.quadruplePattern = SerializedValue.create(quadPattern);
    }

    /**
     * Defines an action to execute when we are on a peer that matches the
     * constraints.
     * 
     * @param overlay
     * @param quadruplePattern
     */
    public abstract void onPeerValidatingKeyConstraints(CanOverlay<SemanticElement> overlay,
                                                        QuadruplePattern quadruplePattern);

    /**
     * {@inheritDoc}
     */
    @Override
    public OptimalBroadcastRequestRouter<StatelessQuadruplePatternRequest, SemanticElement> getRouter() {
        return new OptimalBroadcastRequestRouter<StatelessQuadruplePatternRequest, SemanticElement>() {
            @Override
            public void onPeerValidatingKeyConstraints(final CanOverlay<SemanticElement> overlay,
                                                       final AnycastRequest<SemanticElement> request) {
                StatelessQuadruplePatternRequest.this.onPeerValidatingKeyConstraints(
                        overlay,
                        StatelessQuadruplePatternRequest.this.quadruplePattern.getValue());
            }
        };
    }

    public SerializedValue<QuadruplePattern> getQuadruplePattern() {
        return this.quadruplePattern;
    }

}
